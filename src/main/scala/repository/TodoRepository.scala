package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import model.{Importance, Todo, TodoNotFoundError}
import doobie._
import doobie.implicits._

class TodoRepository(transactor: Transactor[IO]) {
  private implicit val importanceMeta: Meta[Importance] = Meta[String].xmap(Importance.unsafeFromString, _.value)

  def getTodos: Stream[IO, Todo] = {
    sql"SELECT id, description, importance FROM todo".query[Todo].stream.transact(transactor)
  }

  def getTodo(id: Long): IO[Either[TodoNotFoundError.type, Todo]] = {
    sql"SELECT id, description, importance FROM todo WHERE id = $id".query[Todo].option.transact(transactor).map {
      case Some(todo) => Right(todo)
      case None => Left(TodoNotFoundError)
    }
  }

  def createTodo(todo: Todo): IO[Todo] = {
    sql"INSERT INTO todo (description, importance) VALUES (${todo.description}, ${todo.importance})".update.withUniqueGeneratedKeys[Long]("id").transact(transactor).map { id =>
      todo.copy(id = Some(id))
    }
  }

  def deleteTodo(id: Long): IO[Either[TodoNotFoundError.type, Unit]] = {
    sql"DELETE FROM todo WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(())
      } else {
        Left(TodoNotFoundError)
      }
    }
  }

  def updateTodo(id: Long, todo: Todo): IO[Either[TodoNotFoundError.type, Todo]] = {
    sql"UPDATE todo SET description = ${todo.description}, importance = ${todo.importance} WHERE id = $id".update.run.transact(transactor).map { affectedRows =>
      if (affectedRows == 1) {
        Right(todo.copy(id = Some(id)))
      } else {
        Left(TodoNotFoundError)
      }
    }
  }
}
