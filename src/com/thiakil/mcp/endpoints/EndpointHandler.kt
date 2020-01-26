package com.thiakil.mcp.endpoints

import com.thiakil.mcp.McpState
import cuchaz.enigma.translation.representation.AccessFlags
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.parameters.Parameter
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KVisibility

/**
 * Created by Thiakil on 30/12/2019.
 */
abstract class EndpointHandler<T: Any>(
    val responseType: KClass<T>,
    val ircCommand: String? = null,
    val apiPath: String,
    val pythonCallback: String? = null,
    val groups: Array<String> = emptyArray(),
    val description: String,
    val parameters: List<Parameter>? = null,
    val allowDuringReadonly: Boolean = true,
    val method: HttpMethod = HttpMethod.Get
) {
    @Throws(ErrorResponseException::class)
    open fun PipelineContext<Unit, ApplicationCall>.handleEndpoint(appData: McpState): T {
        throw ErrorResponseException(ErrorCode.NOT_FOUND, "Endpoint not implemented")
    }

    protected fun AccessFlags.visibilityString(): String {
        return when {
            isPublic -> "public"
            isPrivate -> "private"
            isProtected -> "protected"
            else -> "package"
        }
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

/**
 * Provides a way to supply a parameterised type to a constructor param and ease use in OpenAPI generation
 * For most members, delegates to List<*>
 *
 * @param listType - the value type of the list elements
 */
class ListKClass<LIST_TYPE : Any>(val listType: KClass<LIST_TYPE>): KClass<List<LIST_TYPE>> {
    override val annotations: List<Annotation>
        get() = emptyList()
    override val constructors: Collection<KFunction<List<LIST_TYPE>>>
        get() = emptyList()
    override val isAbstract: Boolean
        get() = false
    override val isCompanion: Boolean
        get() = false
    override val isData: Boolean
        get() = false
    override val isFinal: Boolean
        get() = false
    override val isInner: Boolean
        get() = false
    override val isOpen: Boolean
        get() = true
    override val isSealed: Boolean
        get() = false
    override val members: Collection<KCallable<*>>
        get() = List::class.members
    override val nestedClasses: Collection<KClass<*>>
        get() = List::class.nestedClasses
    override val objectInstance: List<LIST_TYPE>?
        get() = null
    override val qualifiedName: String?
        get() = List::class.qualifiedName
    override val sealedSubclasses: List<KClass<out List<LIST_TYPE>>>
        get() = emptyList()
    override val simpleName: String?
        get() = List::class.simpleName
    override val supertypes: List<KType>
        get() = List::class.supertypes
    override val typeParameters: List<KTypeParameter>
        get() = List::class.typeParameters
    override val visibility: KVisibility?
        get() = List::class.visibility

    override fun equals(other: Any?): Boolean {
        return other is ListKClass<*> && listType == other.listType
    }

    override fun hashCode(): Int {
        return Objects.hash(List::class, listType)
    }

    override fun isInstance(value: Any?): Boolean {
        return value is List<*>
    }

}