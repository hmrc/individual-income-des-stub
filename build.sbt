import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "individual-income-des-stub"

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    scalaVersion := "2.13.11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=views/.*:s",
    routesImport ++= Seq("uk.gov.hmrc.individualincomedesstub._")
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings() *)
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings) *)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(componentFilter)),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value,
    ComponentTest / testGrouping := oneForkedJvmPerTest((ComponentTest / definedTests).value),
    ComponentTest / parallelExecution := false
  )
  .settings(PlayKeys.playDefaultPort := 9631)
  .settings(majorVersion := 0)
  .settings(scalafmtOnCompile := true)
  .settings(CodeCoverageSettings.settings *)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
