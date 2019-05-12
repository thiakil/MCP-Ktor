package com.thiakil.ktor.openid

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * OpenID Connect Discovery Metadata representation.
 *
 * JSON SHOULD be deserialized to IGNORE unknown properties
 */
data class DiscoveryMetadata(
    /**
     * REQUIRED. URL using the https scheme with no query or fragment component that the OP asserts as its Issuer Identifier. If Issuer discovery is supported (see Section 2), this value MUST be identical to the issuer value returned by WebFinger. This also MUST be identical to the iss Claim value in ID Tokens issued from this Issuer.
     */
    @JsonProperty("issuer")
    val issuer: String? = null,

    /**
     * REQUIRED. URL of the OP's OAuth 2.0 Authorization Endpoint \[OpenID.Core].
     */
    @JsonProperty("authorization_endpoint")
    val authorizationEndpoint: String? = null,

    /**
     * URL of the OP's OAuth 2.0 Token Endpoint \[OpenID.Core]. This is REQUIRED unless only the Implicit Flow is used.
     */
    @JsonProperty("token_endpoint")
    val tokenEndpoint: String? = null,

    /**
     * RECOMMENDED. URL of the OP's UserInfo Endpoint \[OpenID.Core]. This URL MUST use the https scheme and MAY contain port, path, and query parameter components.
     */
    @JsonProperty("userinfo_endpoint")
    val userinfoEndpoint: String? = null,

    /**
     * REQUIRED. URL of the OP's JSON Web Key Set \[JWK] document. This contains the signing key(s) the RP uses to validate signatures from the OP. The JWK Set MAY also contain the Server's encryption key(s), which are used by RPs to encrypt requests to the Server. When both signing and encryption keys are made available, a use (Key Use) parameter value is REQUIRED for all keys in the referenced JWK Set to indicate each key's intended usage. Although some algorithms allow the same key to be used for both signatures and encryption, doing so is NOT RECOMMENDED, as it is less secure. The JWK x5c parameter MAY be used to provide X.509 representations of keys provided. When used, the bare key values MUST still be present and MUST match those in the certificate.
     */
    @JsonProperty("jwks_uri")
    val jwksUri: String? = null,

    /**
     * RECOMMENDED. URL of the OP's Dynamic Client Registration Endpoint \[OpenID.Registration].
     */
    @JsonProperty("registration_endpoint")
    val registrationEndpoint: String? = null,

    /**
     * RECOMMENDED. JSON array containing a list of the OAuth 2.0 \[RFC6749] scope values that this server supports. The server MUST support the openid scope value. Servers MAY choose not to advertise some supported scope values even when this parameter is used, although those defined in \[OpenID.Core] SHOULD be listed, if supported.
     */
    @JsonProperty("scopes_supported")
    val scopesSupported: Array<String>? = null,

    /**
     * REQUIRED. JSON array containing a list of the OAuth 2.0 response_type values that this OP supports. Dynamic OpenID Providers MUST support the code, id_token, and the token id_token Response Type values.
     */
    @JsonProperty("response_types_supported")
    val responseTypesSupported: Array<String> = arrayOf("code", "id_token", "token id_token"),

    /**
     * OPTIONAL. JSON array containing a list of the OAuth 2.0 response_mode values that this OP supports, as specified in OAuth 2.0 Multiple Response Type Encoding Practices \[OAuth.Responses]. If omitted, the default for Dynamic OpenID Providers is ["query", "fragment"].
     */
    @JsonProperty("response_modes_supported")
    val responseModesSupported: Array<String> = arrayOf("query", "fragment"),

    /**
     * OPTIONAL. JSON array containing a list of the OAuth 2.0 Grant Type values that this OP supports. Dynamic OpenID Providers MUST support the authorization_code and implicit Grant Type values and MAY support other Grant Types. If omitted, the default value is ["authorization_code", "implicit"].
     */
    @JsonProperty("grant_types_supported")
    val grantTypesSupported: Array<String> = arrayOf("authorization_code", "implicit"),

    /**
     * OPTIONAL. JSON array containing a list of the Authentication Context Class References that this OP supports.
     */
    @JsonProperty("acr_values_supported")
    val acrValuesSupported: Array<String>? = null,

    /**
     * REQUIRED. JSON array containing a list of the Subject Identifier types that this OP supports. Valid types include pairwise and public.
     */
    @JsonProperty("subject_types_supported")
    val subjectTypesSupported: Array<String>? = null,

    /**
     * REQUIRED. JSON array containing a list of the JWS signing algorithms (alg values) supported by the OP for the ID Token to encode the Claims in a JWT \[JWT]. The algorithm RS256 MUST be included. The value none MAY be supported, but MUST NOT be used unless the Response Type used returns no ID Token from the Authorization Endpoint (such as when using the Authorization Code Flow).
     */
    @JsonProperty("id_token_signing_alg_values_supported")
    val idTokenSigningAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWE encryption algorithms (alg values) supported by the OP for the ID Token to encode the Claims in a JWT \[JWT].
     */
    @JsonProperty("id_token_encryption_alg_values_supported")
    val idTokenEncryptionAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) supported by the OP for the ID Token to encode the Claims in a JWT \[JWT].
     */
    @JsonProperty("id_token_encryption_enc_values_supported")
    val idTokenEncryptionEncValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWS \[JWS] signing algorithms (alg values) \[JWA] supported by the UserInfo Endpoint to encode the Claims in a JWT \[JWT]. The value none MAY be included.
     */
    @JsonProperty("userinfo_signing_alg_values_supported")
    val userinfoSigningAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWE \[JWE] encryption algorithms (alg values) \[JWA] supported by the UserInfo Endpoint to encode the Claims in a JWT \[JWT].
     */
    @JsonProperty("userinfo_encryption_alg_values_supported")
    val userinfoEncryptionAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) \[JWA] supported by the UserInfo Endpoint to encode the Claims in a JWT \[JWT].
     */
    @JsonProperty("userinfo_encryption_enc_values_supported")
    val userinfoEncryptionEncValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the OP for Request Objects, which are described in Section 6.1 of OpenID Connect Core 1.0 \[OpenID.Core]. These algorithms are used both when the Request Object is passed by value (using the request parameter) and when it is passed by reference (using the request_uri parameter). Servers SHOULD support none and RS256.
     */
    @JsonProperty("request_object_signing_alg_values_supported")
    val requestObjectSigningAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWE encryption algorithms (alg values) supported by the OP for Request Objects. These algorithms are used both when the Request Object is passed by value and when it is passed by reference.
     */
    @JsonProperty("request_object_encryption_alg_values_supported")
    val requestObjectEncryptionAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWE encryption algorithms (enc values) supported by the OP for Request Objects. These algorithms are used both when the Request Object is passed by value and when it is passed by reference.
     */
    @JsonProperty("request_object_encryption_enc_values_supported")
    val requestObjectEncryptionEncValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of Client Authentication methods supported by this Token Endpoint. The options are client_secret_post, client_secret_basic, client_secret_jwt, and private_key_jwt, as described in Section 9 of OpenID Connect Core 1.0 \[OpenID.Core]. Other authentication methods MAY be defined by extensions. If omitted, the default is client_secret_basic -- the HTTP Basic Authentication Scheme specified in Section 2.3.1 of OAuth 2.0 \[RFC6749].
     */
    @JsonProperty("token_endpoint_auth_methods_supported")
    val tokenEndpointAuthMethodsSupported: Array<String> = arrayOf("client_secret_basic"),

    /**
     * OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the Token Endpoint for the signature on the JWT \[JWT] used to authenticate the Client at the Token Endpoint for the private_key_jwt and client_secret_jwt authentication methods. Servers SHOULD support RS256. The value none MUST NOT be used.
     */
    @JsonProperty("token_endpoint_auth_signing_alg_values_supported")
    val tokenEndpointAuthSigningAlgValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the display parameter values that the OpenID Provider supports. These values are described in Section 3.1.2.1 of OpenID Connect Core 1.0 \[OpenID.Core].
     */
    @JsonProperty("display_values_supported")
    val displayValuesSupported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the Claim Types that the OpenID Provider supports. These Claim Types are described in Section 5.6 of OpenID Connect Core 1.0 \[OpenID.Core]. Values defined by this specification are normal, aggregated, and distributed. If omitted, the implementation supports only normal Claims.
     */
    @JsonProperty("claim_types_supported")
    val claimTypesSupported: Array<String>? = null,

    /**
     * RECOMMENDED. JSON array containing a list of the Claim Names of the Claims that the OpenID Provider MAY be able to supply values for. Note that for privacy or other reasons, this might not be an exhaustive list.
     */
    @JsonProperty("claims_supported")
    val claimsSupported: Array<String>? = null,

    /**
     * OPTIONAL. URL of a page containing human-readable information that developers might want or need to know when using the OpenID Provider. In particular, if the OpenID Provider does not support Dynamic Client Registration, then information on how to register Clients needs to be provided in this documentation.
     */
    @JsonProperty("service_documentation")
    val serviceDocumentation: String? = null,

    /**
     * OPTIONAL. Languages and scripts supported for values in Claims being returned, represented as a JSON array of BCP47 \[RFC5646] language tag values. Not all languages and scripts are necessarily supported for all Claim values.
     */
    @JsonProperty("claims_locales_supported")
    val claimsLocalesSupported: Array<String>? = null,

    /**
     * OPTIONAL. Languages and scripts supported for the user interface, represented as a JSON array of BCP47 \[RFC5646] language tag values.
     */
    @JsonProperty("ui_locales_supported")
    val uiLocalesSupported: Array<String>? = null,

    /**
     * OPTIONAL. Boolean value specifying whether the OP supports use of the claims parameter, with true indicating support. If omitted, the default value is false.
     */
    @JsonProperty("claims_parameter_supported")
    val claimsParameterSupported: Boolean = false,

    /**
     * OPTIONAL. Boolean value specifying whether the OP supports use of the request parameter, with true indicating support. If omitted, the default value is false.
     */
    @JsonProperty("request_parameter_supported")
    val requestParameterSupported: Boolean = false,

    /**
     * OPTIONAL. Boolean value specifying whether the OP supports use of the request_uri parameter, with true indicating support. If omitted, the default value is true.
     */
    @JsonProperty("request_uri_parameter_supported")
    val requestUriParameterSupported: Boolean = true,

    /**
     * OPTIONAL. Boolean value specifying whether the OP requires any request_uri values used to be pre-registered using the request_uris registration parameter. Pre-registration is REQUIRED when the value is true. If omitted, the default value is false.
     */
    @JsonProperty("require_request_uri_registration")
    val requireRequestUriRegistration: Boolean = false,

    /**
     * OPTIONAL. URL that the OpenID Provider provides to the person registering the Client to read about the OP's requirements on how the Relying Party can use the data provided by the OP. The registration process SHOULD display this URL to the person registering the Client if it is given.
     */
    @JsonProperty("op_policy_uri")
    val opPolicyUri: String? = null,

    /**
     * OPTIONAL. URL that the OpenID Provider provides to the person registering the Client to read about OpenID Provider's terms of service. The registration process SHOULD display this URL to the person registering the Client if it is given.
     */
    @JsonProperty("op_tos_uri")
    val opTOSUri: String? = null,

    //region OpenID Connect Session Management
    /**
     * REQUIRED. URL of an OP iframe that supports cross-origin communications for session state information with the RP Client, using the HTML5 postMessage API. The page is loaded from an invisible iframe embedded in an RP page so that it can run in the OP's security context. It accepts postMessage requests from the relevant RP iframe and uses postMessage to post back the login status of the End-User at the OP.
     */
    @JsonProperty("check_session_iframe")
    val check_session_iframe: String? = null,

    /**
     * REQUIRED. URL at the OP to which an RP can perform a redirect to request that the End-User be logged out at the OP.
     */
    @JsonProperty("end_session_endpoint")
    val end_session_endpoint: String? = null,
    //endregion

    //region OAuth Discovery extras - https://tools.ietf.org/html/draft-ietf-oauth-discovery-06
    /**
     * OPTIONAL. URL of the authorization server's OAuth 2.0 revocation endpoint \[RFC7009].
     */
    @JsonProperty("revocation_endpoint")
    val revocation_endpoint: String? = null,

    /**
     * OPTIONAL. JSON array containing a list of client authentication methods supported by this revocation endpoint. The valid client authentication method values are those registered in the IANA "OAuth Token Endpoint Authentication Methods" registry \[IANA.OAuth.Parameters].
     */
    @JsonProperty("revocation_endpoint_auth_methods_supported")
    val revocation_endpoint_auth_methods_supported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the revocation endpoint for the signature on the JWT [JWT] used to authenticate the client at the revocation endpoint for the private_key_jwt and client_secret_jwt authentication methods. The value none MUST NOT be used.
     */
    @JsonProperty("revocation_endpoint_auth_signing_alg_values_supported")
    val revocation_endpoint_auth_signing_alg_values_supported: Array<String>? = null,

    /**
     * OPTIONAL. URL of the authorization server's OAuth 2.0 introspection endpoint \[RFC7662].
     */
    @JsonProperty("introspection_endpoint")
    val introspection_endpoint: String? = null,

    /**
     * OPTIONAL. JSON array containing a list of client authentication methods supported by this introspection endpoint. The valid client authentication method values are those registered in the IANA "OAuth Token Endpoint Authentication Methods" registry \[IANA.OAuth.Parameters] or those registered in the IANA "OAuth Access Token Types" registry \[IANA.OAuth.Parameters]. (These values are and will remain distinct, due to Section 7.2.)
     */
    @JsonProperty("introspection_endpoint_auth_methods_supported")
    val introspection_endpoint_auth_methods_supported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of the JWS signing algorithms (alg values) supported by the introspection endpoint for the signature on the JWT \[JWT] used to authenticate the client at the introspection endpoint for the private_key_jwt and client_secret_jwt authentication methods. The value none MUST NOT be used.
     */
    @JsonProperty("introspection_endpoint_auth_signing_alg_values_supported")
    val introspection_endpoint_auth_signing_alg_values_supported: Array<String>? = null,

    /**
     * OPTIONAL. JSON array containing a list of PKCE \[RFC7636] code challenge methods supported by this authorization server. Code challenge method values are used in the code_challenge_method parameter defined in Section 4.3 of \[RFC7636]. The valid code challenge method values are those registered in the IANA "PKCE Code Challenge Methods" registry \[IANA.OAuth.Parameters].
     */
    @JsonProperty("code_challenge_methods_supported")
    val code_challenge_methods_supported: Array<String>? = null
    //endregion
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoveryMetadata

        if (issuer != other.issuer) return false
        if (authorizationEndpoint != other.authorizationEndpoint) return false
        if (tokenEndpoint != other.tokenEndpoint) return false
        if (userinfoEndpoint != other.userinfoEndpoint) return false
        if (jwksUri != other.jwksUri) return false
        if (registrationEndpoint != other.registrationEndpoint) return false
        if (scopesSupported != null) {
            if (other.scopesSupported == null) return false
            if (!scopesSupported.contentEquals(other.scopesSupported)) return false
        } else if (other.scopesSupported != null) return false
        if (!responseTypesSupported.contentEquals(other.responseTypesSupported)) return false
        if (!responseModesSupported.contentEquals(other.responseModesSupported)) return false
        if (!grantTypesSupported.contentEquals(other.grantTypesSupported)) return false
        if (acrValuesSupported != null) {
            if (other.acrValuesSupported == null) return false
            if (!acrValuesSupported.contentEquals(other.acrValuesSupported)) return false
        } else if (other.acrValuesSupported != null) return false
        if (subjectTypesSupported != null) {
            if (other.subjectTypesSupported == null) return false
            if (!subjectTypesSupported.contentEquals(other.subjectTypesSupported)) return false
        } else if (other.subjectTypesSupported != null) return false
        if (idTokenSigningAlgValuesSupported != null) {
            if (other.idTokenSigningAlgValuesSupported == null) return false
            if (!idTokenSigningAlgValuesSupported.contentEquals(other.idTokenSigningAlgValuesSupported)) return false
        } else if (other.idTokenSigningAlgValuesSupported != null) return false
        if (idTokenEncryptionAlgValuesSupported != null) {
            if (other.idTokenEncryptionAlgValuesSupported == null) return false
            if (!idTokenEncryptionAlgValuesSupported.contentEquals(other.idTokenEncryptionAlgValuesSupported)) return false
        } else if (other.idTokenEncryptionAlgValuesSupported != null) return false
        if (idTokenEncryptionEncValuesSupported != null) {
            if (other.idTokenEncryptionEncValuesSupported == null) return false
            if (!idTokenEncryptionEncValuesSupported.contentEquals(other.idTokenEncryptionEncValuesSupported)) return false
        } else if (other.idTokenEncryptionEncValuesSupported != null) return false
        if (userinfoSigningAlgValuesSupported != null) {
            if (other.userinfoSigningAlgValuesSupported == null) return false
            if (!userinfoSigningAlgValuesSupported.contentEquals(other.userinfoSigningAlgValuesSupported)) return false
        } else if (other.userinfoSigningAlgValuesSupported != null) return false
        if (userinfoEncryptionAlgValuesSupported != null) {
            if (other.userinfoEncryptionAlgValuesSupported == null) return false
            if (!userinfoEncryptionAlgValuesSupported.contentEquals(other.userinfoEncryptionAlgValuesSupported)) return false
        } else if (other.userinfoEncryptionAlgValuesSupported != null) return false
        if (userinfoEncryptionEncValuesSupported != null) {
            if (other.userinfoEncryptionEncValuesSupported == null) return false
            if (!userinfoEncryptionEncValuesSupported.contentEquals(other.userinfoEncryptionEncValuesSupported)) return false
        } else if (other.userinfoEncryptionEncValuesSupported != null) return false
        if (requestObjectSigningAlgValuesSupported != null) {
            if (other.requestObjectSigningAlgValuesSupported == null) return false
            if (!requestObjectSigningAlgValuesSupported.contentEquals(other.requestObjectSigningAlgValuesSupported)) return false
        } else if (other.requestObjectSigningAlgValuesSupported != null) return false
        if (requestObjectEncryptionAlgValuesSupported != null) {
            if (other.requestObjectEncryptionAlgValuesSupported == null) return false
            if (!requestObjectEncryptionAlgValuesSupported.contentEquals(other.requestObjectEncryptionAlgValuesSupported)) return false
        } else if (other.requestObjectEncryptionAlgValuesSupported != null) return false
        if (requestObjectEncryptionEncValuesSupported != null) {
            if (other.requestObjectEncryptionEncValuesSupported == null) return false
            if (!requestObjectEncryptionEncValuesSupported.contentEquals(other.requestObjectEncryptionEncValuesSupported)) return false
        } else if (other.requestObjectEncryptionEncValuesSupported != null) return false
        if (!tokenEndpointAuthMethodsSupported.contentEquals(other.tokenEndpointAuthMethodsSupported)) return false
        if (tokenEndpointAuthSigningAlgValuesSupported != null) {
            if (other.tokenEndpointAuthSigningAlgValuesSupported == null) return false
            if (!tokenEndpointAuthSigningAlgValuesSupported.contentEquals(other.tokenEndpointAuthSigningAlgValuesSupported)) return false
        } else if (other.tokenEndpointAuthSigningAlgValuesSupported != null) return false
        if (displayValuesSupported != null) {
            if (other.displayValuesSupported == null) return false
            if (!displayValuesSupported.contentEquals(other.displayValuesSupported)) return false
        } else if (other.displayValuesSupported != null) return false
        if (claimTypesSupported != null) {
            if (other.claimTypesSupported == null) return false
            if (!claimTypesSupported.contentEquals(other.claimTypesSupported)) return false
        } else if (other.claimTypesSupported != null) return false
        if (claimsSupported != null) {
            if (other.claimsSupported == null) return false
            if (!claimsSupported.contentEquals(other.claimsSupported)) return false
        } else if (other.claimsSupported != null) return false
        if (serviceDocumentation != other.serviceDocumentation) return false
        if (claimsLocalesSupported != null) {
            if (other.claimsLocalesSupported == null) return false
            if (!claimsLocalesSupported.contentEquals(other.claimsLocalesSupported)) return false
        } else if (other.claimsLocalesSupported != null) return false
        if (uiLocalesSupported != null) {
            if (other.uiLocalesSupported == null) return false
            if (!uiLocalesSupported.contentEquals(other.uiLocalesSupported)) return false
        } else if (other.uiLocalesSupported != null) return false
        if (claimsParameterSupported != other.claimsParameterSupported) return false
        if (requestParameterSupported != other.requestParameterSupported) return false
        if (requestUriParameterSupported != other.requestUriParameterSupported) return false
        if (requireRequestUriRegistration != other.requireRequestUriRegistration) return false
        if (opPolicyUri != other.opPolicyUri) return false
        if (opTOSUri != other.opTOSUri) return false
        if (check_session_iframe != other.check_session_iframe) return false
        if (end_session_endpoint != other.end_session_endpoint) return false
        if (revocation_endpoint != other.revocation_endpoint) return false
        if (revocation_endpoint_auth_methods_supported != null) {
            if (other.revocation_endpoint_auth_methods_supported == null) return false
            if (!revocation_endpoint_auth_methods_supported.contentEquals(other.revocation_endpoint_auth_methods_supported)) return false
        } else if (other.revocation_endpoint_auth_methods_supported != null) return false
        if (revocation_endpoint_auth_signing_alg_values_supported != null) {
            if (other.revocation_endpoint_auth_signing_alg_values_supported == null) return false
            if (!revocation_endpoint_auth_signing_alg_values_supported.contentEquals(other.revocation_endpoint_auth_signing_alg_values_supported)) return false
        } else if (other.revocation_endpoint_auth_signing_alg_values_supported != null) return false
        if (introspection_endpoint != other.introspection_endpoint) return false
        if (introspection_endpoint_auth_methods_supported != null) {
            if (other.introspection_endpoint_auth_methods_supported == null) return false
            if (!introspection_endpoint_auth_methods_supported.contentEquals(other.introspection_endpoint_auth_methods_supported)) return false
        } else if (other.introspection_endpoint_auth_methods_supported != null) return false
        if (introspection_endpoint_auth_signing_alg_values_supported != null) {
            if (other.introspection_endpoint_auth_signing_alg_values_supported == null) return false
            if (!introspection_endpoint_auth_signing_alg_values_supported.contentEquals(other.introspection_endpoint_auth_signing_alg_values_supported)) return false
        } else if (other.introspection_endpoint_auth_signing_alg_values_supported != null) return false
        if (code_challenge_methods_supported != null) {
            if (other.code_challenge_methods_supported == null) return false
            if (!code_challenge_methods_supported.contentEquals(other.code_challenge_methods_supported)) return false
        } else if (other.code_challenge_methods_supported != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = issuer?.hashCode() ?: 0
        result = 31 * result + (authorizationEndpoint?.hashCode() ?: 0)
        result = 31 * result + (tokenEndpoint?.hashCode() ?: 0)
        result = 31 * result + (userinfoEndpoint?.hashCode() ?: 0)
        result = 31 * result + (jwksUri?.hashCode() ?: 0)
        result = 31 * result + (registrationEndpoint?.hashCode() ?: 0)
        result = 31 * result + (scopesSupported?.contentHashCode() ?: 0)
        result = 31 * result + responseTypesSupported.contentHashCode()
        result = 31 * result + responseModesSupported.contentHashCode()
        result = 31 * result + grantTypesSupported.contentHashCode()
        result = 31 * result + (acrValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (subjectTypesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (idTokenSigningAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (idTokenEncryptionAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (idTokenEncryptionEncValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (userinfoSigningAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (userinfoEncryptionAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (userinfoEncryptionEncValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (requestObjectSigningAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (requestObjectEncryptionAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (requestObjectEncryptionEncValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + tokenEndpointAuthMethodsSupported.contentHashCode()
        result = 31 * result + (tokenEndpointAuthSigningAlgValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (displayValuesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (claimTypesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (claimsSupported?.contentHashCode() ?: 0)
        result = 31 * result + (serviceDocumentation?.hashCode() ?: 0)
        result = 31 * result + (claimsLocalesSupported?.contentHashCode() ?: 0)
        result = 31 * result + (uiLocalesSupported?.contentHashCode() ?: 0)
        result = 31 * result + claimsParameterSupported.hashCode()
        result = 31 * result + requestParameterSupported.hashCode()
        result = 31 * result + requestUriParameterSupported.hashCode()
        result = 31 * result + requireRequestUriRegistration.hashCode()
        result = 31 * result + (opPolicyUri?.hashCode() ?: 0)
        result = 31 * result + (opTOSUri?.hashCode() ?: 0)
        result = 31 * result + (check_session_iframe?.hashCode() ?: 0)
        result = 31 * result + (end_session_endpoint?.hashCode() ?: 0)
        result = 31 * result + (revocation_endpoint?.hashCode() ?: 0)
        result = 31 * result + (revocation_endpoint_auth_methods_supported?.contentHashCode() ?: 0)
        result = 31 * result + (revocation_endpoint_auth_signing_alg_values_supported?.contentHashCode() ?: 0)
        result = 31 * result + (introspection_endpoint?.hashCode() ?: 0)
        result = 31 * result + (introspection_endpoint_auth_methods_supported?.contentHashCode() ?: 0)
        result = 31 * result + (introspection_endpoint_auth_signing_alg_values_supported?.contentHashCode() ?: 0)
        result = 31 * result + (code_challenge_methods_supported?.contentHashCode() ?: 0)
        return result
    }
}