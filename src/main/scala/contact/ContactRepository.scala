package contact

import java.util.UUID

import cats.implicits._
import cats.effect._

import doobie.Meta
import doobie.implicits._
import doobie.util.transactor._

import fs2._

import fpa._
import fpa.repository._

object ContactRepository {

  def apply(transactor: Transactor[IO]): Repository[IO, Contact] =
    new Repository[IO, Contact]("contacts") {

      def stream: Stream[IO, Contact] =
        sql"""
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

      def create(contact: Contact): IO[Result[Unit]] =
        sql"""
          INSERT INTO contacts (
              id,
              description,
              importance
          )
          VALUES (
            ${contact.id},
            ${contact.description},
            ${contact.importance}
          )
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(contact.id))

      def read(id: Identity): IO[Result[Contact]] =
        sql"""
          SELECT
            id,
            description,
            importance
          FROM
            contacts
          WHERE
            id = $id
        """
        .query[Contact]
        .option
        .transact(transactor)
        .map {
          case Some(contact) => Right(contact)
          case None => Left(NotFoundError(name, id))
        }

      def delete(id: Identity): IO[Result[Unit]] =
        sql"""
          DELETE FROM
            contacts
          WHERE
            id = $id
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(id))

      def update(contact: Contact): IO[Result[Unit]] =
        sql"""
          UPDATE
            contacts
          SET
            description = ${contact.description},
            importance  = ${contact.importance}
          WHERE
            id = ${contact.id}
        """
        .update
        .run
        .transact(transactor)
        .map(expectUpdate(contact.id))

      private def expectUpdate(id: Option[Identity])(rowCount: Int): Result[Unit] =
        id match {
          case None                        => Left(NoIdentityError(name))
          case Some(id) if (rowCount == 0) => Left(UpdateError(name, id))
          case _                           => Right(())
        }

      private def expectUpdate(id: Identity)(rowCount: Int): Result[Unit] =
        expectUpdate(Some(id))(rowCount)
    }

  implicit val uuidMeta: Meta[UUID] =
    doobie.h2.implicits.UuidType

}
