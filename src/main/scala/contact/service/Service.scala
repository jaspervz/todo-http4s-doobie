package contact
package service

import cats.implicits._
import cats.effect._

import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

import io.circe._
import io.circe.syntax._
import org.http4s.headers._

import fs2.Stream

import model._
import repository._

class Service[F[_] : Effect, A : Decoder : Encoder : Identity](
  segment: String, repository: Repository[F, A]
) extends Http4sDsl[F] {

  def httpService = HttpService[F] {
    case req @ POST -> Root / `segment`                =>  serveCreated(req)
    case req @ PUT  -> Root / `segment` / LongVar(id)  =>  serveUpdated(id, req)
    case GET        -> Root / `segment` / LongVar(id)  =>  serveById(id)
    case DELETE     -> Root / `segment` / LongVar(id)  =>  serveDeleted(id)
    case GET        -> Root / `segment`                =>  serveAll
  }

  def serveCreated(req: Request[F]): F[Response[F]] =
    for {
      entity   <- req.decodeJson[A]
      result   <- repository.create(entity)
      response <- httpCreatedOr500(result)
    } yield response

  def serveUpdated(id: Long, req: Request[F]): F[Response[F]] =
    for {
      entity   <- req.decodeJson[A]
      result   <- repository.update(id, entity)
      response <- httpOkOr404(result)
    } yield response

  def serveById(id: Long): F[Response[F]] =
    repository.read(id).flatMap(httpOkOr404)

  def serveDeleted(id: Long): F[Response[F]] =
    repository.delete(id).flatMap {
      case Left(NotFoundError(_, _)) => NotFound()
      case Right(_)                  => NoContent()
    }

  def serveAll: F[Response[F]] =
    Ok(
      Stream("[")
      ++ repository.getAll.map(_.asJson.noSpaces).intersperse(",")
      ++ Stream("]"),
      `Content-Type`(MediaType.`application/json`)
    )

  private def httpOkOr404(result: Either[_, A]): F[Response[F]] =
    result match {
      case Right(a) => Ok(a.asJson)
      case Left(_)  => NotFound()
    }

  private def httpCreatedOr500(result: Either[_, A]): F[Response[F]] =
    result match {
      case Right(a) => Created(a.asJson, Location(Uri.unsafeFromString(s"/$segment/${a.id.get}")))
      case Left(e)  => InternalServerError(e.toString)
    }

}

object ContactService {

  def apply[F[_] : Effect](repository: Repository[F, Contact]): Service[F, Contact] =
    new Service[F, Contact]("contacts", repository)

}
