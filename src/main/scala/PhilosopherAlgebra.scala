import models._
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std._
import scala.concurrent.duration.FiniteDuration

trait PhilosopherAlgebra[F[_]]:
  def doesPonder(philosopher: Philosopher): F[Unit]
  def canEat(philosopher: Philosopher): F[Boolean]
  def doesEat(philosopher: Philosopher): F[Unit]
  def getsFull(philosopher: Philosopher): F[Unit]


object PhilosopherAlgebraInterpreter:
  def apply[F[_]: Temporal](forkAlg: ForkAlgebra[F], timeout: FiniteDuration) = new PhilosopherAlgebra[F]:
    def doesPonder(philosopher: Philosopher): F[Unit] = Temporal[F].sleep(timeout)
    def canEat(philosopher: Philosopher): F[Boolean] =
      forkAlg.forksAvailable(philosopher.forks).flatMap {
        case ForkState.InUse => false.pure[F]
        case ForkState.Available =>
          philosopher.toStateOfBeing(StateOfBeing.Hungry).pure[F] *> true.pure[F]
      }
    // ?? these copies, don't do anything because they're not returning 
    def doesEat(philosopher: Philosopher): F[Unit] =
      forkAlg.aquireForks(philosopher.forks) >>
      philosopher.toStateOfBeing(StateOfBeing.Eating).pure[F] >>
      Temporal[F].sleep(timeout)
    def getsFull(philosopher: Philosopher): F[Unit] =
      forkAlg.releaseForks(philosopher.forks) >>
      philosopher.toStateOfBeing(StateOfBeing.Thinking).pure[F] >>
      Applicative[F].unit

