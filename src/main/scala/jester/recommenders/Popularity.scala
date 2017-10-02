package jester.recommenders

import jester.{FileNames, ImportData}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, desc, percent_rank}
import org.apache.spark.sql.{Encoders, SparkSession}


object Popularity extends FileNames {

  ImportData()

  val spark = SparkSession.builder
    .master("local")
    .appName("Jester Matrix Fact")
    .getOrCreate()

  val sc = spark.sparkContext

  case class JokeRatings(userId: String, jokeId: String, rating: Double)
  val ratingsSchema = Encoders.product[JokeRatings].schema

  val training = spark.read.option("header", true).schema(ratingsSchema).csv(trainFileName)


  case class JokeRatingsTest(userId: String, jokeId: String)
  val testRatingsSchema = Encoders.product[JokeRatingsTest].schema

  val testSet = spark.read.option("header", true).schema(testRatingsSchema).csv(testFileName)

  val avgRatings = training.groupBy("jokeId").mean("rating").toDF("jokeId", "prediction")
  val testSetPredictions = testSet.join(avgRatings, "jokeId")

  // evaluate - score top 5 %
  val validationSet = spark.read.option("header", true).schema(ratingsSchema).csv(validationFileName)

  val joinedPredictionToValidation = testSetPredictions.join(validationSet, Seq("userId", "jokeId"))


  val evaluator = new RegressionEvaluator()
    .setMetricName("rmse")
    .setLabelCol("rating")
    .setPredictionCol("prediction")
  val rmse = evaluator.evaluate(joinedPredictionToValidation)


  val window = Window.partitionBy("userId").orderBy(desc("prediction"))
  val withRank = joinedPredictionToValidation.withColumn("rank", percent_rank.over(window))

  import spark.implicits._
//  val fifthPercentile =
  withRank.filter($"rank" <= 0.05).select(avg($"rating")).show() // 2.602456181968523


  println(s"RMSE = $rmse")

  println("------------------------")


  spark.close()
}
