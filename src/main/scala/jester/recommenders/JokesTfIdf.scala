package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object JokesTfIdf extends FileNames {

  def run(implicit spark: SparkSession): Unit = {
    import spark.implicits._

    val cleanTextUdf = udf(
      (text: String) => text.replaceAll("""[\p{Punct}]""", "").replaceAll("""[\p{Space}]+""", " ").toLowerCase
    )

    val featurizedData = new HashingTF()
      .setInputCol("jokeWords")
      .setOutputCol("jokeTf")
      .transform(
        new Tokenizer()
          .setInputCol("jokeText")
          .setOutputCol("jokeWords")
          .transform(
            spark.read.option("multiline", value = true).json("data/jokes.json")
              .withColumn("jokeText", cleanTextUdf($"jokeText"))
          )
      )

    new IDF()
      .setInputCol("jokeTf")
      .setOutputCol("jokeTfIdf")
      .fit(featurizedData)
      .transform(featurizedData)
      .select("jokeId", "jokeText", "jokeTfIdf")
      .write.mode("overwrite").parquet(jokeTextTfIdfParquet)
  }
}
