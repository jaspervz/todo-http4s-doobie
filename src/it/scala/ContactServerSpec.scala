import cats.effect.IO
import config.Config
import db.Database
import io.circe.Json
import io.circe.literal._
import org.http4s.circe._
import org.http4s.client.blaze.Http1Client
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import io.circe.optics.JsonPath._
import org.http4s.server.{Server => Http4sServer}
import org.http4s.server.blaze.BlazeBuilder
import repository.ContactRepository
import service.ContactService

class ContactServerSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  private lazy val client = Http1Client[IO]().unsafeRunSync()

  private lazy val config = Config.load("test.conf").unsafeRunSync()

  private lazy val urlStart = s"http://${config.server.host}:${config.server.port}"

  private val server = createServer().unsafeRunSync()

  override def afterAll(): Unit = {
    client.shutdown.unsafeRunSync()
    server.shutdown.unsafeRunSync()
  }

  "Contact server" should {
    "create a Contact" in {
      val description = "my Contact 1"
      val importance = "high"
      val createJson =json"""
        {
          "description": $description,
          "importance": $importance
        }"""
      val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/contacts")).withBody(createJson).unsafeRunSync()
      val json = client.expect[Json](request).unsafeRunSync()
      root.id.long.getOption(json).nonEmpty shouldBe true
      root.description.string.getOption(json) shouldBe Some(description)
      root.importance.string.getOption(json) shouldBe Some(importance)
    }

    "update a Contact" in {
      val id = createContact("my Contact 2", "low")

      val description = "updated Contact"
      val importance = "medium"
      val updateJson = json"""
        {
          "description": $description,
          "importance": $importance
        }"""
      val request = Request[IO](method = Method.PUT, uri = Uri.unsafeFromString(s"$urlStart/contacts/$id")).withBody(updateJson).unsafeRunSync()
      client.expect[Json](request).unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": $description,
          "importance": $importance
        }"""
    }

    "return a single Contact" in {
      val description = "my Contact 3"
      val importance = "medium"
      val id = createContact(description, importance)
      client.expect[Json](Uri.unsafeFromString(s"$urlStart/contacts/$id")).unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": $description,
          "importance": $importance
        }"""
    }

    "delete a Contact" in {
      val description = "my Contact 4"
      val importance = "low"
      val id = createContact(description, importance)
      val deleteRequest = Request[IO](method = Method.DELETE, uri = Uri.unsafeFromString(s"$urlStart/contacts/$id"))
      client.status(deleteRequest).unsafeRunSync() shouldBe Status.NoContent

      val getRequest = Request[IO](method = Method.GET, uri = Uri.unsafeFromString(s"$urlStart/contacts/$id"))
      client.status(getRequest).unsafeRunSync() shouldBe Status.NotFound
    }

    "return all Contacts" in {
      // Remove all existing Contacts
      val json = client.expect[Json](Uri.unsafeFromString(s"$urlStart/contacts")).unsafeRunSync()
      root.each.id.long.getAll(json).foreach { id =>
        val deleteRequest = Request[IO](method = Method.DELETE, uri = Uri.unsafeFromString(s"$urlStart/contacts/$id"))
        client.status(deleteRequest).unsafeRunSync() shouldBe Status.NoContent
      }

      // Add new Contacts
      val description1 = "my Contact 1"
      val description2 = "my Contact 2"
      val importance1 = "high"
      val importance2 = "low"
      val id1 = createContact(description1, importance1)
      val id2 = createContact(description2, importance2)

      // Retrieve Contacts
      client.expect[Json](Uri.unsafeFromString(s"$urlStart/contacts")).unsafeRunSync shouldBe json"""
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

  private def createContact(description: String, importance: String): Long = {
    val createJson =json"""
      {
        "description": $description,
        "importance": $importance
      }"""
    val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/contacts")).withBody(createJson).unsafeRunSync()
    val json = client.expect[Json](request).unsafeRunSync()
    root.id.long.getOption(json).nonEmpty shouldBe true
    root.id.long.getOption(json).get
  }

  private def createServer(): IO[Http4sServer[IO]] = {
    for {
      transactor <- Database.transactor(config.database)
      _ <- Database.initialize(transactor)
      repository = new ContactRepository(transactor)
      server <- BlazeBuilder[IO]
        .bindHttp(config.server.port, config.server.host)
        .mountService(new ContactService(repository).service, "/").start
    } yield server
  }
}
