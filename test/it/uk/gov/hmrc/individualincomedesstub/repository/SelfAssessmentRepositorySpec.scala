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

package it.uk.gov.hmrc.individualincomedesstub.repository

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.api.indexes.IndexType.Ascending
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class SelfAssessmentRepositorySpec  extends UnitSpec with WithFakeApplication with MongoSpecSupport with BeforeAndAfterEach {

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .bindings(bindModules: _*)
    .build()

  val repository = fakeApplication.injector.instanceOf[SelfAssessmentRepository]

  val utr = SaUtr("2432552635")
  val selfAssessment = SelfAssessment(utr, LocalDate.parse("2014-01-01"), Seq(
    SelfAssessmentTaxReturn(TaxYear("2014-15"), parse("2015-01-01"), 13567.77, 1233.33, 22345, 500.25, 200.35, 12.45, 21.23, 52.34, 6.34, 55.24, 4.34, 5.43, 5.32, 134.56)))

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
    await(repository.drop)
  }

  "collection" should {
    "have a unique index on saUtr" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name.contains("saUtrIndex") &&
          i.key == Seq("saUtr" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a self assessment" in {
      val result = await(repository.create(selfAssessment))

      result shouldBe selfAssessment
    }

    "fail to create a duplicate self assessment" in {
      await(repository.create(selfAssessment))

      intercept[DuplicateSelfAssessmentException](await(repository.create(selfAssessment)))
    }
  }

  "find by utr" should {
    "return None when there are no self assessments for a given utr" in {
      await(repository.findByUtr(utr)) shouldBe None
    }

    "return the self assessment" in {
      await(repository.create(selfAssessment))

      val result = await(repository.findByUtr(utr))

      result shouldBe Some(selfAssessment)
    }
  }
}
