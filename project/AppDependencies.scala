import play.core.PlayVersion
import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.19.0"
  val mockitoScalaVersion = "1.17.12"

  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc                %% "bootstrap-backend-play-28" % "7.14.0",
    hmrc                %% "auth-client"               % "6.0.0-play-28",
    hmrc                %% "domain"                    % "8.1.0-play-28",
    hmrc                %% "play-hmrc-api"             % "7.1.0-play-28",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"        % "1.1.0",
    "com.typesafe.play" %% "play-json-joda"            % "2.9.1"
  )

  def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"          % "3.2.15"            % scope,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"             % scope,
    "org.pegdown"            % "pegdown"             % "1.6.0"             % scope,
    "org.mockito"            % "mockito-scala_2.12"  % "1.17.12"           % scope,
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current % scope,
    "org.scalaj"             %% "scalaj-http"        % "2.4.2"             % scope,
    "com.github.tomakehurst" % "wiremock-jre8"       % "2.27.2"            % scope,
    "com.vladsch.flexmark"   % "flexmark-all"        % "0.62.2"            % scope
  )
}
