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

import javax.inject.Inject
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import play.api.libs.json._
import play.api.mvc.Results.{BadRequest, NotFound, Status}
import play.api.mvc.{ControllerComponents, Request, RequestHeader, Result}
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent
import uk.gov.hmrc.play.bootstrap.controller.BackendController
import uk.gov.hmrc.play.bootstrap.http.{ErrorResponse, JsonErrorHandler}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class CustomErrorHandler @Inject()(
    configuration: Configuration,
    auditConnector: AuditConnector,
    httpAuditEvent: HttpAuditEvent)(implicit ec: ExecutionContext)
    extends JsonErrorHandler(auditConnector,
                             httpAuditEvent,
                             configuration = configuration) {

  override def onClientError(request: RequestHeader,
                             statusCode: Int,
                             message: String): Future[Result] = {

    val newMessage = Try {
      Json.parse(message).\\("message").mkString(",").replaceAll("\"", "")
    } match {
      case Success(value) => value
      case Failure(e)     => "Invalid Request"
    }

    implicit val headerCarrier = HeaderCarrierConverter
      .fromHeadersAndSessionAndRequest(request.headers, request = Some(request))
    statusCode match {
      case NOT_FOUND =>
        Future.successful(
          NotFound(
            Json.toJson(
              ErrorResponse(NOT_FOUND,
                            "URI not found",
                            requested = Some(request.path)))))
      case BAD_REQUEST =>
        Future.successful(
          BadRequest(Json.toJson(ErrorResponse(BAD_REQUEST, newMessage))))
      case _ =>
        Future.successful(
          Status(statusCode)(
            Json.toJson(ErrorResponse(statusCode, newMessage))))
    }
  }

}

abstract class CommonController(controllerComponents: ControllerComponents)
    extends BackendController(controllerComponents) {

  override protected def withJsonBody[T](f: (T) => Future[Result])(
      implicit request: Request[JsValue],
      m: Manifest[T],
      reads: Reads[T]): Future[Result] = {
    Try(request.body.validate[T]) match {
      case Success(JsSuccess(payload, _)) => f(payload)
      case Success(JsError(errs)) =>
        Future.successful(
          ErrorInvalidRequest(s"${fieldName(errs)} is required").toHttpResponse)
      case Failure(e) if e.isInstanceOf[ValidationException] =>
        Future.successful(ErrorInvalidRequest(e.getMessage).toHttpResponse)
      case Failure(_) =>
        Future.successful(
          ErrorInvalidRequest("Unable to process request").toHttpResponse)
    }
  }

  private def fieldName[T](errs: Seq[(JsPath, Seq[JsonValidationError])]) = {
    errs.head._1.toString().substring(1)
  }

  private[controller] def recovery: PartialFunction[Throwable, Result] = {
    case e: IllegalArgumentException =>
      ErrorInvalidRequest(e.getMessage).toHttpResponse
    case _: DuplicateSelfAssessmentException =>
      ErrorDuplicateAssessment.toHttpResponse
  }
}
