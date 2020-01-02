package com.thiakil.mcp.endpoints

@Endpoint
object FindKey : EndpointHandler<FindResults>(
    FindResults::class,
    ircCommand = "find",
    apiPath = "/find",
    pythonCallback = "findKey",
    description = "Returns any entries matching a regex pattern. Only returns complete matches.",
    parameters = Endpoints.REGEX_ARGS
)

@Endpoint
object FindAll : EndpointHandler<FindResults>(
    FindResults::class,
    ircCommand = "findall",
    apiPath = "/find-all",
    pythonCallback = "findAllKey",
    description = "Returns any entries matching a regex pattern. Allows partial matches to be returned.",
    parameters = Endpoints.REGEX_ARGS
)

class FindResults {
    val fields: List<MemberInfo>? = null
    val methods: List<MethodMemberInfo>? = null
    val params: List<MemberInfo>? = null
    val classes: List<MemberInfo>? = null
}