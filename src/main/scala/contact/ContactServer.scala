package contact

import cats.effect._
import cats.implicits._

import fs2._

import scala.concurrent.ExecutionContext.global

import org.http4s.server.middleware._
import org.http4s.implicits._
import org.http4s.blaze.server._
import org.http4s.dsl.io._

import doobie.util._
import doobie.hikari._

import fpa._
import org.http4s.server.Server


// object ContactServer extends IOApp {

//   def run(args: List[String]): IO[ExitCode] = {
//     val server = for {
//       config     <- Config.load[IO]()
//       transactor <- Database.transactor[IO](config.database)
//       _          <- Database.initialize(transactor)
//       repository =  ContactRepository(transactor)
//       service    =  ContactService(repository)
//       http       =  Logger(config.logging.logHeaders, config.logging.logBody, service.http)
//       code       <- BlazeServerBuilder[IO]
//                       .bindHttp(config.server.port, config.server.host)
//                       .withHttpApp(http)
//                       .resource
//                       .use(_ => IO.never)
//                       .as(ExitCode.Success)
//     } yield code

//     server
//   }
// }

object ContactServer extends IOApp {

  def create(configFile: String = "application.conf"): IO[ExitCode] =
    resources(configFile).use(instantiate)

  def resources(configFile: String): Resource[IO, Resources] =
    for {
      config     <- Config.load(configFile)
      ec         <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Database.transactor(config.database)(ec)
    } yield Resources(transactor, config)

  def instantiate(resources: Resources): IO[ExitCode] = {
    for {
      _          <- Database.initialize(resources.transactor)
      repository =  ContactRepository(resources.transactor)
      exitCode   <- BlazeServerBuilder[IO]
                      .bindHttp(resources.config.server.port, resources.config.server.host)
                      .withHttpApp(ContactService(repository).http.orNotFound)
                      .serve
                      .compile
                      .lastOrError
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)

  def run(args: List[String]): IO[ExitCode] = create()
}
