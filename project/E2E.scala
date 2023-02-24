import sbt._
import sbt.Keys._
object E2E {
  final val E2ETest = Configuration.of("EndToEndTest", "e2e") extend (Test)
  final val e2eSettings =
    inConfig(E2ETest)(e2eConfig)
  lazy val e2eConfig =
    Defaults.configSettings ++ Defaults.testTasks ++ Seq(
      E2ETest / fork := false,
      E2ETest / parallelExecution := false,
      E2ETest / scalaSource := baseDirectory.value / "src" / "e2e" / "scala"
    )
}