package contact

import fpa.*

object ContactService {

  import cats.effect.*

  def apply(repository: Repository[IO, Contact]): Service[Contact] =
    new Service[Contact]("contacts", repository)

}
