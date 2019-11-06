/*
 * Copyright 2019 HM Revenue & Customs
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
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, JsonFormatters}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EmploymentRepository @Inject() (mongoConnectionProvider: MongoConnectionProvider)
  extends ReactiveRepository[Employment, BSONObjectID]("employment", mongoConnectionProvider.mongoDatabase, JsonFormatters.employmentFormat) {

  override lazy val indexes = Seq(
    Index(key = Seq(("nino", Ascending), ("employerPayeReference", Ascending)), name = Some("ninoAndEmployerPayeReference"), unique = false, background = true)
  )

  def create(employerPayeReference: EmpRef, nino: Nino, request: CreateEmploymentRequest) = {
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

    insert(employment) map (_ => employment)
  }

  def findByReferenceAndNino(employerPayeReference: EmpRef, nino: Nino) = {
    collection.find(Json.obj("employerPayeReference" -> employerPayeReference, "nino" -> nino)).cursor[Employment]().collect[List]()
  }

  def findBy(nino: Nino) = collection.find(Json.obj("nino" -> nino)).cursor[Employment]().collect[Seq]()
}
