package fpa

trait StreamingRepository[F[_], A]:
  import fs2.Stream
  def stream: Stream[F, A]


abstract class CrudRepository[F[_], A](name: String):
  type Result[A] = Either[RepositoryError, A]
  def create(a: A): F[Result[Unit]]
  def read(id: Identity): F[Result[A]]
  def update(a: A): F[Result[Unit]]
  def delete(id: Identity): F[Result[Unit]]


abstract class Repository[F[_], A](val name: String)
  extends CrudRepository[F, A](name)
  with StreamingRepository[F, A]

sealed trait RepositoryError
case class NotFoundError(name: String, id: Identity) extends RepositoryError
case class NoIdentityError(name: String)             extends RepositoryError
case class UpdateError(name: String, id: Identity)   extends RepositoryError
