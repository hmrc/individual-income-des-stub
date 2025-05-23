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

package unit.uk.gov.hmrc.individualincomedesstub.service

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, EmploymentPayFrequency, HmrcPayment}
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository
import uk.gov.hmrc.individualincomedesstub.service.EmploymentService
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

import scala.concurrent.Future.successful

class EmploymentServiceSpec extends TestSupport with MockitoSugar {

  trait Setup {
    val employerReference: EmpRef = EmpRef("123", "DI45678")
    val nino: Nino = Nino("NA000799C")
    val mockEmploymentRepository: EmploymentRepository =
      mock[EmploymentRepository]
    val underTest = new EmploymentService(mockEmploymentRepository)
  }

  private val aCreateEmploymentRequest = CreateEmploymentRequest(
    Some("2016-01-01"),
    Some("2017-01-30"),
    Seq(
      HmrcPayment("2016-01-28", 1000.55, monthPayNumber = Some(10)),
      HmrcPayment("2016-02-28", 1200.44, monthPayNumber = Some(10))
    ),
    None,
    None,
    Some(EmploymentPayFrequency.CALENDAR_MONTHLY.toString)
  )

  "Employment service" should {
    val request = aCreateEmploymentRequest

    "Return the created employment for a given empRef and Nino" in new Setup {
      private val employment = anEmployment(employerReference, nino)

      when(mockEmploymentRepository.create(employerReference, nino, request))
        .thenReturn(successful(employment))

      private val result =
        await(underTest.create(employerReference, nino, request))

      result shouldBe employment
    }

    "Propagate exceptions when an Employment cannot be created" in new Setup {
      when(mockEmploymentRepository.create(employerReference, nino, request))
        .thenThrow(new RuntimeException("failed"))

      intercept[RuntimeException](await(underTest.create(employerReference, nino, request)))
    }
  }

  private def anEmployment(empRef: EmpRef, nino: Nino) = Employment(
    empRef,
    nino,
    Some("2016-01-01"),
    Some("2017-01-30"),
    Seq(
      HmrcPayment("2016-01-28", 1000.55, monthPayNumber = Some(10)),
      HmrcPayment("2016-02-28", 1200.44, monthPayNumber = Some(10))
    ),
    None,
    None,
    Some(EmploymentPayFrequency.CALENDAR_MONTHLY.toString)
  )
}
