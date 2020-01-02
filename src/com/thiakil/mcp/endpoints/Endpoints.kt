package com.thiakil.mcp.endpoints

import io.ktor.http.HttpMethod
import io.swagger.v3.oas.models.Components
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
    private val componentParams: MutableMap<String, Parameter> = mutableMapOf()

    val MCP_TEAM = arrayOf("mcp_team")

    val OPTIONAL_VERSION = refParam("optional_version", parameter("version", required = false).description("Specify this parameter to request an older version (MCP or Minecraft unless otherwise specified)")!!)
    val REGEX_ARGS =
        listOf(
            refParam("regex_pattern", parameter("regex_pattern")),
            OPTIONAL_VERSION,
            refParam("restrict_type", parameter(
                "restrict_types",
                ArraySchema().items(StringSchema()._enum(listOf("class", "field", "method", "param")))
            ).description("Specify one or more items to restrict the search to those types, default is to search all. Comma separated string"))
        )
    val NAME_PATH_ARG = refParam("mcp_srg_name", parameter("name", where = ArgLocation.Path).description("MCP/SRG name. May use SRG index where meaning is not ambiguous.")!!)
    val CLASS_NAME_PATH_ARG = refParam("class_name", parameter("name", where = ArgLocation.Path).description("SRG or Obf (Notch) class name")!!)
    val SINGLE_CLASS_NAME_PATH_ARG = listOf(CLASS_NAME_PATH_ARG)
    val SINGLE_NAME_PATH_ARG = listOf(NAME_PATH_ARG)
    val SET_MEMBER_ARGS = listOf(
        NAME_PATH_ARG,
        refParam("new_mcp_name", parameter("new_name").description("New name to set, ignored if already set and force=false")),
        refParam("mcp_comment", parameter("comment", required = false).description("Comment to set, required if name has already been set. Must not be blank")),
        refParam("force_set_member", parameter("force", BooleanSchema(), required = false).description("Only available to admins")!!)
    )

    private fun refParam(globalName: String, parameter: Parameter): Parameter {
        if (componentParams.containsKey(globalName)){
            throw IllegalStateException("Global Name $globalName already exists")
        }
        componentParams[globalName] = parameter
        return Parameter().`$ref`(globalName)
    }

    fun Components.addParams() {
        if (parameters != null) {
            parameters.keys.forEach {
                if (componentParams.containsKey(it)) {
                    throw IllegalStateException("Duplicate parameter: $it")
                }
            }
        } else {
            parameters = mutableMapOf()
        }
        parameters.putAll(componentParams)
    }
}

// TODO move these into own files



@Endpoint
object GetField : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "gf",
    apiPath = "/fields/{name}",
    pythonCallback = "getMember",
    description = "Returns field information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION)
)

@Endpoint
object GetMethod : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "gm",
    apiPath = "/methods/{name}",
    pythonCallback = "getMember",
    description = "Returns method information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(parameter("class", required = false), Endpoints.NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION)
)

@Endpoint
object GetParam : EndpointHandler<Unit>(
    Unit::class,
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

@Endpoint
object FindKey : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "find",
    apiPath = "/find",
    pythonCallback = "findKey",
    description = "Returns any entries matching a regex pattern. Only returns complete matches.",
    parameters = Endpoints.REGEX_ARGS
)

@Endpoint
object FindAll : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "findall",
    apiPath = "/find-all",
    pythonCallback = "findAllKey",
    description = "Returns any entries matching a regex pattern. Allows partial matches to be returned.",
    parameters = Endpoints.REGEX_ARGS
)

@Endpoint
object FieldHistory : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "fh",
    apiPath = "/fields/{name}/history",
    pythonCallback = "getHistory",
    description = "Gets the change history for the given field. Using MCP name allows you to search for changes to/from that name. SRG index is also accepted.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object MethodHistory : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "mh",
    apiPath = "/methods/{name}/history",
    pythonCallback = "getHistory",
    description = "Gets the change history for the given method. Using MCP name allows you to search for changes to/from that name. SRG index is also accepted.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object ParamHistory : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "ph",
    apiPath = "/params/{name}/history",
    pythonCallback = "getHistory",
    description = "Gets the change history for the given method param. SRG index is also accepted.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object UnnamedFields : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "uf",
    apiPath = "/classes/{name}/unnamed/fields",
    pythonCallback = "listMembers",
    description = "Returns a list of unnamed fields for a given class.",
    parameters = Endpoints.SINGLE_CLASS_NAME_PATH_ARG
)

@Endpoint
object UnnamedMethods : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "um",
    apiPath = "/classes/{name}/unnamed/methods",
    pythonCallback = "listMembers",
    description = "Returns a list of unnamed methods for a given class.",
    parameters = Endpoints.SINGLE_CLASS_NAME_PATH_ARG
)

@Endpoint
object UnnamedParams : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "up",
    apiPath = "/classes/{name}/unnamed/params",
    pythonCallback = "listMembers",
    description = "Returns a list of unnamed method parameters for a given class.",
    parameters = Endpoints.SINGLE_CLASS_NAME_PATH_ARG
)

@Endpoint
object UndoChange : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "undo",
    apiPath = "/undo/{name}",
    pythonCallback = "undoChange",
    description = "Undoes the last *STAGED* name change to a given method/field/param. By default you can only undo your own changes.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowDuringReadonly = false,
    method = HttpMethod.Patch
)

@Endpoint
object RedoChange : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "redo",
    apiPath = "/redo/{name}",
    pythonCallback = "undoChange",
    description = "Redoes the last *UNDONE* staged change to a given method/field/param. By default you can only redo your own changes.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG,
    allowDuringReadonly = false,
    method = HttpMethod.Patch
)

@Endpoint
object RemoveFieldComment : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "rfc",
    apiPath = "/manage/remove-field-comment/{name}",
    pythonCallback = "removeComment",
    groups = arrayOf("maintainer", "mcp_team"),
    description = "Blanks out the field comment.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object RemoveMethodComment : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "rmc",
    apiPath = "/manage/remove-method-comment/{name}",
    pythonCallback = "removeComment",
    groups = arrayOf("maintainer", "mcp_team"),
    description = "Blanks out the method comment.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object RemoveParamComment : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "rpc",
    apiPath = "/manage/remove-param-comment/{name}",
    pythonCallback = "removeComment",
    groups = arrayOf("maintainer", "mcp_team"),
    description = "Blanks out the method parameter comment.",
    parameters = Endpoints.SINGLE_NAME_PATH_ARG
)

@Endpoint
object SetField : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "sf",
    apiPath = "/fields/{name}",
    pythonCallback = "setMember",
    description = "Sets the MCP name and comment for the SRG field specified. SRG index can also be used. Force param can only be used by admins.",
    parameters = Endpoints.SET_MEMBER_ARGS,
    allowDuringReadonly = false,
    method = HttpMethod.Post
)

@Endpoint
object SetMethod : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "sm",
    apiPath = "/methods/{name}",
    pythonCallback = "setMember",
    description = "Sets the MCP name and comment for the SRG method specified. SRG index can also be used. Force param can only be used by admins.",
    parameters = Endpoints.SET_MEMBER_ARGS,
    allowDuringReadonly = false,
    method = HttpMethod.Post
)

@Endpoint
object SetParam : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "sp",
    apiPath = "/params/{name}",
    pythonCallback = "setMember",
    description = "Sets the MCP name and comment for the SRG method parameter specified. SRG index can also be used. Force param can only be used by admins.",
    parameters = Endpoints.SET_MEMBER_ARGS,
    allowDuringReadonly = false,
    method = HttpMethod.Post
)

@Endpoint
object Lock : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "lock",
    apiPath = "/manage/lock/{name}",
    pythonCallback = "setLocked",
    groups = arrayOf("lock_control", "mcp_team"),
    description = "Locks the given field/method/parameter from being edited. Full SRG name must be used if member_type not specified.",
    parameters = listOf(
        Endpoints.NAME_PATH_ARG,
        parameter("member_type", required = false)
    ),
    allowDuringReadonly = false,
    method = HttpMethod.Patch
)

@Endpoint
object Unlock : EndpointHandler<Unit>(
    Unit::class,
    ircCommand = "unlock",
    apiPath = "/manage/unlock/{name}",
    pythonCallback = "setLocked",
    groups = arrayOf("lock_control", "mcp_team"),
    description = "Unlocks the given field/method/parameter to allow editing. Full SRG name must be used if member_type not specified.",
    parameters = listOf(
        Endpoints.NAME_PATH_ARG,
        parameter("member_type", required = false)
    ),
    allowDuringReadonly = false,
    method = HttpMethod.Patch
)