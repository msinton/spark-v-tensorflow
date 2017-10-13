package jester.frp

import org.scalajs.dom.ext.Ajax
import scala.concurrent.ExecutionContext.Implicits.global

import scala.scalajs.js.JSON

object Recommender {

  val joke: Var[Option[String]] = Var(None)

  def makeRequest(): Unit = {
//    joke() = None  This can signal fetching
    Ajax.get("https://icanhazdadjoke.com/", headers = Map("Accept" -> "application/json")).onSuccess {
      case xhr =>
        joke() = Option(JSON.parse(xhr.responseText).joke.toString)
    }
  }
}
