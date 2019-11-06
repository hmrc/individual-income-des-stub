/*
 * Copyright 2019 HM Revenue & Customs
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

import akka.stream.Materializer
import org.mockito.BDDMockito.given
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.controller.SelfAssessmentController
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain.{DuplicateSelfAssessmentException, _}
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future.failed

class SelfAssessmentControllerSpec extends UnitSpec with MockitoSugar with ScalaFutures with WithFakeApplication {

  implicit lazy val materializer: Materializer = fakeApplication.materializer

  val utr = SaUtr("2432552635")
  val saReturn = SelfAssessmentTaxReturnData(
    taxYear = "2014-15",
    submissionDate = "2015-06-01",
    employmentsIncome = Some(123),
    selfEmploymentProfit= Some(456),
    totalIncome = Some(10456),
    trustsIncome = Some(500.25),
    foreignIncome = Some(200.35),
    partnershipsProfit = Some(23.56),
    ukInterestsIncome = Some(12.53),
    foreignDividendsIncome = Some(41.46),
    ukDividendsIncome = Some(16.74),
    ukPropertiesProfit = Some(55.24),
    gainsOnLifePolicies = Some(24.63),
    sharesOptionsIncome = Some(42.12),
    pensionsAndStateBenefitsIncome = Some(27.26),
    otherIncome = Some(134.56),
    businessDescription = None,
    address = None
  )
  val request = SelfAssessmentCreateRequest("2014-01-01", Seq(saReturn))
  val selfAssessment = SelfAssessment(utr, request)

  trait Setup {
    val fakeRequest = FakeRequest()
    val selfAssessmentService = mock[SelfAssessmentService]
    val underTest = new SelfAssessmentController(selfAssessmentService)
  }

  "create self assessment" should {

    "return a 201 (Created) when self assessment data is created successfully" in new Setup {
      given(selfAssessmentService.create(utr, request)).willReturn(selfAssessment)

      Logger.info(Json.fromJson[SelfAssessmentCreateRequest](Json.toJson(request)).toString)

      val result = await(underTest.create(utr)(fakeRequest.withBody(toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe toJson(selfAssessment)
    }

    "return a 400 (BadRequest) when the registration date is invalid" in new Setup {
      val result = await(underTest.create(utr)(fakeRequest.withBody(requestWithField("registrationDate", "11-11-1111"))))

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("code" -> "INVALID_REQUEST", "message" -> "registrationDate: invalid date format")
    }

    "return a 400 (BadRequest) when the taxYear is invalid" in new Setup {
      val result = await(underTest.create(utr)(fakeRequest.withBody(requestWithTaxReturnField("taxYear", "201516"))))

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("code" -> "INVALID_REQUEST", "message" -> "taxYear: invalid tax year format")
    }

    "return a 400 (BadRequest) when the submissionDate is invalid" in new Setup {
      val result = await(underTest.create(utr)(fakeRequest.withBody(requestWithTaxReturnField("submissionDate", "invalid"))))

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("code" -> "INVALID_REQUEST", "message" -> "submissionDate: invalid date format")
    }

    "return a 429 (Conflict) when a self-assessment already exists for the utr" in new Setup {
      given(selfAssessmentService.create(utr, request)).willReturn(failed(new DuplicateSelfAssessmentException()))

      val result = await(underTest.create(utr)(fakeRequest.withBody(toJson(request))))

      status(result) shouldBe CONFLICT
      jsonBodyOf(result) shouldBe Json.obj("code" -> "SA_ALREADY_EXISTS", "message" -> "A self-assessment record already exists for this individual")
    }
  }

  private def requestWithField(fieldName: String, fieldValue: String) = toJson(request).as[JsObject] ++ Json.obj(fieldName -> fieldValue)

  private def requestWithTaxReturnField(fieldName: String, fieldValue: String) = {
    Json.obj(
      "registrationDate" -> request.registrationDate,
      "taxReturns" -> Json.arr(toJson(saReturn).as[JsObject] ++ Json.obj(fieldName -> fieldValue)))
  }

}
