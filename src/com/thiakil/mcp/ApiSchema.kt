package com.thiakil.mcp

import com.thiakil.mcp.endpoints.Endpoints
import com.thiakil.mcp.endpoints.Endpoints.addParams
import com.thiakil.mcp.endpoints.GeneratedEndpointList
import com.thiakil.openapi.openApi
import com.thiakil.openapi.openApiSchema
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI

fun Route.apiSchema() {
    get("/schema.yml") {
        call.respondText(Yaml.pretty(generateSchema()), contentType = ContentType.parse("text/yaml"))
    }
    get("/schema.json") {
        call.respondText(Json.pretty(generateSchema()), contentType = ContentType.Application.Json)
    }
    get("/schema.html") {
        //language=HTML
        call.respondText("""<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Swagger UI</title>
    <link rel="stylesheet" type="text/css" href="//unpkg.com/swagger-ui-dist@3/swagger-ui.css" >
    <style>
      html
      {
        box-sizing: border-box;
        overflow: -moz-scrollbars-vertical;
        overflow-y: scroll;
      }

      *,
      *:before,
      *:after
      {
        box-sizing: inherit;
      }

      body
      {
        margin:0;
        background: #fafafa;
      }
    </style>
  </head>

  <body>
    <div id="swagger-ui"></div>

    <script src="//unpkg.com/swagger-ui-dist@3/swagger-ui-bundle.js"> </script>
    <script src="//unpkg.com/swagger-ui-dist@3/swagger-ui-standalone-preset.js"> </script>
    <script>
    window.onload = function() {
      // Begin Swagger UI call region
      const ui = SwaggerUIBundle({
        url: "schema.json",
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      })
      // End Swagger UI call region

      window.ui = ui
    }
  </script>
  </body>
</html>
        """.trimIndent(), contentType = ContentType.Text.Html)
    }
}

//todo cache (when finalised)
private fun generateSchema(): OpenAPI {
    return openApi {
        info {
            title = "Minecraft Names"
            description = "Api to set/retrieve Minecraft Names (formerly MCP Names)."
            version = "0.1"
        }
        server {
            url = "https://foo.bar.co/api"
            description = "Demo URL, not real just yet :P"
        }
        paths {
            GeneratedEndpointList.ENDPOINTS.groupBy { it.apiPath }.toSortedMap().forEach { (path, endpoints) ->
                path(path) {
                    endpoints.forEach { endpoint ->
                        operation(endpoint.method) {
                            description = endpoint.description
                            parameters = endpoint.parameters
                            responses {
                                default {
                                    jsonContent {
                                        schema = endpoint.responseType.openApiSchema
                                    }
                                }
                            }
                            endpoint.ircCommand?.let { addExtension("x-IRC-command", it) }
                        }
                    }
                }
            }
        }
        components {
            addParams()
        }
    }
}