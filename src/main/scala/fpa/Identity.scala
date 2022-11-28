package fpa

import java.util.UUID
import cats._

type Identity = UUID

object Identity:

  def apply(s: String): Identity =
    UUID.fromString(s)

  val generate: () => Identity =
    () => UUID.randomUUID


trait HasIdentity[F[_], A]:

  def id(a: A): F[Option[Identity]]

  def withId(a: A)(id: Identity): F[A]

  def withGeneratedId(a: A): F[A] =
    withId(a)(Identity.generate())


extension [F[_], A](fa: F[A])(using  F: FlatMap[F], E : HasIdentity[F, A])
  def id: F[Option[Identity]] =
    F.flatMap(fa)(E.id)
  def withId(id: Identity): F[A] =
    F.flatMap(fa)(E.withId(_)(id))
  def withGeneratedId: F[A] =
    F.flatMap(fa)(E.withGeneratedId)