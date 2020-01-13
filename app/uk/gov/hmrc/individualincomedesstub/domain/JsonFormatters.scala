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

package uk.gov.hmrc.individualincomedesstub.domain

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

import scala.util.{Failure, Try}

object JsonFormatters {
  implicit val dateFormatDefault = new Format[DateTime] {
    override def reads(json: JsValue): JsResult[DateTime] = JodaReads.DefaultJodaDateTimeReads.reads(json)
    override def writes(o: DateTime): JsValue = JodaDateTimeNumberWrites.writes(o)
  }
  implicit val hmrcPaymentFormat = Json.format[HmrcPayment]
  implicit val desEmploymentPayFrequencyJsonFormat = EnumJson.enumFormat(DesEmploymentPayFrequency)
  implicit val desPaymentFormat = Json.format[DesPayment]
  implicit val desAddressFormat = Json.format[DesAddress]
  implicit val employmentPayFrequencyJsonFormat = EnumJson.enumFormat(EmploymentPayFrequency)
  implicit val employmentFormat = Json.format[Employment]

  implicit val testAddressFormat = Json.format[TestAddress]
  implicit val testOrganisationDetailsFormat = Json.format[TestOrganisationDetails]
  implicit val testOrganisationFormat = Json.format[TestOrganisation]
  implicit val testIndividualFormat = Json.format[TestIndividual]

  implicit val createEmploymentRequestFormat = Json.format[CreateEmploymentRequest]
  implicit val employmentIncomeResponseFormat = Json.format[EmploymentIncomeResponse]

  implicit val taxYearFormat = new Format[TaxYear] {
    override def reads(json: JsValue): JsResult[TaxYear] = JsSuccess(TaxYear(json.asInstanceOf[JsString].value))

    override def writes(taxYear: TaxYear): JsValue = JsString(taxYear.ty)
  }

  implicit val selfAssessmentTaxReturnFormat = Json.format[SelfAssessmentTaxReturn]
  implicit val selfAssessmentFormat = Json.format[SelfAssessment]
  implicit val selfAssessmentCreateRequestFormat = Json.format[SelfAssessmentCreateRequest]
  implicit val selfAssessmentResponseFormat = Json.format[SelfAssessmentResponse]

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

object EnumJson {

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        Try(JsSuccess(enum.withName(s))) recoverWith {
          case _: NoSuchElementException => Failure(new InvalidEnumException(enum.getClass.getSimpleName, s))
        } get
      case _ => JsError("String value expected")
    }
  }

  implicit def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumReads(enum), enumWrites)
  }

}

class InvalidEnumException(className: String, input:String) extends RuntimeException(s"Enumeration expected of type: '$className', but it does not contain '$input'")
