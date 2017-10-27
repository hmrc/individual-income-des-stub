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

import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status._
import play.api.libs.json.Json.toJson
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.controller.SelfAssessmentController
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class SelfAssessmentControllerSpec extends UnitSpec with MockitoSugar with ScalaFutures with WithFakeApplication {

  implicit lazy val materializer = fakeApplication.materializer

  val taxYear = TaxYear("2014-15")
  val nino = Nino("AB123456A")

  trait Setup {
    val fakeRequest = FakeRequest()
    val selfAssessmentService = mock[SelfAssessmentService]
    val underTest = new SelfAssessmentController(selfAssessmentService)
  }

  "create self employment" should {
    "return a 201 (Created) response when self assessment employment data is created successfully" in new Setup {
      val request = SelfAssessmentCreateRequest(Seq.empty)
      val sa = SelfAssessment(nino, taxYear, Seq.empty)

      when(selfAssessmentService.create(nino, taxYear, request)).thenReturn(sa)

      val result = await(underTest.create(nino, taxYear)(fakeRequest.withBody(toJson(request))))

      status(result) shouldBe CREATED
      jsonBodyOf(result) shouldBe toJson(sa)
    }
  }
}
