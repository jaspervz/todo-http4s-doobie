package service

import model._
import model.DbError._
import model.Importance._
import repository.TodoRepository

import cats.effect.IO
import io.circe.Json
import io.circe.literal._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.{Request, Response, Status, Uri, _}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TodoServiceSpec extends AnyWordSpec with Matchers {

  "TodoService" should {
    "create a todo" in {
      val id = 1
      val todo = Todo(None, "my todo", Low)
      implicit val repo: TodoRepository[IO] = testRepo(List())
      val createJson = json"""
        {
          "description": ${todo.description},
          "importance": ${todo.importance.value}
        }"""
      val response = serve(Request[IO](POST, uri"/todos").withEntity(createJson))
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${todo.description},
          "importance": ${todo.importance.value}
        }"""
    }

    "update a todo" in {
      val id = 1
      val todo = Todo(Some(id), "updated todo", Medium)
      implicit val repo: TodoRepository[IO] = testRepo(List(todo))
      val updateJson = json"""
        {
          "description": ${todo.description},
          "importance": ${todo.importance.value}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/todos/$id")).withEntity(updateJson))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${todo.description},
          "importance": ${todo.importance.value}
        }"""
    }

    "return NOT FOUND if todo does not exist during update" in {
      val id = 1
      val todo = Todo(Some(id), "updated todo", Medium)
      implicit val repo: TodoRepository[IO] = testRepo(List())
      val updateJson = json"""
        {
          "description": ${todo.description},
          "importance": ${todo.importance.value}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/todos/$id")).withEntity(updateJson))
      response.status shouldBe Status.NotFound
    }

    "return a single todo" in {
      val id = 1
      val todo = Todo(Some(id), "my todo", High)
      implicit val repo: TodoRepository[IO] = testRepo(List(todo))

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/todos/$id")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${todo.description},
          "importance": ${todo.importance.value}
        }"""
    }

    "return NOT FOUND if todo does not exist while fetching single todo" in {
      val id = 1
      implicit val repo: TodoRepository[IO] = testRepo(List())

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/todos/$id")))
      response.status shouldBe Status.NotFound
    }

    "return all todos" in {
      val id1 = 1
      val todo1 = Todo(Some(id1), "my todo 1", High)
      val id2 = 2
      val todo2 = Todo(Some(id2), "my todo 2", Medium)
      implicit val repo: TodoRepository[IO] = testRepo(List(todo1, todo2))

      val response = serve(Request[IO](GET, uri"/todos"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        [
         {
           "id": $id1,
           "description": ${todo1.description},
           "importance": ${todo1.importance.value}
         },
         {
           "id": $id2,
           "description": ${todo2.description},
           "importance": ${todo2.importance.value}
         }
        ]"""
    }

    "return empty list if there is no todo in the database" in {
      implicit val repo: TodoRepository[IO] = testRepo(List())

      val response = serve(Request[IO](GET, uri"/todos"))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""[]"""
    }

    "delete a todo" in {
      val id = 1
      val todo = Todo(Some(id), "my todo", High)
      implicit val repo: TodoRepository[IO] = testRepo(List(todo))

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/todos/$id")))
      response.status shouldBe Status.NoContent
    }

    "return NOT FOUND if todo does not exist during deletion" in {
      val id = 1
      implicit val repo: TodoRepository[IO] = testRepo(List())

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/todos/$id")))
      response.status shouldBe Status.NotFound
    }
  }

  private def serve(request: Request[IO])(implicit repo: TodoRepository[IO]): Response[IO] = {
    val service = new TodoService[IO](repo).routes
    service.orNotFound(request).unsafeRunSync()
  }

  def testRepo(items: List[Todo]): TodoRepository[IO] = new TodoRepository[IO] {
    override def getTodo(id: Long): IO[Either[DbError, Todo]] =
      IO.pure(items.find(_.id.contains(id)).toRight(TodoNotFoundError))

    override def getTodos: IO[List[Todo]] =
      IO.pure(items)

    override def createTodo(todo: Todo): IO[Todo] =
      IO.pure(todo.copy(id = Some(items.size.toLong + 1)))

    override def deleteTodo(id: Long): IO[Either[DbError, Unit]] =
      IO.pure(items.find(_.id.contains(id)).map(_ => ()).toRight(TodoNotFoundError))

    override def updateTodo(id: Long, todo: Todo): IO[Either[DbError, Todo]] =
      IO.pure(items.find(_.id.contains(id)).map(_.copy(id = Some(id))).toRight(TodoNotFoundError))
  }
}
