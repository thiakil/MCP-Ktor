package com.thiakil.mcp

import com.thiakil.mcp.endpoints.Endpoints
import com.thiakil.mcp.endpoints.Endpoints.addParams
import com.thiakil.mcp.endpoints.GeneratedEndpointList
import com.thiakil.openapi.isNotBlank
import com.thiakil.openapi.openApi
import com.thiakil.openapi.openApiSchema
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import java.util.*
import kotlin.collections.HashMap

private typealias SchemaIdentityMap = IdentityHashMap<Schema<*>, Schema<*>>

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
        components {
            addParams()
        }
        val myComponents = components
        val myIdentityMap: SchemaIdentityMap = IdentityHashMap()
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
                                        schema = makeRefs(endpoint.responseType.openApiSchema, myComponents, myIdentityMap)
                                    }
                                }
                            }
                            endpoint.ircCommand?.let { addExtension("x-IRC-command", it) }
                        }
                    }
                }
            }
        }
    }
}

private fun Schema<*>.copy():Schema<*> {
    val newSchema = when (this) {
        is ArraySchema -> ArraySchema().also {
            it.items = this.items
        }
        else -> Schema<Any>()
    }
    newSchema.name = this.name
    newSchema.setDefault(this.default)
    newSchema.title = this.title
    newSchema.multipleOf = this.multipleOf
    newSchema.maximum = this.maximum
    newSchema.exclusiveMaximum = this.exclusiveMaximum
    newSchema.minimum = this.minimum
    newSchema.exclusiveMinimum = this.exclusiveMinimum
    newSchema.maxLength = this.maxLength
    newSchema.minLength = this.minLength
    newSchema.pattern = this.pattern
    newSchema.maxItems = this.maxItems
    newSchema.minItems = this.minItems
    newSchema.uniqueItems = this.uniqueItems
    newSchema.maxProperties = this.maxProperties
    newSchema.minProperties = this.minProperties
    newSchema.required = this.required
    newSchema.type = this.type
    newSchema.not = this.not
    if (this.properties != null)
        newSchema.properties = HashMap(this.properties)
    newSchema.additionalProperties = this.additionalProperties
    newSchema.description = this.description
    newSchema.format = this.format
    newSchema.`$ref` = this.`$ref`
    newSchema.nullable = this.nullable
    newSchema.readOnly = this.readOnly
    newSchema.writeOnly = this.writeOnly
    newSchema.example = this.example
    newSchema.externalDocs = this.externalDocs
    newSchema.deprecated = this.deprecated
    newSchema.xml = this.xml
    newSchema.extensions = this.extensions
    newSchema.enum = this.enum
    newSchema.discriminator = this.discriminator

    return newSchema
}

fun makeRefs(schema: Schema<*>, components: Components, identityMap: SchemaIdentityMap): Schema<*> {
    if (components.schemas == null){
        components.schemas = mutableMapOf()
    }
    val schemas = components.schemas!!

    if (identityMap.containsKey(schema)){
        return identityMap[schema]!!
    } else if (schema.`$ref` == null && schema.name.isNotBlank && (!schemas.containsKey(schema.name) || schemas[schema.name] === schema)){
        val copy = schema.copy()
        schemas[schema.name] = copy
        val refSchema = Schema<Any>().`$ref`(schema.name)
        identityMap[schema] = refSchema
        copy.properties?.let { props->
            for (p in props) {
                val newval = makeRefs(p.value, components, identityMap)
                if (newval !== p.value)
                p.setValue(newval)
            }
        }
        return refSchema
    } else if (schema is ArraySchema && schema.items != null){
        return schema.copy().also { (it as ArraySchema).items = makeRefs(schema.items, components, identityMap) }
    }

    return schema
}