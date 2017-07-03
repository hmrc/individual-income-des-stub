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

import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.controller.EmploymentController
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, HmrcPayment}
import uk.gov.hmrc.individualincomedesstub.service.EmploymentService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class EmploymentControllerSpec extends UnitSpec with MockitoSugar with ScalaFutures with WithFakeApplication {

  implicit lazy val materializer = fakeApplication.materializer

  trait Setup {
    val fakeRequest = FakeRequest()
    val mockEmploymentService = mock[EmploymentService]
    val underTest = new EmploymentController(mockEmploymentService)
  }

  "Create employment" should {

    val employerPayeReference = EmpRef("123", "DI45678")
    val nino = Nino("NA000799C")

    "Successfully create an employment record and return the correct response" in new Setup {
      val request = aCreateEmploymentRequest()
      val employment = anEmployment(employerPayeReference, nino)

      when(mockEmploymentService.create(employerPayeReference, nino, request)).thenReturn(employment)

      val result = await(underTest.create(employerPayeReference, nino)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe 201
      bodyOf(result) shouldBe Json.toJson(employment).toString
    }

    "Successfully create an employment with no startDate" in new Setup {
      val request = aCreateEmploymentRequest(startDate = None)
      val employment = anEmployment(employerPayeReference, nino, startDate = None)

      when(mockEmploymentService.create(employerPayeReference, nino, request)).thenReturn(employment)

      val result = await(underTest.create(employerPayeReference, nino)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe 201
      bodyOf(result) shouldBe Json.toJson(employment).toString
    }

    "Successfully create an employment with no endDate" in new Setup {
      val request = aCreateEmploymentRequest(endDate = None)
      val employment = anEmployment(employerPayeReference, nino, endDate = None)

      when(mockEmploymentService.create(employerPayeReference, nino, request)).thenReturn(employment)

      val result = await(underTest.create(employerPayeReference, nino)(fakeRequest.withBody(Json.toJson(request))))

      status(result) shouldBe 201
      bodyOf(result) shouldBe Json.toJson(employment).toString
    }

    "Fail with correct error message for missing payments field" in new Setup {
      val result = await(underTest.create(employerPayeReference, nino)(fakeRequest.withBody(Json.parse("""{"startDate": "2016-01-01", "endDate": "2017-03-01"}"""))))
      status(result) shouldBe 400
      bodyOf(result) shouldBe """{"code":"INVALID_REQUEST","message":"payments is required"}"""
    }

    "Fail with correct error message for invalid payment" in new Setup {
      val result = await(underTest.create(employerPayeReference, nino)(fakeRequest.withBody(
        Json.parse("""{"startDate": "2016-01-01","endDate": "2017-03-01","payments":[{"taxablePayment": 1000.55,"nonTaxablePayment": 0}]}"""))))
      status(result) shouldBe 400
      bodyOf(result) shouldBe """{"code":"INVALID_REQUEST","message":"payments(0)/paymentDate is required"}"""
    }
  }

  private def aCreateEmploymentRequest(startDate: Option[String] = Some("2016-01-01"),
                                       endDate: Option[String] = Some("2017-03-01"),
                                       payments: Seq[HmrcPayment] = Seq(HmrcPayment("2016-01-28", 1000.55, 0), HmrcPayment("2016-02-28", 950.55, 0))) = {

    CreateEmploymentRequest(startDate, endDate, payments)
  }

  private def anEmployment(employerPayeReference: EmpRef,
                           nino: Nino,
                           startDate: Option[String] = Some("2016-01-01"),
                           endDate: Option[String] = Some("2017-03-01"),
                           payments: Seq[HmrcPayment] = Seq(HmrcPayment("2016-01-28", 1000.55, 0), HmrcPayment("2016-02-28", 950.55, 0))) = {

    Employment(employerPayeReference, nino, startDate, endDate, payments)
  }
}

