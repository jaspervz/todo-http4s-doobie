package fpa

import java.util.UUID

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
import service._

import scala.util.Try

class Service[F[_] : Effect, A : Decoder : Encoder : Entity[F, ?]](
  segment: String, repository: Repository[F, A]
) extends Http4sDsl[F] {

  type EndPoint = Kleisli[F, Request[F], Response[F]]
  val  EndPoint = Kleisli

  val E = implicitly[Entity[F, A]]

  def http = HttpService[F] {
    case req @ POST    -> Root / `segment`                    =>  create(req)
    case req @ PUT     -> Root / `segment` / IdentityVar(id)  =>  update(id)(req)
    case req @ GET     -> Root / `segment` / IdentityVar(id)  =>  read(id)(req)
    case req @ DELETE  -> Root / `segment` / IdentityVar(id)  =>  delete(id)(req)
    case req @ GET     -> Root / `segment`                    =>  stream(req)
  }

  def create: EndPoint =
    EndPoint(
      req => for {
        entity   <- req.decodeJson[A].withGeneratedId
        result   <- repository.create(entity)
        response <- httpCreatedOr500(entity)(result)
      } yield response
    )

  def update(id: Identity): EndPoint =
    EndPoint(
      req => for {
        entity   <- req.decodeJson[A].withId(id)
        result   <- repository.update(entity)
        response <- httpOkOr404(entity)(result)
      } yield response
    )

  def read(id: Identity): EndPoint =
    EndPoint(_ => repository.read(id).flatMap(httpOkOr404))

  def delete(id: Identity): EndPoint =
    EndPoint(
      _  => repository.delete(id).flatMap {
        case Left(NotFoundError(_, _)) => NotFound()
        case Left(error)               => InternalServerError(error.toString)
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

  private def httpOkOr404(a: A)(result: Either[_, _]): F[Response[F]] =
    result match {
      case Left(_)  => NotFound()
      case _        => Ok(a.asJson)
    }

  private def httpOkOr404(result: Either[_, A]): F[Response[F]] =
    result match {
      case Left(_)  => NotFound()
      case Right(a) => Ok(a.asJson)
    }

  private def httpCreatedOr500(a: A)(result: Either[_, _]): F[Response[F]] =
    result match {
      case Left(e)  =>
        InternalServerError(e.toString)
      case _        =>
        E.id(a).flatMap(id => Created(a.asJson, Location(Uri.unsafeFromString(s"/$segment/${id.get}"))))
    }
}

package object service {

  object IdentityVar extends PathVar(Identity.apply)

  protected class PathVar[A](cast: String => A) {
    def unapply(str: String): Option[A] =
      Try(cast(str)).toOption
  }
}