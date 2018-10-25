package contact

import cats._
import cats.implicits._
import fs2.Stream

package object repository {

  sealed trait RepositoryError
  case class NotFoundError(entity: String, id: Long) extends RepositoryError

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
    // type Result[A] = F[Either[RepositoryError, A]]
    def create(a: A): F[Either[RepositoryError, A]]
    def read(id: Long): F[Either[RepositoryError, A]]
    def update(id: Long, a: A): F[Either[RepositoryError, A]]
    def delete(id: Long): F[Either[RepositoryError, A]]
  }

  abstract class Repository[F[_], A : Identity]
    extends CrudRepository[F, A] with StreamingRepository[F, A]

}


