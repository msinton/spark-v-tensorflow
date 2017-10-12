package jester.recommenders

import jester.FileNames
import org.apache.spark.sql.SparkSession

object ContentSimilarityAnalysis extends FileNames {

  def run(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    spark.sqlContext.read.parquet(jokeContentSimilarityParquet)
      .sort($"value".desc)
      .show()
  }
}
