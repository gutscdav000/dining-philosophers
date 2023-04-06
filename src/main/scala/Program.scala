import models._
import cats._
import cats.implicits._
import cats.effect._
import cats.effect.std._
import scala.concurrent.duration.FiniteDuration
import concurrent.duration.DurationInt
import scala.collection.immutable._


object Program:
  def program[F[_]: Temporal](): F[Unit] = {
    val forks = Vector(
      Fork(1, ForkState.Available),
      Fork(2, ForkState.Available),
      Fork(3, ForkState.Available),
      Fork(4, ForkState.Available),
      Fork(5, ForkState.Available),
    )
    val philosophers = Vector(
      Philosopher(1, StateOfBeing.Thinking, TwoForks(forks(0), forks(4))),
      Philosopher(2, StateOfBeing.Thinking, TwoForks(forks(1), forks(0))),
      Philosopher(3, StateOfBeing.Thinking, TwoForks(forks(2), forks(1))),
      Philosopher(4, StateOfBeing.Thinking, TwoForks(forks(3), forks(2))),
      Philosopher(5, StateOfBeing.Thinking, TwoForks(forks(4), forks(3))),
    )

    // match each philosopher with a semaphore
    val semaphoresF: F[Map[Int, Semaphore[F]]] =
      philosophers
        .traverse(p => (p.identifier, Semaphore(1)).sequence)
        .map(_.toMap)
    val timeout: FiniteDuration = 5.seconds

    for
      semaphores <- semaphoresF
      //ref <- Ref[F].of(State) /// ???
      forkAlgebra =  ForkAlgebraInterpreter(semaphores)
      philosopherAlgebra = PhilosopherAlgebraInterpreter(forkAlgebra, timeout)
    yield Applicative[F].unit
    
    Applicative[F].unit
  }
