package contact

import doobie._
import io.circe._
import io.circe.generic.semiauto._

package object model {
  
  abstract sealed class Importance(val value: String)
  case object High   extends Importance("high")
  case object Medium extends Importance("medium")
  case object Low    extends Importance("low")

  object Importance {

    private def values: Set[Importance] =
      Set(High, Medium, Low)

    def unsafeFromString(value: String): Importance =
      values.find(_.value == value).get

    implicit val importanceEncoder: Encoder[Importance] =
      Encoder.encodeString.contramap[Importance](_.value)

    implicit val importanceDecoder: Decoder[Importance] =
      Decoder.decodeString.map[Importance](Importance.unsafeFromString)

    implicit val importanceMeta: Meta[Importance] =
      Meta[String].xmap(Importance.unsafeFromString, _.value)

  }

  case class Contact(id: Option[Long], description: String, importance: Importance)

  object Contact {

    implicit val contactEncoder: Encoder[Contact] =
      deriveEncoder[Contact]

    implicit val contactDecoder: Decoder[Contact] =
      deriveDecoder[Contact]

  }

  case class ContactNotFound() extends RuntimeException
}
