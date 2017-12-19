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

import org.joda.time.LocalDate
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future
import scala.concurrent.Future.successful

class SelfAssessmentServiceSpec extends UnitSpec with MockitoSugar {

  val utr = SaUtr("2432552635")
  val taxReturn = SelfAssessmentTaxReturnData(
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
    sharesOptionsIncome = Some(12.63)
  )

  val request = SelfAssessmentCreateRequest(registrationDate = "2015-06-06", taxReturns = Seq(taxReturn))

  trait Setup {
    val repository = mock[SelfAssessmentRepository]
    val underTest = new SelfAssessmentService(repository)

    when(repository.create(any())).thenAnswer(returnSame)
  }

  "create" should {
    "return the created self assessment" in new Setup {
      val expectedSelfAssessment = SelfAssessment(
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
            sharesOptionsIncome = 12.63
          ))
      )

      val result = await(underTest.create(utr, request))

      result shouldBe expectedSelfAssessment
      verify(repository).create(expectedSelfAssessment)
    }

    "defaults the amounts to zero when they are no set" in new Setup {
      val taxReturnWithoutAmounts = taxReturn.copy(
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
        sharesOptionsIncome = None
      )
      val requestWithoutAmounts = request.copy(taxReturns = Seq(taxReturnWithoutAmounts))

      val result = await(underTest.create(utr, requestWithoutAmounts))

      result.taxReturns shouldBe Seq(SelfAssessmentTaxReturn(
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
        sharesOptionsIncome = 0
      ))
    }

    "propagate exceptions when a self assessment cannot be created" in new Setup {
      when(repository.create(any())).thenThrow(new RuntimeException("failed"))
      intercept[RuntimeException](await(underTest.create(utr, request)))
    }
  }

  def returnSame[T] = new Answer[Future[T]] {
    override def answer(invocationOnMock: InvocationOnMock): Future[T] = {
      val argument = invocationOnMock.getArguments()(0)
      successful(argument.asInstanceOf[T])
    }
  }
}
