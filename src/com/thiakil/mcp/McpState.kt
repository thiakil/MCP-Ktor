package com.thiakil.mcp

import com.thiakil.mcp.index.SrgIndex
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI

/**
 * Created by Thiakil on 25/01/2020.
 */
@UseExperimental(KtorExperimentalAPI::class)
class McpState constructor(applicationConfig: ApplicationConfig) {
    private val mcpPath:String = applicationConfig.property("mcpconfig.path").getString()
    private val mcVersion: String = applicationConfig.property("mcpconfig.minecraft_version").getString()

    val index: SrgIndex = SrgIndex.load(mcpPath, mcVersion)
}