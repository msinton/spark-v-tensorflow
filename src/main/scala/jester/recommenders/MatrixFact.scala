package jester.recommenders

import jester.recommenders.Popularity.{ratingsSchema, spark, trainFileName}
import jester.{FileNames, ImportData}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, desc, percent_rank}
import org.apache.spark.sql.{Encoders, Row, SparkSession}
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating


object MatrixFact extends App with FileNames {

  ImportData()

  val spark = SparkSession.builder
    .master("local")
    .appName("Jester Matrix Fact")
    .getOrCreate()

  import spark.implicits._


  val sc = spark.sparkContext

  case class JokeRatings(userId: Int, jokeId: Int, rating: Double)

  val ratingsSchema = Encoders.product[JokeRatings].schema

  val training = spark.read.option("header", true).schema(ratingsSchema).csv(trainFileName)

  val ratings = training.rdd.map { case Row(userId: Int, jokeId: Int, rating: Double) =>
    Rating(userId, jokeId, rating)
  }

  case class JokeRatingsTest(userId: String, jokeId: String)

  val testRatingsSchema = Encoders.product[JokeRatingsTest].schema

  val testSet = spark.read.option("header", true).schema(testRatingsSchema).csv(testFileName)

  val validationSet = spark.read.option("header", true).schema(ratingsSchema).csv(validationFileName)


  // Build the recommendation model using ALS
  val rank = 10
  val numIterations = 10
  val model = ALS.train(ratings, rank, numIterations, 0.01)

  // Evaluate the model on rating data
  val usersProducts = ratings.map { case Rating(user, product, _) =>
    (user, product)
  }
  val predictions = model.predict(usersProducts).map { case Rating(user, product, rate) =>
    ((user, product), rate)
  }

  val ratesAndPreds = ratings.map { case Rating(user, product, rate) =>
    ((user, product), rate)
  }.join(predictions)

  val MSE = ratesAndPreds.map { case ((_, _), (r1, r2)) =>
    val err = r1 - r2
    err * err
  }.mean()
  println("Mean Squared Error = " + MSE)

  val ratesAndPredsDF = ratesAndPreds.toDF("userId_jokeId", "rating_prediction")
  ratesAndPredsDF.show(50)


//
////  // evaluate - score top 5 %
//  val window = Window.partitionBy("userId").orderBy(desc("prediction"))
//  val withRank = joined.withColumn("rank", percent_rank.over(window))
//
//  //  val fifthPercentile =
//  withRank.filter($"rank" <= 0.05).select(avg($"rating")).show() // 2.602456181968523



  println("------------------------")
  //  joined.show(5)

  spark.close()
}

/*
+-----------+--------------------+
|         _1|                  _2|
+-----------+--------------------+
| [47932,87]|[-7.312,-6.372054...|
| [7797,117]|[6.25,5.581998667...|
| [56960,83]|[-0.188,-0.430898...|
| [45818,81]|[3.562,1.25777638...|
|  [6566,19]|[5.312,5.27254093...|
| [19990,72]|[6.281,2.95327926...|
| [38384,39]|[-0.344,0.2531155...|
|  [8961,83]|[9.688,9.76119423...|
|  [29742,8]|[0.938,0.93636228...|
| [53869,93]|[3.094,1.31406282...|
|[49388,130]|[5.469,0.78011627...|
|[61513,117]|[1.969,1.73407680...|
| [38932,89]|[-1.031,-0.519367...|
|  [3170,68]|[-0.281,-3.132239...|
|[59837,136]|[2.75,-0.78450202...|
|[38102,113]|[0.031,0.84034928...|
|[39740,118]|[5.969,4.14262553...|
| [49336,28]|[3.0,4.5074439997...|
|[37983,108]|[4.125,3.06907395...|
|[30789,105]|[3.5,1.4303540071...|
|[14156,135]|[6.938,5.80052434...|
| [1694,118]|[-9.375,-5.375605...|
|[32282,128]|[0.844,2.76411175...|
| [55939,29]|[7.312,2.55200112...|
|  [56856,8]|[-8.844,-8.788962...|
|[10535,106]|[-5.844,-0.508342...|
|  [27281,8]|[-2.562,-2.495713...|
|[45562,105]|[0.75,1.228338728...|
| [41910,13]|[-5.781,-5.702821...|
|[48620,113]|[-4.594,-1.747414...|
| [58021,65]|[1.094,4.98378808...|
|[38980,110]|[7.625,7.88293661...|
 */