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
import uk.gov.hmrc.domain.Nino
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

  val taxYear = TaxYear("2014-15")
  val nino = Nino("AB123456A")

  override def beforeEach() {
    await(repository.drop)
    await(repository.ensureIndexes)
  }

  override def afterEach() {
    await(repository.drop)
  }

  "collection" should {
    "have a unique compound index on nino and taxYear" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name == Some("ninoAndTaxYearIndex") &&
          i.key == Seq("nino" -> Ascending, "taxYear" -> Ascending) &&
          i.background &&
          i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a self assessment" in {
      val sa = selfAssessment()
      val result = await(repository.create(sa))
      result shouldBe sa
    }

    "fail to create a duplicate self assessment" in {
      val sa = selfAssessment()
      await(repository.create(sa))

      intercept[DuplicateSelfAssessmentException](await(repository.create(sa)))
    }
  }

  "find by nino" should {
    "return an empty sequence when there are no self assessments for a given nino" in {
      await(repository.findByNino(nino)) shouldBe Seq.empty
    }

    "return self assessments for a given nino" in {
      val sa = selfAssessment()
      await(repository.create(sa))

      val result = await(repository.findByNino(nino))

      result shouldBe Seq(sa)
    }
  }

  def selfAssessmentReturn(selfEmploymentStartDate: Option[LocalDate] = Some(parse("2015-01-01")),
                           selfAssessmentIncome: Double = 1233.33,
                           employmentsIncome: Double = 13567.77,
                           saReceivedDate: LocalDate = parse("2016-01-01"),
                           selfEmploymentProfit: Double = 1233.33
                          ) = {
    SelfAssessmentReturn(selfEmploymentStartDate, saReceivedDate, selfAssessmentIncome, employmentsIncome, selfEmploymentProfit)
  }

  def selfAssessment(saReturns: Seq[SelfAssessmentReturn] = Seq(selfAssessmentReturn())) = {
    SelfAssessment(nino, taxYear, saReturns)
  }
}
