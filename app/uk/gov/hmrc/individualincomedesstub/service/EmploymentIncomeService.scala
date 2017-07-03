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

package uk.gov.hmrc.individualincomedesstub.service

import javax.inject.{Inject, Singleton}

import org.joda.time.Interval
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.Employment
import uk.gov.hmrc.individualincomedesstub.domain.Employment.overlap
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmploymentIncomeService @Inject()(employmentRepository: EmploymentRepository) {

  def employments(nino: Nino, interval: Interval): Future[Seq[Employment]] =
    employmentRepository.findBy(nino) map { employments => employments filter overlap(interval) }

}