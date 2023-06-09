import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import cats.effect.{IO, IOApp}

object FutureIOEffectExample extends IOApp.Simple {
  def printFutureMessage: Future[Unit] =
    Future(println("Future effect"))

  def printIOMessage: IO[Unit] =
    IO(println("IO effect"))

  def run: IO[Unit] = {
    val futureEffect: Future[Unit] = printFutureMessage
    val ioEffect: IO[Unit]         = printIOMessage

    for {
      _ <- ioEffect
      _ <- IO(futureEffect)
      // _ <- IO.fromFuture(IO(printFutureMessage))
    } yield ()
  }
}
