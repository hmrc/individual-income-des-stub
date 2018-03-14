/*
 * Copyright 2018 HM Revenue & Customs
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

import org.joda.time.LocalDate.parse
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.hmrc.individualincomedesstub.util.Dates

class DatesSpec extends FlatSpec with Matchers {

  "Dates utility" should "derive an interval between two dates" in {
    val (from, to) = (parse("2020-01-01"), parse("2020-01-02"))
    Dates.toInterval(from, to).toString shouldBe "2020-01-01T00:00:00.000Z/2020-01-02T00:00:00.001Z"
  }

}
