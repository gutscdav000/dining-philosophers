import cats._
import cats.effect._

object Main extends IOApp.Simple:
  override def run: IO[Unit] =
    println("Hello world!")
    println(msg)
    IO.unit

def msg = "I was compiled by Scala 3. :)"
