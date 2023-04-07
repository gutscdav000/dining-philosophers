package object models:
  enum StateOfBeing:
    case Thinking
    case Hungry
    case Eating

  enum ForkState:
    case Available
    case InUse

    def &&(other: ForkState): ForkState = this match
      case ForkState.Available if other == ForkState.Available => ForkState.Available
      case _ => ForkState.InUse

  case class Fork(identifier: Int, state: ForkState):
    def &&(other: Fork): ForkState =
      this.state && other.state

    def withForkState(newState: ForkState) = copy(state = newState)

  case class TwoForks(left: Fork, right: Fork)

  case class Philosopher(identifier: Int, state: StateOfBeing, forks: TwoForks):
    def withStateOfBeing(s: StateOfBeing) = copy(state = s)

  case class InvalidStateException(msg: String)
      extends RuntimeException(s"InvalidState Reached: $msg")
