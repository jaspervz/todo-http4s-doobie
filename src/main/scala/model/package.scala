package object model {
  
  abstract sealed class Importance(val value: String)
  case object High extends Importance("high")
  case object Medium extends Importance("medium")
  case object Low extends Importance("low")

  object Importance {
    private def values = Set(High, Medium, Low)

    def unsafeFromString(value: String): Importance = {
      values.find(_.value == value).get
    }
  }

  case class Contact(id: Option[Long], description: String, importance: Importance)

  case class ContactNotFound() extends RuntimeException
}
