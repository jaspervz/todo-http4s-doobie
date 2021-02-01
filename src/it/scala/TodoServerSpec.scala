import cats.effect.{ContextShift, IO, Timer}
import config.Config
import io.circe.Json
import io.circe.literal._
import io.circe.optics.JsonPath._
import org.http4s.circe._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class TodoServerSpec extends AnyWordSpec with Matchers with BeforeAndAfterAll with Eventually {
  private implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  private implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  private lazy val client = BlazeClientBuilder[IO](global).resource

  private val configFile = "test.conf"

  private lazy val config = Config.load(configFile).use(config => IO.pure(config)).unsafeRunSync()

  private lazy val urlStart = s"http://${config.server.host}:${config.server.port}"

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(timeout = scaled(Span(5, Seconds)), interval = scaled(Span(100, Millis)))

  override def beforeAll(): Unit = {
    HttpServer.create(configFile).unsafeRunAsyncAndForget()
    eventually {
      client.use(_.statusFromUri(Uri.unsafeFromString(s"$urlStart/todos"))).unsafeRunSync() shouldBe Status.Ok
    }
    ()
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
      val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/todos")).withEntity(createJson)
      val json = client.use(_.expect[Json](request)).unsafeRunSync()
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
      val request = Request[IO](method = Method.PUT, uri = Uri.unsafeFromString(s"$urlStart/todos/$id")).withEntity(updateJson)
      client.use(_.expect[Json](request)).unsafeRunSync() shouldBe json"""
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
      client.use(_.expect[Json](Uri.unsafeFromString(s"$urlStart/todos/$id"))).unsafeRunSync() shouldBe json"""
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
      client.use(_.status(deleteRequest)).unsafeRunSync() shouldBe Status.NoContent

      val getRequest = Request[IO](method = Method.GET, uri = Uri.unsafeFromString(s"$urlStart/todos/$id"))
      client.use(_.status(getRequest)).unsafeRunSync() shouldBe Status.NotFound
    }

    "return all todos" in {
      // Remove all existing todos
      val json = client.use(_.expect[Json](Uri.unsafeFromString(s"$urlStart/todos"))).unsafeRunSync()
      root.each.id.long.getAll(json).foreach { id =>
        val deleteRequest = Request[IO](method = Method.DELETE, uri = Uri.unsafeFromString(s"$urlStart/todos/$id"))
        client.use(_.status(deleteRequest)).unsafeRunSync() shouldBe Status.NoContent
      }

      // Add new todos
      val description1 = "my todo 1"
      val description2 = "my todo 2"
      val importance1 = "high"
      val importance2 = "low"
      val id1 = createTodo(description1, importance1)
      val id2 = createTodo(description2, importance2)

      // Retrieve todos
      client.use(_.expect[Json](Uri.unsafeFromString(s"$urlStart/todos"))).unsafeRunSync() shouldBe json"""
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
    val createJson = json"""
      {
        "description": $description,
        "importance": $importance
      }"""
    val request = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$urlStart/todos")).withEntity(createJson)
    val json = client.use(_.expect[Json](request)).unsafeRunSync()
    root.id.long.getOption(json).nonEmpty shouldBe true
    root.id.long.getOption(json).get
  }
}
