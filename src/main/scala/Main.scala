import cats._
import cats.effect._

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    Program.program[IO]
