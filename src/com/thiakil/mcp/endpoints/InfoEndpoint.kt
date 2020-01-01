package com.thiakil.mcp.endpoints

import com.fasterxml.jackson.annotation.JsonFormat
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class McpVersion(
    @field:Schema(description = "Version number for MCP", example = "\"9.66\"")
    val mcp_version_code: String,
    @field:Schema(description = "Minecraft version applicable", example = "1.14.3")
    val mc_version_code: String,
    @field:Schema(description = "Type of release", allowableValues = ["release"])
    val mc_version_type_code: String
)


data class InfoResponse(
    @field:ArraySchema(arraySchema = Schema(description = "Versions accepted by the api"))
    val availableVersions: List<McpVersion>,
    @field:Schema(description = "Time period in minutes of the test export", example = "30")
    val test_export_period: Int,//Semi-live (every %d min)
    @field:Schema(description = "Approximate time when daily snapshots are uploaded to maven (for use in ForgeGradle)", example = "08:00Z", format = "ISO 8601 Time (with offset)")
    val maven_upload_time: String,// Snapshot (daily ~%s EST)
    @field:Schema(description = "Location of human-readable page containing a list of previous exports", format = "url")
    val exportsURL: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @field:Schema(description = "Time of last test export completion", format = "date-time", example = "2020-01-01T16:55:55.858Z")
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