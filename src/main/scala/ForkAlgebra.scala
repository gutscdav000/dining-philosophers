import models._
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std._

trait ForkAlgebra[F[_]]:
  def forksAvailable(forks: TwoForks): F[ForkState]
  def aquireForks(forks: TwoForks): F[Unit]
  def releaseForks(forks: TwoForks): F[Unit]
  def getSemaphore(fork: Fork): F[Semaphore[F]]

object ForkAlgebraInterpreter:
  def buildSemaphores[F[_]: Concurrent](forks: Seq[Fork]): F[Map[Fork, Semaphore[F]]] =
    forks.flatTraverse { fork => Semaphore[F](1).map(sem => List(fork -> sem)) }.map(_.toMap)

  def apply[F[_]: MonadThrow: Parallel](using GenSpawn[F, Throwable])(
      semaphores: Map[Fork, Semaphore[F]]): ForkAlgebra[F] =
    new ForkAlgebra[F]:
      override def forksAvailable(forks: TwoForks): F[ForkState] =
        (
          forkAvailable(forks.left),
          forkAvailable(forks.right)
        ).parMapN {
          case (ForkState.Available, ForkState.Available) => ForkState.Available
          case _ => ForkState.InUse
        }

      // operations that should be parallel
      // try this with both cede and parTupled_
      override def aquireForks(forks: TwoForks): F[Unit] =
        aquireFork(forks.left) *>
          GenSpawn[F].cede *> // cede is here to ensure we cede the thread
          aquireFork(forks.right)
      override def releaseForks(forks: TwoForks): F[Unit] =
        releaseFork(forks.left) *> releaseFork(forks.right)

      private def forkAvailable(fork: Fork): F[ForkState] =
        getSemaphore(fork)
          .flatMap(semaphore => semaphore.tryAcquireN(1L))
          .map(bool => if (bool) ForkState.Available else ForkState.InUse)

      // TODO: should this be package private?
      override def getSemaphore(fork: Fork): F[Semaphore[F]] =
        semaphores.get(fork).liftTo[F](InvalidStateException("fork not found"))

      private def aquireFork(fork: Fork): F[Unit] =
        getSemaphore(fork).flatMap(_.acquireN(1))

      private def releaseFork(fork: Fork): F[Unit] =
        getSemaphore(fork).flatMap(_.releaseN(1))
