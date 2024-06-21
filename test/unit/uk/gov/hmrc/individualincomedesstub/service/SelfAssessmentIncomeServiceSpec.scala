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

import java.time.LocalDate
import java.time.LocalDate.parse
import org.mockito.BDDMockito.given
import org.mockito.MockitoSugar
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualincomedesstub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class SelfAssessmentIncomeServiceSpec extends TestSupport with MockitoSugar {

  private val nino = Nino("AB123456A")
  private val utr = SaUtr("2432552635")

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val apiPlatformTestUserConnector: ApiPlatformTestUserConnector =
      mock[ApiPlatformTestUserConnector]
    val repository: SelfAssessmentRepository = mock[SelfAssessmentRepository]

    val underTest =
      new SelfAssessmentIncomeService(apiPlatformTestUserConnector, repository)
  }

  private val selfAssessment = SelfAssessment(
    saUtr = utr,
    registrationDate = LocalDate.parse("2008-04-04"),
    taxReturns = Seq(
      SelfAssessmentTaxReturn(
        taxYear = TaxYear("2014-15"),
        submissionDate = LocalDate.parse("2015-06-01"),
        employmentsIncome = 13567.77,
        selfEmploymentProfit = 1233.33,
        totalIncome = 21233.33,
        trustsIncome = 500.25,
        foreignIncome = 200.35,
        partnershipsProfit = 43.23,
        ukInterestsIncome = 14.64,
        foreignDividendsIncome = 17.42,
        ukDividendsIncome = 18.54,
        ukPropertiesProfit = 55.24,
        gainsOnLifePolicies = 14.23,
        sharesOptionsIncome = 12.34,
        pensionsAndStateBenefitsIncome = 16.95,
        otherIncome = 134.56,
        businessDescription = None,
        address = None
      )
    )
  )

  "income" should {

    "retrieve an individuals self assessment income for a given period" in new Setup {

      given(apiPlatformTestUserConnector.getIndividualByNino(nino))
        .willReturn(successful(TestIndividual(Some(utr))))
      given(repository.findByUtr(utr))
        .willReturn(successful(Some(selfAssessment)))

      private val result = await(underTest.income(nino, 2014, 2015))

      result shouldBe Seq(
        SelfAssessmentResponse(
          taxYear = "2015",
          returnList = Seq(
            SelfAssessmentResponseReturn(
              utr = utr,
              caseStartDate = parse("2008-04-04"),
              receivedDate = parse("2015-06-01"),
              incomeFromAllEmployments = 13567.77,
              profitFromSelfEmployment = 1233.33,
              incomeFromSelfAssessment = 21233.33,
              incomeFromTrust = 500.25,
              incomeFromForeign4Sources = 200.35,
              profitFromPartnerships = 43.23,
              incomeFromUkInterest = 14.64,
              incomeFromForeignDividends = 17.42,
              incomeFromInterestNDividendsFromUKCompaniesNTrusts = 18.54,
              incomeFromProperty = 55.24,
              incomeFromGainsOnLifePolicies = 14.23,
              incomeFromSharesOptions = 12.34,
              incomeFromPensions = 16.95,
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
            )
          )
        )
      )
    }

    "fail with RecordNotFoundException when there is no individual matching the nino" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino))
        .willReturn(Future.failed(new RecordNotFoundException()))

      intercept[RecordNotFoundException] {
        await(underTest.income(nino, 2014, 2015))
      }
    }

    "fail with RecordNotFoundException when there is the individual does not have a UTR" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino))
        .willReturn(successful(TestIndividual(None)))

      intercept[RecordNotFoundException] {
        await(underTest.income(nino, 2014, 2015))
      }
    }

    "fail with RecordNotFoundException when there is no self-assessment for the individual" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino))
        .willReturn(successful(TestIndividual(Some(utr))))
      given(repository.findByUtr(utr)).willReturn(successful(None))

      intercept[RecordNotFoundException] {
        await(underTest.income(nino, 2014, 2015))
      }
    }

    "fail with RecordNotFoundException when there is no self-assessment returns for the individual for the given period" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino))
        .willReturn(successful(TestIndividual(Some(utr))))
      given(repository.findByUtr(utr))
        .willReturn(successful(Some(selfAssessment)))

      intercept[RecordNotFoundException] {
        await(underTest.income(nino, 2013, 2014))
      }
    }

    "propagate exceptions when sa income cannot be retrieved" in new Setup {
      when(repository.findByUtr(utr)).thenThrow(new RuntimeException("failed"))
      intercept[RuntimeException](await(underTest.income(nino, 2015, 2016)))
    }
  }
}
