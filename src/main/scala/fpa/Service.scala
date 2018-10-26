package fpa

import cats.effect._
import cats.data._
import cats.implicits._

import fs2.Stream

import io.circe._
import io.circe.syntax._

import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._

import repository._

class Service[F[_] : Effect, A : Decoder : Encoder : Identity](
  segment: String, repository: Repository[F, A]
) extends Http4sDsl[F] {

  type EndPoint = Kleisli[F, Request[F], Response[F]]
  val  EndPoint = Kleisli

  def http = HttpService[F] {
    case req @ POST   -> Root / `segment`                =>  create(req)
    case req @ PUT    -> Root / `segment` / LongVar(id)  =>  update(id)(req)
    case req @ GET    -> Root / `segment` / LongVar(id)  =>  read(id)(req)
    case req @ DELETE -> Root / `segment` / LongVar(id)  =>  delete(id)(req)
    case req @ GET    -> Root / `segment`                =>  stream(req)
  }

  def create: EndPoint =
    EndPoint(
      req => for {
        entity   <- req.decodeJson[A]
        result   <- repository.create(entity)
        response <- httpCreatedOr500(result)
      } yield response
    )

  def update(id: Long): EndPoint =
    EndPoint(
      req => for {
        entity   <- req.decodeJson[A]
        result   <- repository.update(id, entity)
        response <- httpOkOr404(result)
      } yield response
    )

  def read(id: Long): EndPoint =
    EndPoint(
      _ => repository.read(id).flatMap(httpOkOr404)
    )

  def delete(id: Long): EndPoint =
    EndPoint(
      _ => repository.delete(id).flatMap {
        case Left(NotFoundError(_, _)) => NotFound()
        case Right(_)                  => NoContent()
      }
    )

  def stream: EndPoint =
    EndPoint(
      _ => Ok(
        Stream("[")
        ++ repository.stream.map(_.asJson.noSpaces).intersperse(",")
        ++ Stream("]"),
        `Content-Type`(MediaType.`application/json`)
      )
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