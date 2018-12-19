import org.http4s.Request

package object auth {

  type User[F[_], T] = Request[F] => F[T]

  /** ???
    * - Uppercase two-letter ISO-3166 code, e.g. `NL`, `BE`, etc.
    * - Deprecated !
    */
  type CountryCode = String

  /** ???
    * - Constraints or specification
    */
  type Id = String

  /** ???
    * - Constraints or specification
    * - Type Int, check leaking technical identifiers
    */
  type ClientId = Int

  /** ???
    * - Constraints or specification
    * - Type Int, check leaking technical identifiers
    */
  type CircleId = Int

  /** Organisational Unit */
  case class OrganisationalUnit(
     /** This organisational unit's client identifier, often abbreviated `cltId` */
     clientId: ClientId

     /** This organisational unit's client name, often abbreviated `cltName` */
     , clientName: String

     /** This organisational unit's circle identifier */
     , circleId: CircleId

     /** This organisational unit's circle name */
     , circleName: String

     /** This organisational unit's super circle identifier */
     , superCircleId: CircleId

     /** This organisational unit's super circle name */
     , superCircleName: String
   )

  /** ???
    * - Organisational restrictions is modeled as a set of organisational units
    */
  type OrganisationalRestrictions = Set[OrganisationalUnit]




  /** Twilio account details */

  /** ???
    * - What's the 's' stand for
    * - Constraints
    */
  type Sid = String

  /** ???
    * - Specification
    * - Constraints
    */
  type Token = String

  /** ???
    * - Config or dynamic
    * - Key set
    * - value space per key
    */
  type Mailing = Map[String, String]

  /** ???
    * - Config or dynamic
    * - Key set
    * - value space per key
    */
  type AccountSettings = Map[String, String]

  case class TwilioAccount(

      /** ???
        * - Used when
        */
      id: Long

      /** ???
        * - Used when
        */
    , sid: Sid

      /** ???
        * - Used when
        */
    , token: Token

      /** ???
        * - Used when
        */
    , friendlyName: String

      /** ???
        * - Used when
        */
    , messagingServiceSid: Sid

      /** ???
        * - Used when
        */
    , apiKeySid: Sid

      /** ???
        * - Used when
        * - Constraints
        */
    , apiKeySecret: String

      /** ???
        * - Used when
        */
    , workspaceSid: Sid

      /** ???
        * - Used when
        * - Specification
        */
    , timezone: String

      /** ???
        * - Used when
        */
    , reportMailing: Mailing

      /** ???
        * - Used when
        */
    , syncServiceSid: Sid

      /** ???
        * - Used when
        */
    , accountSettings: AccountSettings
  )

  /** Organisational Restriction Level */
  sealed abstract class OrganisationalRestrictionLevel(level: Int)
  object OrganisationalRestrictionLevel {
    object None        extends OrganisationalRestrictionLevel(0)
    object Self        extends OrganisationalRestrictionLevel(1)
    object Team        extends OrganisationalRestrictionLevel(2)
    object Circle      extends OrganisationalRestrictionLevel(3)
    object SuperCircle extends OrganisationalRestrictionLevel(4)
    object Account     extends OrganisationalRestrictionLevel(5)
  }

}
