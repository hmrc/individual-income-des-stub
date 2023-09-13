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

package it.uk.gov.hmrc.individualincomedesstub.repository

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.BeforeAndAfterEach
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import uk.gov.hmrc.domain.{EmpRef, Nino, SaUtr}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient}
import uk.gov.hmrc.individualincomedesstub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global

class ApiPlatformTestUserConnectorSpec extends TestSupport with BeforeAndAfterEach {

  private val stubPort = sys.env.getOrElse("WIREMOCK", "11121").toInt
  val stubHost = "localhost"
  val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  private val empRef = EmpRef("123", "AI45678")
  private val testOrganisation = TestOrganisation(
    Some(empRef),
    TestOrganisationDetails("Disney Inc", TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ")))

  private val nino = Nino("AB123456A")
  private val utr = SaUtr("2432552635")

  val http: HttpClient = fakeApplication.injector.instanceOf[HttpClient]
  val config: Configuration = fakeApplication.injector.instanceOf[Configuration]
  val serviceConfig: ServicesConfig =
    fakeApplication.injector.instanceOf[ServicesConfig]

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val underTest: ApiPlatformTestUserConnector =
      new ApiPlatformTestUserConnector(http, serviceConfig) {
        override val serviceUrl = s"http://$stubHost:$stubPort"
      }
  }

  override def beforeEach(): Unit = {
    wireMockServer.start()
    configureFor(stubHost, stubPort)
  }

  override def afterEach(): Unit = {
    wireMockServer.stop()
  }

  "get organisation by empRef" should {

    "retrieve a test organisation by empRef" in new Setup {
      stubFor(
        get(urlEqualTo(s"/organisations/empref/${empRef.encodedValue}"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(
                s"""
               {
                 "empRef": "${empRef.value}",
                 "organisationDetails": {
                   "name": "Disney Inc",
                   "address": {
                     "line1": "Capital Tower",
                     "line2": "Aberdeen",
                     "postcode": "SW1 4DQ"
                   }
                 }
               }
             """
              )))

      private val result = await(underTest.getOrganisationByEmpRef(empRef))

      result shouldBe Some(testOrganisation)
    }

    "return nothing if the organisation cannot be found" in new Setup {
      stubFor(
        get(urlEqualTo(s"/organisations/empref/${empRef.encodedValue}"))
          .willReturn(aResponse().withStatus(NOT_FOUND)))

      await(underTest.getOrganisationByEmpRef(empRef)) shouldBe None
    }

    "propagate errors" in new Setup {
      stubFor(
        get(urlEqualTo(s"/organisations/empref/${empRef.encodedValue}"))
          .willReturn(aResponse().withStatus(BAD_REQUEST)))

      intercept[BadRequestException](await(underTest.getOrganisationByEmpRef(empRef)))
    }
  }

  "getIndividualByNino" should {

    "retrieve the individual" in new Setup {
      stubFor(
        get(urlEqualTo(s"/individuals/nino/$nino"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(s"""{"saUtr": "${utr.value}"}""")))

      private val result = await(underTest.getIndividualByNino(nino))

      result shouldBe TestIndividual(Some(utr))
    }

    "fail with RecordNotFoundException when there is no individual matching the nino" in new Setup {
      stubFor(
        get(urlEqualTo(s"/individuals/nino/$nino"))
          .willReturn(aResponse().withStatus(NOT_FOUND)))

      intercept[RecordNotFoundException] {
        await(underTest.getIndividualByNino(nino))
      }
    }

    "propagate errors" in new Setup {
      stubFor(
        get(urlEqualTo(s"/individuals/nino/$nino"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR)))

      intercept[BadRequestException](await(underTest.getOrganisationByEmpRef(empRef)))
    }

  }
}
