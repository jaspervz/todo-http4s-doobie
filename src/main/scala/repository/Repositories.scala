package repository

import cats.effect._
import doobie._

object Repositories {
  def  make[F[_]: Sync] (
    transactor: Resource[F, Transactor[F]]
  ): Repositories[F] =
    Repositories[F](
      todo = TodoRepository.make[F](transactor)
    )
}

final case class Repositories[F[_]] private (
  todo: TodoRepository[F]
)
