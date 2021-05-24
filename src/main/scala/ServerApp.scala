import config.Config
import resources._
import repository._
import service._

import cats.effect._
import cats.implicits._
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.global

object ServerApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val configFile = "application.conf"
    (for {
      config <- Config.load[IO](configFile)
      resources <- AppResources.make[IO](config)
      server <- {
        val repositories = Repositories.make[IO](resources.transactor)
        val httpApi = HttpApi.make[IO](repositories)
        BlazeServerBuilder[IO](global)
          .bindHttp(config.server.port, config.server.host)
          .withHttpApp(httpApi.httpApp).resource
      }
    } yield server).use { server =>
      IO(println(s"HTTP Server started at ${server.address}")) >> IO.never.as(ExitCode.Success)
    }
  }
}
