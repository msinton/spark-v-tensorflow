package jester.recommenders

import jester.FileNames
import org.apache.spark.mllib.linalg.SparseVector
import org.apache.spark.mllib.linalg.distributed.{IndexedRow, IndexedRowMatrix}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Encoders, Row, SparkSession}

import scala.collection.mutable.WrappedArray

object JokesSimilarity extends FileNames {

  def run(implicit spark: SparkSession) = {

    val training = spark.sqlContext.read.parquet(trainParquet)
    val testSet = spark.sqlContext.read.parquet(testParquet)
    val validationSet = spark.sqlContext.read.parquet(validationParquet)

    val numJokes = training.select(max("jokeId")).head().getAs[Int](0) + 1

    val ratingsByUserId: RDD[(Int, (Array[Int], Array[Double]))] = training.rdd.map {
      case Row(userId: Int, jokeId: Int, rating: Double) => (userId, Map(jokeId -> rating))
    }.reduceByKey(_ ++ _).map { case (u, m) => (u, m.toArray.sortBy(_._1).unzip[Int, Double]) }

    val g = ratingsByUserId.map {
      case (userId, (jokes, ratings)) => IndexedRow(userId, new SparseVector(numJokes, jokes, ratings))
    }

    val jokeSimilarities = {
      val s = new IndexedRowMatrix(g).columnSimilarities()
      spark.sqlContext.createDataFrame(s.toIndexedRowMatrix.rows).toDF("jokeId", "similarities")
    }

    // I've got joke similarities, so now given jokes that the user rates highly I can find similar jokes.

//    jokeSimilarities.show(2)
//
    import spark.implicits._
    testSet.as("a").join(validationSet.as("b"), $"a.userId" === $"b.userId" && $"b.jokeId".isin($"a.predictedJokeIds".))

    //    val forJoke = jokeSimilarities.filter($"jokeId" === 19)
//    forJoke.show()
//    val closestMatch = forJoke.head.getAs[SparseVector](1).argmax
//    println("closestMatch", closestMatch)
//    println(forJoke.head.get(1))
//
//    // evaluate

    val window = Window.partitionBy("userId").orderBy(desc("rating"))

    val trainingWithRank = training.withColumn("rank", rank.over(window))
    val applicableUsers = trainingWithRank.filter($"rating" >= 5).select($"userId").distinct()

    val validationWithPercentRank = validationSet.withColumn("percentRank", percent_rank.over(window))

    val numPredictionsToMakeByUserForScoring = validationWithPercentRank.filter($"percentRank" <= 0.05).groupBy("userId").count()

    // separate users that have a good rated joke in our training, from those that do not

    val applicableValidationSet = numPredictionsToMakeByUserForScoring.join(applicableUsers, Seq("userId"), "left")
    val remainingValidationSet = numPredictionsToMakeByUserForScoring.join(applicableValidationSet, Seq("userId"), "left_anti")



//    val jokesSelected = applicableValidationSet.map { case Row(userId: Int, n: Long) =>
//      val userFavouriteJoke = trainingWithRank.filter($"userId" === userId && $"rank" === 1).head().get(1)
////      userFavouriteJoke
//      val similarities = jokeSimilarities.filter($"jokeId" === userFavouriteJoke).head.getAs[SparseVector](1)
//      val zipped = similarities.indices zip similarities.values
//      zipped.sortBy(-_._2).take(n.toInt).unzip._1
//       // choose the most popular joke if we haven't seen this user before
//    }
//
//    jokesSelected.show(50)



    trainingWithRank.show(30)
    val v = applicableValidationSet.join(trainingWithRank.filter($"rank" === 1L), "userId")
//          .map()
//      .toDF("userId", "count", "jokeId", "rating", "rank")

    // for a user take their best joke and then multiply the similarities with its ratings.
//    jokeSimilarities.


    // Q. How to choose from similar jokes that they like depending on how similar and how much they like the joke?
    // I think multiply similarity by rating and sum


    case class UserBestJokes(userId: Int, toPredictCount: Int, jokeId: Int, rating: Int)
    val vSchema = Encoders.product[UserBestJokes].schema
    spark.sqlContext.createDataFrame(v.select("userId", "count", "jokeId", "rating").toDF("userId", "toPredictCount", "jokeId", "rating").rdd, vSchema)
  }

}
