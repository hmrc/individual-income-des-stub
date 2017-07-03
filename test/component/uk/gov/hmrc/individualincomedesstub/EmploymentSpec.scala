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

import play.api.http.HeaderNames._
import play.api.http.MimeTypes._
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.libs.json.Json
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, Payment}

import scala.concurrent.Await.result
import scalaj.http.Http

class EmploymentSpec extends BaseSpec {

  val employerReference = EmpRef("123", "DI45678")
  val employerReferenceEncoded = "123%2FDI45678"
  val validNino = "NA000799C"

  feature("Create employment") {

    scenario("Employment successfully created for given empRef and NINO") {

      Given("A valid create employment request")
      val request = aCreateEmploymentRequest()

      When("I submit a POST request to create an employment")
      val response = requestCreateEmployment(Json.toJson(request).toString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The employment is returned in the response body")
      val employment = anEmployment()
      Json.parse(response.body) shouldBe Json.toJson(employment)

      And("The employment is stored in mongo")
      val storedEmployment = result(employmentRepository.findByReferenceAndNino(employerReference, Nino(validNino)), timeout)
      storedEmployment shouldBe Seq(employment)
    }

    scenario("Request succeeds with missing startDate and endDate") {
      Given("A create employment request with no startDate and endDate")
      val request = aCreateEmploymentRequest(startDate = None, endDate = None)

      When("I request to create an employment")
      val response = requestCreateEmployment(Json.toJson(request).toString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The employment is returned in the response body")
      val employment = anEmployment(startDate = None, endDate = None)
      Json.parse(response.body) shouldBe Json.toJson(employment)

      And("The employment is stored in mongo")
      val storedEmployment = result(employmentRepository.findByReferenceAndNino(employerReference, Nino(validNino)), timeout)
      storedEmployment shouldBe Seq(employment)
    }


    scenario("Request succeeds with missing startDate") {
      Given("A create employment request with no startDate")
      val request = aCreateEmploymentRequest(startDate = None)

      When("I request to create an employment")
      val response = requestCreateEmployment(Json.toJson(request).toString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The employment is returned in the response body")
      val employment = anEmployment(startDate = None)
      Json.parse(response.body) shouldBe Json.toJson(employment)

      And("The employment is stored in mongo")
      val storedEmployment = result(employmentRepository.findByReferenceAndNino(employerReference, Nino(validNino)), timeout)
      storedEmployment shouldBe Seq(employment)
    }

    scenario("Request succeeds with missing endDate") {
      Given("A create employment request with no endDate")
      val request = aCreateEmploymentRequest(endDate = None)

      When("I request to create an employment")
      val response = requestCreateEmployment(Json.toJson(request).toString)

      Then("The response code should be 201 (Created)")
      response.code shouldBe CREATED

      And("The employment is returned in the response body")
      val employment = anEmployment(endDate = None)
      Json.parse(response.body) shouldBe Json.toJson(employment)

      And("The employment is stored in mongo")
      val storedEmployment = result(employmentRepository.findByReferenceAndNino(employerReference, Nino(validNino)), timeout)
      storedEmployment shouldBe Seq(employment)
    }

    scenario("Request fails for invalid startDate format") {
      Given("A create employment request with an invalid startDate")
      val request = s"""{"startDate": "201601-01","payments":[]}"""

      When("I request to create an employment")
      val response = requestCreateEmployment(request)

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"startDate: invalid date format"}"""
    }

    scenario("Request fails for invalid endDate format") {
      Given("A create employment request with an invalid endDate")
      val request = s"""{"endDate": "201703-01","payments":[]}"""

      When("I request to create an employment")
      val response = requestCreateEmployment(request)

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"endDate: invalid date format"}"""
    }

    scenario("Request fails for invalid employment interval") {
      Given("A create employment request with an endDate before the startDate")
      val request =
        s"""
         {
           "startDate": "2016-04-28",
           "endDate": "2016-03-01",
           "payments": [
               {
                   "paymentDate": "2016-01-28",
                   "taxablePayment": 1000.55,
                   "nonTaxablePayment": 0
               }
           ]
         }"""

      When("I request to create an employment")
      val response = requestCreateEmployment(request)

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"Invalid employment period"}"""
    }

    scenario("Request fails for invalid NINO") {
      Given("A valid create employment request")
      val request = Json.toJson(aCreateEmploymentRequest()).toString

      When("I request to create an employment for an invalid NINO")
      val response = requestCreateEmployment(request, nino = "A12345C")

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"Malformed nino submitted"}"""
    }

    scenario("Request fails for unencoded employer reference") {
      Given("A valid create employment request")
      val request = Json.toJson(aCreateEmploymentRequest()).toString

      When("I request to create an employment for an unencoded employer reference")
      val response = requestCreateEmployment(request, reference = "123/DI45678")

      Then("The response code should be 404 (Not Found)")
      response.code shouldBe 404
      response.body shouldBe """{"statusCode":404,"message":"URI not found","requested":"/employer/123/DI45678/employment/NA000799C"}"""
    }

    scenario("Request fails for invalid employer reference") {
      Given("A valid create employment request")
      val request = Json.toJson(aCreateEmploymentRequest()).toString

      When("I request to create an employment for an unencoded employer reference")
      val response = requestCreateEmployment(request, reference = "123DI45678")

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"Invalid employer reference submitted"}"""
    }

    scenario("Request fails for invalid paymentDate format") {
      Given("A create employment request with an invalid paymentDate")
      val request =
        s"""
         {
           "startDate": "2016-01-01",
           "endDate": "2017-03-01",
           "payments": [
               {
                   "paymentDate": "201601-28",
                   "taxablePayment": 1000.55,
                   "nonTaxablePayment": 0
               }
           ]
         }"""

      When("I request to create an employment")
      val response = requestCreateEmployment(request)

      Then("The response code should be 400 (Bad Request)")
      response.code shouldBe BAD_REQUEST
      response.body shouldBe """{"code":"INVALID_REQUEST","message":"paymentDate: invalid date format"}"""
    }
  }

  feature("retrieve employments") {

    scenario("request with an invalid nino") {
      Given("a request with an invalid nino")
      val httpRequest = Http(s"$serviceUrl/individuals/ABCDEFGHI/employments/income")

      When("the employments endpoint is invoked")
      val httpResponse = httpRequest.asString

      Then("the response should be 400 (Bad Request)")
      httpResponse.code shouldBe BAD_REQUEST
      httpResponse.body shouldBe """{"code":"INVALID_REQUEST","message":"Malformed nino submitted"}"""
    }

    scenario("request without a from date") {
      Given("a request without a from date")
      val httpRequest = Http(s"$serviceUrl/individuals/$validNino/employments/income?missingFromDate=whatever")

      When("the employments endpoint is invoked")
      val httpResponse = httpRequest.asString

      Then("the response should be 400 (Bad Request)")
      httpResponse.code shouldBe BAD_REQUEST
      httpResponse.body shouldBe """{"code":"INVALID_REQUEST","message":"fromDate is required"}"""
    }

    scenario("request with a malformed from date") {
      Given("a request without a from date")
      val httpRequest = Http(s"$serviceUrl/individuals/$validNino/employments/income?fromDate=01-01-2017")

      When("the employments endpoint is invoked")
      val httpResponse = httpRequest.asString

      Then("the response should be 400 (Bad Request)")
      httpResponse.code shouldBe BAD_REQUEST
      httpResponse.body shouldBe """{"code":"INVALID_REQUEST","message":"fromDate: invalid date format"}"""
    }

    scenario("request with an invalid date range") {
      Given("a request with an invalid date range")
      val httpRequest = Http(s"$serviceUrl/individuals/$validNino/employments/income?fromDate=2017-01-02&toDate=2017-01-01")

      When("the employments endpoint is invoked")
      val httpResponse = httpRequest.asString

      Then("the response should be 400 (Bad Request)")
      httpResponse.code shouldBe BAD_REQUEST
      httpResponse.body shouldBe """{"code":"INVALID_REQUEST","message":"Invalid time period requested"}"""
    }

    scenario("request with corresponding employments") {
      Given("a request with an invalid date range")
      val httpRequest = Http(s"$serviceUrl/individuals/$validNino/employments/income?fromDate=2017-01-02&toDate=2017-01-01")

      When("the employments endpoint is invoked")
      val httpResponse = httpRequest.asString

      Then("the response should be 400 (Bad Request)")
      httpResponse.code shouldBe BAD_REQUEST
      httpResponse.body shouldBe """{"code":"INVALID_REQUEST","message":"Invalid time period requested"}"""
    }

  }

  private def aCreateEmploymentRequest(startDate: Option[String] = Some("2016-01-01"),
                                       endDate: Option[String] = Some("2017-03-01"),
                                       payments: Seq[Payment] = Seq(Payment("2016-01-28", 1000.55, 0), Payment("2016-02-28", 950.55, 0))) = {

    CreateEmploymentRequest(startDate, endDate, payments)
  }


  private def anEmployment(employerPayeReference: EmpRef = employerReference,
                           nino: Nino = Nino(validNino),
                           startDate: Option[String] = Some("2016-01-01"),
                           endDate: Option[String] = Some("2017-03-01"),
                           payments: Seq[Payment] = Seq(Payment("2016-01-28", 1000.55, 0), Payment("2016-02-28", 950.55, 0))) = {

    Employment(employerPayeReference, nino, startDate, endDate, payments)
  }

  private def requestCreateEmployment(request: String, reference: String = employerReferenceEncoded, nino: String = validNino) = {
    Http(s"$serviceUrl/employer/$reference/employment/$nino")
      .postData(Json.parse(request).toString())
      .headers(CONTENT_TYPE -> JSON
      ).asString
  }
}
