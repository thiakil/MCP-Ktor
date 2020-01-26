package com.thiakil.mcp.endpoints

import com.thiakil.mcp.McpState
import com.thiakil.mcp.endpoints.ErrorCode.NOT_FOUND
import cuchaz.enigma.translation.representation.entry.ClassDefEntry
import cuchaz.enigma.translation.representation.entry.ClassEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpMethod
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.parameters.Parameter
import java.util.*
import kotlin.reflect.KClass

private val ONLY_NUMBER_REGEX = Regex("^\\d+$")

open class GetMemberEndpoint<T : MemberInfo>(
    responseType: KClass<T>,
    ircCommand: String? = null,
    apiPath: String,
    pythonCallback: String? = null,
    groups: Array<String> = emptyArray(),
    description: String,
    parameters: List<Parameter>? = null,
    allowDuringReadonly: Boolean = true,
    method: HttpMethod = HttpMethod.Get
) : EndpointHandler<T>(
    responseType = responseType,
    ircCommand = ircCommand,
    apiPath = apiPath,
    pythonCallback = pythonCallback,
    groups = groups,
    description = description,
    parameters = parameters,
    allowDuringReadonly = allowDuringReadonly,
    method = method
)

@Suppress("unused")
open class MemberInfo(//var mcVersionCode: String = ""
    //val obfName: String = ""
    val mcpName: String?,
    val srgName: String,
    //val classPkgName: String = ""
    val className: String,
    //val classObfName: String = ""
    //val locked: Boolean = false
    //val obfDescriptor: String = ""
    val descriptor: String,
    val visibility: String,
    val comment: String?,
    val lastModified: MemberLastUpdate?
)

@Suppress("unused")
class MethodMemberInfo(
    mcpName: String?, srgName: String, className: String, descriptor: String, visibility: String, comment: String?, lastModified: MemberLastUpdate?,
    val params: List<ParamLite>?,
    //val obfMemberBaseClass: String?,
    val classesFoundIn: List<String>
) : MemberInfo(mcpName, srgName, className, descriptor, visibility, comment, lastModified)

data class ParamLite(val srg_name: String, val mcp_name: String?)

data class MemberLastUpdate(val timestamp: Date, val irc_nick: String?)

@Endpoint
object GetField : GetMemberEndpoint<MemberInfo>(
    MemberInfo::class,
    ircCommand = "gf",
    apiPath = "/fields/{name}",
    pythonCallback = "getMember",
    description = "Returns field information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(/*parameter("class", required = false),*/ Endpoints.NAME_PATH_ARG/*, Endpoints.OPTIONAL_VERSION*/)
){
    override fun PipelineContext<Unit, ApplicationCall>.handleEndpoint(appData: McpState): MemberInfo {
        val name = context.parameters["name"] ?: throw ErrorResponseException(ErrorCode.BAD_PARAMETERS, "Missing name parameter")
        val filter: (String) -> Boolean = if (ONLY_NUMBER_REGEX.matches(name)) {
            val matchStr = "field_${name}_"
            { it.startsWith(matchStr) }
        } else {
            { it == name }
        }
        return appData.index.fields.keys.find(filter)?.let {
            val field = appData.index.fields[it]!!
            return@let MemberInfo(
                srgName = field.name,
                className = field.parent!!.fullName,
                descriptor = field.desc.toString(),
                visibility =field.access.visibilityString(),
                //todo
                mcpName = null,
                comment = null,
                lastModified = null
            )
        } ?: throw ErrorResponseException(NOT_FOUND, "Field not found")
    }
}

@Endpoint
object GetMethod : GetMemberEndpoint<MethodMemberInfo>(
    MethodMemberInfo::class,
    ircCommand = "gm",
    apiPath = "/methods/{name}",
    pythonCallback = "getMember",
    description = "Returns method information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION)
) {
    override fun PipelineContext<Unit, ApplicationCall>.handleEndpoint(appData: McpState): MethodMemberInfo {
        val name = context.parameters["name"] ?: throw ErrorResponseException(ErrorCode.BAD_PARAMETERS, "Missing name parameter")
        val match = if (ONLY_NUMBER_REGEX.matches(name)) {
            appData.index.methodsByIndex.get(name.toInt())
        } else {
            appData.index.methods.get(name)
        }
        return match?.let { methods ->
            val classesFoundIn = methods.map { it.parent!! }.sortedBy { it.fullName }
            val baseMethod = if (methods.size == 1) {
                methods.first()
            } else {
                methods.find { mth->
                    var cls: ClassDefEntry? = (mth.parent as ClassDefEntry).superClass?.let { appData.index.classes[it] }
                    while(cls != null) {
                        if (classesFoundIn.contains(cls)){
                            return@find false
                        }
                        cls = cls.superClass?.let { appData.index.classes[it] }
                    }
                    return@find true
                } ?: error("didn't find a base method")
                //methods.first()
            }
            return@let MethodMemberInfo(
                srgName = baseMethod.name,
                className = baseMethod.parent!!.fullName,
                descriptor = baseMethod.desc.toString(),
                visibility = baseMethod.access.visibilityString(),
                //todo
                mcpName = null,
                comment = null,
                lastModified = null,
                classesFoundIn = classesFoundIn.map(ClassEntry::getFullName),
                params = appData.index.methodParams.get(baseMethod).map { ParamLite(it, null) }
            )
        } ?: throw ErrorResponseException(NOT_FOUND, "Method not found")
    }
}