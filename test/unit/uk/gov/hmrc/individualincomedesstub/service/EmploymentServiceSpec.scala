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

import org.joda.time.LocalDate._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, Payment}
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository
import uk.gov.hmrc.individualincomedesstub.service.EmploymentService
import uk.gov.hmrc.play.test.UnitSpec

class EmploymentServiceSpec extends UnitSpec with MockitoSugar with ScalaFutures {

  trait Setup {
    val employerReference = "123/DI45678"
    val nino = Nino("NA000799C")
    val mockEmploymentRepository = mock[EmploymentRepository]
    val underTest = new EmploymentService(mockEmploymentRepository)
  }

  "Employment service" should {

    val request = aCreateEmploymentRequest

    "Return the created employment for a given empRef and Nino" in new Setup {

      val employment = anEmployment(employerReference, nino)

      when(mockEmploymentRepository.create(employerReference, nino, request)).thenReturn(employment)

      val result = await(underTest.create(employerReference, nino, request))

      result shouldBe employment
    }

    "Propagate exceptions when an Employment cannot be created" in new Setup {
      when(mockEmploymentRepository.create(employerReference, nino, request)).thenThrow(new RuntimeException("failed"))

      intercept[RuntimeException](await(underTest.create(employerReference, nino, request)))
    }
  }

  private def aCreateEmploymentRequest = CreateEmploymentRequest(
    parse("2016-01-01"),
    parse("2017-01-30"),
    Seq(Payment(parse("2016-01-28"), 1000.55, 0), Payment(parse("2016-02-28"), 1200.44, 0)))

  private def anEmployment(empRef: String, nino: Nino) = Employment(
    empRef, nino,
    parse("2016-01-01"),
    parse("2017-01-30"),
    Seq(Payment(parse("2016-01-28"), 1000.55, 0), Payment(parse("2016-02-28"), 1200.44, 0))
  )
}
