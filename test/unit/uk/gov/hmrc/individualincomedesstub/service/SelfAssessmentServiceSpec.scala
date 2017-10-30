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

package unit.uk.gov.hmrc.individualincomedesstub.service

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentService
import uk.gov.hmrc.play.test.UnitSpec

class SelfAssessmentServiceSpec extends UnitSpec with MockitoSugar {

  val taxYear = TaxYear("2014-15")
  val nino = Nino("AB123456A")

  trait Setup {
    val request = SelfAssessmentCreateRequest(Seq.empty)
    val sa = SelfAssessment(nino, taxYear, Seq.empty)
    val repository = mock[SelfAssessmentRepository]
    val underTest = new SelfAssessmentService(repository)
  }

  "create" should {
    "return the created self assessment" in new Setup {
      when(repository.create(sa)).thenReturn(sa)

      val result = await(underTest.create(nino, taxYear, request))

      result shouldBe sa
    }

    "propagate exceptions when a self assessment cannot be created" in new Setup {
      when(repository.create(sa)).thenThrow(new RuntimeException("failed"))
      intercept[RuntimeException](await(underTest.create(nino, taxYear, request)))
    }
  }
}
