import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {
  val hmrcBootstrapVersion = "8.4.0"
  val playVersion = "play-30"
  val hmrc = "uk.gov.hmrc"

  val compile: Seq[ModuleID] = Seq(
    ws,
    hmrc           %% s"bootstrap-backend-$playVersion" % hmrcBootstrapVersion,
    hmrc           %% s"domain-$playVersion"            % "9.0.0",
    s"$hmrc.mongo" %% s"hmrc-mongo-$playVersion"        % "1.7.0",
  )

  def test(scope: Configuration = Test): Seq[ModuleID] = Seq(
    hmrc          %% s"bootstrap-test-$playVersion" % hmrcBootstrapVersion % scope,
    "org.mockito" %% "mockito-scala"                % "1.17.30"            % scope,
    "org.scalaj"  %% "scalaj-http"                  % "2.4.2"              % scope
  )
}
