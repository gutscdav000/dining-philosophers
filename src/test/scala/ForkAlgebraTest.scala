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
  //val genTwoForks: Gen[TwoForks] = (genFork, genFork).mapN(TwoForks(_, _))

  // this is here to ensure we never have duplicate identifiers
  def genForks(numForks: Int): Gen[Set[Fork]] =
    Gen.buildableOfN[Set[Int], Int](numForks, Gen.posNum[Int]).map(_.map(Fork.apply))

  val genTwoForks: Gen[TwoForks] =
    genForks(2).map(_.toList match {
      case (head:: tail :: Nil) => TwoForks(head, tail)
      case _ => throw RuntimeException("shouldn't happen")
    })

  def genTwoForksN(numTwoForks: Int): Gen[Set[TwoForks]] =
    genForks(2 * numTwoForks).map(_.toList.sliding(2).map {
      case (head:: tail :: Nil) => TwoForks(head, tail)
      case _ => throw RuntimeException("shouldn't happen")
    }.toSet)


  //TODO:philosopher algebra testing for live method: use TestControl
  //https://typelevel.org/cats-effect/api/3.x/cats/effect/testkit/TestControl.html

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
          .flatMap(_.acquireN(1)) >>
          forkAlgebra.forksAvailable(twoForks).map(forkState =>
            assertEquals(forkState, ForkState.InUse)
          )
      ).unsafeRunSync()
    }
  }

  property("fork is available") {
    forAll(genTwoForks) { twoForks =>

      val stateMap: IO[Map[Fork, Semaphore[IO]]] =
        buildSemaphores[IO](Seq(twoForks.left, twoForks.right))

      val forkAlgebraF: IO[ForkAlgebra[IO]] =
        stateMap.map(ForkAlgebraInterpreter[IO](_))

      forkAlgebraF.flatMap(forkAlgebra =>
        forkAlgebra.forksAvailable(twoForks).map(forkState =>
          assertEquals(forkState, ForkState.Available)
        )
      ).unsafeRunSync()
    }
  }
}

