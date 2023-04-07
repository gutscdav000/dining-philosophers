import cats._
import cats.effect._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    Slf4jLogger.create[IO].flatMap(Program.program[IO]) >> IO.unit
