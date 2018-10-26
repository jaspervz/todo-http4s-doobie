package contact

import fs2._
import cats.effect.IO
import org.http4s.server.blaze._

import fpa._

object ContactServer extends StreamApp[IO] {

  import fpa.stream._
  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, StreamApp.ExitCode] =
    for {
      config     <- Config.load[IO]().stream
      transactor <- Database.transactor[IO](config.database).stream
      _          <- Database.initialize(transactor).stream
      repository =  ContactRepository(transactor)
      service    =  ContactService(repository)
      code       <- BlazeBuilder[IO]
                      .bindHttp(config.server.port, config.server.host)
                      .mountService(service.http, "/")
                      .serve
    } yield code

}
