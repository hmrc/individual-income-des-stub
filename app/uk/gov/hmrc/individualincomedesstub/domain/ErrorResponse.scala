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

package uk.gov.hmrc.individualincomedesstub.domain

import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._

sealed abstract class ErrorResponse(val httpStatusCode: Int, val errorCode: String, val message: String) {

  def toHttpResponse: Result = Results.Status(httpStatusCode)(Json.toJson(this))
}

case class ErrorInvalidRequest(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)

class ValidationException(message: String) extends RuntimeException(message)

class DuplicateSelfAssessmentException extends RuntimeException

class RecordNotFoundException() extends RuntimeException

case object ErrorDuplicateAssessment
    extends ErrorResponse(CONFLICT, "SA_ALREADY_EXISTS", "A self-assessment record already exists for this individual")
