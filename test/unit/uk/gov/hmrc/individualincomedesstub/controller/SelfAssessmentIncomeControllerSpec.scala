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

package unit.uk.gov.hmrc.individualincomedesstub.controller

import org.joda.time.LocalDate.parse
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json.toJson
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.controller.SelfAssessmentIncomeController
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.selfAssessmentResponseFormat
import uk.gov.hmrc.individualincomedesstub.domain.{SelfAssessmentResponse, SelfAssessmentResponseReturnData}
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future.successful

class SelfAssessmentIncomeControllerSpec extends UnitSpec with MockitoSugar with ScalaFutures with WithFakeApplication {

  implicit lazy val materializer = fakeApplication.materializer

  trait Setup {
    val nino = Nino("AB123456A")
    val fakeRequest = FakeRequest()
    val selfAssessmentIncomeService = mock[SelfAssessmentIncomeService]
    val underTest = new SelfAssessmentIncomeController(selfAssessmentIncomeService)
  }

  "fetch self assessment income" should {
    "retrieve self assessment income for a given period" in new Setup {
      val selfAssessmentResponse = SelfAssessmentResponse("2015", Seq(SelfAssessmentResponseReturnData(Some(parse("2014-01-01")), parse("2015-01-01"), 100.15, 12300.55)))

      when(selfAssessmentIncomeService.income(nino, 2015, 2016)).thenReturn(successful(Seq(selfAssessmentResponse)))

      val result = await(underTest.income(nino, 2015, 2016)(fakeRequest))

      status(result) shouldBe OK
      jsonBodyOf(result) shouldBe toJson(Seq(selfAssessmentResponse))
    }

    "return 404 (Not Found) if there is no self assessment income for a given period" in new Setup {
      when(selfAssessmentIncomeService.income(nino, 2015, 2016)).thenReturn(successful(Seq.empty))

      val result = await(underTest.income(nino, 2015, 2016)(fakeRequest))

      status(result) shouldBe NOT_FOUND
    }
  }
}
