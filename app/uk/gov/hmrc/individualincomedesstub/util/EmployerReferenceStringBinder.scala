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

import java.net.URLDecoder

import uk.gov.hmrc.individualincomedesstub.domain.EmployerReference

class EmployerReferenceStringBinder extends AbstractPathStringBindable[EmployerReference] {

  override def bind(key: String, value: String): Either[String, EmployerReference] = try {
    Right(EmployerReference(URLDecoder.decode(value, "UTF-8")))
  } catch {
    case _: Throwable => Left(errorResponse("Invalid employer reference submitted"))
  }

  override def unbind(key: String, value: EmployerReference): String = value.value

}
