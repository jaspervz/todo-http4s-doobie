package fpa

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException

case class ServerConfig(host: String, port: Int)

case class DatabaseConfig(driver: String, url: String, user: String, password: String)

case class Config(server: ServerConfig, database: DatabaseConfig)

object Config {
  import pureconfig._

  def load(file: String = "application.conf"): IO[Config] =
    IO(loadConfig[Config](ConfigFactory.load(file))).flatMap {
      case Left(e)       => IO.raiseError[Config](new ConfigReaderException[Config](e))
      case Right(config) => IO.pure(config)
    }
}
