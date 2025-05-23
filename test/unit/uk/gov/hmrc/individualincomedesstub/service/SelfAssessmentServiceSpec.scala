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

package unit.uk.gov.hmrc.individualincomedesstub.service

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.domain.*
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentService
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

import java.time.LocalDate
import scala.concurrent.Future

class SelfAssessmentServiceSpec extends TestSupport with MockitoSugar {

  private val utr = SaUtr("2432552635")
  private val taxReturn = SelfAssessmentTaxReturnData(
    taxYear = "2015-16",
    submissionDate = "2015-01-10",
    employmentsIncome = Some(1444.44),
    selfEmploymentProfit = Some(4444.55),
    totalIncome = Some(15777.77),
    trustsIncome = Some(500.25),
    foreignIncome = Some(200.35),
    partnershipsProfit = Some(23.45),
    ukInterestsIncome = Some(15.65),
    foreignDividendsIncome = Some(12.53),
    ukDividendsIncome = Some(23.75),
    ukPropertiesProfit = Some(55.24),
    gainsOnLifePolicies = Some(25.63),
    sharesOptionsIncome = Some(12.63),
    pensionsAndStateBenefitsIncome = Some(16.85),
    otherIncome = Some(134.56),
    businessDescription = None,
    address = None
  )

  private val request = SelfAssessmentCreateRequest(registrationDate = "2015-06-06", taxReturns = Seq(taxReturn))

  trait Setup {
    val repository: SelfAssessmentRepository = mock[SelfAssessmentRepository]
    val underTest = new SelfAssessmentService(repository)

  }

  "create" should {
    "return the created self assessment" in new Setup {
      private val expectedSelfAssessment = SelfAssessment(
        saUtr = utr,
        registrationDate = LocalDate.parse("2015-06-06"),
        taxReturns = Seq(
          SelfAssessmentTaxReturn(
            taxYear = TaxYear("2015-16"),
            submissionDate = LocalDate.parse("2015-01-10"),
            employmentsIncome = 1444.44,
            selfEmploymentProfit = 4444.55,
            totalIncome = 15777.77,
            trustsIncome = 500.25,
            foreignIncome = 200.35,
            partnershipsProfit = 23.45,
            ukInterestsIncome = 15.65,
            foreignDividendsIncome = 12.53,
            ukDividendsIncome = 23.75,
            ukPropertiesProfit = 55.24,
            gainsOnLifePolicies = 25.63,
            sharesOptionsIncome = 12.63,
            pensionsAndStateBenefitsIncome = 16.85,
            otherIncome = 134.56,
            businessDescription = None,
            address = None
          )
        )
      )

      when(repository.create(any())).thenReturn(Future.successful(SelfAssessment(utr, request)))

      private val result = await(underTest.create(utr, request))

      result shouldBe expectedSelfAssessment
      verify(repository, times(1)).create(expectedSelfAssessment)
    }

    "defaults the amounts to zero when they are no set" in new Setup {
      private val taxReturnWithoutAmounts = taxReturn.copy(
        employmentsIncome = None,
        selfEmploymentProfit = None,
        totalIncome = None,
        trustsIncome = None,
        foreignIncome = None,
        partnershipsProfit = None,
        ukInterestsIncome = None,
        foreignDividendsIncome = None,
        ukDividendsIncome = None,
        ukPropertiesProfit = None,
        gainsOnLifePolicies = None,
        sharesOptionsIncome = None,
        pensionsAndStateBenefitsIncome = None,
        otherIncome = None
      )

      private val requestWithoutAmounts = request.copy(taxReturns = Seq(taxReturnWithoutAmounts))

      when(repository.create(any())).thenReturn(Future.successful(SelfAssessment(utr, requestWithoutAmounts)))

      private val result = await(underTest.create(utr, requestWithoutAmounts))

      result.taxReturns shouldBe Seq(
        SelfAssessmentTaxReturn(
          taxYear = TaxYear("2015-16"),
          submissionDate = LocalDate.parse("2015-01-10"),
          employmentsIncome = 0,
          selfEmploymentProfit = 0,
          totalIncome = 0,
          trustsIncome = 0,
          foreignIncome = 0,
          partnershipsProfit = 0,
          ukInterestsIncome = 0,
          foreignDividendsIncome = 0,
          ukDividendsIncome = 0,
          ukPropertiesProfit = 0,
          gainsOnLifePolicies = 0,
          sharesOptionsIncome = 0,
          pensionsAndStateBenefitsIncome = 0,
          otherIncome = 0,
          businessDescription = None,
          address = None
        )
      )
    }

    "propagate exceptions when a self assessment cannot be created" in new Setup {
      when(repository.create(any())).thenThrow(new RuntimeException("failed"))
      intercept[RuntimeException](await(underTest.create(utr, request)))
    }
  }
}
