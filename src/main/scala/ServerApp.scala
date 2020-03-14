import cats.effect.{Blocker, ExitCode, IO, IOApp}
import doobie.util.ExecutionContexts

object ServerApp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val bc: Blocker = Blocker.liftExecutionContext(ExecutionContexts.synchronous)
    HttpServer.create()
  }
}
