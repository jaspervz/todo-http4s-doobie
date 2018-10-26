package fpa

import cats.effect._
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

object Database {

  def transactor[F[_] : Async](config: DatabaseConfig): F[HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](config.driver, config.url, config.user, config.password)

  def initialize[F[_] : Sync](transactor: HikariTransactor[F]): F[Unit] =
    transactor.configure { datasource =>
      Sync[F].delay {
        Flyway
          .configure()
          .dataSource(datasource)
          .load()
          .migrate()
      }
    }

}
