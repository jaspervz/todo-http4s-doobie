import cats.effect._
import config.Config
import db.Database
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.blaze._
import repository.TodoRepository
import service.TodoService
import scala.concurrent.ExecutionContext.global

object HttpServer {
  def create(configFile: String = "application.conf")(implicit contextShift: ContextShift[IO], concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[ExitCode] = {
    resources(configFile).use(create)
  }

  private def resources(configFile: String)(implicit contextShift: ContextShift[IO]): Resource[IO, Resources] = {
    for {
      config <- Config.load(configFile)
      ec <- ExecutionContexts.fixedThreadPool[IO](config.database.threadPoolSize)
      blocker <- Blocker[IO]
      transactor <- Database.transactor(config.database, ec, blocker)
    } yield Resources(transactor, config)
  }

  private def create(resources: Resources)(implicit concurrentEffect: ConcurrentEffect[IO], timer: Timer[IO]): IO[ExitCode] = {
    for {
      _ <- Database.initialize(resources.transactor)
      repository = new TodoRepository(resources.transactor)
      exitCode <- BlazeServerBuilder[IO](global)
        .bindHttp(resources.config.server.port, resources.config.server.host)
        .withHttpApp(new TodoService(repository).routes.orNotFound).serve.compile.lastOrError
    } yield exitCode
  }

  case class Resources(transactor: HikariTransactor[IO], config: Config)
}
