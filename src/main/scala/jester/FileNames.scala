package jester

trait FileNames {

  val trainFileName = "data/jester_train.csv"
  val trainParquet = "data/jester_train"

  val testFileName = "data/jester_test.csv"
  val testParquet = "data/jester_test"

  val validationFileName = "data/jester_validation.csv"
  val validationParquet = "data/jester_validation"

  val sparkMatrixFactModelDir = "models/spark/jester_matrix_fact"

  val jokeSimilaritiesDir = "models/spark/jester_joke_similarities"
}
