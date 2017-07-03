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

import uk.gov.hmrc.domain.EmpRef

import scala.util.Try

class EmployerReferenceStringBinder extends AbstractPathStringBindable[EmpRef] {

  def bind(key: String, value: String) =
    Try(Right(EmpRef.fromIdentifiers(value))) getOrElse Left(errorResponse("Invalid employer reference submitted"))

  def unbind(key: String, empRef: EmpRef): String = empRef.encodedValue

}
