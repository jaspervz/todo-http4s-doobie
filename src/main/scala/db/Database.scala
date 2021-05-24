package db

import config.DatabaseConfig

import cats.implicits._
import cats.effect._
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

object Database {
  def transactor[F[_]: Sync: Concurrent: ContextShift](config: DatabaseConfig, executionContext: ExecutionContext, blocker: Blocker): Resource[F, HikariTransactor[F]] =
    HikariTransactor.newHikariTransactor[F](
      config.driver,
      config.url,
      config.user,
      config.password,
      executionContext,
      blocker
    ).evalMap(t => initialize(t))

  private def initialize[F[_]: Sync](transactor: HikariTransactor[F]): F[HikariTransactor[F]] =
    transactor.configure { dataSource =>
      Sync[F].delay {
        val flyWay = Flyway.configure().dataSource(dataSource).load()
        flyWay.migrate()
        ()
      }
    }.map(_ => transactor)
}
