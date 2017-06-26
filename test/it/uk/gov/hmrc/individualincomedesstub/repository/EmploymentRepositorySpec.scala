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

import org.joda.time.LocalDate.parse
import org.scalatest.BeforeAndAfterEach
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.individualincomedesstub.domain.{CreateEmploymentRequest, Employment, Payment}
import uk.gov.hmrc.individualincomedesstub.repository.EmploymentRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class EmploymentRepositorySpec  extends UnitSpec with WithFakeApplication with MongoSpecSupport with BeforeAndAfterEach {

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .bindings(bindModules:_*)
    .build()

  val employmentRepository = fakeApplication.injector.instanceOf[EmploymentRepository]
  val employerReference = "123/DI45678"
  val nino = Nino("NA000799C")

  override def beforeEach() {
    await(employmentRepository.drop)
    await(employmentRepository.ensureIndexes)
  }

  override def afterEach() {
    await(employmentRepository.drop)
  }

  "create" should {
    "create an employment" in {
      val result = await(employmentRepository.create(employerReference, nino, aCreateEmploymentRequest))
      result shouldBe anEmployment(employerReference, nino)
    }

    "allow multiple employments for the same employer reference and nino" in {
      await(employmentRepository.create(employerReference, nino, aCreateEmploymentRequest))
      await(employmentRepository.create(employerReference, nino, aCreateEmploymentRequest))
      val result = await(employmentRepository.findAll())
      result.size shouldBe 2
    }
  }

  "findByReferenceAndNino" should {

    "return all records for a given paye reference and nino" in {
      val employment = anEmployment(employerReference, nino)

      await(employmentRepository.create(employerReference, nino, aCreateEmploymentRequest))
      await(employmentRepository.create("someReference", nino, aCreateEmploymentRequest))
      await(employmentRepository.create(employerReference, Nino("AA123456C"), aCreateEmploymentRequest))

      val result = await(employmentRepository.findByReferenceAndNino(employerReference, nino))

      result shouldBe Seq(employment)
    }

    "return an empty list if no records exist for a given pay reference and nino" in {
      await(employmentRepository.create(employerReference, nino, aCreateEmploymentRequest))

      val result = await(employmentRepository.findByReferenceAndNino("someReference", nino))

      result.isEmpty shouldBe true
    }
  }

  private def aCreateEmploymentRequest = CreateEmploymentRequest(
    parse("2016-01-01"),
    parse("2017-01-30"),
    Seq(Payment(parse("2016-01-28"), 1000.55, 0), Payment(parse("2016-02-28"), 1200.44, 0)))

  private def anEmployment(empRef: String, nino: Nino) = Employment(
    empRef, nino,
    parse("2016-01-01"),
    parse("2017-01-30"),
    Seq(Payment(parse("2016-01-28"), 1000.55, 0), Payment(parse("2016-02-28"), 1200.44, 0))
  )
}
