/*
 * Copyright 2020 HM Revenue & Customs
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
import org.joda.time.Interval
import play.api.libs.json.Json.{obj, toJson}
import play.api.mvc.ControllerComponents
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.employmentIncomeResponseFormat
import uk.gov.hmrc.individualincomedesstub.service.EmploymentIncomeService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EmploymentIncomeController @Inject()(
    employmentIncomeService: EmploymentIncomeService,
    controllerComponents: ControllerComponents)
    extends CommonController(controllerComponents) {

  def employments(nino: Nino, interval: Interval) = Action.async {
    implicit request =>
      employmentIncomeService.employments(nino, interval) map {
        employmentIncomeResponses =>
          if (employmentIncomeResponses.nonEmpty)
            Ok(obj("employments" -> toJson(employmentIncomeResponses)))
          else NotFound
      } recover recovery
  }

}
