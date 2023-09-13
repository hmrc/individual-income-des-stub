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

import org.joda.time.DateTime
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._
import play.api.libs.json._

import scala.language.{implicitConversions, postfixOps}
import scala.util.{Failure, Try}

object JsonFormatters {
  implicit val dateFormatDefault: Format[DateTime] = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)
    override def writes(o: DateTime): JsValue = JodaDateTimeNumberWrites.writes(o)
  }
  implicit val hmrcPaymentFormat: OFormat[HmrcPayment] = Json.format[HmrcPayment]
  implicit val desEmploymentPayFrequencyJsonFormat: Format[DesEmploymentPayFrequency.Value] =
    EnumJson.enumFormat(DesEmploymentPayFrequency)
  implicit val desPaymentFormat: OFormat[DesPayment] = Json.format[DesPayment]
  implicit val desAddressFormat: OFormat[DesAddress] = Json.format[DesAddress]
  implicit val employmentPayFrequencyJsonFormat: Format[EmploymentPayFrequency.Value] =
    EnumJson.enumFormat(EmploymentPayFrequency)
  implicit val employmentFormat: OFormat[Employment] = Json.format[Employment]

  implicit val testAddressFormat: OFormat[TestAddress] = Json.format[TestAddress]
  implicit val testOrganisationDetailsFormat: OFormat[TestOrganisationDetails] = Json.format[TestOrganisationDetails]
  implicit val testOrganisationFormat: OFormat[TestOrganisation] = Json.format[TestOrganisation]
  implicit val testIndividualFormat: OFormat[TestIndividual] = Json.format[TestIndividual]

  implicit val createEmploymentRequestFormat: OFormat[CreateEmploymentRequest] = Json.format[CreateEmploymentRequest]
  implicit val employmentIncomeResponseFormat: OFormat[EmploymentIncomeResponse] = Json.format[EmploymentIncomeResponse]

  implicit val taxYearFormat: Format[TaxYear] = new Format[TaxYear] {
    override def reads(json: JsValue): JsResult[TaxYear] = JsSuccess(TaxYear(json.asInstanceOf[JsString].value))

    override def writes(taxYear: TaxYear): JsValue = JsString(taxYear.ty)
  }

  implicit val selfAssessmentTaxReturnFormat: OFormat[SelfAssessmentTaxReturn] = Json.format[SelfAssessmentTaxReturn]
  implicit val selfAssessmentFormat: OFormat[SelfAssessment] = Json.format[SelfAssessment]
  implicit val selfAssessmentCreateRequestFormat: OFormat[SelfAssessmentCreateRequest] =
    Json.format[SelfAssessmentCreateRequest]
  implicit val selfAssessmentResponseFormat: OFormat[SelfAssessmentResponse] = Json.format[SelfAssessmentResponse]

  implicit val errorInvalidRequestFormat: Format[ErrorInvalidRequest] = new Format[ErrorInvalidRequest] {
    def reads(json: JsValue): JsResult[ErrorInvalidRequest] = JsSuccess(
      ErrorInvalidRequest((json \ "message").as[String])
    )

    def writes(error: ErrorInvalidRequest): JsValue =
      Json.obj("code" -> error.errorCode, "message" -> error.message)
  }

  implicit val errorResponseWrites: Writes[ErrorResponse] = (e: ErrorResponse) =>
    Json.obj("code" -> e.errorCode, "message" -> e.message)
}

object EnumJson {

  private def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = {
    case JsString(s) =>
      Try(JsSuccess(enum.withName(s))) recoverWith {
        case _: NoSuchElementException => Failure(new InvalidEnumException(enum.getClass.getSimpleName, s))
      } get
    case _ => JsError("String value expected")
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = (v: E#Value) => JsString(v.toString)

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] =
    Format(enumReads(enum), enumWrites)
}

class InvalidEnumException(className: String, input: String)
    extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")
