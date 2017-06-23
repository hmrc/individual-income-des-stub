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

package it.uk.gov.hmrc.individualincomedesstub.service

import java.util.regex.Pattern

import uk.gov.hmrc.individualincomedesstub.domain.Address
import uk.gov.hmrc.individualincomedesstub.service.Randomiser
import uk.gov.hmrc.play.test.UnitSpec

class RandomiserSpec extends UnitSpec {

  trait Setup {
    val underTest = new Randomiser("test-randomiser")
  }

  "randomEmployerPayeReference" should {
    "return a new employer paye reference every time" in new Setup {

      val employerPayeReference1 = underTest.randomEmployerPayeReference()
      val employerPayeReference2 = underTest.randomEmployerPayeReference()

      employerPayeReference1 should not be employerPayeReference2
    }
  }

  "randomEmployerName" should {
    "return a name with 6 letters" in new Setup {
      val employerName = underTest.randomEmployerName()

      employerName.matches("^Company \\w{6}$") shouldBe true
    }

    "return a new employer name every time" in new Setup {

      val employerName1 = underTest.randomEmployerName()
      val employerName2 = underTest.randomEmployerName()

      employerName1 should not be employerName2
    }
  }

  "randomAddress" should {
    "return an address from the configuration file" in new Setup {

      val result = underTest.randomAddress()

      result shouldBe Address("101 Abbey Road", Some("Aberdeen"), "SW1PPT")
    }
  }
}
