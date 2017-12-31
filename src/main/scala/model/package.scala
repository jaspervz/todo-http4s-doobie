package object model {
  abstract sealed class Importance(value: String)
  case object High extends Importance("high")
  case object Medium extends Importance("medium")
  case object Low extends Importance("low")

  case class Todo(id: Option[Long], description: String, importance: Importance)

  case object TodoNotFoundError
}
