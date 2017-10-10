package jester.recommenders

import jester.FileNames
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.SparkSession

import scala.io.Source

object JokesTfIdf extends FileNames {

  def run(implicit spark: SparkSession): Unit = {
    val sentenceData = spark.createDataFrame(
      Source.fromFile(jokeText)
        .getLines()
        .map(line => line.replaceAll("""[\p{Punct}]""", ""))
        .zipWithIndex
        .toSeq
    ).toDF("joke_text", "joke_id")

    val tokenizer = new Tokenizer().setInputCol("joke_text").setOutputCol("joke_words")
    val wordsData = tokenizer.transform(sentenceData)

    val hashingTF = new HashingTF()
      .setInputCol("joke_words").setOutputCol("joke_tf")

    val featurizedData = hashingTF.transform(wordsData)
    featurizedData.show(false)
    // alternatively, CountVectorizer can also be used to get term frequency vectors

    val idf = new IDF().setInputCol("joke_tf").setOutputCol("joke_tfidf")
    val idfModel = idf.fit(featurizedData)

    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.select("joke_id", "joke_text", "joke_tfidf").write.mode("overwrite").parquet(jokeTextTfIdfParquet)
    rescaledData.show(false)
  }
}
