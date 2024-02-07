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
  // Disable default sbt Test options (might change with new versions of bootstrap)
  .settings(Test / testOptions -= Tests
    .Argument("-o", "-u", "target/test-reports", "-h", "target/test-reports/html-report"))
  // Suppress successful events in Scalatest in standard output (-o)
  // Options described here: https://www.scalatest.org/user_guide/using_scalatest_with_sbt
  .settings(
    Test / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/test-reports",
      "-h",
      "target/test-reports/html-report"))
  .settings(onLoadMessage := "")
  .settings(Compile / unmanagedResourceDirectories += baseDirectory.value / "resources")
  .configs(IntegrationTest)
  .settings(integrationTestSettings() *)
  .settings(
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "test")).value,
    IntegrationTest / testOptions := Seq(Tests.Filter(_ startsWith "it")),
    // Disable default sbt Test options (might change with new versions of bootstrap)
    IntegrationTest / testOptions -= Tests
      .Argument("-o", "-u", "target/int-test-reports", "-h", "target/int-test-reports/html-report"),
    IntegrationTest / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/int-test-reports",
      "-h",
      "target/int-test-reports/html-report")
  )
  .configs(ComponentTest)
  .settings(inConfig(ComponentTest)(Defaults.testSettings) *)
  .settings(
    ComponentTest / testOptions := Seq(Tests.Filter(_ startsWith "component")),
    ComponentTest / unmanagedSourceDirectories := (ComponentTest / baseDirectory)(base => Seq(base / "test")).value,
    // Disable default sbt Test options (might change with new versions of bootstrap)
    ComponentTest / testOptions -= Tests
      .Argument("-o", "-u", "target/component-test-reports", "-h", "target/component-test-reports/html-report"),
    ComponentTest / testOptions += Tests.Argument(
      TestFrameworks.ScalaTest,
      "-oNCHPQR",
      "-u",
      "target/component-test-reports",
      "-h",
      "target/component-test-reports/html-report")
  )
  .settings(PlayKeys.playDefaultPort := 9631)
  .settings(majorVersion := 0)
  .settings(scalafmtOnCompile := true)
  .settings(CodeCoverageSettings.settings *)

libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
