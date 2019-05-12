@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.thiakil.ktor.openid

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.*
import io.ktor.auth.Principal
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import io.ktor.response.*
import io.ktor.util.*
import kotlinx.coroutines.*
import org.slf4j.*
import java.io.*

private val Logger: Logger = LoggerFactory.getLogger("com.thiakil.mcp.ktor.openid")

/**
 * OAuth versions used in configuration
 */
@Suppress("KDocMissingDocumentation")
enum class OAuthVersion {
    V20
}

/**
 * Represents OAuth server settings
 * @property name configuration name
 * @property version OAuth version (1a or 2)
 * @property authorizeUrl OAuth server authorization page URL
 * @property accessTokenUrl OAuth server access token request URL
 * @property requestMethod HTTP request method to be used to acquire access token (see vendors documentation)
 * @property clientId client id parameter (provided by OAuth server vendor)
 * @property clientSecret client secret parameter (provided by OAuth server vendor)
 * @property defaultScopes OAuth scopes used by default
 * @property accessTokenRequiresBasicAuth to send BASIC auth header when an access token is requested
 * @property nonceManager to be used to produce and verify nonce values
 * @property authorizeUrlInterceptor an interceptor function to customize authorization URL
 */
sealed class OAuthServerSettings(val name: String, val version: OAuthVersion,
                                 val requestMethod: HttpMethod,
                                 val clientId: String,
                                 val clientSecret: String,
                                 val defaultScopes: List<String>,
                                 val accessTokenRequiresBasicAuth: Boolean,
                                 val nonceManager: NonceManager,
                                 val authorizeUrlInterceptor: URLBuilder.() -> Unit) {
    abstract val authorizeUrl: String
    abstract val accessTokenUrl: String

    init {
        if (!defaultScopes.contains("openid")){
            Logger.warn("'openid' scope not specified in default scopes (${defaultScopes.joinToString()}), the spec says this behavior is undefined!")
        }
    }


    /**
     * OAuthServerSettings that are determined via some external mechanism
     */
    class Fixed constructor(
        name: String,
        override val authorizeUrl: String,
        override val accessTokenUrl: String,
        requestMethod: HttpMethod = HttpMethod.Get,
        clientId: String,
        clientSecret: String,
        defaultScopes: List<String> = listOf("openid"),
        accessTokenRequiresBasicAuth: Boolean = false,
        nonceManager: NonceManager = GenerateOnlyNonceManager,
        authorizeUrlInterceptor: URLBuilder.() -> Unit = {}
    ) : OAuthServerSettings(name, OAuthVersion.V20, requestMethod, clientId, clientSecret, defaultScopes, accessTokenRequiresBasicAuth, nonceManager, authorizeUrlInterceptor)

    open class FromDiscoveryMetadata(name: String,
                                     open val discoveryMetadata: DiscoveryMetadata,
                                     requestMethod: HttpMethod = HttpMethod.Get,
                                     clientId: String,
                                     clientSecret: String,
                                     defaultScopes: List<String> = listOf("openid"),
                                     accessTokenRequiresBasicAuth: Boolean = false,
                                     nonceManager: NonceManager = GenerateOnlyNonceManager,
                                     authorizeUrlInterceptor: URLBuilder.() -> Unit = {}
    ): OAuthServerSettings(name, OAuthVersion.V20, requestMethod, clientId, clientSecret, defaultScopes, accessTokenRequiresBasicAuth, nonceManager, authorizeUrlInterceptor){
        override val authorizeUrl: String
            get() = discoveryMetadata.authorizationEndpoint ?: throw OAuth2Exception.InvalidDiscoveryFile("authorisation_endpoint field is missing")
        override val accessTokenUrl: String
            get() = discoveryMetadata.tokenEndpoint ?: throw OAuth2Exception.InvalidDiscoveryFile("token_endpoint field is missing")
    }

    class FromDiscoveryURL(name: String,
                           private val discoveryUrl: String,
                           private val client: HttpClient = HttpClient(),
                           requestMethod: HttpMethod = HttpMethod.Get,
                           clientId: String,
                           clientSecret: String,
                           defaultScopes: List<String> = listOf("openid"),
                           accessTokenRequiresBasicAuth: Boolean = false,
                           nonceManager: NonceManager = GenerateOnlyNonceManager,
                           authorizeUrlInterceptor: URLBuilder.() -> Unit = {}
    ) : FromDiscoveryMetadata(name, DUMMY_DISCOVERY, requestMethod, clientId, clientSecret, defaultScopes, accessTokenRequiresBasicAuth, nonceManager, authorizeUrlInterceptor){
        override val discoveryMetadata: DiscoveryMetadata by lazy {
            runBlocking {
                JACKSON.readValue<DiscoveryMetadata>(client.get<String>(discoveryUrl))
            }
        }
    }

    class FromDiscoveryFile(name: String, discoveryFile: File,
                            requestMethod: HttpMethod = HttpMethod.Get,
                            clientId: String,
                            clientSecret: String,
                            defaultScopes: List<String> = listOf("openid"),
                            accessTokenRequiresBasicAuth: Boolean = false,
                            nonceManager: NonceManager = GenerateOnlyNonceManager,
                            authorizeUrlInterceptor: URLBuilder.() -> Unit = {}
    ): FromDiscoveryMetadata(name, JACKSON.readValue(discoveryFile), requestMethod, clientId, clientSecret, defaultScopes, accessTokenRequiresBasicAuth, nonceManager, authorizeUrlInterceptor)

    companion object {
        val JACKSON: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
        /**
         * Dummy instance to satisfy super constructor
         */
        val DUMMY_DISCOVERY = DiscoveryMetadata()
    }
}

/**
 * OAauth callback parameters
 */
sealed class OAuthCallback {
    /**
     * OAuth2 token callback parameter
     * @property token OAuth2 token provided by server
     * @property state passed from a client (ktor server) during authorization startup
     */
    data class TokenSingle(val token: String, val state: String) : OAuthCallback()
}

/**
 * OAuth access token acquired from the server
 */
sealed class OAuthAccessTokenResponse : Principal {
    /**
     * OAuth2 access token acquired from the server
     * @property accessToken access token from server
     * @property tokenType OAuth2 token type (usually Bearer)
     * @property expiresIn token expiration timestamp
     * @property refreshToken to be used to refresh access token after expiration
     * @property extraParameters contains additional parameters provided by the server
     */
    data class OpenID(
        val accessToken: String,
        val tokenType: String,
        val expiresIn: Long,
        val refreshToken: String?,
        val jwtToken: OpenIDJWT,
        val extraParameters: Parameters = Parameters.Empty
    ) : OAuthAccessTokenResponse()
}

/**
 * OAuth grant types constants
 */
@Suppress("KDocMissingDocumentation")
object OAuthGrantTypes {
    const val AuthorizationCode = "authorization_code"
    const val Password = "password"
}

/**
 * Respond OAuth redirect
 */
suspend fun PipelineContext<Unit, ApplicationCall>.oauthRespondRedirect(
    provider: OAuthServerSettings,
    callbackUrl: String
) {
    call.redirectAuthenticateOAuth2(
        provider, callbackUrl,
        provider.nonceManager.newNonce(),
        scopes = provider.defaultScopes,
        interceptor = provider.authorizeUrlInterceptor
    )
}

/**
 * Handle OAuth callback
 */
suspend fun PipelineContext<Unit, ApplicationCall>.oauthHandleCallback(
    client: HttpClient,
    dispatcher: CoroutineDispatcher,
    provider: OAuthServerSettings,
    callbackUrl: String,
    loginPageUrl: String,
    configure: HttpRequestBuilder.() -> Unit = {},
    block: suspend (OAuthAccessTokenResponse) -> Unit
) {
    val code = call.oauth2HandleCallback()
    if (code == null) {
        call.respondRedirect(loginPageUrl)
    } else {
        withContext(dispatcher) {
            try {
                val accessToken = oauth2RequestAccessToken(
                    client,
                    provider,
                    callbackUrl,
                    code,
                    emptyMap(),
                    configure
                )

                block(accessToken)
            } catch (cause: OAuth2Exception.InvalidGrant) {
                Logger.trace("Redirected to the login page due to invalid_grant error: {}", cause.message)
                call.oauthHandleFail(loginPageUrl)
            } catch (ioe: IOException) {
                Logger.trace("Redirected to the login page due to IO error", ioe)
                call.oauthHandleFail(loginPageUrl)
            }
        }
    }
}

internal suspend fun ApplicationCall.oauthHandleFail(redirectUrl: String) = respondRedirect(redirectUrl)

internal fun String.appendUrlParameters(parameters: String) =
    when {
        parameters.isEmpty() -> ""
        this.endsWith("?") -> ""
        "?" in this -> "&"
        else -> "?"
    }.let { separator -> "$this$separator$parameters" }