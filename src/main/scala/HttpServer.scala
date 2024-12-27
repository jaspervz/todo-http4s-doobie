import cats.effect._
import com.comcast.ip4s.{Host, Port}
import config.Config
import db.Database
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.implicits._
import repository.TodoRepository
import service.TodoService
import org.http4s.ember.server.EmberServerBuilder

object HttpServer {
  def create(configFile: String = "application.conf"): IO[ExitCode] = {
    resources(configFile).use(create)
  }

  private def resources(configFile: String): Resource[IO, Resources] = {
    for {
      config <- Config.load(configFile)
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      transactor <- Database.transactor(config.database, ec)
    } yield Resources(transactor, config)
  }

  private def create(resources: Resources): IO[ExitCode] = {
    for {
      _ <- Database.initialize(resources.transactor)
      repository = new TodoRepository(resources.transactor)
      port <- IO.fromOption(Port.fromInt(resources.config.server.port))(new IllegalArgumentException(s"Port number ${resources.config.server.port} not valid, min value ${Port.MinValue}, max value ${Port.MaxValue}"))
      host <- IO.fromOption(Host.fromString(resources.config.server.host))(new IllegalArgumentException(s"${resources.config.server.host} is not a valid host"))
      exitCode <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(new TodoService(repository).routes.orNotFound)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)
}
