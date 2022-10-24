import E2E._
import Unit._

lazy val akkaHttpVersion = "10.1.6"
lazy val akkaVersion = "2.5.19"
lazy val slickVersion = "3.2.3"
lazy val upickleVersion = "0.6.7"
lazy val http4sVersion = "0.19.0-M4"
lazy val circeVersion = "0.11.1"
lazy val doobieVersion = "0.6.0"

lazy val root = (project in file("."))
  .configs(E2ETest, UnitTest)
  .settings(
    e2eSettings,
    inThisBuild(List(organization := "org.mint", scalaVersion := "2.12.8")),
    name := "mint-account-service",
    version := "1.0.0",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core"      % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion,
      "org.typelevel" %% "cats-core" % "1.4.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.postgresql" % "postgresql" % "9.4-1203-jdbc4",
      "com.github.pureconfig" %% "pureconfig" % "0.10.1",
      "eu.timepit" %% "refined-pureconfig" % "0.9.3",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "de.heikoseeberger" %% "akka-http-upickle" % "1.23.0",
      "com.lihaoyi" %% "upickle" % upickleVersion,
      "com.lihaoyi" %% "ujson" % upickleVersion,
      "com.softwaremill.macwire" %% "macros" % "2.3.1",
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "com.dimafeng" %% "testcontainers-scala" % "0.36.0" % Test,
      "com.dimafeng" %% "testcontainers-scala-mssqlserver" % "0.36.0" % Test,
      "org.testcontainers" % "mssqlserver" % "1.17.2" % Test,
      "com.storm-enroute" %% "scalameter-core" % "0.10.1" % Test,
      "com.microsoft.sqlserver" % "mssql-jdbc" % "8.2.2.jre8",
      "org.mockito" % "mockito-core" % "3.3.3"
    ),
    dockerBaseImage := "openjdk:8-jre-alpine",
    Docker / packageName := "nenyi/mint-account",
    Test / fork := true,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
  )
  .enablePlugins(JavaAppPackaging, AshScriptPlugin)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
)

// Create a test Scala style task to run with tests
lazy val testScalaStyle = taskKey[Unit]("testScalaStyle")
testScalaStyle := scalastyle.in(Test).toTask("").value
(test in Test) := ((test in Test) dependsOn testScalaStyle).value
(scalastyleConfig in Test) := baseDirectory.value / "project" / "scalastyle-config.xml"

lazy val compileScalaStyle = taskKey[Unit]("compileScalaStyle")
compileScalaStyle := scalastyle.in(Compile).toTask("").value
(test in Test) := ((test in Test) dependsOn compileScalaStyle).value
(scalastyleConfig in Compile) := baseDirectory.value / "project" / "scalastyle-config.xml"
