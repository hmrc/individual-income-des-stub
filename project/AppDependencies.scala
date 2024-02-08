import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "7.23.0"
  val playVersion = "play-28"
  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc                %% s"bootstrap-backend-$playVersion" % hmrcBootstrapVersion,
    hmrc                %% "domain"                          % "8.1.0-play-28",
    s"$hmrc.mongo"      %% s"hmrc-mongo-$playVersion"        % "1.1.0",
    "com.typesafe.play" %% "play-json-joda"                  % "2.9.4"
  )

  def test(scope: Configuration = Test): Seq[ModuleID] = Seq(
    hmrc          %% s"bootstrap-test-$playVersion" % hmrcBootstrapVersion % scope,
    "org.mockito" %% "mockito-scala"                % "1.17.12"            % scope,
    "org.scalaj"  %% "scalaj-http"                  % "2.4.2"              % scope
  )
}
