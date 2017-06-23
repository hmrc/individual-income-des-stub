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

import javax.inject.{Singleton, Inject}

import play.api.libs.json.Json
import play.api.mvc.{Result, Action}
import uk.gov.hmrc.individualincomedesstub.domain.ErrorInternalServer
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.service.EmployerService
import uk.gov.hmrc.play.microservice.controller.BaseController
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EmployerController @Inject()(employerService: EmployerService) extends BaseController {

  def createEmployer() = Action.async {
    employerService.createEmployer() map { employer =>
      Created(Json.toJson(employer))
    } recover recovery
  }

  private def recovery: PartialFunction[Throwable, Result] = {
    case _ => ErrorInternalServer.toHttpResponse
  }
}
