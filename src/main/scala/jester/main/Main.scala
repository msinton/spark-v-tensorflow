package jester.main

import jester.{ImportData, _}
import org.apache.spark.sql.SparkSession

object Main extends App {

  val spark = SparkSession.builder
    .master("local")
    .appName("Jester Matrix Fact")
    .config("spark.sql.warehouse.dir", "/tmp")
    .getOrCreate()

  spark.sparkContext.setCheckpointDir("/tmp/checkpoints")

  ImportData(spark)

  // manually choose what you want to run

//  new recommenders.Popularity(spark)
  new recommenders.MatrixFact(spark)

  spark.close()

}
