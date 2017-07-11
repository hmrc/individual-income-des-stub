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

import org.joda.time.LocalDate.parse
import org.joda.time.{Interval, LocalDate}
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.util.Validators._

case class HmrcPayment(paymentDate: String, taxablePayment: Double, nonTaxablePayment: Double) {
  validDate("paymentDate", paymentDate)

  def isPaidWithin(interval: Interval): Boolean =
    interval.contains(parse(paymentDate).toDateTimeAtStartOfDay)

}

case class DesPayment(paymentDate: LocalDate, totalPayInPeriod: Double, totalNonTaxOrNICsPayments: Double)

object DesPayment {
  def apply(hmrcPayment: HmrcPayment): DesPayment = DesPayment(LocalDate.parse(hmrcPayment.paymentDate), hmrcPayment.taxablePayment, hmrcPayment.nonTaxablePayment)
}

case class DesAddress(line1: String, line2: Option[String], postalCode: String)

object DesAddress {
  def apply(address: Address): DesAddress = DesAddress(address.line1, address.line2, address.postcode)
}

case class Employment(employerPayeReference: EmpRef, nino: Nino, startDate: Option[String], endDate: Option[String], payments: Seq[HmrcPayment]) {
  def containsPaymentWithin(interval: Interval) =
    payments.exists(_.isPaidWithin(interval))
}

object Employment {

  def overlap(interval: Interval)(employment: Employment): Boolean = employment.containsPaymentWithin(interval)

}

case class CreateEmploymentRequest(startDate: Option[String], endDate: Option[String], payments: Seq[HmrcPayment]) {
  (startDate, endDate) match {
    case (Some(start), Some(end)) =>
      validDate("startDate", start)
      validDate("endDate", end)
      validInterval(start, end, "Invalid employment period")
    case (Some(start), None) => validDate("startDate", start)
    case (None, Some(end)) => validDate("endDate", end)
    case _ =>
  }
}


case class EmploymentIncomeResponse
(employerName: Option[String],
 employerAddress: Option[DesAddress],
 employerDistrictNumber: Option[String],
 employerSchemeReference: Option[String],
 employmentStartDate: Option[LocalDate],
 employmentLeavingDate: Option[LocalDate],
 payments: Seq[DesPayment])

object EmploymentIncomeResponse {

  import org.joda.time.LocalDate.parse

  def apply(employment: Employment, maybeEmployer: Option[Employer]): EmploymentIncomeResponse =
    maybeEmployer match {
      case Some(employer) => EmploymentIncomeResponse(
        Option(employer.name), Option(DesAddress(employer.address)), Option(employer.payeReference.taxOfficeNumber), Option(employer.payeReference.taxOfficeReference),
        employment.startDate.map(parse), employment.endDate.map(parse), employment.payments map (DesPayment(_))
      )
      case _ => EmploymentIncomeResponse(
        None, None, None, None,
        employment.startDate.map(parse), employment.endDate.map(parse), employment.payments map (DesPayment(_))
      )
    }
}
