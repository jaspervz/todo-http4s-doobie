import cats.effect._
import com.typesafe.config.ConfigFactory
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._


package object config {
  case class ServerConfig(host: String ,port: Int)

  case class DatabaseConfig(driver: String, url: String, user: String, password: String, threadPoolSize: Int)

  case class Config(server: ServerConfig, database: DatabaseConfig)

  object Config {
    def load[F[_]: Sync: ContextShift](configFile: String = "application.conf"): Resource[F, Config] = {
      Blocker[F].flatMap { blocker =>
        Resource.eval(ConfigSource.fromConfig(ConfigFactory.load(configFile)).loadF[F, Config](blocker))
      }
    }
  }
}
