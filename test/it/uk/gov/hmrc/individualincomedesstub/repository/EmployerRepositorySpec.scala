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
import reactivemongo.core.errors.DatabaseException
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualincomedesstub.domain.{Address, Employer}
import uk.gov.hmrc.individualincomedesstub.repository.EmployerRepository
import uk.gov.hmrc.mongo.MongoSpecSupport
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class EmployerRepositorySpec extends UnitSpec with WithFakeApplication with MongoSpecSupport with BeforeAndAfterEach {

  override lazy val fakeApplication = new GuiceApplicationBuilder()
    .configure("mongodb.uri" -> mongoUri)
    .bindings(bindModules: _*)
    .build()

  val employerRepository = fakeApplication.injector.instanceOf[EmployerRepository]
  val employer = Employer(
    payeReference = EmpRef.fromIdentifiers("904/UZ00057"),
    name = "Company ABF546",
    address = Address("Westfield center", Some("1 Stoke Ave"), "SW1PPT"))

  override def beforeEach() {
    await(employerRepository.drop)
    await(employerRepository.ensureIndexes)
  }

  override def afterEach() {
    await(employerRepository.drop)
  }

  "findByPayeReference" should {
    "return the employer" in {
      await(employerRepository.insert(employer))

      val result = await(employerRepository.findByPayeReference(employer.payeReference))

      result shouldBe Some(employer)
    }

    "return None when there is no employer for the payeReference" in {
      val result = await(employerRepository.findByPayeReference(employer.payeReference))

      result shouldBe None
    }
  }

  "createEmployer" should {
    "create an employer" in {
      val result = await(employerRepository.createEmployer(employer))

      result shouldBe employer
      await(employerRepository.findAll()) shouldBe Seq(employer)
    }

    "fail with DatabaseException when trying to create an employer which already exists" in {
      await(employerRepository.insert(employer))

      intercept[DatabaseException] {
        await(employerRepository.insert(employer))
      }
    }
  }

  "find by emp refs" should {

    "return empty set when corresponding employers no not exist" in {
      await(employerRepository.findBy(Set(employer.payeReference))) shouldBe Set.empty[Employer]
    }

    "return populated set when corresponding employers exist" in {
      await(employerRepository.insert(employer))
      await(employerRepository.findBy(Set(employer.payeReference))) shouldBe Set(employer)
    }

  }

}
