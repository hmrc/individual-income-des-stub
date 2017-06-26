import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object MicroServiceBuild extends Build with MicroService {

  import play.sbt.routes.RoutesKeys._

  val appName = "individual-income-des-stub"

  override lazy val appDependencies: Seq[ModuleID] = compile ++ test()
  override lazy val playSettings : Seq[Setting[_]] = Seq(routesImport ++= Seq("uk.gov.hmrc.domain._", "uk.gov.hmrc.individualincomedesstub.domain._", "uk.gov.hmrc.individualincomedesstub.Binders._"))

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "play-reactivemongo" % "5.2.0",
    "uk.gov.hmrc" %% "play-auditing" % "2.9.0",
    "uk.gov.hmrc" %% "microservice-bootstrap" % "5.15.0",
    "uk.gov.hmrc" %% "play-authorisation" % "4.3.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "uk.gov.hmrc" %% "play-url-binders" % "2.1.0",
    "uk.gov.hmrc" %% "play-config" % "4.3.0",
    "uk.gov.hmrc" %% "play-hmrc-api" % "1.4.0",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "uk.gov.hmrc" %% "domain" % "4.1.0",
    "org.scalacheck" %% "scalacheck" % "1.12.6"
  )

  def test(scope: String = "test,it") = Seq(
    "uk.gov.hmrc" %% "reactivemongo-test" % "2.0.0" % scope,
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % scope,
    "org.scalatest" %% "scalatest" % "3.0.1" % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" %  scope,
    "org.pegdown" % "pegdown" % "1.6.0" % scope,
    "org.mockito" % "mockito-core" % "1.10.19" % scope,
    "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
    "org.scalaj" %% "scalaj-http" % "1.1.6" % scope,
    "com.github.tomakehurst" % "wiremock" % "2.6.0" % scope
  )

}
