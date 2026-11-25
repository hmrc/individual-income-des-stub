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

package uk.gov.hmrc.individualincomeipstub.domain

import java.time.LocalDate.parse
import java.time.LocalDate
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomeipstub.util.Dates.toInterval
import uk.gov.hmrc.individualincomeipstub.util.Interval
import uk.gov.hmrc.individualincomeipstub.util.Validators._

case class HmrcPayment(
  paymentDate: String,
  taxablePayment: Double,
  monthPayNumber: Option[Int] = None,
  weekPayNumber: Option[Int] = None
) {
  validDate("paymentDate", paymentDate)

  def isPaidWithin(interval: Interval): Boolean =
    interval.contains(parse(paymentDate).atStartOfDay())

}

case class IpPayment(
  paymentDate: LocalDate,
  totalPayInPeriod: Double,
  monthPayNumber: Option[Int] = None,
  weekPayNumber: Option[Int] = None
)

object IpPayment {
  def apply(hmrcPayment: HmrcPayment): IpPayment =
    IpPayment(
      LocalDate.parse(hmrcPayment.paymentDate),
      hmrcPayment.taxablePayment,
      hmrcPayment.monthPayNumber,
      hmrcPayment.weekPayNumber
    )
}

case class IpAddress(
  line1: Option[String],
  line2: Option[String],
  line3: Option[String],
  line4: Option[String],
  line5: Option[String],
  postalCode: Option[String]
)

object IpAddress {
  def apply(address: TestAddress): IpAddress =
    IpAddress(
      line1 = Some(address.line1),
      line2 = Some(address.line2),
      line3 = None,
      line4 = None,
      line5 = None,
      postalCode = Some(address.postcode)
    )
}

case class Employment(
                       employerPayeReference: EmpRef,
                       nino: Nino,
                       startDate: Option[String],
                       endDate: Option[String],
                       payments: Seq[HmrcPayment],
                       employmentAddress: Option[IpAddress],
                       payrollId: Option[String],
                       payFrequency: Option[String] = None
) {

  private def containsPaymentWithin(interval: Interval) =
    payments.exists(_.isPaidWithin(interval))

  def isWithin(interval: Interval): Boolean = {

    val employmentWithinInterval = (startDate, endDate) match {
      case (Some(start), Some(end)) => interval.overlaps(toInterval(start, end))
      case (Some(start), None) =>
        interval.overlaps(toInterval(parse(start), LocalDate.now))
      case (None, Some(end)) =>
        interval.fromDate
          .minusDays(1)
          .isBefore(parse(end).atStartOfDay())
      case _ => false
    }

    employmentWithinInterval || containsPaymentWithin(interval)
  }
}

object Employment {

  def overlap(interval: Interval)(employment: Employment): Boolean =
    employment.isWithin(interval)

}

case class CreateEmploymentRequest(
                                    startDate: Option[String],
                                    endDate: Option[String],
                                    payments: Seq[HmrcPayment],
                                    employeeAddress: Option[IpAddress],
                                    payrollId: Option[String],
                                    payFrequency: Option[String]
) {

  if (payFrequency.isDefined) {
    validPayFrequency(payFrequency.get, "payFrequency is invalid")
  }

  (startDate, endDate) match {
    case (Some(start), Some(end)) =>
      validDate("startDate", start)
      validDate("endDate", end)
      validInterval(start, end, "Invalid employment period")
    case (Some(start), None) => validDate("startDate", start)
    case (None, Some(end))   => validDate("endDate", end)
    case _                   =>
  }
}

case class EmploymentIncomeResponse(
                                     employerName: Option[String],
                                     employerAddress: Option[IpAddress],
                                     employerDistrictNumber: Option[String],
                                     employerSchemeReference: Option[String],
                                     employmentStartDate: Option[LocalDate],
                                     employmentLeavingDate: Option[LocalDate],
                                     employmentPayFrequency: Option[IpEmploymentPayFrequency.Value],
                                     employeeAddress: Option[IpAddress],
                                     payrollId: Option[String],
                                     payments: Seq[IpPayment]
)

object EmploymentIncomeResponse {

  import java.time.LocalDate.parse

  def apply(employment: Employment, maybeEmployer: Option[TestOrganisation]): EmploymentIncomeResponse = {

    val ipPayFrequency =
      employment.payFrequency.flatMap(IpEmploymentPayFrequency.from)

    maybeEmployer match {
      case Some(employer) =>
        EmploymentIncomeResponse(
          Some(employer.organisationDetails.name),
          Some(IpAddress(employer.organisationDetails.address)),
          Some(employment.employerPayeReference.taxOfficeNumber),
          Some(employment.employerPayeReference.taxOfficeReference),
          employment.startDate.map(parse),
          employment.endDate.map(parse),
          ipPayFrequency,
          employment.employmentAddress,
          employment.payrollId,
          employment.payments map (IpPayment(_))
        )
      case _ =>
        EmploymentIncomeResponse(
          None,
          None,
          Some(employment.employerPayeReference.taxOfficeNumber),
          Some(employment.employerPayeReference.taxOfficeReference),
          employment.startDate.map(parse),
          employment.endDate.map(parse),
          ipPayFrequency,
          employment.employmentAddress,
          employment.payrollId,
          employment.payments map (IpPayment(_))
        )
    }
  }
}

object IpEmploymentPayFrequency extends Enumeration {

  import EmploymentPayFrequency._

  val W1, W2, W4, IO, IR, M1, M3, M6, MA = Value

  private val conversionMap = Map(
    WEEKLY           -> W1,
    FORTNIGHTLY      -> W2,
    FOUR_WEEKLY      -> W4,
    ONE_OFF          -> IO,
    IRREGULAR        -> IR,
    CALENDAR_MONTHLY -> M1,
    QUARTERLY        -> M3,
    BI_ANNUALLY      -> M6,
    ANNUALLY         -> MA
  )

  def from(payFrequency: String): Option[IpEmploymentPayFrequency.Value] =
    conversionMap.get(EmploymentPayFrequency.withName(payFrequency))

}

object EmploymentPayFrequency extends Enumeration {
  type EmploymentPayFrequency = Value
  val WEEKLY, FORTNIGHTLY, FOUR_WEEKLY, ONE_OFF, IRREGULAR, CALENDAR_MONTHLY, QUARTERLY, BI_ANNUALLY, ANNUALLY = Value
}
