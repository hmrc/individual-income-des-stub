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

package unit.uk.gov.hmrc.individualincomedesstub.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.DesEmploymentPayFrequency._
import uk.gov.hmrc.individualincomedesstub.domain.EmploymentPayFrequency._
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.util.Interval

import java.time.LocalDate
import java.time.LocalDateTime.parse

class EmploymentSpec extends AnyFreeSpec with Matchers {

  "A payment should determine whether it was paid within a given time interval" in new TableDrivenPropertyChecks {
    private val payment = HmrcPayment("2017-01-10", 123.45)

    private val fixtures = Table(
      ("exampleinterval", "expected result"),
      (Interval(parse("2017-01-08T00:00:00.000"), parse("2017-01-09T00:00:00.001")), false),
      (Interval(parse("2017-01-09T00:00:00.000"), parse("2017-01-10T00:00:00.001")), true),
      (Interval(parse("2017-01-10T00:00:00.000"), parse("2017-01-11T00:00:00.001")), true),
      (Interval(parse("2017-01-11T00:00:00.000"), parse("2017-01-12T00:00:00.001")), false)
    )

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      payment.isPaidWithin(exampleInterval) shouldBe expectedResult
    }

  }

  "An employment should determine whether it contains a payment within a given time interval" in new TableDrivenPropertyChecks {
    private val payment = HmrcPayment("2017-01-10", 123.45)
    private val employment =
      Employment(EmpRef("123", "AB12345"), Nino("AB123456C"), None, None, Seq(payment), None, None)

    private val fixtures = Table(
      ("interval example", "expected result"),
      (Interval(parse("2017-01-09T00:00:00.000"), parse("2017-01-09T23:59:59.999")), false),
      (Interval(parse("2017-01-10T00:00:00.000"), parse("2017-01-10T23:59:59.999")), true),
      (Interval(parse("2017-01-11T00:00:00.000"), parse("2017-01-11T23:59:59.999")), false)
    )

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      employment.isWithin(exampleInterval) shouldBe expectedResult
    }
  }

  "An employment with start/end dates should determine whether it falls within a given time interval" in new TableDrivenPropertyChecks {
    private val employment = Employment(
      EmpRef("123", "AB12345"),
      Nino("AB123456C"),
      Option("2017-02-01"),
      Option("2017-02-27"),
      Seq.empty,
      None,
      None
    )

    private val fixtures = Table(
      ("interval example", "expected result"),
      (Interval(parse("2017-01-09T00:00:00.000"), parse("2017-01-31T23:59:59.999")), false),
      (Interval(parse("2017-02-28T00:00:00.000"), parse("2017-03-02T23:59:59.999")), false),
      (Interval(parse("2017-01-01T00:00:00.000"), parse("2017-02-01T00:00:00.001")), true),
      (Interval(parse("2017-02-10T00:00:00.000"), parse("2017-02-15T23:59:59.999")), true),
      (Interval(parse("2017-02-27T00:00:00.000"), parse("2017-03-01T23:59:59.999")), true)
    )

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      employment.isWithin(exampleInterval) shouldBe expectedResult
    }
  }

  "An employment with only start date should determine whether it falls within a given time interval" in new TableDrivenPropertyChecks {
    private val employment =
      Employment(EmpRef("123", "AB12345"), Nino("AB123456C"), Option("2017-02-01"), None, Seq.empty, None, None)

    private val fixtures = Table(
      ("interval example", "expected result"),
      (Interval(parse("2016-12-01T00:00:00.000"), parse("2017-01-31T23:59:59.999")), false),
      (Interval(parse("2017-03-01T00:00:00.000"), parse("2017-05-31T23:59:59.999")), true),
      (Interval(parse("2017-02-01T00:00:00.000"), parse("2017-03-02T23:59:59.999")), true),
      (Interval(parse("2017-01-01T00:00:00.000"), parse("2017-02-01T00:00:00.001")), true)
    )

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      employment.isWithin(exampleInterval) shouldBe expectedResult
    }
  }

  "An employment with only end date should determine whether it falls within a given time interval" in new TableDrivenPropertyChecks {
    private val employment =
      Employment(EmpRef("123", "AB12345"), Nino("AB123456C"), None, Option("2017-02-01"), Seq.empty, None, None)

    private val fixtures = Table(
      ("interval example", "expected result"),
      (Interval(parse("2017-02-02T00:00:00.000"), parse("2017-05-31T23:59:59.999")), false),
      (Interval(parse("2017-02-01T00:00:00.000"), parse("2017-03-02T23:59:59.999")), true),
      (Interval(parse("2016-12-01T00:00:00.000"), parse("2017-02-01T23:59:59.999")), true),
      (Interval(parse("2017-01-01T00:00:00.000"), parse("2017-01-31T00:00:00.001")), true)
    )

    forAll(fixtures) { (exampleInterval, expectedResult) =>
      employment.isWithin(exampleInterval) shouldBe expectedResult
    }
  }

  "An employment income response should derive itself from an employment with a missing employer" in {
    val employmentStartDate = Option("2020-02-01")
    val employmentEndDate = Option("2020-02-29")
    val employment = Employment(
      EmpRef("123", "AB12345"),
      Nino("AB123456C"),
      employmentStartDate,
      employmentEndDate,
      Seq.empty,
      None,
      None
    )

    val employmentIncomeResponse = EmploymentIncomeResponse(employment, None)

    employmentIncomeResponse.employerName shouldBe None
    employmentIncomeResponse.employerAddress shouldBe None
    employmentIncomeResponse.employerDistrictNumber.getOrElse(
      fail("missing employerDistrictNumber")
    ) shouldBe employment.employerPayeReference.taxOfficeNumber
    employmentIncomeResponse.employerSchemeReference.getOrElse(
      fail("missing employerSchemeReference")
    ) shouldBe employment.employerPayeReference.taxOfficeReference

    employmentIncomeResponse.employmentStartDate.get shouldBe toLocalDate(employmentStartDate)
    employmentIncomeResponse.employmentLeavingDate.get shouldBe toLocalDate(employmentEndDate)
    employmentIncomeResponse.payments shouldBe Seq.empty
  }

  "An employment income response should derive itself from an employment with an employer" in {
    val employmentStartDate = Option("2020-02-01")
    val employmentEndDate = Option("2020-02-29")
    val employment = Employment(
      EmpRef("123", "AB12345"),
      Nino("AB123456C"),
      employmentStartDate,
      employmentEndDate,
      Seq.empty,
      None,
      None
    )

    val employer = TestOrganisation(
      Some(EmpRef.fromIdentifiers("123/AB12345")),
      TestOrganisationDetails("Acme", TestAddress("line1", "line2", "AB1 2CD"))
    )

    val employmentIncomeResponse =
      EmploymentIncomeResponse(employment, Some(employer))

    employmentIncomeResponse.employerName shouldBe Some(employer.organisationDetails.name)
    employmentIncomeResponse.employerAddress shouldBe Some(DesAddress(employer.organisationDetails.address))
    employmentIncomeResponse.employerDistrictNumber shouldBe employer.empRef
      .map(_.taxOfficeNumber)
    employmentIncomeResponse.employerSchemeReference shouldBe employer.empRef
      .map(_.taxOfficeReference)

    employmentIncomeResponse.employmentStartDate.get shouldBe toLocalDate(employmentStartDate)
    employmentIncomeResponse.employmentLeavingDate.get shouldBe toLocalDate(employmentEndDate)
    employmentIncomeResponse.payments shouldBe Seq.empty
  }

  "A DES payment should derive itself from a HMRC payment" in {
    val hmrcPayment = HmrcPayment("2020-01-01", 123.45)
    val desPayment = DesPayment(hmrcPayment)
    desPayment.paymentDate shouldBe LocalDate.parse(hmrcPayment.paymentDate)
    desPayment.totalPayInPeriod shouldBe hmrcPayment.taxablePayment
  }

  "A Des pay frequency should derive itself from a  employment pay frequency" in new TableDrivenPropertyChecks {
    private val fixtures = Table(
      ("Employment frequency", "Expected Des employment frequency"),
      (WEEKLY, Some(W1)),
      (FORTNIGHTLY, Some(W2)),
      (FOUR_WEEKLY, Some(W4)),
      (ONE_OFF, Some(IO)),
      (IRREGULAR, Some(IR)),
      (CALENDAR_MONTHLY, Some(M1)),
      (QUARTERLY, Some(M3)),
      (BI_ANNUALLY, Some(M6)),
      (ANNUALLY, Some(MA))
    )

    forAll(fixtures) { (empFrequency, expectedDesEmpFrequency) =>
      DesEmploymentPayFrequency.from(empFrequency.toString) shouldBe expectedDesEmpFrequency
    }
  }

  private def toLocalDate(maybeString: Option[String]) =
    LocalDate.parse(maybeString.get)
}
