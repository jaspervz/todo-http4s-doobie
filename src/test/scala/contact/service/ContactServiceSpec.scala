package contact
package service

import cats.effect.IO
import fs2.Stream
import io.circe._
import io.circe.literal._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import model._
import repository._

class ContactServiceSpec extends WordSpec with MockFactory with Matchers {

  private val repository = stub[ContactRepository]
  private val service    = new ContactService(repository).service

  "ContactService" should {
    "create a Contact" in {
      val id = 1
      val contact = Contact(None, "my Contact", Low)
      (repository.createContact _).when(contact).returns(IO.pure(contact.copy(id = Some(id))))
      val createJson = json"""
        {
          "description": ${contact.description},
          "importance": ${contact.importance.value}
        }"""
      val response = serve(Request[IO](POST, uri("/contacts")).withBody(createJson).unsafeRunSync())
      response.status shouldBe Status.Created
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${contact.description},
          "importance": ${contact.importance.value}
        }"""
    }

    "update a Contact" in {
      val id = 1
      val contact = Contact(None, "updated Contact", Medium)
      (repository.updateContact _).when(id, contact).returns(IO.pure(Right(contact.copy(id = Some(id)))))
      val updateJson = json"""
        {
          "description": ${contact.description},
          "importance": ${contact.importance.value}
        }"""

      val response = serve(Request[IO](PUT, Uri.unsafeFromString(s"/contacts/$id")).withBody(updateJson).unsafeRunSync())
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${contact.description},
          "importance": ${contact.importance.value}
        }"""
    }

    "return a single Contact" in {
      val id = 1
      val contact = Contact(Some(id), "my Contact", High)
      (repository.getContact _).when(id).returns(IO.pure(Right(contact)))

      val response = serve(Request[IO](GET, Uri.unsafeFromString(s"/contacts/$id")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        {
          "id": $id,
          "description": ${contact.description},
          "importance": ${contact.importance.value}
        }"""
    }

    "return all Contacts" in {
      val id1 = 1
      val Contact1 = Contact(Some(id1), "my Contact 1", High)
      val id2 = 2
      val Contact2 = Contact(Some(id2), "my Contact 2", Medium)
      val Contacts = Stream(Contact1, Contact2)
      (repository.getContacts _).when().returns(Contacts)

      val response = serve(Request[IO](GET, uri("/contacts")))
      response.status shouldBe Status.Ok
      response.as[Json].unsafeRunSync() shouldBe json"""
        [
         {
           "id": $id1,
           "description": ${Contact1.description},
           "importance": ${Contact1.importance.value}
         },
         {
           "id": $id2,
           "description": ${Contact2.description},
           "importance": ${Contact2.importance.value}
         }
        ]"""
    }

    "delete a Contact" in {
      val id = 1
      (repository.deleteContact _).when(id).returns(IO.pure(Right(())))

      val response = serve(Request[IO](DELETE, Uri.unsafeFromString(s"/contacts/$id")))
      response.status shouldBe Status.NoContent
    }
  }

  private def serve(request: Request[IO]): Response[IO] = {
    service.orNotFound(request).unsafeRunSync()
  }
}
