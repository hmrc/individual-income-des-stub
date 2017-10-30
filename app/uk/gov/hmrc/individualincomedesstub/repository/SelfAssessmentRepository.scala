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

package uk.gov.hmrc.individualincomedesstub.repository

import javax.inject.{Inject, Singleton}

import play.api.libs.json.Json
import reactivemongo.play.json._
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.TaxYear
import uk.gov.hmrc.individualincomedesstub.domain.JsonFormatters.taxYearFormat
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.mongo.ReactiveRepository
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class SelfAssessmentRepository @Inject()(mongoConnectionProvider: MongoConnectionProvider)
  extends ReactiveRepository[SelfAssessment, BSONObjectID]("selfAssessment", mongoConnectionProvider.mongoDatabase, JsonFormatters.selfAssessmentFormat) {

  override lazy val indexes = Seq(
    Index(key = Seq(("nino", Ascending), ("taxYear", Ascending)), name = Some("ninoAndTaxYearIndex"), unique = true, background = true)
  )

  def create(selfAssessment: SelfAssessment) = {
    insert(selfAssessment) map (_ => selfAssessment) recover {
      case WriteResult.Code(11000) => throw new DuplicateSelfAssessmentException
    }
  }

  def findByNinoAndTaxYear(nino: Nino, taxYear: TaxYear) = collection.find(Json.obj("nino" -> nino, "taxYear" -> taxYear)).cursor[SelfAssessment]().collect[Seq]()
}
