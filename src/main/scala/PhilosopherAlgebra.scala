import models._
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std.Console
import scala.concurrent.duration.FiniteDuration
import concurrent.duration.DurationInt


trait PhilosopherAlgebra[F[_]]:
  def doesPonder(philosopher: Philosopher): F[Philosopher]
  def canEat(philosopher: Philosopher): F[Boolean]
  def doesEat(philosopher: Philosopher): F[Philosopher]
  def getsFull(philosopher: Philosopher): F[Philosopher]
  def live(philosopher: Philosopher): F[Unit]

object PhilosopherAlgebraInterpreter:
  def apply[F[_]: Temporal: Console](forkAlg: ForkAlgebra[F], timeout: FiniteDuration) =
    new PhilosopherAlgebra[F]:
      override def doesPonder(philosopher: Philosopher): F[Philosopher] =
        Temporal[F].sleep(timeout) >>
          Console[F].println(s"Philosopher ${philosopher.identifier} is Hungry") >>
          philosopher.withStateOfBeing(StateOfBeing.Hungry).pure[F]

      override def canEat(philosopher: Philosopher): F[Boolean] =
        forkAlg.forksAvailable(philosopher.forks).flatMap {
          case ForkState.InUse => false.pure[F]
          case ForkState.Available => true.pure[F]
        }.flatTap(b => Console[F].println(s"Philosopher ${philosopher.identifier} can eat: $b"))

      override def doesEat(philosopher: Philosopher): F[Philosopher] =
        Console[F].println(s"Philosopher ${philosopher.identifier} aquiring forks?") >>
        forkAlg.aquireForks(philosopher.forks) >>
          Console[F].println(s"Philosopher ${philosopher.identifier} is Eating") >>
          Temporal[F].sleep(timeout) >>
          philosopher.withStateOfBeing(StateOfBeing.Eating).pure[F]

      override def getsFull(philosopher: Philosopher): F[Philosopher] =
        Console[F].println(s"Philosopher ${philosopher.identifier} releases forks?") >>
        forkAlg.releaseForks(philosopher.forks) >>
          philosopher.withStateOfBeing(StateOfBeing.Thinking).pure[F]

      override def live(philosopher: Philosopher): F[Unit] =
        Monad[F].iterateForeverM(philosopher)(loop)

      private def loop(p: Philosopher): F[Philosopher] =
        for
          p1 <- doesPonder(p)
          _ = Monad[F].untilM_(canEat(p1))
          p3 <- doesEat(p1)
          p4 <- getsFull(p3)
        yield p4
