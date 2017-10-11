package jester

import frp.{ExampleJoke, _}
import org.scalajs.dom
import org.scalajs.dom.{document, html}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@js.native
trait EventName extends js.Object {
  type EventType <: dom.Event
}

object EventName {
  def apply[T <: dom.Event](name: String): EventName { type EventType = T } =
    name.asInstanceOf[EventName { type EventType = T }]

  val onmousedown = apply[dom.MouseEvent]("onmousedown")
}

@js.native
trait ElementExt extends js.Object {
  def addEventListener(name: EventName)(
    f: js.Function1[name.EventType, _]): Unit
}

/**
  * Created by matt on 10/10/17.
  */
object JesterUI  {

  def main(args: Array[String]): Unit = {

    val jokeText = elementById[html.Paragraph]("joketext")

    Signal {
      jokeText.textContent = ExampleJoke.joke()
    }

    val submit = elementById[html.Button]("submit")

//    submit.addEventListener("onclick", addClickedMessage)
  }

  def addClickedMessage(event: dom.Event): Unit = {
    elementById[html.Span]("test").textContent = "You clicked the button!"
  }

  def ratingInputs(): Unit = {
    val ids = (1 to 10).toList map(_.toString)
    val inputs = ids.map(id => elementById[html.Input](id))
    println(inputs)

    val signals = inputs map inputValueSignal

  }

  // Helpers

  def elementById[A <: js.Any](id: String): A =
    document.getElementById(id).asInstanceOf[A]

  def elementValueSignal(element: html.Element, getValue: () => String): Signal[String] = {
    var prevVal = getValue()
    val value = new Var(prevVal)
    val onChange = { (event: dom.Event) =>
      // Reconstruct manually the optimization at the root of the graph
      val newVal = getValue()
      if (newVal != prevVal) {
        prevVal = newVal
        value() = newVal
      }
    }
    element.addEventListener("change", onChange)
    element.addEventListener("keypress", onChange)
    element.addEventListener("keyup", onChange)
    value
  }

  def inputValueSignal(input: html.Input): Signal[String] =
    elementValueSignal(input, () => input.value)


  private lazy val ClearCssClassRegExp =
    new js.RegExp(raw"""(?:^|\s)has-error(?!\S)""", "g")

  def doubleValueOfInput(input: html.Input): Signal[Double] = {
    val text = inputValueSignal(input)
    val parent = input.parentElement
    Signal {
      import js.JSStringOps._
      parent.className = parent.className.jsReplace(ClearCssClassRegExp, "")
      try {
        text().toDouble
      } catch {
        case e: NumberFormatException =>
          parent.className += " has-error"
          Double.NaN
      }
    }
  }
  // <--- Helpers

  /** Computes the square of an integer.
    *  This demonstrates unit testing.
    */
  def square(x: Int): Int = x*x
}
