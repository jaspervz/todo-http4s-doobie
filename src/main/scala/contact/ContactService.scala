package contact

import cats.effect._

import fpa._

object ContactService {

  def apply(repository: Repository[IO, Contact]): Service[Contact] =
    new Service[Contact]("contacts", repository)

}
