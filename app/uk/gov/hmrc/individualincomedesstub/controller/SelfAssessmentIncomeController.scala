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

package uk.gov.hmrc.individualincomedesstub.controller

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import play.api.mvc.Action
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain.TaxYearInterval
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SelfAssessmentIncomeController @Inject() (selfAssessmentIncomeService: SelfAssessmentIncomeService) extends CommonController {

  def income(nino: Nino, taxYearInterval: TaxYearInterval) = Action.async { implicit request =>
    selfAssessmentIncomeService.income(nino, taxYearInterval) map { saReturns =>
      if(saReturns.nonEmpty) Ok(Json.toJson(saReturns))
      else NotFound
    } recover recovery
  }
}