package model

sealed trait DbError

object DbError {
  case object TodoNotFoundError extends DbError
}
