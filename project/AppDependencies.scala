import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "9.11.0"
  val playVersion = "play-30"
  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc           %% s"bootstrap-backend-$playVersion" % hmrcBootstrapVersion,
    hmrc           %% s"domain-$playVersion"            % "11.0.0",
    s"$hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % "2.6.0",
  )

  def test(scope: Configuration = Test): Seq[ModuleID] = Seq(
    hmrc          %% s"bootstrap-test-$playVersion" % hmrcBootstrapVersion        % scope,
    "org.scalatestplus"            %% "scalacheck-1-17"          % "3.2.18.0"     % scope,
    "com.codacy"  %% "scalaj-http"                  % "2.5.0"                     % scope
  )
}
