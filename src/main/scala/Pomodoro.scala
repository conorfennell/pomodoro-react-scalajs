import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import dom.document

object Pomodoro {
  def main(args: Array[String]): Unit = {
    val pomodoros = ScalaComponent.builder[Int]("Pomodoros")
        .render($ => {
          val list = 0.to($.props).map(num => <.li(^.key := num, num)).toVdomArray
          <.ul(list)
        })
        .build


    def onTimeChanged(e: ReactEventFromInput): Callback =
      Callback(println(s"${e.eventType}"))

    val timePicker = ScalaComponent.builder[Unit]("TimePicker")
        .render($ => {
          <.input(
            ^.`type` := "time",
            ^.value := "22:53:05",
            ^.onChange ==> onTimeChanged
          )
        }).build

    val page = ScalaComponent.builder[Unit]("Page")
                  .render($ => {
                    <.div(timePicker(), pomodoros(5))
                  }).build

    page()
      .renderIntoDOM(document.getElementById("pomodoro"))
  }
}