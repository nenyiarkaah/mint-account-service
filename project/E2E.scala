import sbt._
import sbt.Keys._
object E2E {
  final val E2ETest = Configuration.of("EndToEndTest", "e2e") extend (Test)
  final val e2eSettings =
    inConfig(E2ETest)(e2eConfig)
  lazy val e2eConfig =
    Defaults.configSettings ++ Defaults.testTasks ++ Seq(
      fork in E2ETest := false,
      parallelExecution in E2ETest := false,
      scalaSource in E2ETest := baseDirectory.value / "src" / "e2e" / "scala"
    )
}