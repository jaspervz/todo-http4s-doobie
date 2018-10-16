package contact

import fs2._
import fs2.StreamApp._
import cats.effect.IO
import org.http4s.server.blaze.BlazeBuilder

import db._
import repository._
import service._
import config._

object Server extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    for {
      config     <- Stream.eval(Config.load())
      transactor <- Stream.eval(Database.transactor(config.database))
      _          <- Stream.eval(Database.initialize(transactor))
      repository =  new ContactRepository(transactor)
      service    =  new ContactService(repository).service
      exitCode   <- BlazeBuilder[IO]
                      .bindHttp(config.server.port, config.server.host)
                      .mountService(service, "/")
                      .serve
    } yield exitCode

}
