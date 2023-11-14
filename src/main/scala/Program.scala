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

    val (forks, philosophers) = buildPhilosophers(5)

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

  // this function isn't type safe and can have an out of bounds exception
  def buildPhilosophers(numPhilosophers: Int): (Seq[Fork], Seq[Philosopher]) = {
    val forks: Vector[Fork] = (1 to numPhilosophers).map(Fork(_)).toVector
    val philosophers = (1 to numPhilosophers - 1).map(i =>
      Philosopher(i, StateOfBeing.Thinking, TwoForks(forks(i - 1), forks(i)))) :+ Philosopher(
      numPhilosophers,
      StateOfBeing.Thinking,
      TwoForks(forks(0), forks(numPhilosophers - 1)))
    (forks, philosophers)
  }
