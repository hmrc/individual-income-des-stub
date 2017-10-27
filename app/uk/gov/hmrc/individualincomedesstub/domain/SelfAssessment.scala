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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.util.Validators

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

case class SelfAssessmentReturn(selfEmploymentStartDate: Option[String],
                                saReceivedDate: String,
                                selfEmploymentIncome: Double,
                                employmentsIncome: Double)

object SelfAssessmentReturn {
  def apply(saReturnPayload: SelfAssessmentReturnData): SelfAssessmentReturn = {
    SelfAssessmentReturn(
      saReturnPayload.selfEmploymentStartDate,
      saReturnPayload.saReceivedDate,
      saReturnPayload.selfEmploymentIncome.getOrElse(0.0),
      saReturnPayload.employmentsIncome.getOrElse(0.0)
    )
  }
}

case class SelfAssessment(nino: Nino,
                          taxYear: TaxYear,
                          saReturns: Seq[SelfAssessmentReturn])

case class SelfAssessmentReturnData(selfEmploymentStartDate: Option[String],
                                    saReceivedDate: String,
                                    selfEmploymentIncome: Option[Double],
                                    employmentsIncome: Option[Double])

case class SelfAssessmentCreateRequest(saReturns: Seq[SelfAssessmentReturnData]) {
  saReturns map { sa =>
    Validators.validDate("saReceivedDate", sa.saReceivedDate)
    sa.selfEmploymentStartDate map (Validators.validDate("selfEmploymentStartDate", _))
  }
}

case class TaxYear(ty: String) {
  if (!TaxYear.isValid(ty)) throw new IllegalArgumentException

  val startYr = ty.split("-")(0)
}

object TaxYear {

  final val TaxYearRegex = "^(\\d{4})-(\\d{2})$"

  val matchTaxYear: String => Option[Match] = new Regex(s"$TaxYearRegex", "first", "second") findFirstMatchIn _

  def build(ty: String): Option[TaxYear] = TaxYearRegex.r findFirstIn ty map (TaxYear(_))

  def isValid(taxYearReference: String) = matchTaxYear(taxYearReference) exists {
    r => (r.group("first").toInt + 1) % 100 == r.group("second").toInt
  }

}