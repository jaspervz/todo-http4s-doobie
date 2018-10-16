package service

import cats.effect.IO
import model.{Importance, Contact, ContactNotFound}
import org.http4s.{HttpService, MediaType, Uri}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import repository.ContactRepository
import io.circe.generic.auto._
import io.circe.syntax._
import fs2.Stream
import io.circe.{Decoder, Encoder}
import org.http4s.headers.{Location, `Content-Type`}

class ContactService(repository: ContactRepository) extends Http4sDsl[IO] {
  private implicit val encodeImportance: Encoder[Importance] = Encoder.encodeString.contramap[Importance](_.value)

  private implicit val decodeImportance: Decoder[Importance] = Decoder.decodeString.map[Importance](Importance.unsafeFromString)

  val service = HttpService[IO] {
    case GET -> Root / "contacts" =>
      Ok(Stream("[") ++ repository.getContacts.map(_.asJson.noSpaces).intersperse(",") ++ Stream("]"), `Content-Type`(MediaType.`application/json`))

    case GET -> Root / "contacts" / LongVar(id) =>
      for {
        getResult <- repository.getContact(id)
        response <- ContactResult(getResult)
      } yield response

    case req @ POST -> Root / "contacts" =>
      for {
        contact <- req.decodeJson[Contact]
        createdContact <- repository.createContact(contact)
        response <- Created(createdContact.asJson, Location(Uri.unsafeFromString(s"/contacts/${createdContact.id.get}")))
      } yield response

    case req @ PUT -> Root / "contacts" / LongVar(id) =>
      for {
        contact <-req.decodeJson[Contact]
        updateResult <- repository.updateContact(id, contact)
        response <- ContactResult(updateResult)
      } yield response

    case DELETE -> Root / "contacts" / LongVar(id) =>
      repository.deleteContact(id).flatMap {
        case Left(ContactNotFound) => NotFound()
        case Right(_) => NoContent()
      }
  }

  private def ContactResult(result: Either[ContactNotFound.type, Contact]) = {
    result match {
      case Left(ContactNotFound) => NotFound()
      case Right(contact) => Ok(contact.asJson)
    }
  }
}
