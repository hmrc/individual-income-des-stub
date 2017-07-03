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

import org.joda.time.{Interval, LocalDate}

object Dates {

  private def asDate(string: String) = LocalDate.parse(string)

  def toInterval(fromDate: String, toDate: String): Interval =
    toInterval(asDate(fromDate), asDate(toDate))

  def toInterval(fromDate: LocalDate, toDate: LocalDate): Interval =
    new Interval(fromDate.toDate.getTime, toDate.toDateTimeAtStartOfDay.plusMillis(1).toDate.getTime)

}
