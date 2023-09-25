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

package it.uk.gov.hmrc.individualincomedesstub.repository

import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfterEach
import play.api.{Application, Configuration}
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.domain._
import uk.gov.hmrc.individualincomedesstub.repository.SelfAssessmentRepository
import unit.uk.gov.hmrc.individualincomedesstub.util.TestSupport

class SelfAssessmentRepositorySpec extends TestSupport with BeforeAndAfterEach {

  override lazy val fakeApplication: Application = buildFakeApplication(
    Configuration("mongodb.uri" -> "mongodb://localhost:27017/individual-income-des-stub"))

  private val repository =
    fakeApplication.injector.instanceOf[SelfAssessmentRepository]

  private val utr = SaUtr("2432552635")
  private val selfAssessment = SelfAssessment(
    utr,
    LocalDate.parse("2014-01-01"),
    Seq(
      SelfAssessmentTaxReturn(
        taxYear = TaxYear("2014-15"),
        submissionDate = LocalDate.parse("2015-01-01"),
        employmentsIncome = 13567.77,
        selfEmploymentProfit = 1233.33,
        totalIncome = 22345,
        trustsIncome = 500.25,
        foreignIncome = 200.35,
        partnershipsProfit = 12.45,
        ukInterestsIncome = 21.23,
        foreignDividendsIncome = 52.34,
        ukDividendsIncome = 6.34,
        ukPropertiesProfit = 55.24,
        gainsOnLifePolicies = 4.34,
        sharesOptionsIncome = 5.43,
        pensionsAndStateBenefitsIncome = 5.32,
        otherIncome = 134.56,
        businessDescription = None,
        address = None
      ))
  )

  override def beforeEach(): Unit = {
    await(repository.collection.drop().toFuture())
    await(repository.ensureIndexes)
  }

  override def afterEach(): Unit =
    await(repository.collection.drop().toFuture())

  /*
  "collection" should {
    "have a unique index on saUtr" in {
      await(repository.collection.listIndexes().toFuture()).find({ i =>
        i.
        i.name.contains("saUtrIndex") &&
        i.key == Seq("saUtr" -> Ascending) &&
        i.background &&
        i.unique
      }) should not be None
    }
  }
   */

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
