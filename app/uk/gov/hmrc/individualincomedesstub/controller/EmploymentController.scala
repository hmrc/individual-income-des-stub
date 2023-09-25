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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.CreateEmploymentRequest
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.service.EmploymentService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class EmploymentController @Inject()(employmentService: EmploymentService, controllerComponents: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends CommonController(controllerComponents) {

  def create(employerReference: EmpRef, nino: Nino): Action[JsValue] =
    Action.async(parse.json) { implicit request =>
      withJsonBody[CreateEmploymentRequest] { createEmployment =>
        employmentService.create(employerReference, nino, createEmployment) map (e => Created(Json.toJson(e)))
      } recover recovery
    }
}
