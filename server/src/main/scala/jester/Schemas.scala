package jester

import argonaut.derive.{JsonSumCodec, JsonSumCodecFor}
import jester.common.{JokeId, Rating, UserId}
import org.apache.spark.sql.Encoders

object Schemas {

  sealed trait JokeObjects

  case class UserJokeRating(userId: UserId, jokeId: JokeId, rating: Rating) extends JokeObjects

  val userJokeRatingSchema = Encoders.product[UserJokeRating].schema

  case class UserJoke(userId: UserId, jokeId: JokeId) extends JokeObjects

  val UserJokeSchema = Encoders.product[UserJoke].schema

  case class JokeRating(jokeId: JokeId, rating: Rating) extends JokeObjects

  case class JokeRatings(ratings: List[JokeRating]) extends JokeObjects

  implicit def typeFieldJsonSumCodecFor[S]: JsonSumCodecFor[S] =
    JsonSumCodecFor(JsonSumCodec.typeField)
}
