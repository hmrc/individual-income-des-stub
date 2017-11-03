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
import org.joda.time.LocalDate.parse
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService
import uk.gov.hmrc.play.test.UnitSpec

class SelfAssessmentIncomeServiceSpec extends UnitSpec with MockitoSugar {

  val ninoString = "AB123456A"
  val taxYearString = "2014-15"
  val nino = Nino(ninoString)

  trait Setup {
    val repository = mock[SelfAssessmentRepository]
    val underTest = new SelfAssessmentIncomeService(repository)
  }

  "income" should {

    "retrieve an individuals self assessment income for a given period" in new Setup {
      val sa = selfAssessment()

      when(repository.findByNino(nino)).thenReturn(Seq(sa))

      val result = await(underTest.income(nino, 2014, 2015))

      result shouldBe Seq(SelfAssessmentResponse("2015", Seq(SelfAssessmentResponseReturnData(sa.saReturns.head))))
    }

    "return an empty sequence when there is no self assessment income for a given period" in new Setup {
      when(repository.findByNino(nino)).thenReturn(Seq.empty)

      val result = await(underTest.income(nino, 2014, 2015))

      result shouldBe Seq.empty
    }

    "propagate exceptions when sa income cannot be retrieved" in new Setup {
      when(repository.findByNino(nino)).thenThrow(new RuntimeException("failed"))
      intercept[RuntimeException](await(underTest.income(nino, 2015, 2016)))
    }
  }

  def selfAssessmentReturn(selfEmploymentStartDate: Option[LocalDate] = Some(parse("2014-01-01")),
                           selfEmploymentIncome: Double = 1233.33,
                           employmentsIncome: Double = 13567.77,
                           saReceivedDate: LocalDate = parse("2015-01-01"),
                           selfEmploymentProfit: Double = 1233.33) = {
    SelfAssessmentReturn(selfEmploymentStartDate, saReceivedDate, selfEmploymentIncome, employmentsIncome, selfEmploymentProfit)
  }

  def selfAssessment(nino: String = ninoString, taxYear: String = taxYearString, saReturns: Seq[SelfAssessmentReturn] = Seq(selfAssessmentReturn())) = {
    SelfAssessment(Nino(nino), TaxYear(taxYear), saReturns)
  }

}
