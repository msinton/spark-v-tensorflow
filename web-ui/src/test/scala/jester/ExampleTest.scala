package jester

import utest._

object ExampleTest extends TestSuite {

  import JesterUI._

  def tests = TestSuite {
    'ScalaJSExample {
      assert(square(4) == 16)
      assert(square(-5) == 25)
    }
  }
}
