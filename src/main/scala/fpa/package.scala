import java.util.UUID

import cats.effect.Sync

package object fpa {

  type Identity = UUID

  object Identity {

    def apply(s: String): Identity =
      UUID.fromString(s)

    def generate() : Identity =
      UUID.randomUUID
  }

  trait Entity[F[_], A] {
    def id(a: A): F[Option[Identity]]
    def withId(a: A)(id: Identity): F[A]
    def withGeneratedId(a: A): F[A]
  }

  implicit class EntityOps[F[_] : Sync, A : Entity[F, ?]](fa: F[A]) {

    val F = implicitly[Sync[F]]
    val E = implicitly[Entity[F, A]]

    def id: F[Option[Identity]] =
      F.flatMap(fa)(E.id)

    def withGeneratedId: F[A] =
      F.flatMap(fa)(E.withGeneratedId)

    def withId(id: Identity): F[A] =
      F.flatMap(fa)(E.withId(_)(id))
  }
}
