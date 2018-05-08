/*
 * Copyright 2018 HM Revenue & Customs
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
                                   totalIncome: Double,
                                   trustsIncome: Double,
                                   foreignIncome: Double,
                                   partnershipsProfit: Double,
                                   ukInterestsIncome: Double,
                                   foreignDividendsIncome: Double,
                                   ukDividendsIncome: Double,
                                   ukPropertiesProfit: Double,
                                   gainsOnLifePolicies: Double,
                                   sharesOptionsIncome: Double,
                                   pensionsAndStateBenefitsIncome: Double,
                                   otherIncome: Double,
                                   businessDescription: Option[String],
                                   addressLine1: Option[String],
                                   addressLine2: Option[String],
                                   addressLine3: Option[String],
                                   addressLine4: Option[String],
                                   postalCode: Option[String]
                                  ) {

  def isIn(startYear: Int, endYear: Int) = taxYear.endYr.toInt >= startYear && taxYear.endYr.toInt <= endYear
}

object SelfAssessmentTaxReturn {
  def apply(saTaxReturnData: SelfAssessmentTaxReturnData): SelfAssessmentTaxReturn = {
    SelfAssessmentTaxReturn(
      TaxYear(saTaxReturnData.taxYear),
      parse(saTaxReturnData.submissionDate),
      saTaxReturnData.employmentsIncome.getOrElse(0.0),
      saTaxReturnData.selfEmploymentProfit.getOrElse(0.0),
      saTaxReturnData.totalIncome.getOrElse(0.0),
      saTaxReturnData.trustsIncome.getOrElse(0.0),
      saTaxReturnData.foreignIncome.getOrElse(0.0),
      saTaxReturnData.partnershipsProfit.getOrElse(0.0),
      saTaxReturnData.ukInterestsIncome.getOrElse(0.0),
      saTaxReturnData.foreignDividendsIncome.getOrElse(0.0),
      saTaxReturnData.ukDividendsIncome.getOrElse(0.0),
      saTaxReturnData.ukPropertiesProfit.getOrElse(0.0),
      saTaxReturnData.gainsOnLifePolicies.getOrElse(0.0),
      saTaxReturnData.sharesOptionsIncome.getOrElse(0.0),
      saTaxReturnData.pensionsAndStateBenefitsIncome.getOrElse(0.0),
      saTaxReturnData.otherIncome.getOrElse(0.0),
      saTaxReturnData.businessDescription,
      saTaxReturnData.addressLine1,
      saTaxReturnData.addressLine2,
      saTaxReturnData.addressLine3,
      saTaxReturnData.addressLine4,
      saTaxReturnData.postalCode
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
                                       totalIncome: Option[Double],
                                       trustsIncome: Option[Double],
                                       foreignIncome: Option[Double],
                                       partnershipsProfit: Option[Double],
                                       ukInterestsIncome: Option[Double],
                                       foreignDividendsIncome: Option[Double],
                                       ukDividendsIncome: Option[Double],
                                       ukPropertiesProfit: Option[Double],
                                       gainsOnLifePolicies: Option[Double],
                                       sharesOptionsIncome: Option[Double],
                                       pensionsAndStateBenefitsIncome: Option[Double],
                                       otherIncome: Option[Double],
                                       businessDescription: Option[String],
                                       addressLine1: Option[String],
                                       addressLine2: Option[String],
                                       addressLine3: Option[String],
                                       addressLine4: Option[String],
                                       postalCode: Option[String]
                                      )

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
                                        incomeFromSelfAssessment: Double,
                                        incomeFromTrust: Double,
                                        incomeFromForeign4Sources: Double,
                                        profitFromPartnerships: Double,
                                        incomeFromUkInterest: Double,
                                        incomeFromForeignDividends: Double,
                                        incomeFromInterestNDividendsFromUKCompaniesNTrusts: Double,
                                        incomeFromProperty: Double,
                                        incomeFromGainsOnLifePolicies: Double,
                                        incomeFromSharesOptions: Double,
                                        incomeFromPensions: Double,
                                        incomeFromOther: Double,
                                        businessDescription: Option[String],
                                        addressLine1: Option[String],
                                        addressLine2: Option[String],
                                        addressLine3: Option[String],
                                        postalCode: Option[String]
                                       )

object SelfAssessmentResponseReturn {
  def apply(utr: SaUtr, registrationDate: LocalDate, selfAssessmentTaxReturn: SelfAssessmentTaxReturn): SelfAssessmentResponseReturn = {
    SelfAssessmentResponseReturn(
      utr,
      registrationDate,
      selfAssessmentTaxReturn.submissionDate,
      selfAssessmentTaxReturn.employmentsIncome,
      selfAssessmentTaxReturn.selfEmploymentProfit,
      selfAssessmentTaxReturn.totalIncome,
      selfAssessmentTaxReturn.trustsIncome,
      selfAssessmentTaxReturn.foreignIncome,
      selfAssessmentTaxReturn.partnershipsProfit,
      selfAssessmentTaxReturn.ukInterestsIncome,
      selfAssessmentTaxReturn.foreignDividendsIncome,
      selfAssessmentTaxReturn.ukDividendsIncome,
      selfAssessmentTaxReturn.ukPropertiesProfit,
      selfAssessmentTaxReturn.gainsOnLifePolicies,
      selfAssessmentTaxReturn.sharesOptionsIncome,
      selfAssessmentTaxReturn.pensionsAndStateBenefitsIncome,
      selfAssessmentTaxReturn.otherIncome,
      selfAssessmentTaxReturn.businessDescription,
      selfAssessmentTaxReturn.addressLine1,
      selfAssessmentTaxReturn.addressLine2,
      selfAssessmentTaxReturn.addressLine3,
      selfAssessmentTaxReturn.postalCode
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
