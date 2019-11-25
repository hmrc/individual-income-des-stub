/*
 * Copyright 2019 HM Revenue & Customs
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

import org.joda.time.LocalDate
import org.joda.time.LocalDate.parse
import play.api.libs.json._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.individualincomedesstub.util.Validators.{validDate, validTaxYear}
import scala.util.matching.Regex
import scala.util.matching.Regex.Match
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

case class SelfAssessmentTaxReturn(taxYear: TaxYear,
                                   submissionDate: LocalDate,
                                   employmentsIncome: Double,
                                   selfEmploymentProfit: Double,
                                   totalIncome: Double,
                                   trustsIncome: Double,
                                   foreignIncome: Double,
                                   partnershipsProfit: Double,
                                   ukInterestsIncome: Double,
                                   foreignDividendsIncome: Double,
                                   ukDividendsIncome: Double,
                                   ukPropertiesProfit: Double,
                                   gainsOnLifePolicies: Double,
                                   sharesOptionsIncome: Double,
                                   pensionsAndStateBenefitsIncome: Double,
                                   otherIncome: Double,
                                   businessDescription: Option[String],
                                   address: Option[SaAddress]) {

  def isIn(startYear: Int, endYear: Int) = taxYear.endYr.toInt >= startYear && taxYear.endYr.toInt <= endYear
}

object SelfAssessmentTaxReturn {
  def apply(saTaxReturnData: SelfAssessmentTaxReturnData): SelfAssessmentTaxReturn = {
    SelfAssessmentTaxReturn(
      TaxYear(saTaxReturnData.taxYear),
      parse(saTaxReturnData.submissionDate),
      saTaxReturnData.employmentsIncome.getOrElse(0.0),
      saTaxReturnData.selfEmploymentProfit.getOrElse(0.0),
      saTaxReturnData.totalIncome.getOrElse(0.0),
      saTaxReturnData.trustsIncome.getOrElse(0.0),
      saTaxReturnData.foreignIncome.getOrElse(0.0),
      saTaxReturnData.partnershipsProfit.getOrElse(0.0),
      saTaxReturnData.ukInterestsIncome.getOrElse(0.0),
      saTaxReturnData.foreignDividendsIncome.getOrElse(0.0),
      saTaxReturnData.ukDividendsIncome.getOrElse(0.0),
      saTaxReturnData.ukPropertiesProfit.getOrElse(0.0),
      saTaxReturnData.gainsOnLifePolicies.getOrElse(0.0),
      saTaxReturnData.sharesOptionsIncome.getOrElse(0.0),
      saTaxReturnData.pensionsAndStateBenefitsIncome.getOrElse(0.0),
      saTaxReturnData.otherIncome.getOrElse(0.0),
      saTaxReturnData.businessDescription,
      saTaxReturnData.address
    )
  }
}

case class SelfAssessment(saUtr: SaUtr,
                          registrationDate: LocalDate,
                          taxReturns: Seq[SelfAssessmentTaxReturn])

object SelfAssessment {
  def apply(utr: SaUtr, request: SelfAssessmentCreateRequest): SelfAssessment = {
    SelfAssessment(
      utr,
      parse(request.registrationDate),
      request.taxReturns.map(SelfAssessmentTaxReturn(_))
    )
  }
}

case class SelfAssessmentCreateRequest(registrationDate: String, taxReturns: Seq[SelfAssessmentTaxReturnData]) {
  validDate("registrationDate", registrationDate)
  taxReturns foreach { sa =>
    validDate("submissionDate", sa.submissionDate)
    validTaxYear(sa.taxYear)
  }
}

case class SelfAssessmentTaxReturnData(taxYear: String,
                                       submissionDate: String,
                                       employmentsIncome: Option[Double],
                                       selfEmploymentProfit: Option[Double],
                                       totalIncome: Option[Double],
                                       trustsIncome: Option[Double],
                                       foreignIncome: Option[Double],
                                       partnershipsProfit: Option[Double],
                                       ukInterestsIncome: Option[Double],
                                       foreignDividendsIncome: Option[Double],
                                       ukDividendsIncome: Option[Double],
                                       ukPropertiesProfit: Option[Double],
                                       gainsOnLifePolicies: Option[Double],
                                       sharesOptionsIncome: Option[Double],
                                       pensionsAndStateBenefitsIncome: Option[Double],
                                       otherIncome: Option[Double],
                                       businessDescription: Option[String],
                                       address: Option[SaAddress])

object SelfAssessmentTaxReturnData {

  // inlining everything into the case class means there's too many fields, so apply/unapply aren't generated
  implicit val format: Format[SelfAssessmentTaxReturnData] = new Format[SelfAssessmentTaxReturnData] {
    override def reads(json: JsValue): JsResult[SelfAssessmentTaxReturnData] = for {
      taxYear <- (json \ "taxYear").validate[String]
      submissionDate <- (json \ "submissionDate").validate[String]
      employmentsIncome <- (json \ "employmentsIncome").validateOpt[Double]
      selfEmploymentProfit <- (json \ "selfEmploymentProfit").validateOpt[Double]
      totalIncome <- (json \ "totalIncome").validateOpt[Double]
      trustsIncome <- (json \ "trustsIncome").validateOpt[Double]
      foreignIncome <- (json \ "foreignIncome").validateOpt[Double]
      partnershipsProfit <- (json \ "partnershipsProfit").validateOpt[Double]
      ukInterestsIncome <- (json \ "ukInterestsIncome").validateOpt[Double]
      foreignDividendsIncome <- (json \ "foreignDividendsIncome").validateOpt[Double]
      ukDividendsIncome <- (json \ "ukDividendsIncome").validateOpt[Double]
      ukPropertiesProfit <- (json \ "ukPropertiesProfit").validateOpt[Double]
      gainsOnLifePolicies <- (json \ "gainsOnLifePolicies").validateOpt[Double]
      sharesOptionsIncome <- (json \ "sharesOptionsIncome").validateOpt[Double]
      pensionsAndStateBenefitsIncome <- (json \ "pensionsAndStateBenefitsIncome").validateOpt[Double]
      otherIncome <- (json \ "otherIncome").validateOpt[Double]
      businessDescription <- (json \ "businessDescription").validateOpt[String]
      address <- json.validateOpt[SaAddress].map {
        case Some(SaAddress(None, None, None, None, None, None, None, None)) => None
        case other => other
      }
    } yield SelfAssessmentTaxReturnData(
      taxYear,
      submissionDate,
      employmentsIncome,
      selfEmploymentProfit,
      totalIncome,
      trustsIncome,
      foreignIncome,
      partnershipsProfit,
      ukInterestsIncome,
      foreignDividendsIncome,
      ukDividendsIncome,
      ukPropertiesProfit,
      gainsOnLifePolicies,
      sharesOptionsIncome,
      pensionsAndStateBenefitsIncome,
      otherIncome,
      businessDescription,
      address
    )

    override def writes(o: SelfAssessmentTaxReturnData): JsValue = {
      val j = Json.obj(
        "taxYear" -> o.taxYear,
        "submissionDate" -> o.submissionDate,
        "employmentsIncome" -> o.employmentsIncome,
        "selfEmploymentProfit" -> o.selfEmploymentProfit,
        "totalIncome" -> o.totalIncome,
        "trustsIncome" -> o.trustsIncome,
        "foreignIncome" -> o.foreignIncome,
        "partnershipsProfit" -> o.partnershipsProfit,
        "ukInterestsIncome" -> o.ukInterestsIncome,
        "foreignDividendsIncome" -> o.foreignDividendsIncome,
        "ukDividendsIncome" -> o.ukDividendsIncome,
        "ukPropertiesProfit" -> o.ukPropertiesProfit,
        "gainsOnLifePolicies" -> o.gainsOnLifePolicies,
        "sharesOptionsIncome" -> o.sharesOptionsIncome,
        "pensionsAndStateBenefitsIncome" -> o.pensionsAndStateBenefitsIncome,
        "otherIncome" -> o.otherIncome,
        "businessDescription" -> o.businessDescription
      ) ++ o.address.fold(Json.obj())(Json.toJson(_).as[JsObject])

      JsObject(j.fields.filterNot(_._2 == JsNull))
    }
  }
}

case class SaAddress(addressLine1: Option[String],
                     addressLine2: Option[String],
                     addressLine3: Option[String],
                     addressLine4: Option[String],
                     postalCode: Option[String],
                     telephoneNumber: Option[String],
                     baseAddressEffectiveDate: Option[LocalDate],
                     addressTypeIndicator: Option[String])

object SaAddress {
  implicit val format: Format[SaAddress] = Json.format[SaAddress]
}

case class SelfAssessmentResponseReturn(utr: SaUtr,
                                        caseStartDate: LocalDate,
                                        receivedDate: LocalDate,
                                        incomeFromAllEmployments: Double,
                                        profitFromSelfEmployment: Double,
                                        incomeFromSelfAssessment: Double,
                                        incomeFromTrust: Double,
                                        incomeFromForeign4Sources: Double,
                                        profitFromPartnerships: Double,
                                        incomeFromUkInterest: Double,
                                        incomeFromForeignDividends: Double,
                                        incomeFromInterestNDividendsFromUKCompaniesNTrusts: Double,
                                        incomeFromProperty: Double,
                                        incomeFromGainsOnLifePolicies: Double,
                                        incomeFromSharesOptions: Double,
                                        incomeFromPensions: Double,
                                        incomeFromOther: Double,
                                        businessDescription: Option[String],
                                        address: SaAddress)

object SelfAssessmentResponseReturn {
  def apply(utr: SaUtr, registrationDate: LocalDate, selfAssessmentTaxReturn: SelfAssessmentTaxReturn): SelfAssessmentResponseReturn = {
    SelfAssessmentResponseReturn(
      utr,
      registrationDate,
      selfAssessmentTaxReturn.submissionDate,
      selfAssessmentTaxReturn.employmentsIncome,
      selfAssessmentTaxReturn.selfEmploymentProfit,
      selfAssessmentTaxReturn.totalIncome,
      selfAssessmentTaxReturn.trustsIncome,
      selfAssessmentTaxReturn.foreignIncome,
      selfAssessmentTaxReturn.partnershipsProfit,
      selfAssessmentTaxReturn.ukInterestsIncome,
      selfAssessmentTaxReturn.foreignDividendsIncome,
      selfAssessmentTaxReturn.ukDividendsIncome,
      selfAssessmentTaxReturn.ukPropertiesProfit,
      selfAssessmentTaxReturn.gainsOnLifePolicies,
      selfAssessmentTaxReturn.sharesOptionsIncome,
      selfAssessmentTaxReturn.pensionsAndStateBenefitsIncome,
      selfAssessmentTaxReturn.otherIncome,
      selfAssessmentTaxReturn.businessDescription,
      selfAssessmentTaxReturn.address.getOrElse(SaAddress(None, None, None, None, None, None, None, None))
    )
  }

  implicit val format: Format[SelfAssessmentResponseReturn] = new Format[SelfAssessmentResponseReturn] {
    override def reads(json: JsValue): JsResult[SelfAssessmentResponseReturn] = for {
      utr <- (json \ "utr").validate[SaUtr]
      caseStartDate <- (json \ "caseStartDate").validate[LocalDate]
      receivedDate <- (json \ "receivedDate").validate[LocalDate]
      incomeFromAllEmployments <- (json \ "incomeFromAllEmployments").validate[Double]
      profitFromSelfEmployment <- (json \ "profitFromSelfEmployment").validate[Double]
      incomeFromSelfAssessment <- (json \ "incomeFromSelfAssessment").validate[Double]
      incomeFromTrust <- (json \ "incomeFromTrust").validate[Double]
      incomeFromForeign4Sources <- (json \ "incomeFromForeign4Sources").validate[Double]
      profitFromPartnerships <- (json \ "profitFromPartnerships").validate[Double]
      incomeFromUkInterest <- (json \ "incomeFromUkInterest").validate[Double]
      incomeFromForeignDividends <- (json \ "incomeFromForeignDividends").validate[Double]
      incomeFromInterestNDividendsFromUKCompaniesNTrusts <- (json \ "incomeFromInterestNDividendsFromUKCompaniesNTrusts").validate[Double]
      incomeFromProperty <- (json \ "incomeFromProperty").validate[Double]
      incomeFromGainsOnLifePolicies <- (json \ "incomeFromGainsOnLifePolicies").validate[Double]
      incomeFromSharesOptions <- (json \ "incomeFromSharesOptions").validate[Double]
      incomeFromPensions <- (json \ "incomeFromPensions").validate[Double]
      incomeFromOther <- (json \ "incomeFromOther").validate[Double]
      businessDescription <- (json \ "businessDescription").validateOpt[String]
      line1 <- (json \ "addressLine1").validateOpt[String]
      line2 <- (json \ "addressLine2").validateOpt[String]
      line3 <- (json \ "addressLine3").validateOpt[String]
      line4 <- (json \ "addressLine4").validateOpt[String]
      postcode <- (json \ "postalCode").validateOpt[String]
      phoneNumber <- (json \ "telephoneNumber").validateOpt[String]
      effectiveDate <- (json \ "baseAddressEffectivetDate").validateOpt[LocalDate] // misspelled as per the DES spec
      addressType <- (json \ "addressTypeIndicator").validateOpt[String]
    } yield SelfAssessmentResponseReturn(
      utr,
      caseStartDate,
      receivedDate,
      incomeFromAllEmployments,
      profitFromSelfEmployment,
      incomeFromSelfAssessment,
      incomeFromTrust,
      incomeFromForeign4Sources,
      profitFromPartnerships,
      incomeFromUkInterest,
      incomeFromForeignDividends,
      incomeFromInterestNDividendsFromUKCompaniesNTrusts,
      incomeFromProperty,
      incomeFromGainsOnLifePolicies,
      incomeFromSharesOptions,
      incomeFromPensions,
      incomeFromOther,
      businessDescription,
      SaAddress(
        line1,
        line2,
        line3,
        line4,
        postcode,
        phoneNumber,
        effectiveDate,
        addressType
      )
    )

    override def writes(o: SelfAssessmentResponseReturn): JsValue = {
      val json = Json.obj(
        "utr" -> o.utr,
        "caseStartDate" -> o.caseStartDate,
        "receivedDate" -> o.receivedDate,
        "incomeFromAllEmployments" -> o.incomeFromAllEmployments,
        "profitFromSelfEmployment" -> o.profitFromSelfEmployment,
        "incomeFromSelfAssessment" -> o.incomeFromSelfAssessment,
        "incomeFromTrust" -> o.incomeFromTrust,
        "incomeFromForeign4Sources" -> o.incomeFromForeign4Sources,
        "profitFromPartnerships" -> o.profitFromPartnerships,
        "incomeFromUkInterest" -> o.incomeFromUkInterest,
        "incomeFromForeignDividends" -> o.incomeFromForeignDividends,
        "incomeFromInterestNDividendsFromUKCompaniesNTrusts" -> o.incomeFromInterestNDividendsFromUKCompaniesNTrusts,
        "incomeFromProperty" -> o.incomeFromProperty,
        "incomeFromGainsOnLifePolicies" -> o.incomeFromGainsOnLifePolicies,
        "incomeFromSharesOptions" -> o.incomeFromSharesOptions,
        "incomeFromPensions" -> o.incomeFromPensions,
        "incomeFromOther" -> o.incomeFromOther,
        "businessDescription" -> o.businessDescription,
        "addressLine1" -> o.address.addressLine1,
        "addressLine2" -> o.address.addressLine2,
        "addressLine3" -> o.address.addressLine3,
        "addressLine4" -> o.address.addressLine4,
        "postalCode" -> o.address.postalCode,
        "telephoneNumber" -> o.address.telephoneNumber,
        "baseAddressEffectivetDate" -> o.address.baseAddressEffectiveDate,
        "addressTypeIndicator" -> o.address.addressTypeIndicator
      )

      JsObject(json.fields.filterNot(_._2 == JsNull))
    }
  }
}

case class SelfAssessmentResponse(taxYear: String, returnList: Seq[SelfAssessmentResponseReturn])

object SelfAssessmentResponse {
  def apply(utr: SaUtr, registrationDate: LocalDate, selfAssessmentTaxReturn: SelfAssessmentTaxReturn): SelfAssessmentResponse = {
    SelfAssessmentResponse(selfAssessmentTaxReturn.taxYear.endYr,
      Seq(SelfAssessmentResponseReturn(utr, registrationDate, selfAssessmentTaxReturn)))
  }
}

case class TaxYear(ty: String) {
  if (!TaxYear.isValid(ty)) throw new IllegalArgumentException

  val startYr = ty.split("-")(0)
  val endYr = startYr.toInt + 1 toString
}

object TaxYear {

  final val TaxYearRegex = "^(\\d{4})-(\\d{2})$"

  val matchTaxYear: String => Option[Match] = new Regex(s"$TaxYearRegex", "first", "second") findFirstMatchIn _

  def build(ty: String): Option[TaxYear] = TaxYearRegex.r findFirstIn ty map (TaxYear(_))

  def isValid(taxYearReference: String) = matchTaxYear(taxYearReference) exists {
    r => (r.group("first").toInt + 1) % 100 == r.group("second").toInt
  }

}
