package object models:
  enum StateOfBeing:
    case Thinking
    case Hungry
    case Eating

  enum ForkState:
    case Available
    case InUse

  case class Fork(identifier: Int)

  case class TwoForks(left: Fork, right: Fork)

  case class Philosopher(identifier: Int, state: StateOfBeing, forks: TwoForks):
    def withStateOfBeing(s: StateOfBeing) = copy(state = s)

  case class InvalidStateException(msg: String)
      extends RuntimeException(s"InvalidState Reached: $msg")
