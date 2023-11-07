import models._
import cats._
import cats.implicits._
import cats.effect.std._

trait ForkAlgebra[F[_]]:
  def forksAvailable(forks: TwoForks): F[ForkState]
  def aquireForks(forks: TwoForks): F[Unit]
  def releaseForks(forks: TwoForks): F[Unit]

object ForkAlgebraInterpreter:
  def apply[F[_]: MonadThrow: Parallel](semaphores: Map[Int, Semaphore[F]]) =
    new ForkAlgebra[F]:
      override def forksAvailable(forks: TwoForks): F[ForkState] =
        (
          forkAvailable(forks.left),
          forkAvailable(forks.right)
        ).parMapN((a1: ForkState, a2: ForkState) => a1 && a2)

      // never gives up the left because *> is a monad *> use  parTupled_
      // add cede in places to force parallelization. between aquiring forks and other
      // operations that should be parallel
     // one algebra with cede, one algebra with parTupled_
      override def aquireForks(forks: TwoForks): F[Unit] =
        aquireFork(forks.left) *> GenSpawn[F].cede  *> aquireFork(forks.right)
      override def releaseForks(forks: TwoForks): F[Unit] =
        releaseFork(forks.left) *> releaseFork(forks.right)

      private def forkAvailable(fork: Fork): F[ForkState] =
        for {
          forkSemaphore <- semaphores
            .get(fork.identifier)
            .liftTo[F](InvalidStateException("fork not found"))
          numAvail <- forkSemaphore.available
        } yield if (numAvail > 0) ForkState.Available else ForkState.InUse

      // use tryAquire to immediately see if fork is available
      private def aquireFork(fork: Fork): F[Unit] =
        semaphores
          .get(fork.identifier)
          .liftTo[F](InvalidStateException("fork not found"))
          .flatMap(_.acquireN(1))

      private def releaseFork(fork: Fork): F[Unit] =
        semaphores
          .get(fork.identifier)
          .liftTo[F](InvalidStateException("fork not found"))
          .flatMap(_.releaseN(1))
