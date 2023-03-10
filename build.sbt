import play.core.PlayVersion
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings


val appName = "individual-income-des-stub"
val hmrc = "uk.gov.hmrc"

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val playSettings : Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.domain._", "uk.gov.hmrc.individualincomedesstub.domain._", "uk.gov.hmrc.individualincomedesstub.Binders._"))
lazy val plugins : Seq[Plugins] = Seq.empty

def intTestFilter(name: String): Boolean = name startsWith "it"
def unitFilter(name: String): Boolean = name startsWith "unit"
def componentFilter(name: String): Boolean = name startsWith "component"
lazy val ComponentTest = config("component") extend Test

val akkaVersion     = "2.5.23"

val akkaHttpVersion = "10.0.15"


dependencyOverrides += "com.typesafe.akka" %% "akka-stream"    % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-actor"     % akkaVersion

dependencyOverrides += "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion

val compile = Seq(
  ws,
  hmrc %% "bootstrap-backend-play-26" % "2.25.0",
  hmrc %% "auth-client" % "3.2.0-play-26",
  hmrc %% "domain" % "5.10.0-play-26",
  hmrc %% "play-hmrc-api" % "4.1.0-play-26",
  hmrc %% "simple-reactivemongo" % "7.30.0-play-26",
  "com.typesafe.play" %% "play-json-joda" % "2.9.1"
)

def test(scope: String = "test,it") = Seq(
  hmrc %% "reactivemongo-test" % "4.21.0-play-26" % scope,
  hmrc %% "service-integration-test" % "0.12.0-play-26" % scope,
  "org.scalatest" %% "scalatest" % "3.0.8" % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" %  scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "org.mockito" % "mockito-core" % "3.5.10" % scope,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalaj" %% "scalaj-http" % "2.4.2" % scope,
  "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % scope
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin) ++ plugins: _*)
  .settings(playSettings : _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(scalaVersion := "2.12.11")
  .settings(defaultSettings(): _*)
  .settings(
    libraryDependencies ++= appDependencies,
    Test / testOptions := Seq(Tests.Filter(unitFilter)),
    retrieveManaged := true
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test")).value,
    IntegrationTest / unmanagedResourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test/resources")).value,
    IntegrationTest / testOptions := Seq(Tests.Filter(intTestFilter)),
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false)
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings): _*)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(componentFilter)),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value,
    ComponentTest / testGrouping := oneForkedJvmPerTest((ComponentTest / definedTests).value),
    ComponentTest / parallelExecution := false
  )
  .settings(PlayKeys.playDefaultPort := 9631)
  .settings(majorVersion := 0)

def oneForkedJvmPerTest(tests: Seq[TestDefinition]) = {
  tests.map { test =>
    new Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
}
