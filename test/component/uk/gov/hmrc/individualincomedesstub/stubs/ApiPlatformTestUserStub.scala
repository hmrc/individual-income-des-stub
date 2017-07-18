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

package component.uk.gov.hmrc.individualincomedesstub.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlPathEqualTo}
import component.uk.gov.hmrc.individualincomedesstub.MockHost
import play.api.http.Status
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.individualincomedesstub.domain.TestOrganisation

object ApiPlatformTestUserStub extends MockHost(22001) {

  def getByEmpRefReturnsTestOrganisation(empRef: EmpRef, organisation: TestOrganisation) = {
    mock.register(get(urlPathEqualTo(s"/organisations/empref/${empRef.encodedValue}"))
      .willReturn(aResponse().withStatus(Status.OK)
        .withBody(
          s"""
             |{
             |  "empRef": "${empRef.value}",
             |  "organisationDetails": {
             |    "name": "${organisation.organisationDetails.name}",
             |    "address": {
             |      "line1": "${organisation.organisationDetails.address.line1}",
             |      "line2": "${organisation.organisationDetails.address.line2}",
             |      "postcode": "${organisation.organisationDetails.address.postcode}"
             |    }
             |  }
             |}
             """.stripMargin.replaceAll("\n", ""))))
  }

  def getByEmpRefReturnsNoTestOrganisation(empRef: EmpRef) = {
    mock.register(get(urlPathEqualTo(s"/organisations/empref/${empRef.encodedValue}"))
      .willReturn(aResponse().withStatus(Status.NOT_FOUND)))
  }
}
