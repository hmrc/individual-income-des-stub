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

import org.joda.time.LocalDate.parse
import org.mockito.ArgumentMatchers.{any, eq => refEq}
import org.mockito.BDDMockito.given
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status._
import play.api.libs.json.Json.toJson
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualincomedesstub.controller.SelfAssessmentIncomeController
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.selfAssessmentResponseFormat
import uk.gov.hmrc.individualincomedesstub.domain.{RecordNotFoundException, SaAddress, SelfAssessmentResponse, SelfAssessmentResponseReturn}
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

import scala.concurrent.Future.{failed, successful}

class SelfAssessmentIncomeControllerSpec extends TestSupport with MockitoSugar with ScalaFutures {

  implicit lazy val materializer = fakeApplication.materializer
  private val controllerComponents: ControllerComponents =
    fakeApplication.injector.instanceOf[ControllerComponents]

  trait Setup {
    implicit val hc = HeaderCarrier()

    val nino = Nino("AB123456A")
    val fakeRequest = FakeRequest()
    val selfAssessmentIncomeService = mock[SelfAssessmentIncomeService]
    val underTest = new SelfAssessmentIncomeController(selfAssessmentIncomeService, controllerComponents)
  }

  val selfAssessmentResponse = SelfAssessmentResponse(
    taxYear = "2015",
    returnList = Seq(
      SelfAssessmentResponseReturn(
        utr = SaUtr("2432552635"),
        caseStartDate = parse("2014-01-01"),
        receivedDate = parse("2015-06-01"),
        incomeFromAllEmployments = 100.15,
        profitFromSelfEmployment = 2000.55,
        incomeFromSelfAssessment = 12300.15,
        incomeFromTrust = 500.25,
        incomeFromForeign4Sources = 200.25,
        profitFromPartnerships = 65.67,
        incomeFromUkInterest = 13.53,
        incomeFromForeignDividends = 15.74,
        incomeFromInterestNDividendsFromUKCompaniesNTrusts = 13.64,
        incomeFromProperty = 55.24,
        incomeFromGainsOnLifePolicies = 13.53,
        incomeFromSharesOptions = 24.54,
        incomeFromPensions = 17.95,
        incomeFromOther = 134.56,
        businessDescription = None,
        address = SaAddress(
          addressLine1 = None,
          addressLine2 = None,
          addressLine3 = None,
          addressLine4 = None,
          postalCode = None,
          telephoneNumber = None,
          baseAddressEffectiveDate = None,
          addressTypeIndicator = None
        )
      ))
  )

  "fetch self assessment income" should {
    "retrieve self assessment income for a given period" in new Setup {
      given(selfAssessmentIncomeService.income(refEq(nino), refEq(2015), refEq(2016))(any[HeaderCarrier]))
        .willReturn(successful(Seq(selfAssessmentResponse)))

      val result = await(underTest.income(nino, 2015, 2016)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe toJson(Seq(selfAssessmentResponse))
    }

    "return 404 (Not Found) if there is no self assessment income for a given period" in new Setup {
      given(selfAssessmentIncomeService.income(refEq(nino), refEq(2015), refEq(2016))(any[HeaderCarrier]))
        .willReturn(failed(new RecordNotFoundException()))

      val result = await(underTest.income(nino, 2015, 2016)(fakeRequest))

      status(result) shouldBe NOT_FOUND
    }
  }
}
