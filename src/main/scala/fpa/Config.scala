package fpa

import pureconfig.*
import pureconfig.error.*
import pureconfig.generic.derivation.default.*
import com.comcast.ip4s.*

implicit val hostConfigReader: ConfigReader[Host] =
  ConfigReader.fromString[Host](ConvertHelpers.optF(Host.fromString))

implicit val portConfigReader: ConfigReader[Port] =
  ConfigReader.intConfigReader.map(_.toString).emap(ConvertHelpers.optF(Port.fromString))

case class ServerConfig(host: Host, port: Port)
  derives ConfigReader

case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)
  derives ConfigReader

case class LoggingConfig(logHeaders: Boolean, logBody: Boolean)
  derives ConfigReader

case class Config(server: ServerConfig, database: DatabaseConfig, logging: LoggingConfig)
  derives ConfigReader


object Config:

  import cats.*
  import cats.implicits.*
  import cats.effect.*

  import com.typesafe.config.ConfigFactory

  def load: Resource[IO, Config] =
    val config = IO.delay(ConfigSource.default.load[Config]).flatMap {
      case Left(error)  => IO.raiseError[Config](new ConfigReaderException[Config](error))
      case Right(value) => IO.pure(value)
    }
    Resource.eval(config)

