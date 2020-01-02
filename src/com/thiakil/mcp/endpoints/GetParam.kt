package com.thiakil.mcp.endpoints

@Endpoint
object GetParam : EndpointHandler<ParamInfo>(
    ParamInfo::class,
    ircCommand = "gp",
    apiPath = "/params/{name}",
    pythonCallback = "getParam",
    description = "Returns method parameter information. Defaults to current version. Version can be for MCP or MC. Obf class and method names not supported.",
    parameters = listOf(
        parameter("class", required = false),
        parameter("method", required = false),
        Endpoints.NAME_PATH_ARG,
        Endpoints.OPTIONAL_VERSION
    )
)

class ParamInfo: MemberInfo(){
    val method_obf_name: String = ""
    val method_srg_name: String = ""
    val method_mcp_name: String? = null
    val method_obf_descriptor: String = ""
    val method_srg_descriptor: String? = ""
}