/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualincomedesstub.controller

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualincomedesstub.domain.{Address, Employer}
import uk.gov.hmrc.individualincomedesstub.service.EmployerService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future.failed

class EmployerControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val employer = Employer(
    payeReference = EmpRef.fromIdentifiers("904/UZ00057"),
    name = "Company ABF546",
    address = Address("Westfield center", Some("1 Stoke Ave"), "SW1PPT"))
  val mockEmployerService = mock[EmployerService]

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .overrides(bind[EmployerService].toInstance(mockEmployerService))
    .build()

  trait Setup {
    implicit val system = ActorSystem("Sys")
    implicit val materializer = ActorMaterializer()
  }

  "POST /employer" should {

    "return a 201 (Created) with the generated employer" in new Setup {
      given(mockEmployerService.createEmployer()).willReturn(employer)

      val result = invoke(POST, "/employer")

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe Json.parse(
      """
        |{
        |   "payeReference": "904/UZ00057",
        |   "name": "Company ABF546",
        |   "address": {
        |     "line1": "Westfield center",
        |     "line2": "1 Stoke Ave",
        |     "postcode": "SW1PPT"
        |   }
        |}
      """.stripMargin
      )
    }

    "return a 500 (Internal Server Error) when creating the employer fails" in new Setup {
      given(mockEmployerService.createEmployer()).willReturn(failed(new RuntimeException("test error")))

      val result = invoke(POST, "/employer")

      status(result) shouldBe INTERNAL_SERVER_ERROR
      jsonBodyOf(result) shouldBe Json.parse(
        """
          |{
          |   "code": "INTERNAL_SERVER_ERROR",
          |   "message": "Internal server error"
          |}
        """.stripMargin
      )
    }
  }

  private def invoke(httpVerb: String, uriPath: String): Result =
    await(route(fakeApplication, FakeRequest(httpVerb, uriPath)).get)
}
