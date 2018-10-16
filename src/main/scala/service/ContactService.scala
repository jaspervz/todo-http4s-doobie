package service

import cats.effect.IO
import model.{Contact, ContactNotFound}
import org.http4s.{HttpService, MediaType, Request, Response, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import repository.ContactRepository
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import org.http4s.headers.{Location, `Content-Type`}

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
      response       <- Created(createdContact.asJson, Location(Uri.unsafeFromString(s"/contacts/${createdContact.id.get}")))
    } yield response

  def serveUpdatedContact(id: Long, req: Request[IO]): IO[Response[IO]] =
    for {
      contact      <- req.decodeJson[Contact]
      updateResult <- repository.updateContact(id, contact)
      response     <- responseFromResult(updateResult)
    } yield response

  def serveContactById(id: Long): IO[Response[IO]] =
    for {
      getResult <- repository.getContact(id)
      response  <- responseFromResult(getResult)
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

  private def responseFromResult(result: Either[ContactNotFound, Contact]) = {
    result match {
      case Left(ContactNotFound()) => NotFound()
      case Right(contact)          => Ok(contact.asJson)
    }
  }
}
