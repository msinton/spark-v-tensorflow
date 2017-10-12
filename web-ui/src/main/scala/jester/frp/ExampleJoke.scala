package jester.frp

import jester.common.{JokeId, JokeRating}

import scala.util.Random

case class Joke(id: JokeId, content: String)

case class JokeRated(id: JokeId, rating: JokeRating)

object ExampleJoke {

  val numJokes = 100
  private val jokes = ((0 to numJokes) map (i => s"joke $i")).toVector

  private val random = new Random()

  private val jokeId: Var[Int] = Var(random.nextInt(jokes.size))

  private val jokeRatings: Var[Map[JokeId, JokeRating]] = Var(Map())

  private def hasRatedAllJokes = jokeRatings().keySet.size >= numJokes

  def joke: Signal[Joke] = Signal(Joke(jokeId(), jokes(jokeId())))

  def nextJoke(): Unit = {
    val nextId = random.nextInt(jokes.size)
    if (!hasRatedAllJokes && jokeRatings().keySet.contains(nextId)) {
      nextJoke()
    }
    else
      jokeId() = nextId
  }

  def registerRating(jokeId: JokeId, rating: JokeRating): Var[JokeRated] = {

    val newRating = Var(JokeRated(jokeId, rating))

    // Update the submitted ratings whenever newRating updated
    Signal {
      val seen = jokeRatings()
      jokeRatings() = seen.updated(newRating().id, newRating().rating)
    }

    newRating
  }

}
