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

package uk.gov.hmrc.individualincomedesstub.connector

import javax.inject.Singleton

import play.api.Logger
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualincomedesstub.WSHttp
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters._
import uk.gov.hmrc.individualincomedesstub.domain.TestOrganisation
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet, NotFoundException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ApiPlatformTestUserConnector extends ServicesConfig {

  val serviceUrl = baseUrl("api-platform-test-user")
  val http: HttpGet = WSHttp

  def getOrganisationByEmpRef(empRef: EmpRef)(implicit hc: HeaderCarrier): Future[Option[TestOrganisation]] = {
    http.GET[TestOrganisation](s"$serviceUrl/organisations/empref/${empRef.encodedValue}") map (Some(_))
  } recover {
    case e: NotFoundException =>
      Logger.warn(s"unable to retrieve employer with empRef: ${empRef.value}. ${e.getMessage}")
      None
  }
}