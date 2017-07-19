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

object JsonFormatters {
  implicit val hmrcPaymentFormat = Json.format[HmrcPayment]
  implicit val desPaymentFormat = Json.format[DesPayment]
  implicit val desAddressFormat = Json.format[DesAddress]
  implicit val employmentFormat = Json.format[Employment]

  implicit val testAddressFormat = Json.format[TestAddress]
  implicit val testOrganisationDetailsFormat = Json.format[TestOrganisationDetails]
  implicit val testOrganisationFormat = Json.format[TestOrganisation]

  implicit val createEmploymentRequestFormat = Json.format[CreateEmploymentRequest]
  implicit val employmentIncomeResponseFormat = Json.format[EmploymentIncomeResponse]

  implicit val errorInvalidRequestFormat = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }
}
