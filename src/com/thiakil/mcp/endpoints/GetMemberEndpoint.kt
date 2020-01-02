package com.thiakil.mcp.endpoints

import io.ktor.http.HttpMethod
import io.swagger.v3.oas.models.parameters.Parameter
import java.util.*
import kotlin.reflect.KClass

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
open class MemberInfo {
    var mc_version_code: String = ""
    var obf_name: String = ""
    var mcp_name: String? = null
    var srg_name: String = ""
    var class_pkg_name: String = ""
    var class_srg_name: String = ""
    var class_obf_name: String = ""
    var locked: Boolean = false
    var obf_member_base_class: String? = null
    var srg_member_base_class: String? = null
    var obf_descriptor: String = ""
    var srg_descriptor: String? = null
    var is_public: Boolean = true
    var comment: String? = null
    var last_modified: MemberLastUpdate? = null
}

@Suppress("unused")
class MethodMemberInfo: MemberInfo() {
    var is_constrctor: Boolean = false
    var params: List<ParamLite>? = null
}

data class ParamLite(val srg_name: String, val mcp_name: String?)

data class MemberLastUpdate(val timestamp: Date, val irc_nick: String?)

@Endpoint
object GetField : GetMemberEndpoint<MemberInfo>(
    MemberInfo::class,
    ircCommand = "gf",
    apiPath = "/fields/{name}",
    pythonCallback = "getMember",
    description = "Returns field information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION)
)

@Endpoint
object GetMethod : GetMemberEndpoint<MethodMemberInfo>(
    MethodMemberInfo::class,
    ircCommand = "gm",
    apiPath = "/methods/{name}",
    pythonCallback = "getMember",
    description = "Returns method information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION)
)