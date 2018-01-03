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
import repository.TodoRepository
import service.TodoService

class TodoServerSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  private lazy val client = Http1Client[IO]().unsafeRunSync()

  private lazy val config = Config.load("test.conf").unsafeRunSync()

  private lazy val urlStart = s"http://${config.server.host}:${config.server.port}"

  private val server = createServer().unsafeRunSync()

  override def afterAll(): Unit = {
    client.shutdown.unsafeRunSync()
    server.shutdown.unsafeRunSync()
  }

  "Todo server" should {
    "create a todo" in {
      val description = "my todo 1"
      val importance = "high"
      val createJson =json"""
        {
          "description": $description,
          "importance": $importance
        }"""
      val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/todos")).withBody(createJson).unsafeRunSync()
      val json = client.expect[Json](request).unsafeRunSync()
      root.id.long.getOption(json).nonEmpty shouldBe true
      root.description.string.getOption(json) shouldBe Some(description)
      root.importance.string.getOption(json) shouldBe Some(importance)
    }

    "update a todo" in {
      val id = createTodo("my todo 2", "low")

      val description = "updated todo"
      val importance = "medium"
      val updateJson = json"""
        {
          "description": $description,
          "importance": $importance
        }"""
      val request = Request[IO](method = Method.PUT, uri = Uri.unsafeFromString(s"$urlStart/todos/$id")).withBody(updateJson).unsafeRunSync()
      client.expect[Json](request).unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": $description,
          "importance": $importance
        }"""
    }

    "return a single todo" in {
      val description = "my todo 3"
      val importance = "medium"
      val id = createTodo(description, importance)
      client.expect[Json](Uri.unsafeFromString(s"$urlStart/todos/$id")).unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": $description,
          "importance": $importance
        }"""
    }

    "delete a todo" in {
      val description = "my todo 4"
      val importance = "low"
      val id = createTodo(description, importance)
      val deleteRequest = Request[IO](method = Method.DELETE, uri = Uri.unsafeFromString(s"$urlStart/todos/$id"))
      client.status(deleteRequest).unsafeRunSync() shouldBe Status.NoContent

      val getRequest = Request[IO](method = Method.GET, uri = Uri.unsafeFromString(s"$urlStart/todos/$id"))
      client.status(getRequest).unsafeRunSync() shouldBe Status.NotFound
    }

    "return all todos" in {
      // Remove all existing todos
      val json = client.expect[Json](Uri.unsafeFromString(s"$urlStart/todos")).unsafeRunSync()
      root.each.id.long.getAll(json).foreach { id =>
        val deleteRequest = Request[IO](method = Method.DELETE, uri = Uri.unsafeFromString(s"$urlStart/todos/$id"))
        client.status(deleteRequest).unsafeRunSync() shouldBe Status.NoContent
      }

      // Add new todos
      val description1 = "my todo 1"
      val description2 = "my todo 2"
      val importance1 = "high"
      val importance2 = "low"
      val id1 = createTodo(description1, importance1)
      val id2 = createTodo(description2, importance2)

      // Retrieve todos
      client.expect[Json](Uri.unsafeFromString(s"$urlStart/todos")).unsafeRunSync shouldBe json"""
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

  private def createTodo(description: String, importance: String): Long = {
    val createJson =json"""
      {
        "description": $description,
        "importance": $importance
      }"""
    val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/todos")).withBody(createJson).unsafeRunSync()
    val json = client.expect[Json](request).unsafeRunSync()
    root.id.long.getOption(json).nonEmpty shouldBe true
    root.id.long.getOption(json).get
  }

  private def createServer(): IO[Http4sServer[IO]] = {
    for {
      transactor <- Database.transactor(config.database)
      _ <- Database.initialize(transactor)
      repository = new TodoRepository(transactor)
      server <- BlazeBuilder[IO]
        .bindHttp(config.server.port, config.server.host)
        .mountService(new TodoService(repository).service, "/").start
    } yield server
  }
}
