package com.thiakil.mcp.endpoints

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Endpoint
object GetClass : EndpointHandler<GetClassResult>(
    GetClassResult::class,
    ircCommand = "gc",
    apiPath = "/classes/{name}",
    pythonCallback = "getClass",
    description = "Returns class information. Defaults to current version. Version can be for MCP or MC.",
    parameters = listOf(Endpoints.CLASS_NAME_PATH_ARG, Endpoints.OPTIONAL_VERSION),
    allowPublic = true
)

//todo class type, mojang mapped name?
data class GetClassResult(
    @field:Schema(description = "Simple SRG class name", example = "Block")
    val srg_name: String,
    @field:Schema(description = "Obfuscated (Notch) name", example = "bmq")
    val obf_name: String,
    @field:Schema(description = "SRG package name", example = "net/minecraft/block")
    val pkg_name: String,
    @field:Schema(description = "If the class extends another, this will be the super class' obfuscated name", example = "bmq")
    val super_obf_name: String? = null,
    @field:Schema(description = "If the class extends another, this will be the super class' SRG name (simple)", example = "Block")
    val super_srg_name: String? = null,
    @field:Schema(description = "If the class is an inner class, this will be the outer class' SRG name (simple)", example = "Block")
    val outer_srg_name: String? = null,
    @field:ArraySchema(arraySchema = Schema(description = "A list of any direct interfaces this class implements", example = "[\"IGrowable\"]"))
    val srg_interfaces: List<String>? = null,
    @field:ArraySchema(arraySchema = Schema(description = "A list of classes known to extend this class", example = "[\"Block\"]"))
    val extending: List<String>? = null,
    @field:ArraySchema(arraySchema = Schema(description = "A list of classes known to implement this interface", example = "[\"Block\"]"))
    val implementing: List<String>? = null
)