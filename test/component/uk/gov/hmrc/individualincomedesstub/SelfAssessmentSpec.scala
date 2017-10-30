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
import org.joda.time.LocalDate.parse
import play.api.http.HeaderNames._
import play.api.http.MimeTypes._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.selfAssessmentFormat
import uk.gov.hmrc.individualincomedesstub.domain.{SelfAssessment, SelfAssessmentReturn, TaxYear}

import scala.concurrent.Await.result
import scalaj.http.Http

class SelfAssessmentSpec extends BaseSpec {

  val taxYearString = "2014-15"
  val ninoString = "AB123456A"
  val taxYear = TaxYear(taxYearString)
  val nino = Nino(ninoString)

  feature("Create self assessment") {

    scenario("Self assessment successfully created for valid NINO and TaxYear") {

      Given("A valid create self assessment request")
      val request = Json.parse(
        """
          {
           "saReturns": [
             {
               "selfEmploymentStartDate": "2014-01-01",
               "saReceivedDate": "2015-01-01",
               "selfEmploymentIncome": 1233.33,
               "employmentsIncome": 13567.77
             },
             {
               "selfEmploymentStartDate": "2015-01-01",
               "saReceivedDate": "2016-01-01",
               "selfEmploymentIncome": 1338.33,
               "employmentsIncome": 14906.10
             }
           ]
          }
        """)

      When("I submit a POST request to create a self assessment")
      val response = requestCreateSelfAssessment(request, ninoString, taxYearString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The self assessment is returned in the response body")

      val saReturns = Seq(
        selfAssessmentReturn(),
        selfAssessmentReturn(Some(parse("2015-01-01")), 1338.33, 14906.10, parse("2016-01-01"))
      )

      val sa = selfAssessment(saReturns = saReturns)
      Json.parse(response.body) shouldBe Json.toJson(sa)

      And("The self assessment is stored in mongo")
      val storedSa = result(selfAssessmentRepository.findByNinoAndTaxYear(nino, taxYear), timeout)
      storedSa shouldBe Seq(sa)
    }

    scenario("Self assessment successfully created with no start date") {

      Given("A valid create self assessment request with no employment start date")
      val request = Json.parse(
        """
          {
           "saReturns": [
             {
               "saReceivedDate": "2015-01-01",
               "selfEmploymentIncome": 1233.33,
               "employmentsIncome": 13567.77
             }
           ]
          }
        """)

      When("I submit a POST request to create a self assessment")
      val response = requestCreateSelfAssessment(request, ninoString, taxYearString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The self assessment is returned in the response body")
      val sa = selfAssessment(saReturns = Seq(selfAssessmentReturn(selfEmploymentStartDate = None)))
      Json.parse(response.body) shouldBe Json.toJson(sa)

      And("The self assessment is stored in mongo")
      val storedSa = result(selfAssessmentRepository.findByNinoAndTaxYear(nino, taxYear), timeout)
      storedSa shouldBe Seq(sa)
    }

    scenario("Self assessment successfully created with default income values") {

      Given("A valid create self assessment request with no sa income and employments income")
      val request = Json.parse(
        """
          {
           "saReturns": [
             {
              "selfEmploymentStartDate": "2014-01-01",
               "saReceivedDate": "2015-01-01"
             }
           ]
          }
        """)

      When("I submit a POST request to create a self assessment")
      val response = requestCreateSelfAssessment(request, ninoString, taxYearString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The self assessment is created and returned with default income values")
      val employment = selfAssessment(saReturns = Seq(selfAssessmentReturn(selfEmploymentIncome = 0.0, employmentsIncome = 0.0)))
      Json.parse(response.body) shouldBe Json.toJson(employment)

      And("The self assessment is stored in mongo")
      val storedEmployment = result(selfAssessmentRepository.findByNinoAndTaxYear(nino, taxYear), timeout)
      storedEmployment shouldBe Seq(employment)
    }

    scenario("Request fails for missing sa return received date") {
      Given("A create self assessment request with no sa return received date")
      val request = Json.parse(
        """
          {
           "saReturns": [
             {
              "selfEmploymentStartDate": "2014-01-01"
             }
           ]
          }
        """)

      When("I submit a POST request to create a self assessment")
      val response = requestCreateSelfAssessment(request, ninoString, taxYearString)

      Then("The response code shoud be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The response body contains the appropriate error message")
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"saReturns(0)/saReceivedDate is required"}"""
    }

    scenario("Request fails for invalid NINO") {

      When("I submit a POST request to create a self assessment with an invalid NINO")
      val response = requestCreateSelfAssessment(Json.parse("{}"), "A23456A", taxYearString)

      Then("The response code shoud be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The response body contains the appropriate error message")
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"Malformed nino submitted"}"""
    }

    scenario("Request fails for invalid tax year") {

      When("I submit a POST request to create a self assessment with an invalid tax year")
      val response = requestCreateSelfAssessment(Json.parse("{}"), "AB123456A", "2014-1")

      Then("The response code shoud be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST

      And("The response body contains the appropriate error message")
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"Malformed tax year submitted"}"""
    }

  }

  def selfAssessmentReturn(selfEmploymentStartDate: Option[LocalDate] = Some(parse("2014-01-01")),
                           selfEmploymentIncome: Double = 1233.33,
                           employmentsIncome: Double = 13567.77,
                           saReceivedDate: LocalDate = parse("2015-01-01")) = {
    SelfAssessmentReturn(selfEmploymentStartDate, saReceivedDate, selfEmploymentIncome, employmentsIncome)
  }

  def selfAssessment(nino: String = ninoString, taxYear: String = taxYearString, saReturns: Seq[SelfAssessmentReturn] = Seq(selfAssessmentReturn())) = {
    SelfAssessment(Nino(nino), TaxYear(taxYear), saReturns)
  }

  private def requestCreateSelfAssessment(request: JsValue, nino: String, taxYear: String) = {
    Http(s"$serviceUrl/$nino/self-assessment/$taxYear")
      .postData(request.toString())
      .headers(CONTENT_TYPE -> JSON
      ).asString
  }
}
