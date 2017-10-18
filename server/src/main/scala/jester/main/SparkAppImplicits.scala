package jester.main

import org.apache.spark.sql.SparkSession

object SparkAppImplicits {

  implicit val spark: SparkSession = SparkSession.builder
    .master("local")
    .appName("Jester Recommender")
    .config("spark.sql.warehouse.dir", "/tmp")
    .getOrCreate()

  spark.sparkContext.setCheckpointDir("/tmp/checkpoints")
}
