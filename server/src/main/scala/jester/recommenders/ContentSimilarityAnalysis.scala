package jester.recommenders

import jester.FileNames
import org.apache.spark.sql.SparkSession

object ContentSimilarityAnalysis extends FileNames {

  case class UserBestJokes(userId: Int, toPredictCount: Int, jokeId: Int, rating: Int)

  case class RowMaxSimilarity(jokeId: Long, columnJokeId: Int, similarity: Double)

  def run(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    val jokeSimilarities = spark.sqlContext.read.parquet(jokeContentSimilarityParquet)

    jokeSimilarities.sort($"value".desc).show()
  }
}
