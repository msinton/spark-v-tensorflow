package jester

import jester.Schemas._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType

object ImportData extends FileNames with DownloadUrls {

  def downloadAndSaveOnce(spark: SparkSession, url: String, filePath: String, schema: StructType, outFile: String): Unit = {
    if (!scala.tools.nsc.io.Path(filePath).exists) {
      val csv = scala.io.Source.fromURL(url)
      scala.tools.nsc.io.Path(filePath).createFile().writeAll(csv.mkString)

      // save as Parquet
      val df = spark.read.option("header", true).schema(schema).csv(filePath)
      df.write.mode("overwrite").parquet(outFile)
    }
  }

  def apply(implicit spark: SparkSession): Unit = {
    downloadAndSaveOnce(spark, trainUrl, trainFileName, ratingsSchema, trainParquet)
    downloadAndSaveOnce(spark, testUrl, testFileName, testRatingsSchema, testParquet)
    downloadAndSaveOnce(spark, validationUrl, validationFileName, ratingsSchema, validationParquet)
  }
}
