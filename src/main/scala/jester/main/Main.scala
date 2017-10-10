package jester.main

import jester.{ImportData, _}
import org.apache.spark.sql.SparkSession

object Main extends App {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local")
    .appName("Jester Matrix Fact")
    .config("spark.sql.warehouse.dir", "/tmp")
    .getOrCreate()

  spark.sparkContext.setCheckpointDir("/tmp/checkpoints")

  ImportData

  // manually choose what you want to run

//  recommenders.Popularity.run
//  recommenders.MatrixFact.run
//  recommenders.JokesTfIdf.run
//  recommenders.JokesContentSimilarity.run
//  recommenders.evaluate.JokesSimilarity.run
//  recommenders.JokesSimilarity.createSimilaritiesDF
//  recommenders.JokesSimilarity.recommendJokes(List((13, 6.1), (12, 4.1), (37, 7.5)))


  spark.close()

}
