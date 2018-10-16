package repository

import cats.effect.IO
import doobie.util.transactor.Transactor
import fs2.Stream
import model.{Importance, Contact, ContactNotFound}
import doobie._
import doobie.implicits._

class ContactRepository(transactor: Transactor[IO]) {

  private implicit val importanceMeta: Meta[Importance] = Meta[String].xmap(Importance.unsafeFromString, _.value)

  def getContacts: Stream[IO, Contact] = sql"""
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

  def getContact(id: Long): IO[Either[ContactNotFound, Contact]] = sql"""
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
      case None => Left(ContactNotFound())
    }

  def createContact(contact: Contact): IO[Contact] = sql"""
    INSERT INTO
      contacts (description, importance)
    VALUES
      (${contact.description}, ${contact.importance})
  """
    .update
    .withUniqueGeneratedKeys[Long]("id")
    .transact(transactor)
    .map(id => contact.copy(id = Some(id)))

  def deleteContact(id: Long): IO[Either[ContactNotFound, Unit]] = sql"""
    DELETE FROM
      contacts
    WHERE
      id = $id
  """
    .update
    .run
    .transact(transactor)
    .map(count => if (count == 1) Right(()) else Left(ContactNotFound()))

  def updateContact(id: Long, contact: Contact): IO[Either[ContactNotFound, Contact]] = sql"""
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
    .map(count => if (count == 1) Right(contact.copy(id = Some(id))) else Left(ContactNotFound()))

}
