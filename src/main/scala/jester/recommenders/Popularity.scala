package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, desc, percent_rank}


object Popularity extends FileNames {

  def run(implicit spark: SparkSession) = {
    val training = spark.sqlContext.read.parquet(trainParquet)
    val testSet = spark.sqlContext.read.parquet(testParquet)
    val validationSet = spark.sqlContext.read.parquet(validationParquet)

    val avgRatings = training.groupBy("jokeId").mean("rating").toDF("jokeId", "prediction")
    val testSetPredictions = testSet.join(avgRatings, "jokeId")

    val joinedPredictionToValidation = testSetPredictions.join(validationSet, Seq("userId", "jokeId"))

    val evaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")
    val rmse = evaluator.evaluate(joinedPredictionToValidation)

    // evaluate - score top 5 %
    val window = Window.partitionBy("userId").orderBy(desc("prediction"))
    val withRank = joinedPredictionToValidation.withColumn("rank", percent_rank.over(window))

    import spark.implicits._
    withRank.filter($"rank" <= 0.05).select(avg($"rating")).show() // 2.602456181968523

    println(s"RMSE = $rmse")
  }

}
