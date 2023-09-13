import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.19.0"
  val mockitoScalaVersion = "1.17.12"
  val playVersion = "play-28"

  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc                %% s"bootstrap-backend-$playVersion" % hmrcBootstrapVersion,
    hmrc                %% "domain"                          % "8.1.0-play-28",
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % "1.1.0",
    "com.typesafe.play" %% "play-json-joda"                  % "2.9.1"
  )

  def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
    hmrc                     %% s"bootstrap-test-$playVersion" % hmrcBootstrapVersion % scope,
    "org.scalatest"          %% "scalatest"                    % "3.2.15"             % scope,
    "org.mockito"            %% "mockito-scala"                % mockitoScalaVersion  % scope,
    "org.scalaj"             %% "scalaj-http"                  % "2.4.2"              % scope,
    "com.github.tomakehurst" % "wiremock-jre8"                 % "2.27.2"             % scope,
    "com.vladsch.flexmark"   % "flexmark-all"                  % "0.62.2"             % scope
  )
}
