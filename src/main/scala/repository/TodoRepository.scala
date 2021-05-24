package repository

import model._
import model.DbError._

import cats.effect._
import cats.implicits._
import doobie.util.transactor.Transactor
import doobie._
import doobie.implicits._

trait TodoRepository[F[_]] {
  def getTodos: F[List[Todo]]
  def getTodo(id: Long): F[Either[DbError, Todo]]
  def createTodo(todo: Todo): F[Todo]
  def deleteTodo(id: Long): F[Either[DbError, Unit]]
  def updateTodo(id: Long, todo: Todo): F[Either[DbError, Todo]]
}

object TodoRepository {
  def make[F[_]: Sync](
      transactorResource: Resource[F, Transactor[F]]
  ): TodoRepository[F] =
    new TodoRepository[F] {
      private implicit val importanceMeta: Meta[Importance] = Meta[String].timap(Importance.unsafeFromString)(_.value)

      def getTodos: F[List[Todo]] =
        transactorResource.use { t =>
          sql"SELECT id, description, importance FROM todo".query[Todo].stream.transact(t).compile.toList
        }

      def getTodo(id: Long): F[Either[DbError, Todo]] =
        transactorResource.use { t =>
          sql"SELECT id, description, importance FROM todo WHERE id = $id".query[Todo].option.transact(t).map {
            case Some(todo) => Right(todo)
            case None => Left(TodoNotFoundError)
          }
        }

      def createTodo(todo: Todo): F[Todo] =
        transactorResource.use { t =>
          sql"INSERT INTO todo (description, importance) VALUES (${todo.description}, ${todo.importance})".update.withUniqueGeneratedKeys[Long]("id").transact(t).map { id =>
            todo.copy(id = Some(id))
          }
        }

      def deleteTodo(id: Long): F[Either[DbError, Unit]] =
        transactorResource.use { t =>
          sql"DELETE FROM todo WHERE id = $id".update.run.transact(t).map { affectedRows =>
            if (affectedRows == 1) {
              Right(())
            } else {
              Left(TodoNotFoundError)
            }
          }
        }

      def updateTodo(id: Long, todo: Todo): F[Either[DbError, Todo]] =
        transactorResource.use { t =>
          sql"UPDATE todo SET description = ${todo.description}, importance = ${todo.importance} WHERE id = $id".update.run.transact(t).map { affectedRows =>
            if (affectedRows == 1) {
              Right(todo.copy(id = Some(id)))
            } else {
              Left(TodoNotFoundError)
            }
          }
        }
    }
}

