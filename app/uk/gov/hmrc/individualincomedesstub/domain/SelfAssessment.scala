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
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.util.Validators.validDate

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

case class SelfAssessmentReturn(selfEmploymentStartDate: Option[LocalDate],
                                saReceivedDate: LocalDate,
                                selfAssessmentIncome: Double,
                                employmentsIncome: Double,
                                selfEmploymentProfit: Double)

object SelfAssessmentReturn {
  def apply(saReturnPayload: SelfAssessmentReturnData): SelfAssessmentReturn = {
    SelfAssessmentReturn(
      saReturnPayload.selfEmploymentStartDate.map(parse(_)),
      parse(saReturnPayload.saReceivedDate),
      saReturnPayload.selfAssessmentIncome.getOrElse(0.0),
      saReturnPayload.employmentsIncome.getOrElse(0.0),
      saReturnPayload.selfEmploymentProfit.getOrElse(0.0)
    )
  }
}

case class SelfAssessment(nino: Nino,
                          taxYear: TaxYear,
                          saReturns: Seq[SelfAssessmentReturn]) {
  def isIn(startYear: Int, endYear: Int) = taxYear.startYr.toInt >= startYear - 1 && taxYear.endYr.toInt <= endYear
}

case class SelfAssessmentReturnData(selfEmploymentStartDate: Option[String],
                                    saReceivedDate: String,
                                    selfAssessmentIncome: Option[Double],
                                    employmentsIncome: Option[Double],
                                    selfEmploymentProfit: Option[Double])

case class SelfAssessmentCreateRequest(saReturns: Seq[SelfAssessmentReturnData]) {
  saReturns map { sa =>
    validDate("saReceivedDate", sa.saReceivedDate)
    sa.selfEmploymentStartDate map (validDate("selfEmploymentStartDate", _))
  }
}

case class SelfAssessmentResponseReturnData(caseStartDate: Option[LocalDate],
                                            receivedDate: LocalDate,
                                            incomeFromSelfAssessment: Double,
                                            incomeFromAllEmployments: Double,
                                            profitFromSelfEmployment: Double)

object SelfAssessmentResponseReturnData {
  def apply(selfAssessmentReturn: SelfAssessmentReturn): SelfAssessmentResponseReturnData = {
    SelfAssessmentResponseReturnData(
      selfAssessmentReturn.selfEmploymentStartDate,
      selfAssessmentReturn.saReceivedDate,
      selfAssessmentReturn.selfAssessmentIncome,
      selfAssessmentReturn.employmentsIncome,
      selfAssessmentReturn.selfEmploymentProfit
    )
  }
}

case class SelfAssessmentResponse(taxYear: String, returnList: Seq[SelfAssessmentResponseReturnData])

object SelfAssessmentResponse {
  def apply(selfAssessment: SelfAssessment): SelfAssessmentResponse = {
    SelfAssessmentResponse(selfAssessment.taxYear.endYr, selfAssessment.saReturns.map(SelfAssessmentResponseReturnData(_)))
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
