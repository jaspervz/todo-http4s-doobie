package service

import model._
import model.DbError._
import repository.TodoRepository

import cats._
import cats.effect._
import cats.implicits._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.headers.Location
import org.http4s.{HttpRoutes, Uri}

class TodoService[F[_]: Sync: Defer: JsonDecoder](repository: TodoRepository[F]) extends Http4sDsl[F] {

  private val prefixPath = "/todos"

  private val httpRoutes = HttpRoutes.of[F] {
    case GET -> Root  =>
      Ok(repository.getTodos)

    case GET -> Root / LongVar(id) =>
      for {
        getResult <- repository.getTodo(id)
        response <- todoResult(getResult)
      } yield response

    case req @ POST -> Root =>
      for {
        todo <- req.decodeJson[Todo]
        createdTodo <- repository.createTodo(todo)
        response <- Created(createdTodo.asJson, Location(Uri.unsafeFromString(s"/todos/${createdTodo.id.get}")))
      } yield response

    case req @ PUT -> Root / LongVar(id) =>
      for {
        todo <-req.decodeJson[Todo]
        updateResult <- repository.updateTodo(id, todo)
        response <- todoResult(updateResult)
      } yield response

    case DELETE -> Root / LongVar(id) =>
      repository.deleteTodo(id).flatMap {
        case Left(TodoNotFoundError) => NotFound()
        case Right(_) => NoContent()
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

  private def todoResult(result: Either[DbError, Todo]) = {
    result match {
      case Left(TodoNotFoundError) => NotFound()
      case Right(todo) => Ok(todo.asJson)
    }
  }
}
