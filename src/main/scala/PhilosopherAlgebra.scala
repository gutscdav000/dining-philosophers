import models._
import cats._
import cats.implicits._
import cats.effect._
import concurrent.duration.DurationInt
import org.typelevel.log4cats.Logger
import scala.concurrent.duration.FiniteDuration

trait PhilosopherAlgebra[F[_]]:
  def doesPonder(philosopher: Philosopher): F[Philosopher]
  def canEat(philosopher: Philosopher): F[Boolean]
  def doesEat(philosopher: Philosopher): F[Philosopher]
  def getsFull(philosopher: Philosopher): F[Philosopher]
  def live(philosopher: Philosopher): F[Unit]

object PhilosopherAlgebraInterpreter:
  def apply[F[_]: Temporal](
      forkAlg: ForkAlgebra[F],
      logger: Logger[F],
      timeout: FiniteDuration) =
    new PhilosopherAlgebra[F]:
        //fork algebra that wraps a fork algebra, to sleep. outter algebra performs sleeping.
        // also do this for PhilosopherAlgebra... mocking clocks is a pain in the ass

      override def doesPonder(philosopher: Philosopher): F[Philosopher] =
        Temporal[F].sleep(timeout) >>
          logger.info(s"Philosopher ${philosopher.identifier} is Hungry") >>
          philosopher.withStateOfBeing(StateOfBeing.Hungry).pure[F]

      override def canEat(philosopher: Philosopher): F[Boolean] =
        forkAlg
          .forksAvailable(philosopher.forks)
          .flatMap {
            case ForkState.InUse => false.pure[F]
            case ForkState.Available => true.pure[F]
          }
          .flatTap(b => logger.info(s"Philosopher ${philosopher.identifier} can eat: $b"))

      override def doesEat(philosopher: Philosopher): F[Philosopher] =
        logger.info(s"Philosopher ${philosopher.identifier} aquiring forks?") >>
          forkAlg.aquireForks(philosopher.forks) >>
          logger.info(s"Philosopher ${philosopher.identifier} is Eating") >>
          Temporal[F].sleep(timeout) >>
          philosopher.withStateOfBeing(StateOfBeing.Eating).pure[F]

      override def getsFull(philosopher: Philosopher): F[Philosopher] =
        logger.info(s"Philosopher ${philosopher.identifier} releases forks?") >>
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
