import diode.react.ReactConnector
import diode.{Action, ActionHandler, Circuit}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import dom.document

case class Pomo(key: Int , i:Int = 0)
case class RootModel(pomodoros: List[Pomo])

case class Tick() extends Action
case class Tock() extends Action


object PomodoroCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override def initialModel: RootModel = RootModel(List(Pomo(1), Pomo(2)))

  val counterHandler = new ActionHandler(zoomTo(_.pomodoros)) {
    override def handle = {
      case Tick() => updated(value.map(pomo => pomo.copy(i = pomo.i + 1)))
      case Tock() => updated(value.map(pomo => pomo.copy(i = pomo.i * 2)))
      case _ => updated(List[Pomo]())
    }
  }

  override val actionHandler: HandlerFunction = composeHandlers(counterHandler)
}


object Pomodoro {
  def main(args: Array[String]): Unit = {

    val pom = PomodoroCircuit
      .connect((r: RootModel) => r)

    document.getElementById("pomodoro")

    pom((p: ModelProxy[RootModel]) => Pomos.apply(p))
      .renderIntoDOM(document.getElementById("pomodoro"))
  }
}

object Pomos {
  case class State()
  case class Props(proxy: ModelProxy[RootModel], onTick: (Action => Callback) => Callback, onTock: (Action => Callback) => Callback)

  class Backend($ : BackendScope[Props, State]) {
    def mounted(props: Props) = Callback {}

    def render(p: Props, s: State) = {
      val dispatch: Action => Callback = p.proxy.dispatchCB
      val pomodoros = p.proxy.value.pomodoros

      val list = pomodoros.map(pomodoro => <.li(^.key := pomodoro.key, pomodoro.i)).toVdomArray
      <.div(
        <.ul(list,
          <.button(^.value := "Tick", ^.onClick --> p.onTick(dispatch), "Tick"),
          <.button(^.value := "Tock", ^.onClick --> p.onTock(dispatch), "Tock")
        )
      )
    }
  }

  private val component = ScalaComponent
    .builder[Props]("PomoList")
    .initialStateFromProps(p => State())
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build

  def apply(proxy: ModelProxy[RootModel]) =
    component(Props(proxy, (dispatch: Action => Callback) => dispatch(Tick()), (dispatch: Action => Callback) => dispatch(Tock())))
}