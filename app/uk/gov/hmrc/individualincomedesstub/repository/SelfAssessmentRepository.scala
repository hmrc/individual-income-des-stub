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

package uk.gov.hmrc.individualincomedesstub.repository

import com.mongodb.MongoWriteException
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelfAssessmentRepository @Inject()(mongo: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[SelfAssessment](
      mongoComponent = mongo,
      collectionName = "selfAssessment",
      domainFormat = JsonFormatters.selfAssessmentFormat,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("saUtr"),
          IndexOptions().name("saUtrIndex").unique(true).background(true)
        )
      )
    ) {

  def create(selfAssessment: SelfAssessment): Future[SelfAssessment] =
    collection
      .insertOne(selfAssessment)
      .recover {
        case _: MongoWriteException =>
          throw new DuplicateSelfAssessmentException
      }
      .head
      .map(_ => selfAssessment)

  def findByUtr(saUtr: SaUtr): Future[Option[SelfAssessment]] =
    collection.find(equal("saUtr", saUtr.value)).headOption()
}
