package com.thiakil.mcp.endpoints

import com.fasterxml.jackson.annotation.JsonFormat
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class McpVersion(
    @field:Schema(description = "Version number for MCP")
    val mcp_version_code: String,
    @field:Schema(description = "Minecraft version applicable")
    val mc_version_code: String,
    @field:Schema(description = "Type of release", allowableValues = ["stable", "snapshot"])
    val mc_version_type_code: String
)


data class InfoResponse(
    val availableVersions: List<McpVersion>,
    val test_export_period: Int,//Semi-live (every %d min)
    val maven_upload_time: String,// Snapshot (daily ~%s EST) TODO some java type
    val exportsURL: String,  //TODO URL formatting in openapi
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    val lastTestExport: Date
)

@Endpoint
object InfoEndpoint : EndpointHandler<InfoResponse>(
    InfoResponse::class,
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
            Date()
        )
    }
}