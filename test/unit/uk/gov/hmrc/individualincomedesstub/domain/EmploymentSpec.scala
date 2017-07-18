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

package unit.uk.gov.hmrc.individualincomedesstub.domain

import org.joda.time.LocalDateTime.parse
import org.joda.time.{Interval, LocalDate, LocalDateTime}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FreeSpec, Matchers}
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain._

class EmploymentSpec extends FreeSpec with Matchers {

  "A payment should determine whether it was paid within a given time interval" in new TableDrivenPropertyChecks {
    val payment = HmrcPayment("2017-01-10", 123.45, 67.89)

    val fixtures = Table(("exampleinterval", "expected result"),
      (toInterval(parse("2017-01-08T00:00:00.000"), parse("2017-01-09T00:00:00.001")), false),
      (toInterval(parse("2017-01-09T00:00:00.000"), parse("2017-01-10T00:00:00.001")), true),
      (toInterval(parse("2017-01-10T00:00:00.000"), parse("2017-01-11T00:00:00.001")), true),
      (toInterval(parse("2017-01-11T00:00:00.000"), parse("2017-01-12T00:00:00.001")), false))

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      payment.isPaidWithin(exampleInterval) shouldBe expectedResult
    }

  }

  "An employment should determine whether it contains a payment within a given time interval" in new TableDrivenPropertyChecks {
    val payment = HmrcPayment("2017-01-10", 123.45, 67.89)
    val employment = Employment(EmpRef("123", "AB12345"), Nino("AB123456C"), Option("2017-02-01"), Option("2017-02-28"), Seq(payment))

    val fixtures = Table(("interval example", "expected result"),
      (toInterval(parse("2017-01-09T00:00:00.000"), parse("2017-01-09T23:59:59.999")), false),
      (toInterval(parse("2017-01-10T00:00:00.000"), parse("2017-01-10T23:59:59.999")), true),
      (toInterval(parse("2017-01-11T00:00:00.000"), parse("2017-01-11T23:59:59.999")), false))

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      employment.containsPaymentWithin(exampleInterval) shouldBe expectedResult
    }
  }

  "An employment income response should derive itself from an employment without a missing employer" in {
    val employmentStartDate = Option("2020-02-01")
    val employmentEndDate = Option("2020-02-29")
    val employment = Employment(EmpRef("123", "AB12345"), Nino("AB123456C"), employmentStartDate, employmentEndDate, Seq.empty)

    val employmentIncomeResponse = EmploymentIncomeResponse(employment, None)

    employmentIncomeResponse.employerName shouldBe None
    employmentIncomeResponse.employerAddress shouldBe None
    employmentIncomeResponse.employerDistrictNumber shouldBe None
    employmentIncomeResponse.employerSchemeReference shouldBe None

    employmentIncomeResponse.employmentStartDate.get shouldBe toLocalDate(employmentStartDate)
    employmentIncomeResponse.employmentLeavingDate.get shouldBe toLocalDate(employmentEndDate)
    employmentIncomeResponse.payments shouldBe Seq.empty
  }

  "An employment income response should derive itself from an employment with an employer" in {
    val employmentStartDate = Option("2020-02-01")
    val employmentEndDate = Option("2020-02-29")
    val employment = Employment(EmpRef("123", "AB12345"), Nino("AB123456C"), employmentStartDate, employmentEndDate, Seq.empty)

    val employer = TestOrganisation(
      Some(EmpRef.fromIdentifiers("123/AB12345")), TestOrganisationDetails("Acme",
      TestAddress("line1", "line2", "AB1 2CD")))

    val employmentIncomeResponse = EmploymentIncomeResponse(employment, Some(employer))

    employmentIncomeResponse.employerName shouldBe Some(employer.organisationDetails.name)
    employmentIncomeResponse.employerAddress shouldBe Some(DesAddress(employer.organisationDetails.address))
    employmentIncomeResponse.employerDistrictNumber shouldBe employer.empRef.map(_.taxOfficeNumber)
    employmentIncomeResponse.employerSchemeReference shouldBe employer.empRef.map(_.taxOfficeReference)

    employmentIncomeResponse.employmentStartDate.get shouldBe toLocalDate(employmentStartDate)
    employmentIncomeResponse.employmentLeavingDate.get shouldBe toLocalDate(employmentEndDate)
    employmentIncomeResponse.payments shouldBe Seq.empty
  }

  "A DES payment should derive itself from a HMRC payment" in {
    val hmrcPayment = HmrcPayment("2020-01-01", 123.45, 67.89)
    val desPayment = DesPayment(hmrcPayment)
    desPayment.paymentDate shouldBe LocalDate.parse(hmrcPayment.paymentDate)
    desPayment.totalPayInPeriod shouldBe hmrcPayment.taxablePayment
    desPayment.totalNonTaxOrNICsPayments shouldBe hmrcPayment.nonTaxablePayment
  }

  private def toInterval(from: LocalDateTime, to: LocalDateTime): Interval =
    new Interval(from.toDate.getTime, to.toDate.getTime)

  private def toLocalDate(maybeString: Option[String]) =
    LocalDate.parse(maybeString.get)

}
