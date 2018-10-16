package contact
package service

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers._

import model._
import repository._

class ContactService(repository: ContactRepository) extends Http4sDsl[IO] {

  val service = HttpService[IO] {
    case req @ POST -> Root / "contacts"                =>  serveCreatedContact(req)
    case req @ PUT  -> Root / "contacts" / LongVar(id)  =>  serveUpdatedContact(id, req)
    case GET        -> Root / "contacts" / LongVar(id)  =>  serveContactById(id)
    case DELETE     -> Root / "contacts" / LongVar(id)  =>  serveDeletedContact(id)
    case GET        -> Root / "contacts"                =>  serveAllContacts
  }

  def serveCreatedContact(req: Request[IO]): IO[Response[IO]] =
    for {
      contact        <- req.decodeJson[Contact]
      createdContact <- repository.createContact(contact)
      response       <- httpCreated(createdContact)
    } yield response

  def serveUpdatedContact(id: Long, req: Request[IO]): IO[Response[IO]] =
    for {
      contact      <- req.decodeJson[Contact]
      updateResult <- repository.updateContact(id, contact)
      response     <- httpOk(updateResult)
    } yield response

  def serveContactById(id: Long): IO[Response[IO]] =
    for {
      result   <- repository.getContact(id)
      response <- httpOk(result)
    } yield response

  def serveDeletedContact(id: Long): IO[Response[IO]] =
    repository.deleteContact(id).flatMap {
      case Left(ContactNotFound()) => NotFound()
      case Right(_)                => NoContent()
    }

  def serveAllContacts: IO[Response[IO]] =
    Ok(
      Stream("[")
        ++ repository.getContacts.map(_.asJson.noSpaces).intersperse(",")
        ++ Stream("]"),
      `Content-Type`(MediaType.`application/json`)
    )

}
