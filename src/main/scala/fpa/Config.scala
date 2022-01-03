package fpa

import cats._
import cats.implicits._

import cats.effect._

case class ServerConfig(host: String, port: Int)

case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

case class LoggingConfig(logHeaders: Boolean, logBody: Boolean)

case class Config(server: ServerConfig, database: DatabaseConfig, logging: LoggingConfig)


object Config {

  import com.typesafe.config.ConfigFactory
  import pureconfig._
  import pureconfig.error._
  import pureconfig.generic.auto._

  def load(file: String = "application.conf"): Resource[IO, Config] = {
    val config = IO.delay(ConfigSource.default.load[Config]).flatMap {
      case Left(error)  => IO.raiseError[Config](new ConfigReaderException[Config](error))
      case Right(value) => IO.pure(value)
    }
    
    Resource.eval(config)
  }
}
