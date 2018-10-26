package fpa

import fpa.repository._

trait StreamingRepository[F[_], A] {
  import fs2.Stream
  def readAll: Stream[F, A]
}

abstract class CrudRepository[F[_], A : Identity] {
  type Result[A] = Either[RepositoryError, A]
  def create(a: A): F[Result[A]]
  def read(id: Long): F[Result[A]]
  def update(id: Long, a: A): F[Result[A]]
  def delete(id: Long): F[Result[Unit]]
}

abstract class Repository[F[_], A : Identity]
  extends CrudRepository[F, A] with StreamingRepository[F, A]

package object repository {

  sealed trait RepositoryError
  case class NotFoundError(entity: String, id: Long) extends RepositoryError

  trait Identity[A] {
    def id(a: A): Option[Long]
  }

  implicit class IdentityOps[A : Identity](a: A) {
    def id: Option[Long] = implicitly[Identity[A]].id(a)
  }

}