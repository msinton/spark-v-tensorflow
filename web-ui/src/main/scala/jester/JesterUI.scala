package jester

import jester.common.{JokeId, JokeRating}
import jester.frp.{ExampleJoke, _}
import org.scalajs.dom
import org.scalajs.dom.raw.MouseEvent
import org.scalajs.dom.{document, html}

import scalatags.JsDom.all._
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

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
  * Annotation turns the object into an in-scope javascript object called `jester`
  */
@JSExportTopLevel("jester")
object JesterUI  {

  private var jokeRated: Option[Var[JokeRated]] = None

  private def updateJokeRated(jokeId: JokeId, rating: JokeRating): Unit = {
    jokeRated match {
      case None =>
        val s = ExampleJoke.registerRating(jokeId, rating)
        jokeRated = Option(s)

      case Some(r) =>
        r() = JokeRated(jokeId, rating)
    }
  }

  @JSExport
  def main(target: html.Div): Unit = {

    val jokeText = elementById[html.Paragraph]("joketext")

    Signal {
      jokeText.textContent = ExampleJoke.joke().content
    }

    showOnScreen(target)
  }

  def showOnScreen(target: html.Div): Unit = {
    target.appendChild(div(
      ratingInputs()
    ).render).render
  }

  def createJokeRated(rating: JokeRating): JokeRated = {
    val jokeId = ExampleJoke.joke().id
    JokeRated(jokeId, rating)
  }

  def createRatingRadio(jokeRating: JokeRating): html.Input = {
    val ratingInput = input(`type`:="radio", jokeRating.toString).render

    ratingInput.onclick = (_: MouseEvent) => {

      val jokeId = ExampleJoke.joke().id
      updateJokeRated(jokeId, jokeRating)

      ratingInput.checked = false
      ExampleJoke.nextJoke() // move this to 'submit' button if required!
    }
    ratingInput
  }

  def ratingInputs(): Seq[html.Span] = {
    (1 to 10) map (x => span(
      createRatingRadio(x),
      s"$x "
    ).render)
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
