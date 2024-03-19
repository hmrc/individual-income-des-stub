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

package unit.uk.gov.hmrc.individualincomedesstub.util

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import uk.gov.hmrc.individualincomedesstub.util.{
  Dates,
  Interval,
  IntervalQueryStringBinder
}

import java.time.LocalDate
import java.time.LocalDateTime.parse

class IntervalQueryStringBinderSpec
    extends AnyFlatSpec
    with Matchers
    with EitherValues {

  private val intervalQueryStringBinder = new IntervalQueryStringBinder

  "Interval query string binder" should "fail to bind a missing or malformed from or a malformed to parameter" in new TableDrivenPropertyChecks {
    private val fixtures = Table(
      ("parameters", "response"),
      (Map[String, Seq[String]]().empty,
       """{"code":"INVALID_REQUEST","message":"from is required"}"""),
      (Map("from" -> Seq.empty[String]),
       """{"code":"INVALID_REQUEST","message":"from is required"}"""),
      (Map("from" -> Seq("")),
       """{"code":"INVALID_REQUEST","message":"from: invalid date format"}"""),
      (Map("from" -> Seq("20200131")),
       """{"code":"INVALID_REQUEST","message":"from: invalid date format"}"""),
      (Map("from" -> Seq("2020-01-31"), "to" -> Seq("")),
       """{"code":"INVALID_REQUEST","message":"to: invalid date format"}"""),
      (Map("from" -> Seq("2020-01-31"), "to" -> Seq("20201231")),
       """{"code":"INVALID_REQUEST","message":"to: invalid date format"}""")
    )

    fixtures foreach {
      case (parameters, response) =>
        val maybeEither = intervalQueryStringBinder.bind("", parameters)
        maybeEither.isDefined shouldBe true
        maybeEither.get.isLeft shouldBe true
        maybeEither.get.left.value shouldBe response
    }
  }

  it should "default to today's date when a valid from parameter is present but a to parameter is missing" in {
    val parameters = Map("from" -> Seq("2017-01-31"))
    val maybeEither = intervalQueryStringBinder.bind("", parameters)
    maybeEither.isDefined shouldBe true
    maybeEither.get.isRight shouldBe true
    maybeEither.get shouldBe Right(
      toInterval("2017-01-31T00:00:00.000",
                 LocalDate.now().atStartOfDay().plusNanos(1000000).toString))
  }

  it should "succeed in binding an interval from well formed from and to parameters" in {
    val parameters = Map("from" -> Seq("2020-01-31"), "to" -> Seq("2020-12-31"))
    val maybeEither = intervalQueryStringBinder.bind("", parameters)
    maybeEither.isDefined shouldBe true
    maybeEither.get.isRight shouldBe true
    maybeEither.get shouldBe Right(
      toInterval("2020-01-31T00:00:00.000", "2020-12-31T00:00:00.001"))
  }

  it should "fail to bind an interval from an invalid date range" in {
    val parameters = Map("from" -> Seq("2020-12-31"), "to" -> Seq("2020-01-31"))
    val maybeEither = intervalQueryStringBinder.bind("", parameters)
    maybeEither.isDefined shouldBe true
    maybeEither.get.isLeft shouldBe true
    maybeEither.get.left.value shouldBe """{"code":"INVALID_REQUEST","message":"Invalid time period requested"}"""
  }

  it should "unbind intervals to query parameters" in {
    val interval = toInterval("2020-01-31T00:00:00", "2020-12-31T00:00:00")
    intervalQueryStringBinder.unbind("", interval) shouldBe "from=2020-01-31&to=2020-12-31"
  }

  private def toInterval(from: String, to: String): Interval =
    Dates.toInterval(parse(from).toLocalDate, parse(to).toLocalDate)
}
