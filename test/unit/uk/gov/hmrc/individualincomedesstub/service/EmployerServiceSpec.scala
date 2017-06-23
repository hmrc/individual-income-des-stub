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

import org.mockito.Mockito.verify
import org.mockito.Matchers.any
import org.mockito.invocation.InvocationOnMock
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.individualincomedesstub.repository.EmployerRepository
import uk.gov.hmrc.individualincomedesstub.service.EmployerService
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.BDDMockito.given
import org.mockito.stubbing.Answer
import scala.concurrent.Future
import scala.concurrent.Future.failed

class EmployerServiceSpec extends UnitSpec with MockitoSugar {

  trait Setup {
    val mockedRepository = mock[EmployerRepository]
    val underTest = new EmployerService(mockedRepository)

    given(mockedRepository.createEmployer(any())).willAnswer(returnSame)
  }

  "createEmployer" should {

    "generate an employer and save it in the database" in new Setup {
      val employer = await(underTest.createEmployer())

      verify(mockedRepository).createEmployer(employer)
    }

    "return a different employer every time" in new Setup {
      val employer1 = await(underTest.createEmployer())
      val employer2 = await(underTest.createEmployer())

      employer1 should not be employer2
    }

    "fail when the repository fails" in new Setup {
      given(mockedRepository.createEmployer(any())).willReturn(failed(new RuntimeException("test error")))

      intercept[RuntimeException]{await(underTest.createEmployer())}
    }
  }

  private def returnSame[T] = new Answer[Future[T]] {
    override def answer(invocationOnMock: InvocationOnMock): Future[T] = {
      Future.successful(invocationOnMock.getArguments()(0).asInstanceOf[T])
    }
  }
}
