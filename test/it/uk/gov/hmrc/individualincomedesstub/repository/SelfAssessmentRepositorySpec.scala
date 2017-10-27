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

import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import reactivemongo.api.indexes.IndexType
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
    "have a unique index on id" in {
      await(repository.collection.indexesManager.list()).find({ i =>
        i.name == Some("ninoIndex") &&
          i.key == Seq("nino" -> IndexType.Ascending) &&
          i.background &&
          !i.unique
      }) should not be None
    }
  }

  "create" should {
    "create a self employment" in {
      val request = selfAssessmentCreateRequest()
      val sa = selfAssessment()
      val result = await(repository.create(nino, taxYear, request))
      result shouldBe sa
    }

    "create a self employment with default income values for its saReturns" in {

      val saReturnData = selfAssessmentReturnData(selfEmploymentIncome = None, employmentsIncome = None)
      val saReturn = selfAssessmentReturn(selfEmploymentIncome = 0.0, employmentsIncome = 0.0)
      val sa = selfAssessment(Seq(saReturn, saReturn))

      val result = await(repository.create(nino, taxYear, selfAssessmentCreateRequest(Seq(saReturnData, saReturnData))))

      result shouldBe sa
    }

    "create a self employment with no start date for its saReturns" in {
      val saReturnPayload = selfAssessmentReturnData(selfEmploymentStartDate = None)
      val saReturn = selfAssessmentReturn(selfEmploymentStartDate = None)
      val sa = selfAssessment(Seq(saReturn, saReturn))

      val result = await(repository.create(nino, taxYear, selfAssessmentCreateRequest(Seq(saReturnPayload, saReturnPayload))))
      result shouldBe sa
    }

    "allow multiple self employments for a given nino" in {
      val request = selfAssessmentCreateRequest()
      val sa = selfAssessment()
      await(repository.create(nino, taxYear, request))
      await(repository.create(nino, taxYear, request))

      val result = await(repository.findAll())

      result shouldBe Seq(sa, sa)
    }
  }

  "find by nino" should {
    "return an empty sequence when there are no employments for a given nino" in {
      await(repository.findByNino(nino)) shouldBe Seq.empty
    }

    "return all employments for a given nino" in {
      val request = selfAssessmentCreateRequest()
      val sa = selfAssessment()
      await(repository.create(nino, taxYear, request))
      await(repository.create(nino, taxYear, request))

      val result = await(repository.findByNino(nino))

      result shouldBe Seq(sa, sa)
    }
  }

  def selfAssessmentReturnData(selfEmploymentStartDate: Option[String] = Some("2015-01-01"),
                               selfEmploymentIncome: Option[Double] = Some(1233.33),
                               employmentsIncome: Option[Double] = Some(13567.77),
                               saReceivedDate: String = "2016-01-01") = {
    SelfAssessmentReturnData(selfEmploymentStartDate, saReceivedDate, selfEmploymentIncome, employmentsIncome)
  }

  def selfAssessmentCreateRequest(saReturns: Seq[SelfAssessmentReturnData] = Seq(selfAssessmentReturnData())) = {
    SelfAssessmentCreateRequest(saReturns)
  }

  def selfAssessmentReturn(selfEmploymentStartDate: Option[String] = Some("2015-01-01"),
                           selfEmploymentIncome: Double = 1233.33,
                           employmentsIncome: Double = 13567.77,
                           saReceivedDate: String = "2016-01-01") = {
    SelfAssessmentReturn(selfEmploymentStartDate, saReceivedDate, selfEmploymentIncome, employmentsIncome)
  }

  def selfAssessment(saReturns: Seq[SelfAssessmentReturn] = Seq(selfAssessmentReturn())) = {
    SelfAssessment(nino, taxYear, saReturns)
  }
}
