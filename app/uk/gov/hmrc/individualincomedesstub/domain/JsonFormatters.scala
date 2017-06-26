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

import play.api.libs.json._
import uk.gov.hmrc.domain.{SimpleObjectReads, SimpleObjectWrites}

object JsonFormatters {
  implicit val addressJsonFormat = Json.format[Address]
  implicit val employerJsonFormat = Json.format[Employer]
  implicit val paymentFormat = Json.format[Payment]
  implicit val employmentFormat = Json.format[Employment]

  implicit val createEmploymentRequestFormat = Json.format[CreateEmploymentRequest]

  implicit val errorInvalidRequestParameterFormat = new Format[ErrorInvalidPathParameter] {
    def reads(json: JsValue): JsResult[ErrorInvalidPathParameter] = JsSuccess(
      ErrorInvalidPathParameter((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidPathParameter): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val employerReferenceWrite: Writes[EmployerReference] = new SimpleObjectWrites[EmployerReference](_.value)
  implicit val employerReferenceRead: Reads[EmployerReference] = new SimpleObjectReads[EmployerReference]("empRef", EmployerReference.apply)

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}
