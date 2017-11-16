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

package unit.uk.gov.hmrc.individualincomedesstub.service

import org.joda.time.LocalDate.parse
import org.mockito.BDDMockito.given
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualincomedesstub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualincomedesstub.domain.{RecordNotFoundException, SelfAssessment, _}
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.Future.successful

class SelfAssessmentIncomeServiceSpec extends UnitSpec with MockitoSugar {

  val nino = Nino("AB123456A")
  val utr = SaUtr("2432552635")

  trait Setup {
    implicit val hc = HeaderCarrier()

    val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
    val repository = mock[SelfAssessmentRepository]

    val underTest = new SelfAssessmentIncomeService(apiPlatformTestUserConnector, repository)
  }

  val selfAssessment = SelfAssessment(
    saUtr = utr,
    registrationDate = parse("2008-04-04"),
    taxReturns = Seq(
      SelfAssessmentTaxReturn(
        taxYear = TaxYear("2014-15"),
        submissionDate = parse("2015-06-01"),
        employmentsIncome = 13567.77,
        selfEmploymentProfit = 1233.33,
        totalIncome = 21233.33)))

  "income" should {

    "retrieve an individuals self assessment income for a given period" in new Setup {

      given(apiPlatformTestUserConnector.getIndividualByNino(nino)).willReturn(successful(TestIndividual(Some(utr))))
      given(repository.findByUtr(utr)).willReturn(Some(selfAssessment))

      val result = await(underTest.income(nino, 2014, 2015))

      result shouldBe Seq(
        SelfAssessmentResponse(
          taxYear = "2015",
          returnList = Seq(SelfAssessmentResponseReturn(
            utr = utr,
            caseStartDate = parse("2008-04-04"),
            receivedDate = parse("2015-06-01"),
            incomeFromAllEmployments = 13567.77,
            profitFromSelfEmployment = 1233.33,
            incomeFromSelfAssessment = 21233.33
          ))))
    }

    "fail with RecordNotFoundException when there is no individual matching the nino" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino)).willReturn(Future.failed(new RecordNotFoundException()))

      intercept[RecordNotFoundException]{await(underTest.income(nino, 2014, 2015))}
    }

    "fail with RecordNotFoundException when there is the individual does not have a UTR" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino)).willReturn(successful(TestIndividual(None)))

      intercept[RecordNotFoundException]{await(underTest.income(nino, 2014, 2015))}
    }

    "fail with RecordNotFoundException when there is no self-assessment for the individual" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino)).willReturn(successful(TestIndividual(Some(utr))))
      given(repository.findByUtr(utr)).willReturn(None)

      intercept[RecordNotFoundException]{await(underTest.income(nino, 2014, 2015))}
    }

    "fail with RecordNotFoundException when there is no self-assessment returns for the individual for the given period" in new Setup {
      given(apiPlatformTestUserConnector.getIndividualByNino(nino)).willReturn(successful(TestIndividual(Some(utr))))
      given(repository.findByUtr(utr)).willReturn(Some(selfAssessment))

      intercept[RecordNotFoundException]{await(underTest.income(nino, 2013, 2014))}
    }

    "propagate exceptions when sa income cannot be retrieved" in new Setup {
      when(repository.findByUtr(utr)).thenThrow(new RuntimeException("failed"))
      intercept[RuntimeException](await(underTest.income(nino, 2015, 2016)))
    }
  }
}
