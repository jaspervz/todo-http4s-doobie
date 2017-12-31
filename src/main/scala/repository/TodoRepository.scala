package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import model.Todo
import model.TodoNotFoundError

class TodoRepository(transactor: Transactor[IO]) {
  def getTodos: Stream[IO, Todo] = ???

  def getTodo(id: Long): IO[Either[TodoNotFoundError.type, Todo]] = ???

  def createTodo(todo: Todo): IO[Todo] = ???

  def deleteTodo(id: Long): IO[Either[TodoNotFoundError.type, Unit]] = ???

  def updateTodo(id: Long, todo: Todo): IO[Either[TodoNotFoundError.type, Todo]] = ???
}
