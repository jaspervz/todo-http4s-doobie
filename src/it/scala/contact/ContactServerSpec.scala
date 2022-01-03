package contact

import cats.effect._

import org.http4s._
import org.http4s.circe._
import org.http4s.client.dsl.io._
import org.http4s.dsl.io._
import org.http4s.syntax.all._

import org.scalatest._
import org.scalatest.concurrent._
import org.scalatest.matchers.should._
import org.scalatest.time._
import org.scalatest.wordspec._

import scala.concurrent._

import fpa._

import io.circe._
import literal._
import optics._

class contactServerSpec
  extends AsyncWordSpec
  with Matchers
  with BeforeAndAfterAll
  with Eventually {

  import cats.effect.unsafe.implicits.global
  
  ContactServer.create("test.conf").unsafeRunAndForget()

  lazy val client: Resource[IO, org.http4s.client.Client[IO]] =
    org.http4s.blaze.client.BlazeClientBuilder[IO].resource

  import JsonPath._

  val config: Config =
    Config.load("test.conf").use(IO.pure).unsafeRunSync()

  val endpoint: Uri =
    Uri.unsafeFromString(s"http://${config.server.host}:${config.server.port}/contacts")

  def asyncJsonFrom(request: Request[IO]): Json =
    client.use(_.expect[Json](request)).unsafeRunSync()

  "ContactServer" should {
    "create a contact" in {
      val description = "create contact"
      val importance  = "high"
      val entity =
        json"""{
          "description": $description,
          "importance": $importance
        }"""

      val json = asyncJsonFrom(POST(endpoint).withEntity(entity))
      
      root.id.string.getOption(json).nonEmpty shouldBe true
      root.description.string.getOption(json) shouldBe Some(description)
      root.importance.string.getOption(json)  shouldBe Some(importance)
    }

    "update a contact" in {
      val id = createContact("my contact 2", "low")

      val description = "updated contact"
      val importance  = "medium"
      val updateJson =
        json"""{
          "description": $description,
          "importance": $importance
        }"""

      asyncJsonFrom(PUT(endpoint / id).withEntity(updateJson)) shouldBe json"""
        {
          "id": $id,
          "description": $description,
          "importance": $importance
        }"""
    }

    "return a single contact" in {
      val description = "my contact 3"
      val importance  = "medium"
      val id = createContact(description, importance)

      client.use(_.expect[Json](endpoint / id)).unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": $description,
          "importance": $importance
        }"""
    }

    "delete a contact" in {
      val description = "my contact 4"
      val importance  = "low"
      val id = createContact(description, importance)

      client.use(_.status(DELETE(endpoint / id))).unsafeRunSync() shouldBe Status.NoContent

      client.use(_.status(GET(endpoint / id))).unsafeRunSync() shouldBe Status.NotFound
    }

    "return all contacts" in {
      // Remove all existing contacts
      val json = client.use(_.expect[Json](endpoint)).unsafeRunSync()
      root.each.id.string.getAll(json).foreach { id =>
        client.use(_.status(DELETE(endpoint / id))).unsafeRunSync() shouldBe Status.NoContent
      }

      // Add new contacts
      val description1 = "my contact 1"
      val description2 = "my contact 2"
      val importance1 = "high"
      val importance2 = "low"
      val id1 = createContact(description1, importance1)
      val id2 = createContact(description2, importance2)

      // Retrieve contacts
      client.use(_.expect[Json](endpoint)).unsafeRunSync() shouldBe json"""
        [
          {
            "id": $id1,
            "description": $description1,
            "importance": $importance1
          },
          {
            "id": $id2,
            "description": $description2,
            "importance": $importance2
          }
        ]"""
    }
  }

  private def createContact(description: String, importance: String): String = {
    val createJson = json"""
      {
        "description": $description,
        "importance": $importance
      }"""
    val request = Request[IO](method = Method.POST, uri = endpoint).withEntity(createJson)
    val json = asyncJsonFrom(request)

    root.id.string.getOption(json).nonEmpty shouldBe true
    root.id.string.getOption(json).get
  }
}
