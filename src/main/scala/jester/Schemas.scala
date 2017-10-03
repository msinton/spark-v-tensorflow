package jester

import org.apache.spark.sql.Encoders

object Schemas {

  case class JokeRatings(userId: Int, jokeId: Int, rating: Double)

  val ratingsSchema = Encoders.product[JokeRatings].schema

  case class JokeRatingsTest(userId: String, jokeId: String)

  val testRatingsSchema = Encoders.product[JokeRatingsTest].schema

}
