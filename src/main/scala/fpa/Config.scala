package fpa

import cats._
import cats.implicits._

case class ServerConfig(host: String, port: Int)

case class DatabaseConfig(driver: String, url: String, user: String, password: String)

case class LoggingConfig(logHeaders: Boolean, logBody: Boolean)

case class Config(server: ServerConfig, database: DatabaseConfig, logging: LoggingConfig)


object Config {

  import com.typesafe.config.ConfigFactory

  import pureconfig.error.ConfigReaderException
  import pureconfig._

  def load[F[_]](file: String = "application.conf")(implicit F: MonadError[F, Throwable]): F[Config] =
    F.pure(loadConfig[Config](ConfigFactory.load(file))).flatMap {
      case Left(e)       => F.raiseError[Config](new ConfigReaderException[Config](e))
      case Right(config) => F.pure(config)
    }
}
