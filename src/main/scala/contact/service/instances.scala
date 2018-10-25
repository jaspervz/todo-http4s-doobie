package contact
package service

import cats.effect._

import model._
import repository._


object ContactService {

  def apply[F[_] : Effect](repository: Repository[F, Contact]): Service[F, Contact] =
    new Service[F, Contact]("contacts", repository)

}
