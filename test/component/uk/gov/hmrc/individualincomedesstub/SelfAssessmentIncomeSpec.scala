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

package component.uk.gov.hmrc.individualincomedesstub

import component.uk.gov.hmrc.individualincomedesstub.stubs.ApiPlatformTestUserStub
import java.time.LocalDate
import play.api.http.Status._
import play.api.libs.json.Json
import scalaj.http.Http
import uk.gov.hmrc.domain.{Nino, SaUtr}
import uk.gov.hmrc.individualincomedesstub.domain.{SelfAssessment, SelfAssessmentTaxReturn, TaxYear, TestIndividual}

class SelfAssessmentIncomeSpec extends BaseSpec {

  private val nino = Nino("AB123456A")
  private val utr = SaUtr("2432552635")

  Feature("Retrieve DES stubbed self assessment income") {

    Scenario("Fetch self assessment income when there is income associated to a given tax year range") {

      Given("A test individual")
      ApiPlatformTestUserStub.getByNinoReturnsTestIndividual(nino, TestIndividual(Some(utr)))

      And("Self assessment income exist for a given period")
      selfAssessmentRepository.create(
        SelfAssessment(
          saUtr = utr,
          registrationDate = LocalDate.parse("2013-01-01"),
          taxReturns = Seq(
            SelfAssessmentTaxReturn(
              taxYear = TaxYear("2013-14"),
              submissionDate = LocalDate.parse("2014-06-01"),
              employmentsIncome = 100.15,
              selfEmploymentProfit = 100.15,
              totalIncome = 400.30,
              trustsIncome = 500.25,
              foreignIncome = 200.35,
              partnershipsProfit = 323.12,
              ukInterestsIncome = 25.76,
              foreignDividendsIncome = 27.54,
              ukDividendsIncome = 62.13,
              ukPropertiesProfit = 55.24,
              gainsOnLifePolicies = 12.53,
              sharesOptionsIncome = 42.34,
              pensionsAndStateBenefitsIncome = 27.42,
              otherIncome = 134.56,
              businessDescription = None,
              address = None
            ),
            SelfAssessmentTaxReturn(
              taxYear = TaxYear("2014-15"),
              submissionDate = LocalDate.parse("2015-07-01"),
              employmentsIncome = 200.20,
              selfEmploymentProfit = 300.35,
              totalIncome = 600.15,
              trustsIncome = 400.45,
              foreignIncome = 250.45,
              partnershipsProfit = 12.32,
              ukInterestsIncome = 36.32,
              foreignDividendsIncome = 12.73,
              ukDividendsIncome = 29.06,
              ukPropertiesProfit = 22.25,
              gainsOnLifePolicies = 16.21,
              sharesOptionsIncome = 13.75,
              pensionsAndStateBenefitsIncome = 71.24,
              otherIncome = 34.56,
              businessDescription = None,
              address = None
            )
          )
        )
      )

      When("I fetch self assessment income for that period")
      val response = fetchSelfAssessmentIncome(nino, "2013", "2014")

      Then("The response code should be 200 (Ok)")
      response.code shouldBe OK

      And("The self assessment income is returned in the response body")
      Json.parse(response.body) shouldBe Json.parse("""
          [
              {
                  "taxYear": "2014",
                  "returnList": [
                      {
                          "utr": "2432552635",
                          "caseStartDate": "2013-01-01",
                          "receivedDate": "2014-06-01",
                          "incomeFromAllEmployments": 100.15,
                          "profitFromSelfEmployment": 100.15,
                          "incomeFromSelfAssessment": 400.30,
                          "incomeFromTrust": 500.25,
                          "incomeFromForeign4Sources": 200.35,
                          "profitFromPartnerships": 323.12,
                          "incomeFromUkInterest": 25.76,
                          "incomeFromForeignDividends": 27.54,
                          "incomeFromInterestNDividendsFromUKCompaniesNTrusts": 62.13,
                          "incomeFromProperty": 55.24,
                          "incomeFromGainsOnLifePolicies": 12.53,
                          "incomeFromSharesOptions": 42.34,
                          "incomeFromPensions": 27.42,
                          "incomeFromOther": 134.56
                      }
                  ]
               }
          ]
        """)
    }

    Scenario("Fetch self assessment income when there is no income associated to a given tax year range") {

      Given("A test individual")
      ApiPlatformTestUserStub.getByNinoReturnsTestIndividual(nino, TestIndividual(Some(utr)))

      When("I fetch self assessment income for a period with no income")
      val response = fetchSelfAssessmentIncome(nino, "2014", "2015")

      Then("The response code should be 404 (Not Found)")
      response.code shouldBe NOT_FOUND
    }

    Scenario("Request fails for missing start year") {

      When("I attempt to fetch self assessment without a start year")
      val response = Http(s"$serviceUrl/individuals/nino/$nino/self-assessment/income?endYear=2014").asString

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"statusCode":400,"message":"Invalid Request"}""")
    }

    Scenario("Request fails for missing end year") {

      When("I attempt to fetch self assessment without an end year")
      val response = Http(s"$serviceUrl/individuals/nino/$nino/self-assessment/income?startYear=2014").asString

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The correct error message in the response body")
      Json.parse(response.body) shouldBe Json.parse("""{"statusCode":400,"message":"Invalid Request"}""")
    }
  }

  private def fetchSelfAssessmentIncome(nino: Nino, startYear: String, endYear: String) =
    Http(s"$serviceUrl/individuals/nino/$nino/self-assessment/income?startYear=$startYear&endYear=$endYear").asString
}
