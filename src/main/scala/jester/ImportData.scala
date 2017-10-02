package jester


object ImportData extends FileNames{

  def apply(): Unit = {
    //  Import files (TODO tidy away)
    if (!scala.tools.nsc.io.Path(trainFileName).exists) {
      val url = "https://query.data.world/s/xEzuH2qsyXPCRheqbIYuVifPlwn9Bh"
      val csv = scala.io.Source.fromURL(url)
      scala.tools.nsc.io.Path(trainFileName).createFile().writeAll(csv.mkString)
    }

    if (!scala.tools.nsc.io.Path(testFileName).exists) {
      val testUrl = "https://query.data.world/s/HWy0QkAkgCO7txGSEcPXIlXARQ9UHU"
      val csv = scala.io.Source.fromURL(testUrl)
      scala.tools.nsc.io.Path(testFileName).createFile().writeAll(csv.mkString)
    }

    if (!scala.tools.nsc.io.Path(validationFileName).exists) {
      val validationUrl = "https://query.data.world/s/m2lqv4IUeCgUhNvMvYiOihKBVVVIml"
      val csv = scala.io.Source.fromURL(validationUrl)
      scala.tools.nsc.io.Path(validationFileName).createFile().writeAll(csv.mkString)
    }
  }
}
