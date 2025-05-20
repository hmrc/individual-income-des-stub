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

package unit.uk.gov.hmrc.individualincomedesstub.controller

import org.apache.pekko.stream.Materializer
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.*
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.controller.SelfAssessmentController
import uk.gov.hmrc.individualincomedesstub.domain.*
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.*
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentService
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.{failed, successful}

class SelfAssessmentControllerSpec extends TestSupport with MockitoSugar with ScalaFutures {

  implicit lazy val materializer: Materializer = fakeApplication.materializer
  private val controllerComponents: ControllerComponents =
    fakeApplication.injector.instanceOf[ControllerComponents]
  def externalServices: Seq[String] = Seq("Stub")

  private val utr = SaUtr("2432552635")
  private val saReturn = SelfAssessmentTaxReturnData(
    taxYear = "2014-15",
    submissionDate = "2015-06-01",
    employmentsIncome = Some(123),
    selfEmploymentProfit = Some(456),
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
  private val request = SelfAssessmentCreateRequest("2014-01-01", Seq(saReturn))
  private val selfAssessment = SelfAssessment(utr, request)

  trait Setup {
    val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    val selfAssessmentService: SelfAssessmentService =
      mock[SelfAssessmentService]
    val underTest =
      new SelfAssessmentController(selfAssessmentService, controllerComponents)
  }

  "create self assessment" should {

    "return a 201 (Created) when self assessment data is created successfully" in new Setup {
      when(selfAssessmentService.create(utr, request))
        .thenReturn(successful(selfAssessment))

      private val result =
        await(underTest.create(utr)(fakeRequest.withBody(toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe toJson(selfAssessment)
    }

    "return a 400 (BadRequest) when the registration date is invalid" in new Setup {
      private val result =
        await(underTest.create(utr)(fakeRequest.withBody(requestWithField("registrationDate", "11-11-1111"))))

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe Json
        .obj("code" -> "INVALID_REQUEST", "message" -> "registrationDate: invalid date format")
    }

    "return a 400 (BadRequest) when the taxYear is invalid" in new Setup {
      private val result =
        await(underTest.create(utr)(fakeRequest.withBody(requestWithTaxReturnField("taxYear", "201516"))))

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe Json.obj("code" -> "INVALID_REQUEST", "message" -> "taxYear: invalid tax year format")
    }

    "return a 400 (BadRequest) when the submissionDate is invalid" in new Setup {
      private val result =
        await(underTest.create(utr)(fakeRequest.withBody(requestWithTaxReturnField("submissionDate", "invalid"))))

      status(result) shouldBe BAD_REQUEST
      jsonBodyOf(result) shouldBe Json
        .obj("code" -> "INVALID_REQUEST", "message" -> "submissionDate: invalid date format")
    }

    "return a 429 (Conflict) when a self-assessment already exists for the utr" in new Setup {
      when(selfAssessmentService.create(utr, request))
        .thenReturn(failed(new DuplicateSelfAssessmentException()))

      private val result =
        await(underTest.create(utr)(fakeRequest.withBody(toJson(request))))

      status(result) shouldBe CONFLICT
      jsonBodyOf(result) shouldBe Json
        .obj("code" -> "SA_ALREADY_EXISTS", "message" -> "A self-assessment record already exists for this individual")
    }
  }

  private def requestWithField(fieldName: String, fieldValue: String) =
    toJson(request).as[JsObject] ++ Json.obj(fieldName -> fieldValue)

  private def requestWithTaxReturnField(fieldName: String, fieldValue: String) =
    Json.obj(
      "registrationDate" -> request.registrationDate,
      "taxReturns"       -> Json.arr(toJson(saReturn).as[JsObject] ++ Json.obj(fieldName -> fieldValue))
    )
}
