package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.ALS
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{avg, desc, percent_rank}


object MatrixFact extends FileNames {

  def run(implicit spark: SparkSession): Unit = {

    import spark.implicits._

    val training = spark.sqlContext.read.parquet(trainParquet)
    val validationSet = spark.sqlContext.read.parquet(validationParquet)

    // Build the recommendation model using ALS
    val maxNumIterations = 100

    // to evaluate - score top 5 %
    val window = Window.partitionBy("userId").orderBy(desc("prediction"))

    val evaluator = new RegressionEvaluator()
      .setMetricName("rmse")
      .setLabelCol("rating")
      .setPredictionCol("prediction")

    // Add to lists in order to discover best hyperparameters
    // best found according to validation data: rank 16, lambda 0.08
    val alsBuilder = new ALS()
      .setUserCol("userId")
      .setItemCol("jokeId")
      .setRatingCol("rating")
      .setMaxIter(maxNumIterations)
      .setColdStartStrategy("drop")

    // top recommended joke score (avg the actual predictions from these)

    val results = for {
      rank <- List(16)
      lambda <- List(8e-2)
      model = alsBuilder.setRank(rank).setRegParam(lambda).fit(training)
      ratesAndPreds = model.transform(validationSet)
      rmse = evaluator.evaluate(ratesAndPreds)
      withRank = ratesAndPreds.withColumn("rank", percent_rank.over(window))
      score = withRank.filter($"rank" <= 0.05).select(avg($"rating")).head().get(0)
    } yield (rank, lambda, score, rmse, model)

    val sortedResults = results.sortBy(_._4).reverse
    val bestModel = sortedResults.head._5
//    bestModel.write.overwrite.save(sparkMatrixFactModelDir)


    println(sortedResults) // score = 2.518311803580506, rmse = 4.69693997691974 , rank = 16, lambda = 0.08
  }

  def load: ALS = ALS.load(sparkMatrixFactModelDir)
}

