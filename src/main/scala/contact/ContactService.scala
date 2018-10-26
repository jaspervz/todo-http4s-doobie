package contact

import cats.effect._
import fpa._

object ContactService {

  def apply[F[_] : Effect](repository: Repository[F, Contact]): Service[F, Contact] =
    new Service[F, Contact]("contacts", repository)

}
