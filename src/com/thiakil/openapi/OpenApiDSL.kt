package com.thiakil.openapi

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses

@DslMarker
annotation class OpenApiDSL

@OpenApiDSL
fun responses(body: ApiResponses.()->Unit): ApiResponses {
    return ApiResponses().apply(body)
}

@OpenApiDSL
fun ApiResponses.response(responseCode: String, body: ApiResponse.() -> Unit) {
    addApiResponse(responseCode, ApiResponse().apply(body))
}

@OpenApiDSL
fun ApiResponses.default(body: ApiResponse.() -> Unit) {
    addApiResponse(ApiResponses.DEFAULT, ApiResponse().apply(body))
}

private fun ApiResponse.getOrCreateContent(): Content {
    return when (this.content){
        null -> {
            this.content = Content()
            this.content
        }
        else -> this.content
    }
}

@OpenApiDSL
fun ApiResponse.content(body: Content.()->Unit) {
    getOrCreateContent().apply(body)
}

@OpenApiDSL
fun ApiResponse.content(type: String, body: MediaType.()->Unit) {
    getOrCreateContent().apply {
        addMediaType(type, MediaType().apply(body))
    }
}

@OpenApiDSL
fun ApiResponse.jsonContent(body: MediaType.()->Unit) {
    content("application/json", body)
}

@OpenApiDSL
fun MediaType.objectSchema(body: ObjectSchema.()->Unit) {
    if (schema == null){
        schema = ObjectSchema()
    }
    (schema as ObjectSchema).apply(body)
}

@OpenApiDSL
fun MediaType.arraySchema(body: ArraySchema.()->Unit) {
    if (schema == null){
        schema = ArraySchema()
    }
    (schema as ArraySchema).apply(body)
}

@OpenApiDSL
fun ArraySchema.itemsObject(body: ObjectSchema.()->Unit){
    items = ObjectSchema().apply(body)
}

