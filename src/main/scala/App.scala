import java.util.Date

import diode.react.ReactConnector
import diode.{Action, ActionHandler, Circuit}
import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import org.scalajs.dom
import dom.document

case class Pomo(key: Int, i:Int = 0)
case class RootModel(pomodoros: List[Pomo], time: String)

case class Tick() extends Action
case class Tock() extends Action
case class UpdateTime(time: String) extends Action

object PomodoroCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override def initialModel: RootModel = RootModel(List(Pomo(1), Pomo(2)), "18:00:00")

  val pomoHandler = new ActionHandler(zoomTo(_.pomodoros)) {
    override def handle = {
      case Tick() => updated(value.map(pomo => pomo.copy(i = pomo.i + 1)))
      case Tock() => updated(value.map(pomo => pomo.copy(i = pomo.i * 2)))
      case UpdateTime(time) => {
        // todo: Scala match error Firefox and Safari
        val Array(hour, minute) = time.split(":")
        val now = new Date()
        val start = now.getTime
        now.setHours(hour.toInt)
        now.setMinutes(minute.toInt)
        val end = now.getTime

        val length = end - start
        val m25 = 1000 * 60 * 25
        val numberOfPomodoros = Math.floor(length / m25).toInt

        updated(0.until(numberOfPomodoros).map(index => Pomo(index, index)).toList)
      }
      case _ => updated(List[Pomo]())
    }
  }

  val timeHandler = new ActionHandler(zoomTo(_.time)) {
    override def handle = {
      case UpdateTime(time) => {
        println(s"__$time")
        updated(time)
      }
      case _ => updated(value)
    }
  }

  override val actionHandler: HandlerFunction = foldHandlers(timeHandler, pomoHandler)
}


object App {
  def main(args: Array[String]): Unit = {

    val pomodoroDashboard = PomodoroCircuit.connect((r: RootModel) => r)

    pomodoroDashboard((p: ModelProxy[RootModel]) => PomodoroDashboard.apply(p))
      .renderIntoDOM(document.getElementById("pomodoro"))
  }
}

object PomodoroDashboard {
  case class State()
  case class Props(proxy: ModelProxy[RootModel], onTick: (Action => Callback) => Callback, onTock: (Action => Callback) => Callback)

  class Backend($ : BackendScope[Props, State]) {
    def mounted(props: Props) = Callback {}

    def render(p: Props, s: State) = {
      val dispatch: Action => Callback = p.proxy.dispatchCB
      val pomodoros = p.proxy.value.pomodoros
      val time = p.proxy.value.time
      val x = (e: ReactEventFromInput) => {
        println(s"ACTION__${e.target.value}")
        dispatch(UpdateTime(e.target.value))
      }

      val list = pomodoros.map(pomodoro => <.li(^.key := pomodoro.key, <.progress(^.value := 70, ^.max := 100))).toVdomArray
      <.div(
        <.input(^.`type` := "time",^.value := time, ^.onChange ==> x),
        <.ul(list),
        <.button(^.onClick --> p.onTick(dispatch), "Tick"),
        <.button(^.onClick --> p.onTock(dispatch), "Tock")
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