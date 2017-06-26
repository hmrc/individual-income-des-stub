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

package uk.gov.hmrc.individualincomedesstub.domain

import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._

sealed abstract class ErrorResponse(
                                     val httpStatusCode: Int,
                                     val errorCode: String,
                                     val message: String) {

  def toHttpResponse = Results.Status(httpStatusCode)(Json.toJson(this))
}

case class ErrorInvalidRequestPayload(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)
case class ErrorInvalidPathParameter(errorMessage: String) extends ErrorResponse(BAD_REQUEST, "INVALID_REQUEST", errorMessage)
class ValidationException(message: String) extends RuntimeException(message)
class InvalidNinoException extends RuntimeException

case object ErrorInternalServer extends ErrorResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error")

