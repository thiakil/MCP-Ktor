package com.thiakil.mcp

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.thiakil.ktor.openid.OAuthAccessTokenResponse
import com.thiakil.ktor.openid.OAuthServerSettings
import com.thiakil.ktor.openid.openid
import com.thiakil.mcp.api.MappedObject
import com.thiakil.mcp.api.MappedName
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.host
import io.ktor.request.path
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.sessions.*
import io.ktor.util.NonceManager
import io.ktor.util.generateNonce
import kotlin.collections.listOf
import kotlin.collections.mapOf

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_OVERRIDE")
object MemoryNonceGenerator: NonceManager {
    private val generatedNonces: MutableSet<String> = mutableSetOf()
    private val seenNonces: MutableSet<String> = mutableSetOf()

    override suspend fun newNonce(): String {
        val newNonce = generateNonce()
        generatedNonces.add(newNonce)
        return newNonce
    }

    override suspend fun verifyNonce(nonce: String): Boolean = generatedNonces.contains(nonce) && seenNonces.add(nonce)

}

val googleOauthProvider = OAuthServerSettings.FromDiscoveryURL(
    name = "google",
    discoveryUrl = "https://accounts.google.com/.well-known/openid-configuration",
    /*authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    accessTokenUrl = "https://www.googleapis.com/oauth2/v3/token",*/
    requestMethod = HttpMethod.Post,

    clientId = "574579380105-p5mhb56cf2ql53d5qkuod3jvmgc13qhs.apps.googleusercontent.com", // @TODO: Remember to change this!
    clientSecret = "7BBeAN2bVZgzwMThLrbjnPqQ", // @TODO: Remember to change this!
    defaultScopes = listOf("openid", "profile", "email"),
    nonceManager = MemoryNonceGenerator
)

data class LoginSession(val authIssuer: String, val authUserId: String)

@Suppress("unused", "UNUSED_PARAMETER") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
        }
    }

    /*install(CallLogging) {
        level = Level.INFO
        logger = LoggerFactory.getLogger("com.thiakil.mcp")
    }*/

    install(Sessions) {
        cookie<LoginSession>("MY_SESSION", SessionStorageMemory()) {
            //cookie.extensions["SameSite"] = "lax"
        }
    }

    //install(ForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy
    //install(XForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy

    install(Authentication) {
        openid("google-oauth") {
            client = HttpClient(Apache)
            providerLookup = { googleOauthProvider }
            urlProvider = {
                redirectUrl("/login")
            }
            skipWhen { call -> call.request.path() != "/login" && call.sessions.get<LoginSession>() != null }
        }
    }

    routing {
        authenticate("google-oauth") {
            route("/login") {
                get {
                    val principal = call.authentication.principal<OAuthAccessTokenResponse.OpenID>()
                        ?: error("No principal")

                    call.sessions.set(LoginSession(principal.jwtToken.issuer, principal.jwtToken.subject))

                    call.respondText("Hello from login\n"+principal.jwtToken, contentType = ContentType.Text.Plain)
                }
            }
            route("/testauthed"){
                get {
                    call.respondText { "You appear to be authed\n"+call.sessions.get<LoginSession>() }
                }
            }
        }

        route("/api") {

        }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/session/increment") {
            /*val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")*/
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

private fun <T: MappedName> Route.mappedObject(mountPoint :String, processor: MappedObject<T>) {
    route("/$mountPoint") {
        route("/{name}") {
            //param("name") {
            route("/history") {
                get {
                    when (val history = processor.getHistory(call.parameters["name"]!!)){
                        null -> call.respond(HttpStatusCode.NotFound, "Name not found")
                        else -> call.respond(history)
                    }
                }
            }

            get {
               when (val obj = processor.getOne(call.parameters["name"]!!)){
                   null -> call.respond(HttpStatusCode.NotFound, "Name not found")
                   else -> call.respond(obj)
               }
            }
            //}
        }
        get {
            call.respond(processor.getAll())
        }
    }
}

private fun ApplicationCall.redirectUrl(path: String): String {
    val defaultPort = if (request.origin.scheme == "http") 80 else 443
    val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
    val protocol = request.origin.scheme
    return "$protocol://$hostPort$path"
}