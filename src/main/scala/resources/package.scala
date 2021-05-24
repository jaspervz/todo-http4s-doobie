import config._
import db._

import cats.effect._
import doobie._
import doobie.util.ExecutionContexts

package object resources {

  final case class AppResources[F[_]](
    transactor: Resource[F, Transactor[F]]
  )

  object AppResources {
    def make[F[_]: Concurrent: ContextShift](config: Config): Resource[F, AppResources[F]] = {

      val transactorResource = for {
        ec <- ExecutionContexts.fixedThreadPool[F](config.database.threadPoolSize)
        blocker <- Blocker[F]
        transactor <- Database.transactor(config.database, ec, blocker)
      } yield transactor

      Resource.pure(AppResources(transactorResource))
    }
  }

}
