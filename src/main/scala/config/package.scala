import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._


package object config {
  case class ServerConfig(host: String ,port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

  case class Config(server: ServerConfig, database: DatabaseConfig)

  object Config {
    def load(configFile: String = "application.conf"): IO[Config] = {
      ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[IO, Config]
    }
  }
}
