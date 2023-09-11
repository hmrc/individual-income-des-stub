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

import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.mongodb.scala.model.Filters._
import play.api.libs.json.Format

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, JsonFormatters}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmploymentRepository @Inject()(mongoComponent: MongoComponent)
    extends PlayMongoRepository[Employment](
      collectionName = "employment",
      mongoComponent = mongoComponent,
      domainFormat = JsonFormatters.employmentFormat,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("nino", "employerPayeReference"),
          IndexOptions().name("ninoAndEmployerPayeReference").unique(false).background(true)
        )
      ),
      extraCodecs = Seq(
        Codecs.playFormatCodec(Format(EmpRef.empRefRead, EmpRef.empRefWrite)),
        Codecs.playFormatCodec(Format(Nino.ninoRead, Nino.ninoWrite))
      )
    ) {

  def create(employerPayeReference: EmpRef, nino: Nino, request: CreateEmploymentRequest): Future[Employment] = {
    val employment = Employment(
      employerPayeReference,
      nino,
      request.startDate,
      request.endDate,
      request.payments,
      request.employeeAddress,
      request.payrollId,
      request.payFrequency
    )
    collection.insertOne(employment).toFuture().map(_ => employment)
  }

  def findByReferenceAndNino(employerPayeReference: EmpRef, nino: Nino): Future[Seq[Employment]] =
    collection
      .find(
        and(
          equal("employerPayeReference", employerPayeReference),
          equal("nino", nino)
        )
      )
      .toFuture()

  def findBy(nino: Nino): Future[Seq[Employment]] =
    collection.find(equal("nino", nino)).toFuture()
}
