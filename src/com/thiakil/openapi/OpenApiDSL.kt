package com.thiakil.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag

@DslMarker
annotation class OpenApiDSL

typealias Body<T> = T.()->Unit

@OpenApiDSL
fun MediaType.objectSchema(body: Body<ObjectSchema>) {
    if (schema == null){
        schema = ObjectSchema()
    }
    (schema as ObjectSchema).apply(body)
}

@OpenApiDSL
fun MediaType.arraySchema(body: Body<ArraySchema>) {
    if (schema == null){
        schema = ArraySchema()
    }
    (schema as ArraySchema).apply(body)
}

@OpenApiDSL
fun ArraySchema.itemsObject(body: Body<ObjectSchema>){
    items = ObjectSchema().apply(body)
}

@OpenApiDSL
class OpenApiKt : OpenAPI(){
    @OpenApiDSL
    fun info(body: Body<InfoKt>){
        info = InfoKt().apply(body)
    }

    @OpenApiDSL
    fun server(body: Body<ServerKt>) {
        addServersItem(ServerKt().apply(body))
    }

    @OpenApiDSL
    fun tag(body: Body<TagKt>) {
        addTagsItem(TagKt().apply(body))
    }

    @OpenApiDSL
    fun paths(body: Body<PathsKt>) {
        if (paths == null){
            paths = PathsKt()
        } else if (paths !is PathsKt) {
            val oldPaths = paths
            val newPaths = PathsKt()
            paths = newPaths
            newPaths.putAll(oldPaths)
            newPaths.extensions = oldPaths.extensions
        }
        (paths as PathsKt).apply(body)
    }
}

@OpenApiDSL
fun openApi(body: Body<OpenApiKt>): OpenApiKt = OpenApiKt().apply(body)

@OpenApiDSL
class InfoKt: Info()

@OpenApiDSL
class ServerKt: Server()

@OpenApiDSL class TagKt: Tag()

@OpenApiDSL class PathsKt: Paths() {
    @OpenApiDSL
    fun path(name: String, body: Body<PathItemKt>) {
        addPathItem(name, PathItemKt().apply(body))
    }
}

@OpenApiDSL
class PathItemKt: PathItem() {
    @OpenApiDSL
    fun post(body: Body<OperationKt>) {
        post(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun get(body: Body<OperationKt>) {
        get(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun put(body: Body<OperationKt>) {
        put(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun patch(body: Body<OperationKt>) {
        patch(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun delete(body: Body<OperationKt>) {
        delete(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun head(body: Body<OperationKt>) {
        head(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun options(body: Body<OperationKt>) {
        options(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun trace(body: Body<OperationKt>) {
        trace(OperationKt().apply(body))
    }

    @OpenApiDSL
    fun operation(method: HttpMethod, body: Body<OperationKt>){
        operation(method, OperationKt().apply(body))
    }

    private fun io.ktor.http.HttpMethod.toOpenApi(): HttpMethod {
        return HttpMethod.valueOf(this.value)
    }

    @OpenApiDSL
    fun operation(method: io.ktor.http.HttpMethod, body: Body<OperationKt>){
        operation(method.toOpenApi(), OperationKt().apply(body))
    }
}

@OpenApiDSL
class OperationKt: Operation() {
    @OpenApiDSL
    fun tags(vararg tags: String) {
        tags.forEach { addTagsItem(it) }
    }

    @OpenApiDSL
    fun parameter(body: Body<ParamKt>) {
        addParametersItem(ParamKt().apply(body))
    }

    @OpenApiDSL
    fun requestBody(body: Body<RequestBodyKt>) {
        requestBody(RequestBodyKt().apply(body))
    }

    @OpenApiDSL
    fun responses(body: Body<ResponsesKt>){
        if (responses == null) {
            responses = ResponsesKt()
        } else if (responses !is ResponsesKt) {
            val newResponses = ResponsesKt()
            newResponses.putAll(responses)
            newResponses.extensions = responses.extensions
            responses = newResponses
        }
        (responses as ResponsesKt).apply(body)
    }
}

@OpenApiDSL
class ParamKt: Parameter()

@OpenApiDSL
class RequestBodyKt: RequestBody()

@OpenApiDSL
class ResponsesKt: ApiResponses() {
    @OpenApiDSL
    fun response(responseCode: String, body: Body<ResponseKt>) {
        addApiResponse(responseCode, ResponseKt().apply(body))
    }

    @OpenApiDSL
    fun default(body: Body<ResponseKt>) {
        addApiResponse(DEFAULT, ResponseKt().apply(body))
    }
}

@OpenApiDSL
class ResponseKt: ApiResponse() {
    private fun getOrCreateContent(): Content {
        return when (this.content){
            null -> {
                this.content = Content()
                this.content
            }
            else -> this.content
        }
    }

    @OpenApiDSL
    fun content(body: Body<Content>) {
        getOrCreateContent().apply(body)
    }

    @OpenApiDSL
    fun content(type: String, body: Body<MediaType>) {
        getOrCreateContent().apply {
            addMediaType(type, MediaType().apply(body))
        }
    }

    @OpenApiDSL
    fun jsonContent(body: Body<MediaType>) {
        content("application/json", body)
    }
}