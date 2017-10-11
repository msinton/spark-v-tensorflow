name := "bigdata"

version := "0.1"

val sparkVersion = "2.2.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.11",

  libraryDependencies ++= Seq(
    "com.lihaoyi" %%% "utest" % "0.5.4" % Test,
    "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
  ),

  testFrameworks += new TestFramework("utest.runner.Framework")
)


lazy val root = (project in file("."))
  .aggregate(server, webUI)

lazy val server = project.in(file("server")).
  settings(commonSettings,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-streaming" % sparkVersion,
      "org.apache.spark" %% "spark-mllib" % sparkVersion,
      "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.0"
    )
  )

lazy val webUI = project.in(file("web-ui")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",

    // Add the sources of the server project
    unmanagedSourceDirectories in Compile +=
      (scalaSource in (server, Compile)).value / "frp"
  ) //.dependsOn(server)


