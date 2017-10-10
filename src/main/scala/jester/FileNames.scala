package jester

trait FileNames {

  val trainFileName = "data/jester_train.csv"
  val trainParquet = "data/jester_train"

  val testFileName = "data/jester_test.csv"
  val testParquet = "data/jester_test"

  val validationFileName = "data/jester_validation.csv"
  val validationParquet = "data/jester_validation"

  val jokeText = "data/jester_jokes.txt"
  val jokeTextTfIdfParquet = "data/jester_jokes"
  val jokeContentSimilarityParquet = "data/jester_content_similarity"

  val sparkMatrixFactModelDir = "models/spark/jester_matrix_fact"

  val jokeSimilaritiesDir = "models/spark/jester_joke_similarities"
}
