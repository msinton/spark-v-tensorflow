package jester

import jester.frp.{Recommender, Signal}
import org.scalajs.dom.{document, html}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("recommendation")
object RecommendedJoke  {

  @JSExport
  def main(): Unit = {
    Recommender.makeRequest()

    Signal(Recommender.joke().foreach(setJokeContent))
    Signal(Recommender.joke().foreach(_ => removeLoader()))
  }

  def setJokeContent(content: String): Unit = elementById[html.Div]("recommended-joke-content").textContent = content

  def removeLoader(): Unit = {
    val dimmer = elementById[html.Div]("recommended-joke-dimmer")
    dimmer.parentElement.removeChild(dimmer)
  }

  def elementById[A <: js.Any](id: String): A = document.getElementById(id).asInstanceOf[A]
}
