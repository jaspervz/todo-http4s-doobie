package contact

import cats.effect.IO
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.headers._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import repository._

package object service {

  def httpOk[E <: RepositoryError,  A : Encoder](result: Either[E, A]): IO[Response[IO]] =
    result match {
      case Left(_)  => httpNotFound()
      case Right(a) => Ok(a.asJson)
    }

  def httpCreated[E <: RepositoryError, A : Encoder : Identity](segment: String)(result: Either[E, A]): IO[Response[IO]] =
    result match {
      case Left(e)  => InternalServerError(e.toString)
      case Right(a) => Created(a.asJson, Location(Uri.unsafeFromString(s"/$segment/${a.id.get}")))
    }

  def httpNotFound(): IO[Response[IO]] =
    NotFound()

}
