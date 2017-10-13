package jester.service.messages

import argonaut.derive.{JsonSumCodec, JsonSumCodecFor}
import jester.common.{JokeId, JokeRating}

object JesterMessages {

  sealed trait JesterMessage

  case class Joke(id: JokeId, content: String) extends JesterMessage

  case class JokeRated(id: JokeId, rating: JokeRating) extends JesterMessage

  case class StartingJokes(jokes: List[Joke]) extends JesterMessage

  case class Invalid(reason: String) extends JesterMessage

  case class Logout(sessionId: String) extends JesterMessage

  implicit def typeFieldJsonSumCodecFor[S]: JsonSumCodecFor[S] =
    JsonSumCodecFor(JsonSumCodec.typeField)
}