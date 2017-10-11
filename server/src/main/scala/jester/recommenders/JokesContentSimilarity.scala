package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.linalg.{SparseVector, Vector}
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.sql.SparkSession

object JokesContentSimilarity extends FileNames {

  case class UserBestJokes(userId: Int, toPredictCount: Int, jokeId: Int, rating: Int)

  case class RawJokeTfIdf(jokeId: BigInt, jokeTfIdf: SparseVector)

  def run(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    val jokeSimilarityMatrix = spark.sqlContext.createDataFrame(
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
        .toIndexedRowMatrix()
        .rows
    )

    jokeSimilarityMatrix.write.mode("overwrite").parquet(jokeContentSimilarityParquet)

    val vector = jokeSimilarityMatrix.filter($"index" === 70).select($"vector").head().getAs[org.apache.spark.mllib.linalg.SparseVector](0)

    (vector.indices zip vector.values) sortBy(-_._2) take 5 foreach println
  }
}
