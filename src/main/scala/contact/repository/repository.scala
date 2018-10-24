package contact

import doobie.util.transactor.Transactor
import fs2.Stream

package object repository {

  sealed trait RepositoryError
  case class NotFoundError(name: String, id: Long) extends RepositoryError

  trait Identity[A] {
    def id(a: A): Option[Long]
  }

  implicit class IdentityOps[A : Identity](a: A) {
    def id: Option[Long] = implicitly[Identity[A]].id(a)
  }

  trait StreamingRepository[F[_], A] {
    def getAll: Stream[F, A]
  }

  abstract class CrudRepository[F[_], A : Identity] {
    type Result[A] = F[Either[RepositoryError, A]]
    def create(a: A): Result[A]
    def read(id: Long): Result[A]
    def update(id: Long, a: A): Result[A]
    def delete(id: Long): Result[Unit]
  }

  abstract class Repository[F[_], A : Identity](transactor: Transactor[F])
    extends CrudRepository[F, A] with StreamingRepository[F, A]

}


