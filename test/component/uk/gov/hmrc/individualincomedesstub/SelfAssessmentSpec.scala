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

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import play.api.http.HeaderNames._
import play.api.http.MimeTypes._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.selfAssessmentFormat
import uk.gov.hmrc.individualincomedesstub.domain.{SelfAssessment, SelfAssessmentTaxReturn, TaxYear}

import scala.concurrent.Await.result
import scalaj.http.Http

class SelfAssessmentSpec extends BaseSpec {

  val utr = SaUtr("2432552635")
  val selfAssessment = SelfAssessment(
    utr,
    LocalDate.parse("2014-01-01"),
    Seq(
      SelfAssessmentTaxReturn(
        taxYear = TaxYear("2014-15"),
        submissionDate = parse("2016-01-01"),
        employmentsIncome = 13567.77,
        selfEmploymentProfit = 1233.33,
        totalIncome = 22345,
        trustsIncome = 12345.55,
        foreignIncome = 500.25,
        partnershipsProfit = 23.45,
        ukInterestsIncome = 14.74,
        foreignDividendsIncome = 11.46,
        ukDividendsIncome = 10.32,
        ukPropertiesProfit = 55.24,
        gainsOnLifePolicies = 4.32,
        sharesOptionsIncome = 12.32,
        pensionsAndStateBenefitsIncome = 28.42,
        otherIncome = 134.56,
        businessDescription = None,
        address = None
      ))
  )

  Feature("Create self assessment") {

    Scenario("Self assessment successfully created for a valid UTR") {

      Given("A valid create self assessment request")
      val request = Json.parse("""
          {
           "registrationDate": "2014-01-01",
           "taxReturns": [
             {
                "taxYear": "2014-15",
                "submissionDate": "2016-01-01",
                "employmentsIncome": 13567.77,
                "selfEmploymentProfit": 1233.33,
                "totalIncome": 22345,
                "trustsIncome": 12345.55,
                "foreignIncome": 500.25,
                "partnershipsProfit": 23.45,
                "ukInterestsIncome": 14.74,
                "foreignDividendsIncome": 11.46,
                "ukDividendsIncome": 10.32,
                "ukPropertiesProfit": 55.24,
                "gainsOnLifePolicies": 4.32,
                "sharesOptionsIncome": 12.32,
                "pensionsAndStateBenefitsIncome": 28.42,
                "otherIncome": 134.56
             }
           ]
          }
        """)

      When("I submit a POST request to create a self assessment")
      val response = requestCreateSelfAssessment(request, utr.value)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The self assessment is returned in the response body")
      Json.parse(response.body) shouldBe Json.toJson(selfAssessment)

      And("The self assessment is stored in mongo")
      val storedSa = result(selfAssessmentRepository.findByUtr(utr), timeout)
      storedSa shouldBe Some(selfAssessment)
    }

    Scenario("Self assessment successfully created with default income values") {
      val expectedReturn = SelfAssessmentTaxReturn(
        taxYear = TaxYear("2014-15"),
        submissionDate = LocalDate.parse("2016-01-01"),
        employmentsIncome = 0,
        selfEmploymentProfit = 0,
        totalIncome = 0,
        trustsIncome = 0,
        foreignIncome = 0,
        partnershipsProfit = 0,
        ukInterestsIncome = 0,
        foreignDividendsIncome = 0,
        ukDividendsIncome = 0,
        ukPropertiesProfit = 0,
        gainsOnLifePolicies = 0,
        sharesOptionsIncome = 0,
        pensionsAndStateBenefitsIncome = 0,
        otherIncome = 0,
        businessDescription = None,
        address = None
      )

      Given("A valid create self assessment request with no income values")
      val request = Json.parse("""
          {
           "registrationDate": "2014-01-01",
           "taxReturns": [
             {
                "taxYear": "2014-15",
                "submissionDate": "2016-01-01"
             }
           ]
          }
        """)

      When("I submit a POST request to create a self assessment")
      val response = requestCreateSelfAssessment(request, utr.value)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The self assessment is created and returned with default income values")
      Json.parse(response.body) shouldBe Json.toJson(selfAssessment.copy(taxReturns = Seq(expectedReturn)))

      And("The self assessment is stored in mongo")
      val storedEmployment = result(selfAssessmentRepository.findByUtr(utr), timeout)
      storedEmployment shouldBe Some(selfAssessment.copy(taxReturns = Seq(expectedReturn)))
    }
  }

  private def requestCreateSelfAssessment(request: JsValue, utr: String) =
    Http(s"$serviceUrl/$utr/self-assessment")
      .postData(request.toString())
      .headers(CONTENT_TYPE -> JSON)
      .asString
}
