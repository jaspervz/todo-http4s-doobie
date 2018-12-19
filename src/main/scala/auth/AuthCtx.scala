package auth

/** ???
  * - Noted here but prevalent, almost no diff between types and interfaces !
  * - AuthCtx is either optional or deprecated !
  * - UserCtx is an interface indeed for the user (person)
  * - *ApiInfos are of the same type, add only a name, and concrete for the user (system)
  */
sealed trait AuthCtx {

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
    * - Duplicates TwilioAccount.token
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
sealed trait UserCtx extends AuthCtx {
  def sessionId: Id
}

case class EmployeeCtx(
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
  , organisationalRestrictions: Set[OrganisationalUnit]
) extends UserCtx

case class CustomerCtx(
   // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
   // User
  , sessionId: Id
   // Customer
  , customerId: Id
  , encryptedCustomerId: Option[String]
) extends UserCtx

/** ???
  * - AuthCtx with UserCtx
  * - Nil case
  */
case class AnonymousCtx(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // User
  , sessionId: Id
) extends UserCtx

case class ContactingApiCtx(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // ContactingApi
  , name: String
) extends AuthCtx

case class ForeignApiCtx(
  // Auth
    countryCode: CountryCode
  , subAccount: Option[TwilioAccount]
  , accountToken: Option[Token]
  , organisationalRestrictionLevel: OrganisationalRestrictionLevel
  // ForeignApi
  , name: String
) extends AuthCtx


/** Normalised, i.e. still very dirty and very implicit ... but what else can we expect */

sealed trait AuthType
sealed trait PersonType
sealed trait SystemType
case object Anonymous extends AuthType with PersonType
case object Employee  extends AuthType with PersonType
case object Customer  extends AuthType with PersonType
case object Internal  extends AuthType with SystemType
case object External  extends AuthType with SystemType

sealed abstract class Restriction(value: Int)
object Restriction {
  object Self        extends Restriction(1)
  object Team        extends Restriction(2)
  object Circle      extends Restriction(3)
  object SuperCircle extends Restriction(4)
  object Account     extends Restriction(5)
}

sealed trait AuthInfo {
  def authType:                       AuthType
  // Context
  def organisationalRestrictionLevel: Option[Restriction]
  def countryCode:                    Option[CountryCode]
  def twilioAccount:                  Option[TwilioAccount]
  def accountToken:                   Option[Token]
  // Person
  def sessionId:                      Option[Id]
  // Customer
  def customerId:                     Option[Id]
  def encryptedCustomerId:            Option[String]
  // Employee
  def employeeId:                     Option[Id]
  def team:                           Option[OrganisationalUnit]
  def organisationalRestrictions:     Set[OrganisationalUnit]
  // System
  def name:                           Option[String]
}
