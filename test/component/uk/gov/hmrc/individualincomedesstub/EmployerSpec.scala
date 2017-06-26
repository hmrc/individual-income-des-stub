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
import play.api.libs.json.Json.{obj, stringify}
import play.api.test.Helpers._
import uk.gov.hmrc.individualincomedesstub.domain.Employer
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._

import scala.concurrent.Await._
import scalaj.http.Http


class EmployerSpec extends BaseSpec {

  scenario("Create an employer") {

    When("I call POST /employer")
    val response = Http(s"$serviceUrl/employer")
      .postData(stringify(obj()))
      .headers(HeaderNames.CONTENT_TYPE -> "application/json")
      .asString

    Then("The response status should be 201 (Created) with the employer in the body")
    response.code shouldBe CREATED
    val employer = Json.parse(response.body).as[Employer]

    And("The employer is stored in mongo")
    val storedEmployer = result(employerRepository.findByPayeReference(employer.payeReference), timeout)
    storedEmployer shouldBe Some(employer)
  }
}
