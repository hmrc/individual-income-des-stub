/*
 * Copyright 2020 HM Revenue & Customs
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

import org.joda.time.format.DateTimeFormat
import org.joda.time.{Interval, LocalDate}
import uk.gov.hmrc.individualincomedesstub.domain.{EmploymentPayFrequency, TaxYear, ValidationException}

import scala.util.Try

object Validators {

  def valid(requirement: Boolean, message: String) = {
    if(!requirement)
      throw new ValidationException(message)
  }

  def validDate(fieldName: String, value: String) = {
    valid(Try(LocalDate.parse(value, DateTimeFormat.forPattern("yyyy-MM-dd"))).isSuccess, s"$fieldName: invalid date format")
  }

  def validInterval(startDate: String, endDate: String, errorMessage: String) = {
    valid(Try(new Interval(LocalDate.parse(startDate).toDate.getTime, LocalDate.parse(endDate).toDate.getTime)).isSuccess, errorMessage)
  }

  def validPayFrequency(string: String, errorMessage: String): Unit = {
    valid(Try(EmploymentPayFrequency.withName(string)).isSuccess, errorMessage)
  }

  def validTaxYear(string: String): Unit = {
    valid(Try(TaxYear(string)).isSuccess, "taxYear: invalid tax year format")
  }
}
