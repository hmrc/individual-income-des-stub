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

import org.apache.pekko.stream.Materializer
import controllers.Assets
import play.api.Configuration
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.filters.cors.CORSActionBuilder
import uk.gov.hmrc.individualincomedesstub.views._
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DocumentationController @Inject() (configuration: Configuration, cc: ControllerComponents, assets: Assets)(
  implicit
  materializer: Materializer,
  executionContext: ExecutionContext
) extends BackendController(cc) {
  private lazy val whitelistedApplicationIds: Seq[String] =
    configuration.get[Seq[String]]("api.access.version-1.0.whitelistedApplicationIds")

  def definition(): Action[AnyContent] = Action {
    Ok(txt.definition(whitelistedApplicationIds))
      .withHeaders(CONTENT_TYPE -> JSON)
  }

  def yaml(version: String, file: String): Action[AnyContent] =
    CORSActionBuilder(configuration).async { implicit request =>
      assets.at(s"/public/api/conf/$version", file)(request)
    }
}
