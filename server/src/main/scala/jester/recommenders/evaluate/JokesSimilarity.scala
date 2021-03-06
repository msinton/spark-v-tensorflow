package jester.recommenders.evaluate

import jester.FileNames
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object JokesSimilarity extends FileNames {

  def run(implicit spark: SparkSession) = {

    import spark.implicits._

    val validationSet = spark.sqlContext.read.parquet(validationParquet)
    val training = spark.sqlContext.read.parquet(trainParquet)
    val jokeSimilarities = spark.sqlContext.read.parquet(jokeSimilaritiesUpperDir)

    // now, given jokes that the user rates highly I can find similar jokes.

    val windowByUsers_descRating = Window.partitionBy("userId").orderBy(desc("rating"))

    val trainingWithRank = training.withColumn("rank", rank.over(windowByUsers_descRating))

    // apply the scoring (recommend to each user the number of jokes that represent their top 5% from validation set)

    val numPredictionsToMakeByUserForScoring =
      validationSet.withColumn("percentRank", percent_rank.over(windowByUsers_descRating))
        .filter($"percentRank" <= 0.05)
        .groupBy("userId")
        .count()

    // separate users that have a good rated joke in our training set, from those that do not

    val applicableUsers = trainingWithRank.filter($"rating" >= 5).select($"userId").distinct()
    val applicableValidationSet = numPredictionsToMakeByUserForScoring.join(applicableUsers, Seq("userId"), "inner")
    val remainingValidationSet = numPredictionsToMakeByUserForScoring.join(applicableValidationSet, Seq("userId"), "left_anti")

    val sortedJokeSimilarities = jokeSimilarities.rdd.map { r =>
      val vector = r.getAs[SparseVector](1)
      val zipped = vector.indices zip vector.values
      val (jokeIds, similarityScores) = zipped.sortBy(-_._2).unzip
      (r.getAs[Long](0).toInt, jokeIds, similarityScores)
    }.toDF("jokeId", " jokeIds", "similarityScores")

    // Get the joke that user has rated highest (rank 1)
    val validationWithTrainingHighestRated =
      applicableValidationSet.join(trainingWithRank.filter($"rank" === 1L), "userId")
        .select("userId", "count", "jokeId", "rating")
        .toDF("userId", "toPredictCount", "jokeId", "rating")

    val joinedToSimilar = validationWithTrainingHighestRated.rdd.map { r =>
      val userId = r.getAs[Int](0)
      val numToPredict = r.getAs[Long](1).toInt
      val favouriteJokeId = r.getAs[Int](2)
      (userId, numToPredict, favouriteJokeId)
    }.toDF("userId", "numToPredict", "favouriteJokeId")
      .as("a")
      .join(sortedJokeSimilarities.as("similarities"), $"a.favouriteJokeId" === $"similarities.jokeId")

    // `explode` here takes the Array of jokes and creates a new row for each of these - so that we can
    // evaluate predictions as normal

    val userBestJokePredictionsForScoring = joinedToSimilar.rdd.map{ r =>
      val numToPredict = r.getAs[Int](1)
      (r.getAs[Int](0), r.getAs[Seq[Int]](4).take(numToPredict))
    }
      .toDF("userId", "predictedJokeIds").as("a")
      .withColumn("predictedJokeId", explode($"a.predictedJokeIds")).drop($"a.predictedJokeIds")

    // I need to now get the user's ratings for each of these jokes, and calc the mean

    val score = userBestJokePredictionsForScoring.as("a").join(
      validationSet.as("v"), $"a.userId" === $"v.userId" && $"a.predictedJokeId" === $"v.jokeId")
      .select(avg($"rating"))

    //    score.show // 5.1266044283  - 29221 recommendations

    // now give the other users the most popular jokes as the best recommended ones

    val jokesWithAvgRating = training.groupBy("jokeId").mean("rating").toDF("jokeId", "prediction")

    val topJokes = jokesWithAvgRating.orderBy(desc("prediction")).select($"jokeId").rdd.take(100).map(r => r.getAs[Int](0))

    val userTopJokes = remainingValidationSet.rdd.map { r =>
      (r.getAs[Int](0), (1 to r.getAs[Long](1).toInt).map(x => topJokes(x)))
    }

    val remainingScore = userTopJokes.toDF("userId", "topJokes")
      .withColumn("jokeId", explode($"topJokes"))
      .drop("topJokes")
      .join(validationSet, Seq("userId", "jokeId"))

    remainingScore
      .select(avg($"rating")).show // -0.0832065127782358 - 2426 recommendations

    // overall score = 4.727230011451


    // Q. How to choose from similar jokes that they like depending on how similar and how much they like the joke?
    // I think multiply similarity by rating and sum

  }
}
