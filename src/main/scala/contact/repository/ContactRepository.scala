package contact
package repository

import cats._
import cats.implicits._
import fs2.Stream
import doobie.util.transactor.Transactor
import doobie.implicits._

import model._


object ContactRepository {

  def apply[F[_] : Monad](transactor: Transactor[F]): Repository[F, Contact] =
    new Repository[F, Contact] {

      def getAll: Stream[F, Contact] = sql"""
        SELECT
          id,
          description,
          importance
        FROM
          contacts
      """
        .query[Contact]
        .stream
        .transact(transactor)

      def create(contact: Contact): F[Result[Contact]] = sql"""
        INSERT INTO
          contacts (description, importance)
        VALUES
          (${contact.description}, ${contact.importance})
      """
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(transactor)
        .map(id => Right(contact.copy(id = Some(id))))

      def read(id: Long): F[Result[Contact]] = sql"""
        SELECT
          id,
          description,
          importance
        FROM
          contacts
        WHERE id = $id
      """
        .query[Contact]
        .option
        .transact(transactor)
        .map {
          case Some(contact) => Right(contact)
          case None => Left(NotFoundError("contact", id))
        }

      def delete(id: Long): F[Result[Unit]] = sql"""
        DELETE FROM
          contacts
        WHERE
          id = $id
      """
        .update
        .run
        .transact(transactor)
        .map(count =>
          if (count == 1) Right(()) else Left(NotFoundError("contacts", id))
        )

      def update(id: Long, contact: Contact): F[Result[Contact]] = sql"""
        UPDATE
          contacts
        SET
          description = ${contact.description},
          importance  = ${contact.importance}
        WHERE
          id = $id
      """
        .update
        .run
        .transact(transactor)
        .map(count =>
          if (count == 1) Right(contact.copy(id = Some(id))) else Left(NotFoundError("contacts", id))
        )

    }
}
