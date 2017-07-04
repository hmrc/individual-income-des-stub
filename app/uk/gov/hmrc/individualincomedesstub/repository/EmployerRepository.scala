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
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.ReadPreference
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualincomedesstub.domain.{Employer, JsonFormatters}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class EmployerRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)
  extends ReactiveRepository[Employer, BSONObjectID]("employer", reactiveMongoComponent.mongoConnector.db,
    JsonFormatters.employerJsonFormat, Json.format[BSONObjectID]) {

  override lazy val indexes = Seq(
    Index(Seq(("payeReference", Ascending)), Some("payeReferenceIndex"), background = true, unique = true)
  )

  def createEmployer(employer: Employer): Future[Employer] = insert(employer) map (_ => employer)

  def findByPayeReference(payeReference: EmpRef): Future[Option[Employer]] = collection.find(Json.obj("payeReference" -> payeReference.value)).one[Employer]

  def findBy(empRefs: Set[EmpRef]): Future[Set[Employer]] = {
    val empRefsAsStrings = empRefs map (_.value)
    val payeReferenceSelector = Json.obj("payeReference" -> BSONDocument("$in" -> empRefsAsStrings))
    collection.find(payeReferenceSelector).cursor[Employer](ReadPreference.primary).collect[Set]()
  }

}
