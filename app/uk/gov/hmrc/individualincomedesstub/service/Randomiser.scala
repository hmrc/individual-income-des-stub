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

package uk.gov.hmrc.individualincomedesstub.service

import com.typesafe.config.ConfigFactory
import org.scalacheck.Gen
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualincomedesstub.domain.Address
import scala.collection.JavaConverters._

import scala.util.Random

class Randomiser(fileName: String = "randomiser") {

  private lazy val config = ConfigFactory.load(fileName)
  private val employerPayeReferenceGenerator: Gen[EmpRef] = for {
    taxOfficeNumber <- Gen.choose(100, 999).map(x => x.toString)
    taxOfficeReference <- Gen.listOfN(10, Gen.alphaNumChar).map(_.mkString.toUpperCase)
  } yield EmpRef.fromIdentifiers(s"$taxOfficeNumber/$taxOfficeReference")

  private def randomConfigString(configKey: String): String = {
    val strings = config.getStringList(configKey).asScala
    strings(Random.nextInt(strings.size))
  }

  def randomEmployerPayeReference() = employerPayeReferenceGenerator.sample.get

  def randomEmployerName() = "Company " + Gen.listOfN(6, Gen.alphaNumChar).map(_.mkString.toUpperCase).sample.get

  def randomAddress() = Address(
    line1 = randomConfigString("randomiser.address.line1"),
    line2 = Some(randomConfigString("randomiser.address.line2")),
    postcode = randomConfigString("randomiser.address.postcode")
  )
}
