package contact

import cats.effect._

import io.circe._
import io.circe.literal._
import io.circe.optics._

import org.http4s.circe._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext

import fpa.Config
import org.http4s.client.Client
import org.http4s.Response

class contactServerSpec
  extends AnyWordSpec
  with Matchers
  with BeforeAndAfterAll
  with Eventually {

  import cats.effect.unsafe.implicits.global
  
  import JsonPath._

  lazy val config: Config =
    Config.load("test.conf").use(IO.pure).unsafeRunSync()

  lazy val context =
    s"http://${config.server.host}:${config.server.port}"

  lazy val endpoint =
    Uri.unsafeFromString(s"$context/contacts")

  lazy val client: Resource[IO, Client[IO]] =
    BlazeClientBuilder[IO].resource

  def jsonResponseFrom(request: Request[IO]): Json =
    client.use(_.expect[Json](request)).unsafeRunSync()

  def jsonResponseFrom(uri: Uri): Json =
    client.use(_.expect[Json](uri)).unsafeRunSync()

  def endpointWith(id: String): Uri =
    Uri.unsafeFromString(s"$context/contacts/$id")

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout  = scaled(Span(5, Seconds)),
      interval = scaled(Span(100, Millis))
    )

  override def beforeAll(): Unit = {
    ContactServer.create("test.conf").unsafeRunAndForget()

    eventually(client.use(_.statusFromUri(endpoint)).unsafeRunSync() shouldBe Status.Ok)

    ()
  }

  "Contact server" should {
    "create a contact" in {
      val description = "create contact"
      val importance  = "high"
      val entity =
        json"""{
          "description": $description,
          "importance": $importance
        }"""

      val request = Request[IO](method = Method.POST, uri = endpoint).withEntity(entity)
      val json = jsonResponseFrom(request)
      
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

      val request = Request[IO](method = Method.PUT, uri = endpointWith(id)).withEntity(updateJson)
      jsonResponseFrom(request) shouldBe json"""
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

      client.use(_.expect[Json](endpointWith(id))).unsafeRunSync() shouldBe json"""
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

      val deleteRequest = Request[IO](method = Method.DELETE, uri = endpointWith(id))
      client.use(_.status(deleteRequest)).unsafeRunSync() shouldBe Status.NoContent

      val getRequest = Request[IO](method = Method.GET, uri = endpointWith(id))
      client.use(_.status(getRequest)).unsafeRunSync() shouldBe Status.NotFound
    }

    "return all contacts" in {
      // Remove all existing contacts
      val json = client.use(_.expect[Json](endpoint)).unsafeRunSync()
      root.each.id.string.getAll(json).foreach { id =>
        val deleteRequest = Request[IO](method = Method.DELETE, uri = endpointWith(id))
        client.use(_.status(deleteRequest)).unsafeRunSync() shouldBe Status.NoContent
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
    val json = jsonResponseFrom(request)

    root.id.string.getOption(json).nonEmpty shouldBe true
    root.id.string.getOption(json).get
  }
}
