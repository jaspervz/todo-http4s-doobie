package contact

import cats.effect.*
import cats.implicits.*

import fs2.*

import scala.concurrent.ExecutionContext.global

import org.http4s.server.middleware.*
import org.http4s.implicits.*
import org.http4s.ember.server.*
import org.http4s.dsl.io.*

import doobie.util._
import doobie.hikari.HikariTransactor

import fpa.*
import org.http4s.server.Server


object ContactServer extends IOApp {

  def create: IO[ExitCode] =
    resources.use(instantiate)

  def resources: Resource[IO, Resources] =
    for {
      config     <- Config.load
      ec         <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Database.transactor(config.database)(ec)
    } yield Resources(transactor, config)

  def instantiate(resources: Resources): IO[ExitCode] = {
    for {
      _          <- Database.initialize(resources.transactor)
      repository =  ContactRepository(resources.transactor)
      exitCode   <- EmberServerBuilder
                      .default[IO]
                      .withHost(resources.config.server.host)
                      .withPort(resources.config.server.port)
                      .withHttpApp(ContactService(repository).http.orNotFound)
                      .build
                      .use(_ => IO.never)
                      .as(ExitCode.Success)
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)

  def run(args: List[String]): IO[ExitCode] =
    create
}
