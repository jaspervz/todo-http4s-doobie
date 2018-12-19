package auth

sealed trait AuthInfo {

  /** ???
    * - Deprecated
    */
  def countryCode: CountryCode

  /** ???
    * - Option
    * - Should this really be part of the interface
    */
  def subAccount: Option[TwilioAccount]

  /** ???
    * - Option
    * - Should this really be part of the interface
    */
  def accountToken: Option[Token]

  /** ???
    * - Contains a "form" of optionality as concrete types may be default to `None`
    * - Should this really be part of the interface
    */
  def organisationalRestrictionLevel: OrganisationalRestrictionLevel
}

/** ???
  * - Adds only sessionId
  * - Should this really be part of the interface
  */
sealed trait UserInfo extends AuthInfo {
  def sessionId: Id
}

case class EmployeeInfo(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // User
  , sessionId: Id
  // Employee
  , employeeId: Id
  , team: Option[OrganisationalUnit]
  , organisationalRestrictions: OrganisationalRestrictions
) extends UserInfo

case class CustomerInfo(
   // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
   // User
  , sessionId: Id
   // Customer
  , customerId: Id
  , encryptedCustomerId: Option[Id]
) extends UserInfo

/** ???
  * - AuthInfo with UserInfo
  * - Nil case
  */
case class AnonymousInfo(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // User
  , sessionId: Id
) extends UserInfo

/** ???
  * -
  */
case class ContactingApiInfo(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // ContactingApi
  , name: String
) extends AuthInfo

case class ForeignApiInfo(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // ForeignApi
  , name: String
) extends AuthInfo

