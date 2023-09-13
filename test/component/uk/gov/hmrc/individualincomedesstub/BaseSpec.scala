/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package component.uk.gov.hmrc.individualincomedesstub

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import component.uk.gov.hmrc.individualincomedesstub.stubs.ApiPlatformTestUserStub
import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.individualincomedesstub.domain.{Employment, SelfAssessment}
import uk.gov.hmrc.individualincomedesstub.repository.{EmploymentRepository, SelfAssessmentRepository}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import scala.concurrent.Await.result
import scala.concurrent.duration.{Duration, FiniteDuration}

trait BaseSpec
    extends AnyFeatureSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers with GuiceOneServerPerSuite
    with GivenWhenThen {

  implicit override lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "auditing.enabled"                                  -> false,
      "auditing.traceRequests"                            -> false,
      "microservice.services.api-platform-test-user.port" -> ApiPlatformTestUserStub.port,
      "mongodb.uri"                                       -> "mongodb://localhost:27017/individual-income-des-stub-component-tests"
    )
    .build()

  val timeout: FiniteDuration = Duration(5, TimeUnit.SECONDS)
  val serviceUrl = s"http://localhost:$port"
  val employmentRepository: EmploymentRepository = app.injector.instanceOf[EmploymentRepository]
  val selfAssessmentRepository: SelfAssessmentRepository = app.injector.instanceOf[SelfAssessmentRepository]
  val mocks: Seq[ApiPlatformTestUserStub.type] = Seq(ApiPlatformTestUserStub)

  val repositories: Seq[PlayMongoRepository[_ >: Employment with SelfAssessment <: Product]] = Seq(employmentRepository, selfAssessmentRepository)

  override protected def beforeEach(): Unit = {
    repositories.foreach(r => result(r.collection.drop().toFuture(), timeout))
    repositories.foreach(r => result(r.ensureIndexes, timeout))
    mocks.foreach(m => if (!m.server.isRunning) m.server.start())
  }

  override protected def afterEach(): Unit =
    mocks.foreach(_.mock.resetMappings())

  override def afterAll(): Unit = {
    repositories.foreach(r => result(r.collection.drop().toFuture(), timeout))
    mocks.foreach(_.server.stop())
  }
}

case class MockHost(port: Int) {
  val server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port))
  val mock = new WireMock("localhost", port)
  val url = s"http://localhost:$port"
}
