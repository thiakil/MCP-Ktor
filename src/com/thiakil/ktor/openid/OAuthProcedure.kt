@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.thiakil.ktor.openid

import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationFailedCause
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.AuthenticationProvider
import io.ktor.client.*
import org.slf4j.*

private val Logger: Logger = LoggerFactory.getLogger("com.thiakil.mcp.ktor.openid")

/**
 * OAuth provider key
 */
val OAuthKey: Any = "OpenID"

private fun OAuth2Exception.toAuthFailedCause(): AuthenticationFailedCause = AuthenticationFailedCause.Error(message!!)

/**
 * Represents an OAuth provider for [Authentication] feature
 */
class OAuthAuthenticationProvider(name: String?) : AuthenticationProvider(name) {
    /**
     * HTTP client instance used by this provider to make HTTP calls to OAuth server
     */
    lateinit var client: HttpClient

    /**
     * Lookup function to find OAuth server settings for the particular call
     */
    lateinit var providerLookup: ApplicationCall.() -> OAuthServerSettings?

    /**
     * URL provider that should produce login url for the particular call
     */
    lateinit var urlProvider: ApplicationCall.(OAuthServerSettings) -> String

    init {
        pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
            val provider = call.providerLookup() ?: return@intercept
            val token = call.oauth2HandleCallback()
            val callbackRedirectUrl = call.urlProvider(provider)
            if (token == null) {
                context.challenge(OAuthKey, AuthenticationFailedCause.NoCredentials) {
                    call.redirectAuthenticateOAuth2(
                        provider, callbackRedirectUrl,
                        state = provider.nonceManager.newNonce(),
                        scopes = provider.defaultScopes,
                        interceptor = provider.authorizeUrlInterceptor
                    )
                    it.complete()
                }
            } else {
                try {
                    println("token request: ${call.request.local}, ${call.request.headers}")
                    val accessToken = oauth2RequestAccessToken(client, provider, callbackRedirectUrl, token)
                    context.principal(accessToken)
                } catch (cause: OAuth2Exception.InvalidNonce) {
                    //AuthenticationFailedCause.Error("Nonce invalid")
                    Logger.error("Nonce failure: {}", cause.message)
                    context.error(OAuthKey, cause.toAuthFailedCause())
                } catch (cause: OAuth2Exception.InvalidGrant) {
                    Logger.error("OAuth invalid grant reported: {}", cause.message)
                    //AuthenticationFailedCause.InvalidCredentials
                    context.error(OAuthKey, cause.toAuthFailedCause())
                } catch (cause: Throwable) {
                    Logger.trace("OAuth2 request access token failed", cause)
                    context.error(
                        OAuthKey,
                        AuthenticationFailedCause.Error("Failed to request OAuth2 access token due to $cause")
                    )
                }
            }
        }
    }
}

/**
 * Installs OAuth Authentication mechanism
 */
fun Authentication.Configuration.openid(name: String? = null, configure: OAuthAuthenticationProvider.() -> Unit) {
    val provider = OAuthAuthenticationProvider(name).apply(configure)
    register(provider)
}