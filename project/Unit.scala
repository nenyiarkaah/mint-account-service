import sbt.Keys._
import sbt._

object Unit {
  final val UnitTest = Configuration.of("UnitTest", "ut") extend (Test)
  final val unitSettings =
    inConfig(UnitTest)(unitConfig)
  lazy val unitConfig =
    Defaults.configSettings ++ Defaults.testTasks ++ Seq(
      fork in UnitTest := false,
      parallelExecution in UnitTest := false,
      scalaSource in UnitTest := baseDirectory.value / "src" / "test" / "scala"
    )
}