package com.thiakil.mcp

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.thiakil.mcp.api.MappedObject
import com.thiakil.mcp.api.MappedName
import com.thiakil.mcp.endpoints.ErrorCode
import com.thiakil.mcp.endpoints.ErrorResponseException
import com.thiakil.mcp.endpoints.GeneratedEndpointList
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.NonceManager
import io.ktor.util.generateNonce
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

val LOGGER: Logger = LoggerFactory.getLogger("com.thiakil.mcp")

data class LoginSession(val authIssuer: String, val authUserId: String)

@UseExperimental(KtorExperimentalAPI::class)
@Suppress("unused", "UNUSED_PARAMETER") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    val appState = McpState(environment.config)

    install(ContentNegotiation) {
        jackson {
            registerKotlinModule()
            setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
        }
    }

    /*install(CallLogging) {
        level = Level.INFO
        logger = LoggerFactory.getLogger("com.thiakil.mcp")
    }*/

    /*install(Sessions) {
        cookie<LoginSession>("MY_SESSION", SessionStorageMemory()) {
            //cookie.extensions["SameSite"] = "lax"
        }
    }*/

    //install(ForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy
    //install(XForwardedHeaderSupport) // WARNING: for security, do not include this if not behind a reverse proxy

    routing {

        route("/api") {
            for (cmd in GeneratedEndpointList.ENDPOINTS) {
                route(cmd.apiPath, cmd.method) {
                    handle {
                        try {
                            with(cmd){
                                call.respond(handleEndpoint(appState))
                            }
                        } catch (e: ErrorResponseException) {
                            e.respond(call)
                        } catch (e: Exception) {
                            ErrorResponseException(ErrorCode.UNKNOWN_ERROR, "Internal error").respond(call)
                            LOGGER.error("Endpoint threw exception", e)
                        }
                    }
                }
            }
        }

        apiSchema()

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