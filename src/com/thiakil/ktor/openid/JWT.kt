package com.thiakil.ktor.openid

import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.Payload
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class OpenIDJWT(private val sourcePayload: Payload): Payload by sourcePayload {
    /**
     * End-User's full name in displayable form including all name parts, possibly including
     * titles and suffixes, ordered according to the End-User's locale and preferences.
     */
    val fullName: String? by lazy { sourcePayload.getClaim("name")?.asString() }

    /**
     * Given name(s) or first name(s) of the End-User. Note that in some cultures, people
     * can have multiple given names; all can be present, with the names being separated by space characters.
     */
    val givenName: String? by lazy { sourcePayload.getClaim("given_name")?.asString() }

    /**
     * Surname(s) or last name(s) of the End-User. Note that in some cultures, people can
     * have multiple family names or no family name; all can be present, with the names being separated by space characters.
     */
    val familyName: String? by lazy { sourcePayload.getClaim("family_name")?.asString() }

    /**
     * Middle name(s) of the End-User. Note that in some cultures, people can have
     * multiple middle names; all can be present, with the names being separated by space characters. Also note that
     * in some cultures, middle names are not used.
     */
    val middleName: String? by lazy { sourcePayload.getClaim("middle_name")?.asString() }

    /**
     * Casual name of the End-User that may or may not be the same as the `given_name`.
     * For instance, a `nickname` value of `Mike` might be returned alongside a `given_name` value
     * of `Michael`.
     */
    val nickname: String? by lazy { sourcePayload.getClaim("nickname")?.asString() }

    /**
     * Shorthand name by which the End-User wishes to be referred to at the RP,
     * such as `janedoe` or `j.doe`. This value MAY be any valid JSON string including special characters
     * such as `@`, `/`, or whitespace. The RP MUST NOT rely upon this value being unique, as discussed in
     * Section 5.7 (Claim Stability and
     * Uniqueness).
     */
    val preferredUsername: String? by lazy { sourcePayload.getClaim("preferred_username")?.asString() }

    /**
     * URL of the End-User's profile page. The contents of this Web page SHOULD be about the
     * End-User.
     */
    val profile: String? by lazy { sourcePayload.getClaim("profile")?.asString() }

    /**
     * URL of the End-User's profile picture. This URL MUST refer to an image file (for example,
     * a PNG, JPEG, or GIF image file), rather than to a Web page containing an image. Note that this URL SHOULD
     * specifically reference a profile photo of the End-User suitable for displaying when describing the End-User,
     * rather than an arbitrary photo taken by the End-User.
     */
    val picture: String? by lazy { sourcePayload.getClaim("picture")?.asString() }

    /**
     * URL of the End-User's Web page or blog. This Web page SHOULD contain information
     * published by the End-User or an organization that the End-User is affiliated with.
     */
    val website: String? by lazy { sourcePayload.getClaim("website")?.asString() }

    /**
     * End-User's preferred e-mail address. Its value MUST conform to the
     * RFC 5322 (Resnick, P., Ed., “Internet Message Format,” October 2008.) \[RFC5322] addr-spec syntax.
     * The RP MUST NOT rely upon this value being unique, as discussed in Section 5.7 (Claim Stability and Uniqueness).
     */
    val email: String? by lazy { sourcePayload.getClaim("email")?.asString() }

    /**
     * True if the End-User's e-mail address has been verified; otherwise false.
     * When this Claim Value is `true`, this means that the OP took affirmative steps to ensure that this
     * e-mail address was controlled by the End-User at the time the verification was performed. The means by which
     * an e-mail address is verified is context-specific, and dependent upon the trust framework or contractual
     * agreements within which the parties are operating.
     */
    val emailVerified: Boolean by lazy { sourcePayload.getClaim("email_verified")?.asBoolean() ?: false }

    /**
     * End-User's gender. Values defined by this specification are `female` and
     * `male`. Other values MAY be used when neither of the defined values are applicable.
     */
    val gender: String? by lazy { sourcePayload.getClaim("gender")?.asString() }

    /**
     * End-User's birthday, represented as an ISO 8601:2004 (International Organization for
     * Standardization, “ISO 8601:2004. Data elements and interchange formats - Information interchange - Representation
     * of dates and times,” 2004.) [ISO8601‑2004] `YYYY-MM-DD` format. The year MAY be `0000`, indicating that it is omitted.
     * To represent only the year, `YYYY` format is allowed. Note that depending on the underlying platform's date related
     * function, providing just year can result in varying month and day, so the implementers need to take this factor
     * into account to correctly process the dates.
     */
    val birthdate: String? by lazy { sourcePayload.getClaim("birthdate")?.asString() }

    /**
     * String from zoneinfo \[zoneinfo] (Public Domain, “The tz database,” June 2011.) time zone
     * database representing the End-User's time zone. For example, `Europe/Paris` or `America/Los_Angeles`.
     */
    val zoneinfo: String? by lazy { sourcePayload.getClaim("zoneinfo")?.asString() }

    /**
     * End-User's locale, represented as a BCP47
     * (Phillips, A. and M. Davis, “Tags for Identifying Languages,” September 2009.)
     * \[RFC5646] language tag. This is typically an ISO 639-1 Alpha-2
     * (International Organization for Standardization, “ISO 639-1:2002. Codes for the
     * representation of names of languages -- Part 1: Alpha-2 code,” 2002.) [ISO639‑1] language
     * code in lowercase and an ISO 3166-1 Alpha-2
     * (International Organization for Standardization, “ISO 3166-1:1997. Codes for
     * the representation of names of countries and their subdivisions -- Part 1: Country codes,” 1997.)
     * [ISO3166‑1] country code in uppercase, separated by a dash. For example, `en-US` or `fr-CA`. As
     * a compatibility note, some implementations have used an underscore as the separator rather than a dash, for
     * example, `en_US`; Relying Parties MAY choose to accept this locale syntax as well.
     */
    val locale: String? by lazy { sourcePayload.getClaim("locale")?.asString() }

    /**
     * End-User's preferred telephone number. E.164
     * (International Telecommunication Union, “E.164: The international public
     * telecommunication numbering plan,” 2010.) \[E.164] is RECOMMENDED as the format of this
     * Claim, for example, `+1 (425) 555-1212` or `+56 (2) 687 2400`. If the phone number contains an
     * extension, it is RECOMMENDED that the extension be represented using the RFC 3966
     * (Schulzrinne, H., “The tel URI for Telephone Numbers,” December 2004.)
     * \[RFC3966] extension syntax, for example, `+1 (604) 555-1234;ext=5678`.
     */
    val phoneNumber: String? by lazy { sourcePayload.getClaim("phone_number")?.asString() }

    /**
     * True if the End-User's phone number has been verified; otherwise false.
     * When this Claim Value is `true`, this means that the OP took affirmative steps to ensure that this phone
     * number was controlled by the End-User at the time the verification was performed. The means by which a phone
     * number is verified is context-specific, and dependent upon the trust framework or contractual agreements within
     * which the parties are operating. When true, the `phone_number` Claim MUST be in E.164 format and any
     * extensions MUST be represented in RFC 3966 format.
     */
    val phoneNumberVerified: Boolean by lazy { sourcePayload.getClaim("phone_number_verified")?.asBoolean() ?: false }

    /**
     * End-User's preferred postal address. The value of the `address` member is a
     * JSON \[RFC4627] (Crockford, D., “The
     * application/json Media Type for JavaScript Object Notation (JSON),” July 2006.)
     * structure containing some or all of the members defined in
     * Section 5.1.1 (Address Claim).
     */
    val address: OpenIDAddress? by lazy {
        try {
            sourcePayload.getClaim("address")?.`as`(OpenIDAddress::class.java)
        }  catch (exception: JWTDecodeException) {
            null
        }
    }

    /**
     * Time when the End-User authentication occurred. Its value is a JSON number representing
     * the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time. When a max_age request is
     * made or when auth_time is requested as an Essential Claim, then this Claim is REQUIRED; otherwise, its inclusion
     * is OPTIONAL. (The auth_time Claim semantically corresponds to the OpenID 2.0 PAPE \[OpenID.PAPE] auth_time
     * response parameter.)
     */
    val authTimestamp: Date? by lazy { sourcePayload.getClaim("auth_time")?.asDate() }

    /**
     * String value used to associate a Client session with an ID Token, and to mitigate replay
     * attacks. The value is passed through unmodified from the Authentication Request to the ID Token. If present in
     * the ID Token, Clients MUST verify that the nonce Claim Value is equal to the value of the nonce parameter sent
     * in the Authentication Request. If present in the Authentication Request, Authorization Servers MUST include a
     * nonce Claim in the ID Token with the Claim Value being the nonce value sent in the Authentication Request.
     * Authorization Servers SHOULD perform no other processing on nonce values used. The nonce value is a case
     * sensitive string.
     */
    val nonce: String? by lazy { sourcePayload.getClaim("nonce")?.asString() }

    /**
     * OPTIONAL. Authentication Context Class Reference. String specifying an Authentication Context
     * Class Reference value that identifies the Authentication Context Class that the authentication performed satisfied.
     * The value "0" indicates the End-User authentication did not meet the requirements of ISO/IEC 29115 \[ISO29115]
     * level 1. Authentication using a long-lived browser cookie, for instance, is one example where the use of "level 0"
     * is appropriate. Authentications with level 0 SHOULD NOT be used to authorize access to any resource of any monetary
     * value. (This corresponds to the OpenID 2.0 PAPE \[OpenID.PAPE] nist_auth_level 0.) An absolute URI or an RFC 6711
     * \[RFC6711] registered name SHOULD be used as the acr value; registered names MUST NOT be used with a different
     * meaning than that which is registered. Parties using this claim will need to agree upon the meanings of the values
     * used, which may be context-specific. The acr value is a case sensitive string.
     */
    val authenticationContextReference: String? by lazy { sourcePayload.getClaim("acr")?.asString() }

    /**
     * OPTIONAL. Authentication Methods References. JSON array of strings that are identifiers for
     * authentication methods used in the authentication. For instance, values might indicate that both password and
     * OTP authentication methods were used. The definition of particular values to be used in the amr Claim is beyond
     * the scope of this specification. Parties using this claim will need to agree upon the meanings of the values used,
     * which may be context-specific. The amr value is an array of case sensitive strings.
     */
    val authenticationMethodsReference: Array<String>? by lazy {
        try {
            sourcePayload.getClaim("amr")?.asArray(String::class.java)
        } catch (exception: JWTDecodeException) {
            null
        }
    }

    /**
     * OPTIONAL. Authorized party - the party to which the ID Token was issued. If present, it MUST
     * contain the OAuth 2.0 Client ID of this party. This Claim is only needed when the ID Token has a single audience
     * value and that audience is different than the authorized party. It MAY be included even when the authorized party
     * is the same as the sole audience. The azp value is a case sensitive string containing a StringOrURI value.
     */
    val authorizedParty: String? by lazy { sourcePayload.getClaim("azp")?.asString() }

    override fun toString():String {
        return "OpenIDJWT(issuer=$issuer, "+
                "subject=$subject, "+
                "audience=$audience, "+
                "expiresAt=$expiresAt, "+
                "notBefore=$notBefore, "+
                "issuedAt=$issuedAt, "+
                "jti=$id, "+
                "fullName=$fullName, "+
                "givenName=$givenName, "+
                "familyName=$familyName, "+
                "middleName=$middleName, "+
                "nickname=$nickname, "+
                "preferredUsername=$preferredUsername, "+
                "profile=$profile, "+
                "picture=$picture, "+
                "website=$website, "+
                "email=$email, "+
                "emailVerified=$emailVerified, "+
                "gender=$gender, "+
                "birthdate=$birthdate, "+
                "zoneinfo=$zoneinfo, "+
                "locale=$locale, "+
                "phoneNumber=$phoneNumber, "+
                "phoneNumberVerified=$phoneNumberVerified, "+
                "address=$address, "+
                "authTimestamp=$authTimestamp, "+
                "nonce=$nonce, "+
                "authenticationContextReference=$authenticationContextReference, "+
                "authenticationMethodsReference=$authenticationMethodsReference, "+
                "authorizedParty=$authorizedParty)"
    }
}

data class OpenIDAddress(
    /**
     * Full mailing address, formatted for display or use on a mailing label. This field MAY contain multiple lines,
     * separated by newlines. Newlines can be represented either as a carriage return/line feed pair ("\r\n") or as
     * a single line feed character ("\n").
     */
    @JsonProperty("formatted")
    val formatted: String? = null,

    /**
     * Full street address component, which MAY include house number, street name, Post Office Box, and multi-line
     * extended street address information. This field MAY contain multiple lines, separated by newlines.
     * Newlines can be represented either as a carriage return/line feed pair ("\r\n") or as a single line feed
     * character ("\n").
     */
    @JsonProperty("street_address")
    val streetAddress: String? = null,

    /**
     * City or locality component.
     */
    @JsonProperty("locality")
    val locality: String? = null,

    /**
     * State, province, prefecture, or region component.
     */
    @JsonProperty("region")
    val region: String? = null,

    /**
     * Zip code or postal code component.
     */
    @JsonProperty("postal_code")
    val postalCode: String? = null,

    /**
     * Country name component.
     */
    @JsonProperty("country")
    val country: String? = null
)