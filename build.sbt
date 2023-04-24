import E2E._
import Unit._
import scala.sys.process.Process

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
    dockerBuildxSettings,
    inThisBuild(List(organization := "org.mint", scalaVersion := "2.12.8")),
    name := "mint-account",
    ThisBuild / envFileName := ".env",
    version := "2.0.1",
    scalacOptions ++= Seq("-Ypartial-unification"),
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core"      % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion,
      "org.typelevel" %% "cats-core" % "2.0.0",
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
    dockerBaseImage := "eclipse-temurin:8u345-b01-jre-jammy",
    Docker / packageName := "mint-account",
    dockerRepository := sys.env.get("REGISTRY"),
    dockerUpdateLatest := true,
    dockerBuildOptions := Seq("--force-rm", "-t", "[dockerAlias]", "--platform=linux/arm,linux/amd64"),
    Test / fork := true,
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
  )
  .enablePlugins(JavaAppPackaging, AshScriptPlugin)
  .enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, buildInfoBuildNumber, scalaVersion, sbtVersion),
    buildInfoPackage := "org.mint.info",
    buildInfoOptions += BuildInfoOption.ToJson,
  )

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
testScalaStyle := (Test / scalastyle).toTask("").value
(Test / test) := ((Test / test) dependsOn testScalaStyle).value
(Test / scalastyleConfig) := baseDirectory.value / "project" / "scalastyle-config.xml"

lazy val compileScalaStyle = taskKey[Unit]("compileScalaStyle")
compileScalaStyle := (Compile / scalastyle).toTask("").value
(Test / test) := ((Test / test) dependsOn compileScalaStyle).value
(Compile / scalastyleConfig) := baseDirectory.value / "project" / "scalastyle-config.xml"

lazy val ensureDockerBuildx = taskKey[Unit]("Ensure that docker buildx configuration exists")
lazy val dockerBuildWithBuildx = taskKey[Unit]("Build docker images using buildx")
lazy val dockerBuildxSettings = Seq(
  ensureDockerBuildx := {
    if (Process("docker buildx inspect multi-arch-builder").! == 1) {
      Process("docker buildx create --use --name multi-arch-builder", baseDirectory.value).!
    }
  },
  dockerBuildWithBuildx := {
    streams.value.log("Building and pushing image with Buildx")
    dockerAliases.value.foreach(
      alias => Process("docker buildx build --platform=linux/arm,linux/arm64,linux/amd64 --push -t " +
        alias + " .", baseDirectory.value / "target" / "docker"/ "stage").!
    )
  },
Docker / publish:= Def.sequential(
    Docker / publishLocal,
    ensureDockerBuildx,
    dockerBuildWithBuildx
  ).value
)