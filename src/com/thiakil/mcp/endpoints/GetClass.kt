package com.thiakil.mcp.endpoints

import com.thiakil.mcp.McpState
import cuchaz.enigma.translation.representation.entry.ClassEntry
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Endpoint
object GetClass : EndpointHandler<List<GetClassResult>>(
    ListKClass(GetClassResult::class),
    ircCommand = "gc",
    apiPath = "/classes/{name}",
    pythonCallback = "getClass",
    description = "Returns class information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(Endpoints.CLASS_NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION)
) {
    override fun PipelineContext<Unit, ApplicationCall>.handleEndpoint(appData: McpState): List<GetClassResult> {
        val className = context.parameters["name"]?.replace('.', '/') ?: throw ErrorResponseException(ErrorCode.BAD_PARAMETERS, "Missing name parameter")
        return appData.index.classes.filter { it.name == className }.map { cls ->
            GetClassResult(
                srgName = cls.fullName,
                superName = when(val superN = cls.superClass?.fullName){
                    "java/lang/Object" -> null
                    else -> superN
                },
                outerName = cls.outerClass?.fullName,
                interfaces = cls.interfaces?.map(ClassEntry::getFullName),
                extenders = appData.index.classes.filter { it.superClass == cls }.map(ClassEntry::getFullName),
                implementers = if (cls.access.isInterface) appData.index.classes.filter { it.interfaces?.contains(cls) ?: false }.map(ClassEntry::getFullName) else null,
                visibility = with(cls.access){
                    when {
                        isPublic -> "public"
                        isPrivate -> "private"
                        isProtected -> "protected"
                        else -> "package"
                    }
                },
                classType = with(cls.access) {
                    when {
                        isInterface -> "interface"
                        isEnum -> "enum"
                        else -> null
                    }
                }
            )
        }
    }
}

data class GetClassResult(
    @field:Schema(description = "Full SRG class name (JVM name)", example = "net/minecraft/Block")
    val srgName: String,
    //@field:Schema(description = "Obfuscated (Notch) name", example = "bmq")
    //val obf_name: String,
    //@field:Schema(description = "SRG package name", example = "net/minecraft/block")
    //val pkg_name: String,
    //@field:Schema(description = "If the class extends another, this will be the super class' obfuscated name", example = "bmq")
    //val super_obf_name: String? = null,
    @field:Schema(description = "If the class extends another, this will be the super class' SRG name", example = "net/minecraft/Block")
    val superName: String?,
    @field:Schema(description = "If the class is an inner class, this will be the outer class' SRG name", example = "net/minecraft/Block")
    val outerName: String?,
    @field:ArraySchema(arraySchema = Schema(description = "A list of any direct interfaces this class implements", example = "[\"net/minecraft/IGrowable\"]"))
    val interfaces: List<String>?,
    @field:ArraySchema(arraySchema = Schema(description = "A list of classes known to extend this class", example = "[\"net/minecraft/Block\"]"))
    val extenders: List<String>?,
    @field:ArraySchema(arraySchema = Schema(description = "A list of classes known to implement this interface", example = "[\"net/minecraft/Block\"]"))
    val implementers: List<String>?,
    @field:Schema(allowableValues = ["public", "protected", "private", "package"])
    val visibility: String,
    @field:Schema(description = "If enum or interface, specifies the type. Not present for normal classes.",allowableValues = ["enum", "interface"], nullable = true)
    val classType: String?
)