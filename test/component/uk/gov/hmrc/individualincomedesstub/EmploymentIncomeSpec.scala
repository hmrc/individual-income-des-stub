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

package component.uk.gov.hmrc.individualincomedesstub

import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.{Employer, CreateEmploymentRequest, HmrcPayment}

import scala.concurrent.Await.result
import scalaj.http.Http

class EmploymentIncomeSpec extends BaseSpec {

  val nino = Nino("NA000799C")
  val employment = CreateEmploymentRequest(
    startDate = Some("2016-01-01"),
    endDate = Some("2016-06-01"),
    payments = Seq(HmrcPayment("2016-05-30", 252.55, 12.25))
  )

  feature("Employment Income returns a DES Stubbed response") {

    scenario("Fetch Employment Income when there are payments") {

      Given("A employer")
      val employer = result(employerRepository.createEmployer(Employer()), timeout)

      And("An employment")
      employmentRepository.create(employer.payeReference, nino, employment)

      When("I fetch the employment income when there was a payment")
      val response = fetchEmploymentIncome(nino.value, employment.startDate.get)

      Then("The response code should be 200 (Ok)")
      response.code shouldBe OK

      And("The employment income is returned in the response body")
      Json.parse(response.body) shouldBe Json.parse(
        s"""
          |{
          |   "employments":[
          |      {
          |         "employerName":"${employer.name}",
          |         "employerAddress":{
          |            "line1":"${employer.address.line1}",
          |            "line2":"${employer.address.line2.get}",
          |            "postalCode":"${employer.address.postcode}"
          |         },
          |         "employerDistrictNumber":"${employer.payeReference.taxOfficeNumber}",
          |         "employerSchemeReference":"${employer.payeReference.taxOfficeReference}",
          |         "employmentStartDate":"2016-01-01",
          |         "employmentLeavingDate":"2016-06-01",
          |         "payments":[
          |            {
          |               "paymentDate":"2016-05-30",
          |               "totalPayInPeriod":252.55,
          |               "totalNonTaxOrNICsPayments":12.25
          |            }
          |         ]
          |      }
          |   ]
          |}
        """.stripMargin
      )
    }
  }

  private def fetchEmploymentIncome(nino: String, fromDate: String) = {
    Http(s"$serviceUrl/individuals/nino/$nino/employments/income?from=$fromDate").asString
  }
}
