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

import play.api.http.HeaderNames
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.Employment

import scala.concurrent.Await.result
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.employmentFormat

import scalaj.http.Http

class EmploymentSpec extends BaseSpec {

  val employerReference = "123/DI45678"
  val employerReferenceEncoded = "123%2FDI45678"
  val validNino = "NA000799C"

  feature("Create employment") {

    scenario("Employment successfully created for given empRef and NINO") {

      Given("A valid create employment request")
      val request = createRequest

      When("I submit a POST request to create an employment")
      val response = requestCreateEmployment(request)

      Then("The response code should be 201 (Created)")
      response.code shouldBe 201

      And("The employment is returned in the response body")
      Json.parse(response.body) shouldBe Json.parse(createResponse)

      And("The employment is stored in mongo")
      val employment = Json.parse(response.body).as[Employment]
      val storedEmployment = result(employmentRepository.findByReferenceAndNino(employerReference, Nino(validNino)), timeout)
      storedEmployment shouldBe Seq(employment)
    }

    scenario("Request fails for invalid NINO") {
      Given("A valid create employment request")
      val request = createRequest

      When("I request to create an employment for an invalid NINO")
      val result = requestCreateEmployment(request, nino = "A12345C")

      Then("The response code should be 400 (Bad Request)")
      result.code shouldBe 400
      result.body shouldBe """{"code":"INVALID_REQUEST","message":"Malformed nino submitted"}"""
    }

    scenario("Request fails for unencoded employer reference") {
      Given("A valid create employment request")
      val request = createRequest

      When("I request to create an employment for an unencoded employer reference")
      val result = requestCreateEmployment(request, reference = employerReference)

      Then("The response code should be 404 (Not Found)")
      result.code shouldBe 404
    }
  }

  private def createResponse = {
    s"""
       |{
       |"employerPayeReference":"$employerReference",
       |"nino":"$validNino",
       |"startDate": "2016-01-01",
       |"endDate": "2017-03-01",
       |"payments": [
       |    {
       |        "paymentDate": "2016-01-28",
       |        "taxablePayment": 1000.55,
       |        "nonTaxablePayment": 0
       |    },
       |    {
       |        "paymentDate": "2016-02-28",
       |        "taxablePayment": 950.55,
       |        "nonTaxablePayment": 0
       |    }
       |]
       |}""".stripMargin.replaceAll("\n", "")
  }

  private def createRequest = {
    s"""
       |{
       |"startDate": "2016-01-01",
       |"endDate": "2017-03-01",
       |"payments": [
       |    {
       |        "paymentDate": "2016-01-28",
       |        "taxablePayment": 1000.55,
       |        "nonTaxablePayment": 0
       |    },
       |    {
       |        "paymentDate": "2016-02-28",
       |        "taxablePayment": 950.55,
       |        "nonTaxablePayment": 0
       |    }
       |]
       |}
     """.stripMargin.replaceAll("\n", "")
  }

  private def requestCreateEmployment(request: String, reference: String = employerReferenceEncoded, nino: String = validNino) = {
    Http(s"$serviceUrl/employer/$reference/employment/$nino")
      .postData(Json.parse(request).toString())
      .headers(HeaderNames.CONTENT_TYPE -> "application/json"
      ).asString
  }
}
