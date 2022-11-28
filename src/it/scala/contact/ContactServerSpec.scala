package contact

import cats.effect.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.client.dsl.io.*
import org.http4s.dsl.io.*
import org.http4s.syntax.all.*
import org.scalatest.*
import org.scalatest.concurrent.*
import org.scalatest.matchers.should.*
import org.scalatest.time.*
import org.scalatest.flatspec.*

import scala.concurrent.*
import fpa.*
import io.circe.*
import literal.*

class ContactServerSpec
  extends AsyncFlatSpec
  with Checkpoints
  with Matchers
  with BeforeAndAfterAll
  with Eventually:

  import cats.effect.unsafe.implicits.global


  val server: IO[ExitCode] =
    ContactServer.create("test.conf")

  var shutdown: () => Future[Unit] =
    println("Start ContactServer..")
    server.unsafeRunCancelable()

  override implicit val patienceConfig =
    PatienceConfig(timeout = scaled(Span(10, Seconds)), interval = scaled(Span(1, Second)))

  override protected def beforeAll(): Unit =
    eventually(client.use(_.expect[Json](endpoint)).unsafeRunSync())
    println("ContactServer started")

  override protected def afterAll(): Unit =
    import scala.concurrent.duration.*
    println("Stopping ContactServer..")
    Await.result(shutdown(), 10.seconds)
    println("ContactServer stopped.")

  lazy val client: Resource[IO, org.http4s.client.Client[IO]] =
    org.http4s.blaze.client.BlazeClientBuilder[IO].resource

  val config: Config =
    Config.load("test.conf").use(IO.pure).unsafeRunSync()

  val endpoint: Uri =
    Uri.unsafeFromString(s"http://${config.server.host}:${config.server.port}/contacts")

  def asyncJsonFrom(request: Request[IO]): Json =
    client.use(_.expect[Json](request)).unsafeRunSync()

  "ContactServer" should "create a contact" in {

      val description = "create contact"
      val importance  = "high"
      val entity =
        json"""{
          "description": $description,
          "importance": $importance
        }"""

      val json = asyncJsonFrom(POST(endpoint).withEntity(entity)).hcursor

      val cp = new Checkpoint
      cp { json.downField("id").as[Identity].isRight   === true }
      cp { json.downField("description").as[String]    === Right(description) }
      cp { json.downField("importance").as[Importance] === Right(importance) }
      cp.reportAll()

      Succeeded
  }

  it should "update a contact" in {
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

  it should "return a single contact" in {
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

  it should "delete a contact" in {
      val description = "my contact 4"
      val importance  = "low"
      val id = createContact(description, importance)

      client.use(_.status(DELETE(endpoint / id))).unsafeRunSync() shouldBe Status.NoContent
      client.use(_.status(GET(endpoint / id))).unsafeRunSync()    shouldBe Status.NotFound
  }

  it should "return all contacts" in {
    // Remove all existing contacts
    val json = client.use(_.expect[Json](endpoint)).unsafeRunSync()
    json.hcursor.as[List[Contact]].foreach(_.foreach(c =>
      client.use(_.status(DELETE(endpoint / c.id.get))).unsafeRunSync() shouldBe Status.NoContent
    ))

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

  private def createContact(description: String, importance: String): String = {
    val createJson = json"""
      {
        "description": $description,
        "importance": $importance
      }"""
    val request = Request[IO](method = Method.POST, uri = endpoint).withEntity(createJson)
    val id = asyncJsonFrom(request).hcursor.downField("id").as[Identity]

    id.getOrElse(sys.error(s"error creating: ${createJson.spaces2}\n$id}")).toString
  }

