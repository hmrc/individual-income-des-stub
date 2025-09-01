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

package uk.gov.hmrc.individualincomedesstub.util

import uk.gov.hmrc.individualincomedesstub.util.Dates.toInterval

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

class IntervalQueryStringBinder extends AbstractQueryStringBindable[Interval] {

  private val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Interval]] =
    Some(for {
      from   <- getParam(params, "from")
      to     <- getParam(params, "to", Some(LocalDate.now()))
      result <- Either.cond(from `isBefore` to, toInterval(from, to), errorResponse("Invalid time period requested"))
    } yield result)

  private def getParam(
    params: Map[String, Seq[String]],
    paramName: String,
    default: Option[LocalDate] = None
  ): Either[String, LocalDate] =
    params.get(paramName) match {
      case Some(date :: _) =>
        Try(LocalDate.parse(date, format)).toEither.left.map(_ => errorResponse(s"$paramName: invalid date format"))
      case _ => default.toRight(errorResponse(s"$paramName is required"))
    }

  override def unbind(key: String, dateRange: Interval): String =
    s"from=${dateRange.fromDate.format(format)}&to=${dateRange.toDate.format(format)}"

}
