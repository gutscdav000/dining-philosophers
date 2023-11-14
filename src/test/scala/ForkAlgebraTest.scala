import cats.effect._
import cats.effect.implicits._
import cats.effect.std.Semaphore
import cats.implicits._
import models._
import org.scalacheck.cats.implicits._
import munit._
import org.scalacheck.Prop._
import org.scalacheck.Gen
import ForkAlgebraInterpreter._
import cats.effect.unsafe.IORuntime.global

class ForkAlgebraTest extends CatsEffectSuite with ScalaCheckSuite {

  val genFork: Gen[Fork] = Gen.posNum[Int].map(Fork(_))
  val genTwoForks: Gen[TwoForks] = (genFork, genFork).mapN(TwoForks(_, _))

  //TODO: test fork availability
  //TODO: test that aquire & release round trips
  property("fork is not available availability") {
    forAll(genTwoForks) { twoForks =>

      val stateMap: IO[Map[Fork, Semaphore[IO]]] =
        buildSemaphores[IO](Seq(twoForks.left, twoForks.right))

      val forkAlgebraF: IO[ForkAlgebra[IO]] =
        stateMap.map(ForkAlgebraInterpreter[IO](_))

      forkAlgebraF.flatMap(forkAlgebra =>
        forkAlgebra
          .getSemaphore(twoForks.left)
          .flatMap(_.acquireN(1)) >>
          forkAlgebra
          .getSemaphore(twoForks.right)
          .flatTap(s => IO(println("C")) >> IO.pure(s))
          .flatMap(_.acquireN(1)) >>
          IO(println("D")) >>
          forkAlgebra.forksAvailable(twoForks).map(forkState =>
            assertEquals(forkState, ForkState.InUse)
          ) >> IO(println("E"))
      ).unsafeRunSync()
      println("4")
    }
  }
}

