package contact

import cats.effect.IO
import org.http4s._
import org.http4s.headers._
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe._
import io.circe.syntax._
import model._

package object service {

  def httpOk[A : Encoder](result: Either[_, A]): IO[Response[IO]] =
    result match {
      case Left(_)  => httpNotFound()
      case Right(a) => Ok(a.asJson)
    }

  def httpCreated(c: Contact): IO[Response[IO]] =
    Created(c.asJson, Location(Uri.unsafeFromString(s"/contacts/${c.id.get}")))

  def httpNotFound(): IO[Response[IO]] =
    NotFound()

}
