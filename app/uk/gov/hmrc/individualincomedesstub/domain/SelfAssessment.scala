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

package uk.gov.hmrc.individualincomedesstub.domain

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.util.Validators.{validDate, validTaxYear}

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

case class SelfAssessmentTaxReturn(taxYear: TaxYear,
                                   submissionDate: LocalDate,
                                   employmentsIncome: Double,
                                   selfEmploymentProfit: Double,
                                   totalIncome: Double) {

  def isIn(startYear: Int, endYear: Int) = taxYear.endYr.toInt >= startYear && taxYear.endYr.toInt <= endYear
}

object SelfAssessmentTaxReturn {
  def apply(saTaxReturnData: SelfAssessmentTaxReturnData): SelfAssessmentTaxReturn = {
    SelfAssessmentTaxReturn(
      TaxYear(saTaxReturnData.taxYear),
      parse(saTaxReturnData.submissionDate),
      saTaxReturnData.employmentsIncome.getOrElse(0.0),
      saTaxReturnData.selfEmploymentProfit.getOrElse(0.0),
      saTaxReturnData.totalIncome.getOrElse(0.0)
    )
  }
}

case class SelfAssessment(saUtr: SaUtr,
                          registrationDate: LocalDate,
                          taxReturns: Seq[SelfAssessmentTaxReturn])

object SelfAssessment {
  def apply(utr: SaUtr, request: SelfAssessmentCreateRequest): SelfAssessment = {
    SelfAssessment(
      utr,
      parse(request.registrationDate),
      request.taxReturns.map(SelfAssessmentTaxReturn(_))
    )
  }
}

case class SelfAssessmentTaxReturnData(taxYear: String,
                                       submissionDate: String,
                                       employmentsIncome: Option[Double],
                                       selfEmploymentProfit: Option[Double],
                                       totalIncome: Option[Double])

case class SelfAssessmentCreateRequest(registrationDate: String, taxReturns: Seq[SelfAssessmentTaxReturnData]) {
  validDate("registrationDate", registrationDate)
  taxReturns foreach { sa =>
    validDate("submissionDate", sa.submissionDate)
    validTaxYear(sa.taxYear)
  }
}

case class SelfAssessmentResponseReturn(utr: SaUtr,
                                        caseStartDate: LocalDate,
                                        receivedDate: LocalDate,
                                        incomeFromAllEmployments: Double,
                                        profitFromSelfEmployment: Double,
                                        incomeFromSelfAssessment: Double)

object SelfAssessmentResponseReturn {
  def apply(utr: SaUtr, registrationDate: LocalDate, selfAssessmentTaxReturn: SelfAssessmentTaxReturn): SelfAssessmentResponseReturn = {
    SelfAssessmentResponseReturn(
      utr,
      registrationDate,
      selfAssessmentTaxReturn.submissionDate,
      selfAssessmentTaxReturn.employmentsIncome,
      selfAssessmentTaxReturn.selfEmploymentProfit,
      selfAssessmentTaxReturn.totalIncome
    )
  }
}

case class SelfAssessmentResponse(taxYear: String, returnList: Seq[SelfAssessmentResponseReturn])

object SelfAssessmentResponse {
  def apply(utr: SaUtr, registrationDate: LocalDate, selfAssessmentTaxReturn: SelfAssessmentTaxReturn): SelfAssessmentResponse = {
    SelfAssessmentResponse(selfAssessmentTaxReturn.taxYear.endYr,
      Seq(SelfAssessmentResponseReturn(utr, registrationDate, selfAssessmentTaxReturn)))
  }
}

case class TaxYear(ty: String) {
  if (!TaxYear.isValid(ty)) throw new IllegalArgumentException

  val startYr = ty.split("-")(0)
  val endYr = startYr.toInt + 1 toString
}

object TaxYear {

  final val TaxYearRegex = "^(\\d{4})-(\\d{2})$"

  val matchTaxYear: String => Option[Match] = new Regex(s"$TaxYearRegex", "first", "second") findFirstMatchIn _

  def build(ty: String): Option[TaxYear] = TaxYearRegex.r findFirstIn ty map (TaxYear(_))

  def isValid(taxYearReference: String) = matchTaxYear(taxYearReference) exists {
    r => (r.group("first").toInt + 1) % 100 == r.group("second").toInt
  }

}
