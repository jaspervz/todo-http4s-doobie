import java.util.UUID

import cats._

package object fpa {

  type Identity = UUID

  object Identity {

    def apply(s: String): Identity =
      UUID.fromString(s)

    val generate: () => Identity =
      () => UUID.randomUUID
  }

  trait HasIdentity[F[_], A] {

    def id(a: A): F[Option[Identity]]

    def withId(a: A)(id: Identity): F[A]

    def withGeneratedId(a: A): F[A] =
      withId(a)(Identity.generate())
  }

  implicit class EntityOps[F[_] : FlatMap, A : HasIdentity[F, *]](fa: F[A]) {

    val F = implicitly[FlatMap[F]]
    val E = implicitly[HasIdentity[F, A]]

    def id: F[Option[Identity]] =
      F.flatMap(fa)(E.id)

    def withGeneratedId: F[A] =
      F.flatMap(fa)(E.withGeneratedId)

    def withId(id: Identity): F[A] =
      F.flatMap(fa)(E.withId(_)(id))
  }
}
