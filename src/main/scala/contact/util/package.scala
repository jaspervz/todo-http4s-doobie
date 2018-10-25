package contact

package object util {

  object stream {
    import fs2.Stream
    implicit class EffectOps[F[_], A](fa: F[A]) {
      def stream: Stream[F, A] = Stream.eval(fa)
    }
  }

}
