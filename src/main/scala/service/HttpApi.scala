package service

import repository.Repositories

import cats.effect._
import org.http4s._
import org.http4s.implicits._

object HttpApi {
  def make[F[_]: Concurrent: Timer](
    repositories: Repositories[F]
  ): HttpApi[F] = new HttpApi[F](repositories)
}

final class HttpApi[F[_]: Concurrent: Timer] private (
  repositories: Repositories[F]
) {
  private val todoService = new TodoService[F](repositories.todo).routes

  val httpApp: HttpApp[F] = todoService.orNotFound
}
