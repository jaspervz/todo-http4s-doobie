package model

import io.circe._
import io.circe.generic.semiauto._

case class Todo(id: Option[Long], description: String, importance: Importance)

object Todo {
  implicit val todoEncoder: Encoder[Todo] = deriveEncoder
  implicit val todoDecoder: Decoder[Todo] = deriveDecoder
}
