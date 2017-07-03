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

import org.joda.time.LocalDate.parse
import org.mockito.Mockito.{reset, _}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.Employment
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository
import uk.gov.hmrc.individualincomedesstub.service.EmploymentIncomeService
import uk.gov.hmrc.individualincomedesstub.util.Dates.toInterval

import scala.concurrent.Future
import scala.concurrent.Future.successful

class EmploymentIncomeServiceSpec extends WordSpecWithFutures with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val nino = Nino("AB123456C")
  private val employmentRepository = mock[EmploymentRepository]
  private val employmentIncomeService = new EmploymentIncomeService(employmentRepository)

  override protected def beforeEach() = reset(employmentRepository)

  "Employment income service employments function" should {

    def mockEmploymentRepositoryFindByNino(nino: Nino, eventualEmployments: Future[Seq[Employment]]) =
      when(employmentRepository.findBy(nino)).thenReturn(eventualEmployments)

    "return an empty sequence when a corresponding employment does not exist" in {
      mockEmploymentRepositoryFindByNino(nino, successful(Seq.empty))
      await(employmentIncomeService.employments(nino, toInterval(parse("2017-01-01"), parse("2017-06-30")))).isEmpty shouldBe true
    }

    "return a populated filtered sequence when corresponding employments exist" in new TableDrivenPropertyChecks {
      val janToMarEmployment = Employment(EmpRef("101", "AB10001"), nino, Option("2017-01-01"), Option("2017-03-31"), Seq.empty)
      val aprToJunEmployment = Employment(EmpRef("102", "AB10002"), nino, Option("2017-04-01"), Option("2017-06-30"), Seq.empty)
      val julToSepEmployment = Employment(EmpRef("103", "AB10003"), nino, Option("2017-07-01"), Option("2017-09-30"), Seq.empty)
      val octToDecEmployment = Employment(EmpRef("104", "AB10004"), nino, Option("2017-10-01"), Option("2017-12-31"), Seq.empty)
      val employments = Seq(janToMarEmployment, aprToJunEmployment, julToSepEmployment, octToDecEmployment)

      mockEmploymentRepositoryFindByNino(nino, successful(employments))

      val fixtures = Table(
        ("interval", "employments"),
        (toInterval(parse("2016-01-01"), parse("2016-12-31")), Seq.empty),
        (toInterval(parse("2017-01-01"), parse("2017-03-31")), Seq(janToMarEmployment)),
        (toInterval(parse("2017-01-01"), parse("2017-06-30")), Seq(janToMarEmployment, aprToJunEmployment)),
        (toInterval(parse("2017-01-01"), parse("2017-09-30")), Seq(janToMarEmployment, aprToJunEmployment, julToSepEmployment)),
        (toInterval(parse("2017-01-01"), parse("2017-12-31")), Seq(janToMarEmployment, aprToJunEmployment, julToSepEmployment, octToDecEmployment)),
        (toInterval(parse("2018-01-01"), parse("2018-12-31")), Seq.empty)
      )

      forAll(fixtures) { (exampleInterval, expectedResult) =>
        await(employmentIncomeService.employments(nino, exampleInterval)) shouldBe expectedResult
      }

    }

  }

}

trait WordSpecWithFutures extends WordSpecLike {

  import scala.concurrent.Await.result
  import scala.concurrent.Future
  import scala.concurrent.duration._

  def await[A](future: Future[A])(implicit timeout: Duration = 5 seconds) = result(future, timeout)

}
