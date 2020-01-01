package com.thiakil.mcp.endpoints

import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext


@Endpoint
object CommitMappings : EndpointHandler<String>(
    String::class,
    ircCommand = "commit",
    apiPath = "/manage/commit-mappings",
    pythonCallback = "commitMappings",
    groups = Endpoints.MCP_TEAM,
    description = "Commits staged mapping changes. If SRG name is specified only that member will be committed. If method/field/param is specified only that member type will be committed. Give no arguments to commit all staged changes.",
    parameters = listOf(parameter("srg_name", required = false)),
    allowDuringReadonly = false
)