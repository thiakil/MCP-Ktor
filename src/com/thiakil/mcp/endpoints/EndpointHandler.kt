package com.thiakil.mcp.endpoints

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.parameters.Parameter

/**
 * Created by Thiakil on 30/12/2019.
 */
abstract class EndpointHandler<T: Any>(
    val ircCommand: String = "",
    val apiPath: String,
    val pythonCallback: String = "",
    val groups: Array<String> = emptyArray(),
    val description: String = "",
    val parameters: Array<Parameter> = emptyArray(),
    val allowPublic: Boolean = false,
    val allowDuringReadonly: Boolean = true,
    val method: HttpMethod = HttpMethod.Get
) {
    @Throws(ErrorResponseException::class)
    open fun respond(context: PipelineContext<Unit, ApplicationCall>): T {
        throw ErrorResponseException(ErrorCode.UNKNOWN_ERROR, "Endpoint not implemented")
    }
}

enum class ErrorCode(val httpCode: HttpStatusCode) {
    NOT_FOUND(HttpStatusCode.NotFound),
    BAD_PARAMETERS(HttpStatusCode.BadRequest),
    UNKNOWN_ERROR(HttpStatusCode.InternalServerError)
}

class ErrorResponseException(val errorCode: ErrorCode, val humanReason: String): RuntimeException() {
    suspend fun respond(call: ApplicationCall) {
        call.respond(errorCode.httpCode, mapOf(
            "error" to errorCode.name,
            "info" to humanReason
        ))
    }
}