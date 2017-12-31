package db

import cats.effect.IO
import config.DatabaseConfig
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

object Database {
  def transactor(config: DatabaseConfig): IO[HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](config.driver, config.url, config.user, config.password)
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { datasource =>
      IO {
        val flyWay = new Flyway()
        flyWay.setDataSource(datasource)
        flyWay.migrate()
      }
    }
  }
}
