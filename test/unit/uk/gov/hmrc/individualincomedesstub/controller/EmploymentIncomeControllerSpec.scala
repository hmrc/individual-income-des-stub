/*
 * Copyright 2018 HM Revenue & Customs
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

package unit.uk.gov.hmrc.individualincomedesstub.controller

import org.joda.time.LocalDate.parse
import org.joda.time.{Interval, LocalDate}
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json.toJson
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.controller.EmploymentIncomeController
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.employmentIncomeResponseFormat
import uk.gov.hmrc.individualincomedesstub.domain.{Employment, EmploymentIncomeResponse}
import uk.gov.hmrc.individualincomedesstub.service.EmploymentIncomeService

import scala.concurrent.Future
import scala.concurrent.Future.successful
import uk.gov.hmrc.http.HeaderCarrier

class EmploymentIncomeControllerSpec extends PlaySpec with Results with MockitoSugar {

  private val employmentIncomeService = mock[EmploymentIncomeService]
  private val employmentIncomeController = new EmploymentIncomeController(employmentIncomeService)
  private implicit val hc = HeaderCarrier()

  "Employment income controller employment function" should {

    val nino = Nino("AB123456C")
    val interval = toInterval(parse("2017-01-01"), parse("2017-06-30"))

    def mockEmploymentIncomeService(eventualEmploymentResponses: Future[Seq[EmploymentIncomeResponse]]) =
      when(employmentIncomeService.employments(Matchers.eq(nino), any(classOf[Interval]))(any[HeaderCarrier])).thenReturn(eventualEmploymentResponses)

    def asEmploymentResponse(employment: Employment) = EmploymentIncomeResponse(employment, None)

    "return a http 404 (Not Found) response when service does not return any employments" in {
      mockEmploymentIncomeService(successful(Seq.empty))
      val eventualResult = employmentIncomeController.employments(nino, interval)(FakeRequest())
      status(eventualResult) mustBe NOT_FOUND
    }

    "return a http 200 (Ok) response with populated employment array when service returns employments" in {
      val employment1 = Employment(EmpRef("101", "AB10001"), nino, Option("2017-01-01"), Option("2017-03-31"), Seq.empty)
      val employment2 = Employment(EmpRef("102", "AB10002"), nino, Option("2017-04-01"), Option("2017-06-30"), Seq.empty)
      val employment3 = Employment(EmpRef("103", "AB10003"), nino, Option("2017-07-01"), Option("2017-09-30"), Seq.empty)

      val employments = Seq(employment1, employment2, employment3)
      val employmentResponses = employments map (EmploymentIncomeResponse(_, None))
      mockEmploymentIncomeService(successful(employmentResponses))

      val eventualResult = employmentIncomeController.employments(nino, interval)(FakeRequest())
      status(eventualResult) mustBe OK
      contentAsString(eventualResult) mustBe s"""{"employments":[${toJson(asEmploymentResponse(employment1))},${toJson(asEmploymentResponse(employment2))},${toJson(asEmploymentResponse(employment3))}]}"""
    }

  }

  private def toInterval(from: LocalDate, to: LocalDate): Interval =
    new Interval(from.toDate.getTime, to.toDate.getTime)

}
