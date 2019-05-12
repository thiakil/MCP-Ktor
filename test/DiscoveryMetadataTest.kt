package com.thiakil.mcp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.thiakil.ktor.openid.DiscoveryMetadata
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Created by Thiakil on 29/04/2019.
 */
class DiscoveryMetadataTest {
    @Test
    fun testString(){
        val decoded: DiscoveryMetadata = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(GOOGLE_DISCOVERY_FILE)
        assertEquals("https://accounts.google.com", decoded.issuer, "Issuer not found")
        assert(decoded.responseTypesSupported.contains("code token id_token")){"response_types_supported parse failed"}
    }

    companion object{
        @Language("JSON")
        val GOOGLE_DISCOVERY_FILE = """
{
 "issuer": "https://accounts.google.com",
 "authorization_endpoint": "https://accounts.google.com/o/oauth2/v2/auth",
 "token_endpoint": "https://oauth2.googleapis.com/token",
 "userinfo_endpoint": "https://openidconnect.googleapis.com/v1/userinfo",
 "revocation_endpoint": "https://oauth2.googleapis.com/revoke",
 "jwks_uri": "https://www.googleapis.com/oauth2/v3/certs",
 "response_types_supported": [
  "code",
  "token",
  "id_token",
  "code token",
  "code id_token",
  "token id_token",
  "code token id_token",
  "none"
 ],
 "subject_types_supported": [
  "public"
 ],
 "id_token_signing_alg_values_supported": [
  "RS256"
 ],
 "scopes_supported": [
  "openid",
  "email",
  "profile"
 ],
 "token_endpoint_auth_methods_supported": [
  "client_secret_post",
  "client_secret_basic"
 ],
 "claims_supported": [
  "aud",
  "email",
  "email_verified",
  "exp",
  "family_name",
  "given_name",
  "iat",
  "iss",
  "locale",
  "name",
  "picture",
  "sub"
 ],
 "code_challenge_methods_supported": [
  "plain",
  "S256"
 ]
}
""".trimIndent()
    }
}