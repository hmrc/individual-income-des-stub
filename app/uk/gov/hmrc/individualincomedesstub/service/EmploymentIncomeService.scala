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

package uk.gov.hmrc.individualincomedesstub.service

import org.joda.time.Interval
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.individualincomedesstub.connector.ApiPlatformTestUserConnector
import uk.gov.hmrc.individualincomedesstub.domain.Employment.overlap
import uk.gov.hmrc.individualincomedesstub.domain.{
  EmploymentIncomeResponse,
  TestOrganisation
}
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmploymentIncomeService @Inject()(
    employmentRepository: EmploymentRepository,
    apiPlatformTestUserConnector: ApiPlatformTestUserConnector)(
    implicit ec: ExecutionContext) {

  private def getEmployers(empRefs: Seq[EmpRef])(
      implicit hc: HeaderCarrier): Future[Seq[TestOrganisation]] = {
    val futures =
      empRefs.map(apiPlatformTestUserConnector.getOrganisationByEmpRef)
    Future.sequence(futures).map(_.flatten.toSeq)
  }

  def employments(nino: Nino, interval: Interval)(
      implicit hc: HeaderCarrier): Future[Seq[EmploymentIncomeResponse]] =
    for {
      employments <- employmentRepository.findBy(nino) map (_ filter overlap(
        interval))
      employerPayeReferences = employments map (_.employerPayeReference)
      employers <- getEmployers(employerPayeReferences)
      maybeEmployers = employments map (employment =>
        employers find (_.empRef.exists(_ == employment.employerPayeReference)))
    } yield
      employments zip maybeEmployers map {
        case (employment, employer) =>
          val paymentsWithinInterval =
            employment.payments.filter(_.isPaidWithin(interval))
          EmploymentIncomeResponse(
            employment.copy(payments = paymentsWithinInterval),
            employer)
      }
}
