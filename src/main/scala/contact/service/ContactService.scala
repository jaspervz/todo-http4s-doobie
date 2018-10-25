package contact
package service

import cats._
import cats.data._
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

class Service[A : Decoder : Encoder : Identity](
  segment: String, repository: Repository[IO, A]
) extends Http4sDsl[IO] {

  def httpService = HttpService[IO] {
    case req @ POST -> Root / `segment`                =>  serveCreated(req)
    case req @ PUT  -> Root / `segment` / LongVar(id)  =>  serveUpdated(id, req)
    case GET        -> Root / `segment` / LongVar(id)  =>  serveById(id)
    case DELETE     -> Root / `segment` / LongVar(id)  =>  serveDeleted(id)
    case GET        -> Root / `segment`                =>  serveAll
  }

  def serveCreated(req: Request[IO]): IO[Response[IO]] =
//    ???
    for {
      entity   <- req.decodeJson[A]
      result   <- repository.create(entity)
      response <- httpCreated(segment)(result)
    } yield response

  def serveUpdated(id: Long, req: Request[IO]): IO[Response[IO]] =
    for {
      entity   <- req.decodeJson[A]
      result   <- repository.update(id, entity)
      response <- httpOk(result)
    } yield response

  def serveById(id: Long): IO[Response[IO]] =
    for {
      result   <- repository.read(id)
      response <- httpOk(result)
    } yield response

  def serveDeleted(id: Long): IO[Response[IO]] =
    repository.delete(id).flatMap {
      case Left(NotFoundError(_, _)) => NotFound()
      case Right(_)                  => NoContent()
    }

  def serveAll: IO[Response[IO]] =
    Ok(
      Stream("[")
      ++ repository.getAll.map(_.asJson.noSpaces).intersperse(",")
      ++ Stream("]"),
      `Content-Type`(MediaType.`application/json`)
    )

}

object ContactService {

  def apply[F[_] : Sync](repository: Repository[F, Contact]): Service[Contact] =
    new Service[Contact]("contacts", repository)

}
