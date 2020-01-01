package com.thiakil.mcp.endpoints

import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import java.time.Instant

data class McpVersion(
    val mcp_version_code: String,
    val mc_version_code: String,
    val mc_version_type_code: String
)


data class InfoResponse(
    val availableVersions: List<McpVersion>,
    val test_export_period: Int,//Semi-live (every %d min)
    val maven_upload_time: String,// Snapshot (daily ~%s EST) TODO some java type
    val exportsURL: String,  //TODO URL formatting in openapi
    val lastTestExport: Instant
)

@Endpoint(/*
    apiPath = "/",
    description = "Basic api info, combination of IRC commands versions, testcsv, and exports",
    allowpub = true*/
)
object InfoEndpoint : EndpointHandler<InfoResponse>(
    apiPath = "/",
    description = "Basic api info, combination of IRC commands versions, testcsv, and exports",
    allowPublic = true
) {
    override fun respond(context: PipelineContext<Unit, ApplicationCall>): InfoResponse {
        return InfoResponse(
            listOf(
                McpVersion("1.2.3", "4.5.6", "snapshot"),
                McpVersion("1.2.3", "4.5.6", "stable"),
                McpVersion("1.2.4", "4.5.7", "snapshot")
            ),
            30,
            "12:00AM UTC",
            "http://export.mcpbot.bspk.rs",
            Instant.now()
        )
    }
}