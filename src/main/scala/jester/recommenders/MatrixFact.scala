package jester.recommenders

import jester.{FileNames, ImportData}
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, desc, percent_rank}
import org.apache.spark.sql.{Row, SparkSession}


class MatrixFact(spark: SparkSession) extends FileNames {

  import spark.implicits._

  val sc = spark.sparkContext

  val training = spark.sqlContext.read.parquet(trainParquet)
  val testSet = spark.sqlContext.read.parquet(testParquet)
  val validationSet = spark.sqlContext.read.parquet(validationParquet)

  val ratings = training.rdd.map { case Row(userId: Int, jokeId: Int, rating: Double) =>
    Rating(userId, jokeId, rating)
  }

  // Build the recommendation model using ALS
  val numIterations = 30

  // Evaluate the model on rating data
  val usersProducts = validationSet.select($"userId", $"jokeId").as[(Int, Int)]

  // to evaluate - score top 5 %
  val window = Window.partitionBy("userId").orderBy(desc("prediction"))

  val evaluator = new RegressionEvaluator()
    .setMetricName("rmse")
    .setLabelCol("rating")
    .setPredictionCol("prediction")

  // Add to lists in order to discover best hyperparameters
  // best found according to validation data: rank 16, lambda 0.08
  val results = for {
    rank <- List(16)
    lambda <- List(8e-2)
    model = ALS.train(ratings, rank, numIterations, lambda)
    matrixPredictions = model.predict(usersProducts.rdd).toDF("userId", "jokeId", "prediction")
    ratesAndPreds = validationSet.join(matrixPredictions,  Seq("userId", "jokeId"))
    withRank = ratesAndPreds.withColumn("rank", percent_rank.over(window))
    rmse = evaluator.evaluate(ratesAndPreds)
    score = withRank.filter($"rank" <= 0.05).select(avg($"rating")).head().get(0)
  } yield (rank, lambda, score, rmse, model)

  val sortedResults = results.sortBy(_._4).reverse
  val bestModel = sortedResults.head._5
  bestModel.save(sc, sparkMatrixFactModelDir)

  println(sortedResults) // score = 2.518311803580506, rmse = 4.69693997691974 , rank = 16, lambda = 0.08

}

