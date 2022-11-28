package fpa

import pureconfig.*
import pureconfig.error.*
import pureconfig.generic.derivation.default.*

case class ServerConfig(host: String, port: Int)
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

  def load(file: String = "application.conf"): Resource[IO, Config] =
    val config = IO.delay(ConfigSource.default.load[Config]).flatMap {
      case Left(error)  => IO.raiseError[Config](new ConfigReaderException[Config](error))
      case Right(value) => IO.pure(value)
    }
    Resource.eval(config)

