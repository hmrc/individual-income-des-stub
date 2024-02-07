import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

lazy val ComponentTest = config("component") extend Test

lazy val microservice = Project("individual-income-des-stub", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    scalaVersion := "2.13.11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test(),
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=views/.*:s",
    routesImport ++= Seq("uk.gov.hmrc.individualincomedesstub._"),
    Test / testOptions := Seq(Tests.Filter(_ startsWith "unit"))
  )
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(integrationTestSettings() *)
  .settings(
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test")).value,
    IntegrationTest / testOptions := Seq(Tests.Filter(_ startsWith "it"))
  )
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings) *)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(_ startsWith "component")),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value
  )
  .settings(PlayKeys.playDefaultPort := 9631)
  .settings(majorVersion := 0)
  .settings(scalafmtOnCompile := true)
  .settings(CodeCoverageSettings.settings *)

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
