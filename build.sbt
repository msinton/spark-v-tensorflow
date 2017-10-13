name := "bigdata"

version := "0.1"

val sparkVersion = "2.2.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.11",

  resolvers += Resolver.sonatypeRepo("releases"),

  libraryDependencies ++= Seq(
    "io.argonaut" %% "argonaut" % "6.1",

    "com.github.alexarchambault" %% "argonaut-shapeless_6.1" % "1.1.1",

    "com.lihaoyi" %%% "utest" % "0.5.4" % Test,
    "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
  ),

  testFrameworks += new TestFramework("utest.runner.Framework")
)

lazy val server = project.in(file("server")).
  settings(commonSettings,
    libraryDependencies ++= Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-streaming" % sparkVersion,
      "org.apache.spark" %% "spark-mllib" % sparkVersion,
      "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.0",

      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

      "com.typesafe.akka" %% "akka-http" % "10.0.6",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.0.6" % Test,
    )
  )

lazy val webUI = project.in(file("web-ui")).
  enablePlugins(ScalaJSPlugin).
  settings(commonSettings,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.2",
      "com.lihaoyi" %%% "scalatags" % "0.6.7"
    ),
  ).dependsOn(server)


