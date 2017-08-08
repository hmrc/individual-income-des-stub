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

import component.uk.gov.hmrc.individualincomedesstub.stubs.ApiPlatformTestUserStub
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain._

import scalaj.http.Http

class EmploymentIncomeSpec extends BaseSpec {

  val nino = Nino("NA000799C")
  val empRef = EmpRef("123", "AI45678")
  val employment = CreateEmploymentRequest(
    startDate = Some("2016-01-01"),
    endDate = Some("2016-06-01"),
    payments = Seq(HmrcPayment("2016-05-30", 252.55, monthPayNumber = Some(10))),
    Some(EmploymentPayFrequency.CALENDAR_MONTHLY.toString)
  )
  val employer = TestOrganisation(
    Some(empRef),
    TestOrganisationDetails("Disney Inc", TestAddress("Capital Tower", "Aberdeen", "SW1 4DQ")))

  feature("Employment Income returns a DES Stubbed response") {

    scenario("Fetch Employment Income when there are payments and employer details") {

      Given("An employer can be found for a given empRef")
      ApiPlatformTestUserStub.getByEmpRefReturnsTestOrganisation(empRef, employer)

      And("An employment")
      employmentRepository.create(empRef, nino, employment)

      When("I fetch the employment income when there was a payment")
      val response = fetchEmploymentIncome(nino.value, employment.startDate.get)

      Then("The response code should be 200 (Ok)")
      response.code shouldBe OK

      And("The employment income is returned in the response body")
      Json.parse(response.body) shouldBe Json.parse(
        s"""
          {
             "employments":[
                {
                   "employerName":"${employer.organisationDetails.name}",
                   "employerAddress":{
                      "line1":"${employer.organisationDetails.address.line1}",
                      "line2":"${employer.organisationDetails.address.line2}",
                      "postalCode":"${employer.organisationDetails.address.postcode}"
                   },
                   "employerDistrictNumber":"${empRef.taxOfficeNumber}",
                   "employerSchemeReference":"${empRef.taxOfficeReference}",
                   "employmentStartDate":"2016-01-01",
                   "employmentLeavingDate":"2016-06-01",
                   "payFrequency":"M1",
                   "payments":[
                      {
                         "paymentDate":"2016-05-30",
                         "totalPayInPeriod":252.55,
                         "monthPayNumber":10
                      }
                   ]
                }
             ]
          }
        """
      )
    }

    scenario("Fetch Employment Income when there are payments but no employer details") {

      Given("An employer cannot be found for a given empRef")
      ApiPlatformTestUserStub.getByEmpRefReturnsNoTestOrganisation(empRef)

      And("An employment")
      employmentRepository.create(empRef, nino, employment)

      When("I fetch the employment income when there was a payment")
      val response = fetchEmploymentIncome(nino.value, employment.startDate.get)

      Then("The response code should be 200 (Ok)")
      response.code shouldBe OK

      And("The employment income is returned in the response body")
      Json.parse(response.body) shouldBe Json.parse(
        s"""
           {
              "employments":[
                 {
                    "employerDistrictNumber":"123",
                    "employerSchemeReference":"AI45678",
                    "employmentStartDate":"2016-01-01",
                    "employmentLeavingDate":"2016-06-01",
                    "payFrequency":"M1",
                    "payments":[
                       {
                          "paymentDate":"2016-05-30",
                          "totalPayInPeriod":252.55,
                          "monthPayNumber":10
                       }
                    ]
                 }
              ]
           }
        """
      )
    }
  }

  private def fetchEmploymentIncome(nino: String, fromDate: String) = {
    Http(s"$serviceUrl/individuals/nino/$nino/employments/income?from=$fromDate").asString
  }
}
