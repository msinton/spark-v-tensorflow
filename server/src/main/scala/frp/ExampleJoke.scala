package frp

import scala.util.Random

object ExampleJoke {

  val jokes = ((1 to 100) map (i => s"joke $i")).toVector

  val random = new Random()

  def joke: Signal[String] = {
    Signal(jokes(random.nextInt(jokes.size)))
  }
}
