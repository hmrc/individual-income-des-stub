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

package uk.gov.hmrc.individualincomedesstub.domain

import uk.gov.hmrc.domain.{EmpRef, Nino}
import uk.gov.hmrc.individualincomedesstub.domain.Validators._

case class Payment(paymentDate: String, taxablePayment: Double, nonTaxablePayment: Double) {
  validDate("paymentDate", paymentDate)
}

case class Employment(employerPayeReference: EmpRef, nino: Nino, startDate: Option[String], endDate: Option[String], payments: Seq[Payment])

case class CreateEmploymentRequest(startDate: Option[String], endDate: Option[String], payments: Seq[Payment]) {
  (startDate, endDate) match {
    case (Some(start), Some(end)) =>
      validDate("startDate", start)
      validDate("endDate", end)
      validInterval(start, end, "Invalid employment period")
    case (Some(start), None) => validDate("startDate", start)
    case (None, Some(end)) => validDate("endDate", end)
    case _ =>
  }
}
