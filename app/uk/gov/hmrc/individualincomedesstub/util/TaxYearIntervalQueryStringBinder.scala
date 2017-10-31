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

package uk.gov.hmrc.individualincomedesstub.util

import uk.gov.hmrc.individualincomedesstub.domain.{TaxYear, TaxYearInterval, ValidationException}
import uk.gov.hmrc.individualincomedesstub.util.Dates.toTaxYearInterval

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

class TaxYearIntervalQueryStringBinder extends AbstractQueryStringBindable[TaxYearInterval] {

  final lazy val yearRegex = "^\\d{4}$".r

  val matchYear: String => Option[Match] = new Regex(s"$yearRegex", "year") findFirstMatchIn _

  override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, TaxYearInterval]] = {
    (getParam(params, "startYear"), getParam(params, "endYear")) match {
      case (Right(from), Right(to)) => Some(taxYearInterval(from, to))
      case (_, Left(msg)) => Some(Left(msg))
      case (Left(msg), _) => Some(Left(msg))
    }
  }

  private def taxYearInterval(fromTaxYear: TaxYear, toTaxYear: TaxYear): Either[String, TaxYearInterval] = try {
    Right(toTaxYearInterval(fromTaxYear, toTaxYear))
  } catch {
    case e: ValidationException => Left(errorResponse(e.getMessage))
  }

  private def taxYear(year: String, paramName: String) = {
    yearRegex.findFirstIn(year) match {
      case Some(taxYear) => Right(TaxYear(s"${taxYear.toInt - 1}-${taxYear.takeRight(2)}"))
      case None => Left(errorResponse(s"$paramName: invalid tax year format"))
    }
  }

  private def getParam(params: Map[String, Seq[String]], paramName: String): Either[String, TaxYear] = {
    try {
      params.get(paramName).flatMap(_.headOption) match {
        case Some(year) => taxYear(year, paramName)
        case None => Left(errorResponse(s"$paramName is required"))
      }
    } catch {
      case _: Throwable => Left(errorResponse(s"$paramName: invalid tax year format"))
    }
  }

  override def unbind(key: String, taxYearInterval: TaxYearInterval): String = {
    s"startYear=${taxYearInterval.fromTaxYear.startYr}&endYear=${taxYearInterval.toTaxYear.endYr}"
  }
}
