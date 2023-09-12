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

package uk.gov.hmrc.individualincomedesstub.controller

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain.RecordNotFoundException
import uk.gov.hmrc.individualincomedesstub.service.SelfAssessmentIncomeService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SelfAssessmentIncomeController @Inject()(
  selfAssessmentIncomeService: SelfAssessmentIncomeService,
  controllerComponents: ControllerComponents)(implicit ec: ExecutionContext)
    extends CommonController(controllerComponents) {

  def income(nino: Nino, startYear: Int, endYear: Int): Action[AnyContent] = Action.async { implicit request =>
    selfAssessmentIncomeService.income(nino, startYear, endYear) map { saReturns =>
      Ok(Json.toJson(saReturns))
    } recover {
      case _: RecordNotFoundException => NotFound
    }
  }
}
