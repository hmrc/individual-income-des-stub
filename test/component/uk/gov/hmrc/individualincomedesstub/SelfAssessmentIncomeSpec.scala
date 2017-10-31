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

import org.joda.time.LocalDate
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.{SelfAssessment, SelfAssessmentReturn, TaxYear}

import scalaj.http.Http

class SelfAssessmentIncomeSpec extends BaseSpec {

  val taxYearString = "2014-15"
  val ninoString = "AB123456A"
  val taxYear = TaxYear(taxYearString)
  val nino = Nino(ninoString)

  feature("Retrieve DES stubbed self assessment income") {

    scenario("Fetch self assessment income when there is income associated to a given tax year range") {

      Given("Self assessment income exist for a given period")
      selfAssessmentRepository.create(
        SelfAssessment(
          nino,
          TaxYear("2013-14"),
          Seq(SelfAssessmentReturn(Some(LocalDate.parse("2013-01-01")), LocalDate.parse("2014-01-1"), 100.15, 100.15))))
      selfAssessmentRepository.create(
        SelfAssessment(
          nino,
          TaxYear("2014-15"),
          Seq(SelfAssessmentReturn(Some(LocalDate.parse("2013-01-01")), LocalDate.parse("2015-01-1"), 200.20, 300.35))))

      When("I fetch self assessment income for that period")
      val response = fetchSelfAssessmentIncome(nino, "2014", "2015")

      Then("The response code should be 200 (Ok)")
      response.code shouldBe OK

      And("The self assessment income is returned in the response body")
      Json.parse(response.body) shouldBe Json.parse(
        """
          [
              {
                  "taxYear": "2014",
                  "returnList": [
                      {
                          "caseStartDate": "2013-01-01",
                          "receivedDate": "2014-01-01",
                          "incomeFromSelfEmployment": 100.15,
                          "incomeFromAllEmployments": 100.15
                      }
                  ]
               },
               {
                   "taxYear": "2015",
                   "returnList": [
                      {
                          "caseStartDate": "2013-01-01",
                          "receivedDate": "2015-01-01",
                          "incomeFromSelfEmployment": 200.20,
                          "incomeFromAllEmployments": 300.35
                      }
                  ]
              }
          ]
        """)
    }

    scenario("Fetch self assessment income when there is no income associated to a given tax year range") {

      When("I fetch self assessment income for a period with no income")
      val response = fetchSelfAssessmentIncome(nino, "2014", "2015")

      Then("The response code should be 404 (Not Found)")
      response.code shouldBe NOT_FOUND
    }

    scenario("Request fails for an invalid tax year range") {

      When("I attempt to fetch self assessment for an invalid tax year range")
      val response = fetchSelfAssessmentIncome(nino, "2014", "2013")

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"code":"INVALID_REQUEST","message":"Invalid time period requested"}""")
    }

    scenario("Request fails for an invalid start year") {

      When("I attempt to fetch self assessment using an invalid start year")
      val response = fetchSelfAssessmentIncome(nino, "213", "2014")

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"code":"INVALID_REQUEST","message":"startYear: invalid tax year format"}""")
    }

    scenario("Request fails for an invalid end year") {

      When("I attempt to fetch self assessment using an invalid end year")
      val response = fetchSelfAssessmentIncome(nino, "2013", "201")

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"code":"INVALID_REQUEST","message":"endYear: invalid tax year format"}""")
    }

    scenario("Request fails for missing start year") {

      When("I attempt to fetch self assessment without a start year")
      val response = Http(s"$serviceUrl/individuals/nino/$nino/self-assessment/income?endYear=2014").asString

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"code":"INVALID_REQUEST","message":"startYear is required"}""")
    }

    scenario("Request fails for missing end year") {

      When("I attempt to fetch self assessment without an end year")
      val response = Http(s"$serviceUrl/individuals/nino/$nino/self-assessment/income?startYear=2014").asString

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"code":"INVALID_REQUEST","message":"endYear is required"}""")
    }
  }

  private def fetchSelfAssessmentIncome(nino: Nino, startYear: String, endYear: String) = {
    Http(s"$serviceUrl/individuals/nino/$nino/self-assessment/income?startYear=$startYear&endYear=$endYear").asString
  }
}
