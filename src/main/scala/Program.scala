import models._
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Semaphore
import concurrent.duration.DurationInt
import org.typelevel.log4cats.Logger
import scala.concurrent.duration.FiniteDuration
import scala.collection.immutable._

object Program:
  def program[F[_]: Temporal: Parallel](logger: Logger[F]): F[Unit] = {

    //TODO: write a function to create forks and philosophers correctly
    val forks = Vector(
      Fork(1),
      Fork(2),
      Fork(3),
      Fork(4),
      Fork(5)
    )
    val philosophers = Vector(
      Philosopher(1, StateOfBeing.Thinking, TwoForks(forks(0), forks(4))),
      Philosopher(2, StateOfBeing.Thinking, TwoForks(forks(1), forks(0))),
      Philosopher(3, StateOfBeing.Thinking, TwoForks(forks(2), forks(1))),
      Philosopher(4, StateOfBeing.Thinking, TwoForks(forks(3), forks(2))),
      Philosopher(5, StateOfBeing.Thinking, TwoForks(forks(4), forks(3)))
    )


    val semaphoresF: F[Map[Fork, Semaphore[F]]] =
      ForkAlgebraInterpreter.buildSemaphores[F](forks)
    val timeout: FiniteDuration = 1.seconds

    for
      semaphores <- semaphoresF
      forkAlgebra = ForkAlgebraInterpreter(semaphores)
      philosopherAlgebra = PhilosopherAlgebraInterpreter(forkAlgebra, logger, timeout)
      _ <- logger.info(s"--- Philosophers begin to dine ---")
      _ <- philosophers.parTraverse(p => philosopherAlgebra.live(p))
    yield Applicative[F].unit

  }
