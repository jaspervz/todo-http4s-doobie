package contact

package object util {

  object stream {
    import fs2.Stream
    implicit class IOOps[F[_], A](ioa: F[A]) {
      def stream: Stream[F, A] = Stream.eval(ioa)
    }
  }
}
