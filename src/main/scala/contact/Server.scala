package contact

import fs2._
import fs2.StreamApp._
import cats.effect.IO
import org.http4s.server.blaze.BlazeBuilder

import db._
import repository._
import service._
import config._
import util.stream._

object Server extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    for {
      config     <- Config.load().stream
      transactor <- Database.transactor(config.database).stream
      _          <- Database.initialize(transactor).stream
      repository =  ContactRepository(transactor)
      service    =  new ContactService(repository).service
      exitCode   <- BlazeBuilder[IO]
                      .bindHttp(config.server.port, config.server.host)
                      .mountService(service, "/")
                      .serve
    } yield exitCode

}
