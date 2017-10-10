package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.linalg.{SparseVector, Vector}
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.sql.SparkSession

object JokesContentSimilarity extends FileNames {

  case class UserBestJokes(userId: Int, toPredictCount: Int, jokeId: Int, rating: Int)

  case class RawJokeTfIdf(jokeId: Int, jokeTfIdf: SparseVector)

  def run(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    spark.sqlContext.createDataFrame(
      new CoordinateMatrix(
        spark.sqlContext.read.parquet(jokeTextTfIdfParquet)
          .select("joke_id", "joke_tfidf")
          .as[(Int, Vector)]
          .map{ case (jokeId, tfIdfVector) => RawJokeTfIdf(jokeId, tfIdfVector.toSparse)}
          .flatMap {
            case RawJokeTfIdf(jokeId, jokeTfIdf) =>
              jokeTfIdf
                .indices
                .map(termHash => MatrixEntry(termHash, jokeId, jokeTfIdf(termHash)))
          }.rdd
      ).toIndexedRowMatrix()
        .columnSimilarities()
        .toIndexedRowMatrix()
        .rows
    ).write.mode("overwrite").parquet(jokeContentSimilarityParquet)
  }
}
