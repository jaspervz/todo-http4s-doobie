import cats.effect.{ExitCode, IO, IOApp}

object ServerApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    HttpServer.create()
  }
}
