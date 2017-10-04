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
import org.mockito.Matchers._
import org.mockito.Mockito.{reset, _}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpecLike}
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository
import uk.gov.hmrc.individualincomedesstub.service.EmploymentIncomeService
import uk.gov.hmrc.individualincomedesstub.util.Dates.toInterval

import scala.concurrent.Future
import scala.concurrent.Future.successful
import scala.language.postfixOps
import uk.gov.hmrc.http.HeaderCarrier

class EmploymentIncomeServiceSpec extends WordSpecWithFutures with Matchers with MockitoSugar with BeforeAndAfterEach {

  private val nino = Nino("AB123456C")
  private val employmentRepository = mock[EmploymentRepository]
  private val apiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
  private val employmentIncomeService = new EmploymentIncomeService(employmentRepository, apiPlatformTestUserConnector)
  implicit val hc = HeaderCarrier()

  override protected def beforeEach() = reset(employmentRepository, apiPlatformTestUserConnector)

  "Employment income service employments function" should {

    def mockEmploymentRepositoryFindByNino(nino: Nino, eventualEmployments: Future[Seq[Employment]]) =
      when(employmentRepository.findBy(nino)).thenReturn(eventualEmployments)

    def mockTestUserConnectorGetOrganisationByEmpRef(eventualOrganisation: Future[Option[TestOrganisation]]) =
      when(apiPlatformTestUserConnector.getOrganisationByEmpRef(any[EmpRef])(any[HeaderCarrier])).thenReturn(eventualOrganisation)

    "return an empty sequence when a corresponding employment does not exist" in {
      mockEmploymentRepositoryFindByNino(nino, successful(Seq.empty))
      await(employmentIncomeService.employments(nino, toInterval(parse("2017-01-01"), parse("2017-06-30")))).isEmpty shouldBe true
    }

    "return a populated filtered sequence when corresponding employments with payments exist" in new TableDrivenPropertyChecks {
      def incomeResponse(employment: Employment) = {
        new EmploymentIncomeResponse(None, None, Some(employment.employerPayeReference.taxOfficeNumber),
          Some(employment.employerPayeReference.taxOfficeReference), employment.startDate.map(parse),
          employment.endDate.map(parse), None, employment.payments.map(DesPayment(_)))
      }

      def payment(paymentDate: String) = HmrcPayment(paymentDate, 123.45)

      val employmentWithPaymentAtEndOfMar = Employment(EmpRef("101", "AB10001"), nino, None, None, Seq(payment("2017-03-31")))
      val employmentWithPaymentAtEndOfJun = Employment(EmpRef("102", "AB10002"), nino, None, None, Seq(payment("2017-06-30")))
      val employmentWithPaymentAtEndOfSep = Employment(EmpRef("103", "AB10003"), nino, None, None, Seq(payment("2017-09-30")))
      val employmentWithPaymentAtEndOfDec = Employment(EmpRef("104", "AB10004"), nino, None, None, Seq(payment("2017-12-31")))
      val employments = Seq(employmentWithPaymentAtEndOfMar, employmentWithPaymentAtEndOfJun, employmentWithPaymentAtEndOfSep, employmentWithPaymentAtEndOfDec)

      mockEmploymentRepositoryFindByNino(nino, successful(employments))
      mockTestUserConnectorGetOrganisationByEmpRef(Future.successful(None))

      val fixtures = Table(
        ("interval", "employments"),
        (toInterval(parse("2016-01-01"), parse("2016-12-31")), Seq.empty),
        (toInterval(parse("2017-01-01"), parse("2017-03-31")), Seq(employmentWithPaymentAtEndOfMar)),
        (toInterval(parse("2017-01-01"), parse("2017-06-30")), Seq(employmentWithPaymentAtEndOfMar, employmentWithPaymentAtEndOfJun)),
        (toInterval(parse("2017-01-01"), parse("2017-09-30")), Seq(employmentWithPaymentAtEndOfMar, employmentWithPaymentAtEndOfJun, employmentWithPaymentAtEndOfSep)),
        (toInterval(parse("2017-01-01"), parse("2017-12-31")), Seq(employmentWithPaymentAtEndOfMar, employmentWithPaymentAtEndOfJun, employmentWithPaymentAtEndOfSep, employmentWithPaymentAtEndOfDec)),
        (toInterval(parse("2018-01-01"), parse("2018-12-31")), Seq.empty)
      )

      forAll(fixtures) { (exampleInterval, expectedResult) =>
        await(employmentIncomeService.employments(nino, exampleInterval)) shouldBe (expectedResult map (incomeResponse(_)))
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
