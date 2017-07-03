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

package unit.uk.gov.hmrc.individualincomedesstub.util

import org.joda.time.LocalDateTime.parse
import org.joda.time.{Interval, LocalDateTime}
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{EitherValues, FlatSpec, Matchers}
import uk.gov.hmrc.individualincomedesstub.util.{Dates, IntervalQueryStringBinder}

class IntervalQueryStringBinderSpec extends FlatSpec with Matchers with EitherValues {

  private val intervalQueryStringBinder = new IntervalQueryStringBinder

  "Interval query string binder" should "fail to bind a missing or malformed fromDate or a malformed toDate parameter" in new TableDrivenPropertyChecks {
    val fixtures = Table(
      ("parameters", "response"),
      (Map[String, Seq[String]]().empty, """{"code":"INVALID_REQUEST","message":"fromDate is required"}"""),
      (Map("fromDate" -> Seq.empty[String]), """{"code":"INVALID_REQUEST","message":"fromDate is required"}"""),
      (Map("fromDate" -> Seq("")), """{"code":"INVALID_REQUEST","message":"fromDate: invalid date format"}"""),
      (Map("fromDate" -> Seq("20200131")), """{"code":"INVALID_REQUEST","message":"fromDate: invalid date format"}"""),
      (Map("fromDate" -> Seq("2020-01-31"), "toDate" -> Seq("")), """{"code":"INVALID_REQUEST","message":"toDate: invalid date format"}"""),
      (Map("fromDate" -> Seq("2020-01-31"), "toDate" -> Seq("20201231")), """{"code":"INVALID_REQUEST","message":"toDate: invalid date format"}""")
    )

    fixtures foreach { case (parameters, response) =>
      val maybeEither = intervalQueryStringBinder.bind("", parameters)
      maybeEither.isDefined shouldBe true
      maybeEither.get.isLeft shouldBe true
      maybeEither.get.left.value shouldBe response
    }
  }

  it should "default to today's date when a valid fromDate parameter is present but a toDate parameter is missing" in {
    val parameters = Map("fromDate" -> Seq("2017-01-31"))
    val maybeEither = intervalQueryStringBinder.bind("", parameters)
    maybeEither.isDefined shouldBe true
    maybeEither.get.isRight shouldBe true
    maybeEither.get.right.value shouldBe toInterval("2017-01-31T00:00:00.000", LocalDateTime.now().withTime(0, 0, 0, 1).toString())
  }

  it should "succeed in binding an interval from well formed fromDate and toDate parameters" in {
    val parameters = Map("fromDate" -> Seq("2020-01-31"), "toDate" -> Seq("2020-12-31"))
    val maybeEither = intervalQueryStringBinder.bind("", parameters)
    maybeEither.isDefined shouldBe true
    maybeEither.get.isRight shouldBe true
    maybeEither.get.right.value shouldBe toInterval("2020-01-31T00:00:00.000", "2020-12-31T00:00:00.001")
  }

  it should "fail to bind an interval from an invalid date range" in {
    val parameters = Map("fromDate" -> Seq("2020-12-31"), "toDate" -> Seq("2020-01-31"))
    val maybeEither = intervalQueryStringBinder.bind("", parameters)
    maybeEither.isDefined shouldBe true
    maybeEither.get.isLeft shouldBe true
    maybeEither.get.left.value shouldBe """{"code":"INVALID_REQUEST","message":"Invalid time period requested"}"""
  }

  it should "unbind intervals to query parameters" in {
    val interval = toInterval("2020-01-31", "2020-12-31")
    intervalQueryStringBinder.unbind("", interval) shouldBe "fromDate=2020-01-31&toDate=2020-12-31"
  }

  private def toInterval(fromDate: String, toDate: String): Interval =
    Dates.toInterval(parse(fromDate).toLocalDate, parse(toDate).toLocalDate)

}
