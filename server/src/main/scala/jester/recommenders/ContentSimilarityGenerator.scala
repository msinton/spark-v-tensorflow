package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.linalg.{SparseVector, Vector}
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.sql.SparkSession

object ContentSimilarityGenerator extends FileNames {

  case class UserBestJokes(userId: Int, toPredictCount: Int, jokeId: Int, rating: Int)

  case class RawJokeTfIdf(jokeId: BigInt, jokeTfIdf: SparseVector)

  case class RowMaxSimilarity(jokeId: Long, columnJokeId: Int, similarity: Double)

  def run(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    spark.sqlContext.createDataFrame(
      new CoordinateMatrix(
        spark.sqlContext.read.parquet(jokeTextTfIdfParquet)
          .select("jokeId", "jokeTfIdf")
          .as[(BigInt, Vector)]
          .map{ case (jokeId, tfIdfVector) => RawJokeTfIdf(jokeId, tfIdfVector.toSparse)}
          .flatMap {
            case RawJokeTfIdf(jokeId, jokeTfIdf) =>
              jokeTfIdf
                .indices
                .map(termHash => MatrixEntry(termHash, jokeId.longValue(), jokeTfIdf(termHash)))
          }.rdd
      ).toIndexedRowMatrix()
        .columnSimilarities()
        .entries
    ).write.mode("overwrite").parquet(jokeContentSimilarityParquet)
  }
}
