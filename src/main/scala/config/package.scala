import cats.effect.IO
import pureconfig.error.ConfigReaderException

package object config {
  case class ServerConfig(host: String ,port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String)

  case class Config(server: ServerConfig, database: DatabaseConfig)

  object Config {
    import pureconfig._

    def load: IO[Config] = {
      IO {
        loadConfig[Config]
      }.flatMap {
        case Left(e) => IO.raiseError[Config](new ConfigReaderException[Config](e))
        case Right(config) => IO.pure(config)
      }
    }
  }
}
