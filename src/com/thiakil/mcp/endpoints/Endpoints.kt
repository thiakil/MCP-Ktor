package com.thiakil.mcp.endpoints

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.BooleanSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter

enum class ArgLocation(val schemaString: String) {
    Query("query"),
    Path("path")
}

fun parameter(
    name: String,
    type: Schema<*> = StringSchema(),
    required: Boolean = true,
    where: ArgLocation = ArgLocation.Query
): Parameter {
    return Parameter().name(name).required(required).schema(type).`in`(where.schemaString)
}

object Endpoints {
    val MCP_TEAM = arrayOf("mcp_team")
    val OPTIONAL_VERSION = parameter("version", required = false)
    val REGEX_ARG = parameter("regex_pattern")
    val REGEX_ARGS =
        arrayOf(
            REGEX_ARG, OPTIONAL_VERSION,
            parameter(
                "restrict_types",
                ArraySchema().items(StringSchema()._enum(listOf("class", "field", "method", "param")))
            )
        )
    val NAME_ARG = parameter("name")
    val NAME_PATH_ARG = parameter("name", where = ArgLocation.Path)
    val SINGLE_NAME_ARG = arrayOf(NAME_ARG)
    val SINGLE_NAME_PATH_ARG = arrayOf(NAME_PATH_ARG)
    val SET_MEMBER_ARGS = arrayOf(
        parameter(
            "srg_name",
            where = ArgLocation.Path
        ),
        parameter("new_name"),
        parameter("comment", required = false),
        parameter("force", BooleanSchema(), required = false)
    )
}

// TODO move these into own files

@Endpoint
object GetClass : EndpointHandler<Unit>(
    ircCommand = "gc",
    apiPath = "/classes/{name}",
    pythonCallback = "getClass",
    description = "Returns class information. Defaults to current version. Version can be for MCP or MC.",
    parameters = arrayOf(Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION),
    allowPublic = true
)

@Endpoint
object GetField : EndpointHandler<Unit>(
    ircCommand = "gf",
    apiPath = "/fields/{name}",
    pythonCallback = "getMember",
    description = "Returns field information. Defaults to current version. Version can be for MCP or MC.",
    parameters = arrayOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION),
    allowPublic = true
)

@Endpoint
object GetMethod : EndpointHandler<Unit>(
    ircCommand = "gm",
    apiPath = "/methods/{name}",
    pythonCallback = "getMember",
    description = "Returns method information. Defaults to current version. Version can be for MCP or MC.",
    parameters = arrayOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION),
    allowPublic = true
)

@Endpoint
object GetParam : EndpointHandler<Unit>(
    ircCommand = "gp",
    apiPath = "/params/{name}",
    pythonCallback = "getParam",
    description = "Returns method parameter information. Defaults to current version. Version can be for MCP or MC. Obf class and method names not supported.",
    parameters = arrayOf(
        parameter("class", required = false),
        parameter("method", required = false),
        Endpoints.NAME_PATH_ARG,
        Endpoints.OPTIONAL_VERSION
    ),
    allowPublic = true
)

@Endpoint
object FindKey : EndpointHandler<Unit>(
    ircCommand = "find",
    apiPath = "/find",
    pythonCallback = "findKey",
    description = "Returns any entries matching a regex pattern. Only returns complete matches.",
    parameters = Endpoints.REGEX_ARGS,
    allowPublic = true
)

@Endpoint
object FindAll : EndpointHandler<Unit>(
    ircCommand = "findall",
    apiPath = "/find-all",
    pythonCallback = "findAllKey",
    description = "Returns any entries matching a regex pattern. Allows partial matches to be returned.",
    parameters = Endpoints.REGEX_ARGS,
    allowPublic = true
)

@Endpoint
object FieldHistory : EndpointHandler<Unit>(
    ircCommand = "fh",
    apiPath = "/fields/{name}/history",
    pythonCallback = "getHistory",
    description = "Gets the change history for the given field. Using MCP name allows you to search for changes to/from that name. SRG index is also accepted.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowPublic = true
)

@Endpoint
object MethodHistory : EndpointHandler<Unit>(
    ircCommand = "mh",
    apiPath = "/methods/{name}/history",
    pythonCallback = "getHistory",
    description = "Gets the change history for the given method. Using MCP name allows you to search for changes to/from that name. SRG index is also accepted.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowPublic = true
)

@Endpoint
object ParamHistory : EndpointHandler<Unit>(
    ircCommand = "ph",
    apiPath = "/params/{name}/history",
    pythonCallback = "getHistory",
    description = "Gets the change history for the given method param. SRG index is also accepted.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowPublic = true
)

@Endpoint
object UnnamedFields : EndpointHandler<Unit>(
    ircCommand = "uf",
    apiPath = "/classes/{name}/unnamed/fields",
    pythonCallback = "listMembers",
    description = "Returns a list of unnamed fields for a given class.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowPublic = true
)

@Endpoint
object UnnamedMethods : EndpointHandler<Unit>(
    ircCommand = "um",
    apiPath = "/classes/{name}/unnamed/methods",
    pythonCallback = "listMembers",
    description = "Returns a list of unnamed methods for a given class.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowPublic = true
)

@Endpoint
object UnnamedParams : EndpointHandler<Unit>(
    ircCommand = "up",
    apiPath = "/classes/{name}/unnamed/params",
    pythonCallback = "listMembers",
    description = "Returns a list of unnamed method parameters for a given class.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowPublic = true
)

@Endpoint
object UndoChange : EndpointHandler<Unit>(
    ircCommand = "undo",
    apiPath = "/undo/{name}",
    pythonCallback = "undoChange",
    description = "Undoes the last *STAGED* name change to a given method/field/param. By default you can only undo your own changes.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowDuringReadonly = false
)

@Endpoint
object RedoChange : EndpointHandler<Unit>(
    ircCommand = "redo",
    apiPath = "/redo/{name}",
    pythonCallback = "undoChange",
    description = "Redoes the last *UNDONE* staged change to a given method/field/param. By default you can only redo your own changes.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowDuringReadonly = false
)

@Endpoint
object RemoveFieldComment : EndpointHandler<Unit>(
    ircCommand = "rfc",
    apiPath = "/manage/remove-field-comment/{name}",
    pythonCallback = "removeComment",
    groups = arrayOf("maintainer", "mcp_team"),
    description = "Blanks out the field comment.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object RemoveMethodComment : EndpointHandler<Unit>(
    ircCommand = "rmc",
    apiPath = "/manage/remove-method-comment/{name}",
    pythonCallback = "removeComment",
    groups = arrayOf("maintainer", "mcp_team"),
    description = "Blanks out the method comment.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object RemoveParamComment : EndpointHandler<Unit>(
    ircCommand = "rpc",
    apiPath = "/manage/remove-param-comment/{name}",
    pythonCallback = "removeComment",
    groups = arrayOf("maintainer", "mcp_team"),
    description = "Blanks out the method parameter comment.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object SetField : EndpointHandler<Unit>(
    ircCommand = "sf",
    apiPath = "/fields/set/{srg_name}",
    pythonCallback = "setMember",
    description = "Sets the MCP name and comment for the SRG field specified. SRG index can also be used. Force param can only be used by admins.",
    parameters = Endpoints.SET_MEMBER_ARGS,
    allowPublic = true,
    allowDuringReadonly = false
)

@Endpoint
object SetMethod : EndpointHandler<Unit>(
    ircCommand = "sm",
    apiPath = "/methods/set/{srg_name}",
    pythonCallback = "setMember",
    description = "Sets the MCP name and comment for the SRG method specified. SRG index can also be used. Force param can only be used by admins.",
    parameters = Endpoints.SET_MEMBER_ARGS,
    allowPublic = true,
    allowDuringReadonly = false
)

@Endpoint
object SetParam : EndpointHandler<Unit>(
    ircCommand = "sp",
    apiPath = "/params/set/{srg_name}",
    pythonCallback = "setMember",
    description = "Sets the MCP name and comment for the SRG method parameter specified. SRG index can also be used. Force param can only be used by admins.",
    parameters = Endpoints.SET_MEMBER_ARGS,
    allowPublic = true,
    allowDuringReadonly = false
)

@Endpoint
object Lock : EndpointHandler<Unit>(
    ircCommand = "lock",
    apiPath = "/manage/lock/{name}",
    pythonCallback = "setLocked",
    groups = arrayOf("lock_control", "mcp_team"),
    description = "Locks the given field/method/parameter from being edited. Full SRG name must be used if member_type not specified.",
    parameters = arrayOf(
        Endpoints.NAME_PATH_ARG,
        parameter("member_type", required = false)
    ),
    allowDuringReadonly = false
)

@Endpoint
object Unlock : EndpointHandler<Unit>(
    ircCommand = "unlock",
    apiPath = "/manage/unlock/{name}",
    pythonCallback = "setLocked",
    groups = arrayOf("lock_control", "mcp_team"),
    description = "Unlocks the given field/method/parameter to allow editing. Full SRG name must be used if member_type not specified.",
    parameters = arrayOf(
        Endpoints.NAME_PATH_ARG,
        parameter("member_type", required = false)
    ),
    allowDuringReadonly = false
)