package com.thiakil.openapi

import com.fasterxml.jackson.annotation.JsonView
import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.*
import org.apache.commons.lang3.StringUtils
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

// https://stackoverflow.com/a/38084930
class LazyWithReceiver<This,Return>(val initializer:This.()->Return)
{
    private val values = WeakHashMap<This,Return>()

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef:Any,property: KProperty<*>):Return = synchronized(values)
    {
        thisRef as This
        return values.getOrPut(thisRef) {thisRef.initializer()}
    }
}

val KClassifier.openApiSchema: Schema<*>
    get() = when (this){
        String::class -> StringSchema()
        Int::class, Long::class -> IntegerSchema()
        Float::class, Double::class, BigInteger::class -> NumberSchema()
        Boolean::class -> BooleanSchema()
        Date::class -> DateTimeSchema()
        is KClass<*> -> this.openApiSchema
        else -> throw RuntimeException("Classifier isn't KClass??")
    }

val KType.openApiSchema : Schema<*> by LazyWithReceiver<KType, Schema<*>> {
    when (val classifier = classifier) {
        List::class -> arrayType()
        null -> throw RuntimeException("Classifier was null??")
        else -> {
            if (classifier.javaClass.isArray) {
                arrayType()
            } else {
                classifier.openApiSchema
            }
        }
    }
}

private fun KType.arrayType(): ArraySchema {
    return ArraySchema().apply {
        items = arguments[0].type?.classifier?.openApiSchema
            ?: throw RuntimeException("Not sure how to handle array")
    }
}

//copied so we can use ArraySchema.arraySchema
fun getArraySchema(arraySchema: io.swagger.v3.oas.annotations.media.ArraySchema?): ArraySchema? {
    if (arraySchema == null || !AnnotationsUtils.hasArrayAnnotation(arraySchema)) {
        return null
    }
    val arraySchemaObject = ArraySchema()
    if (arraySchema.uniqueItems) {
        arraySchemaObject.uniqueItems = arraySchema.uniqueItems
    }
    if (arraySchema.maxItems > 0) {
        arraySchemaObject.maxItems = arraySchema.maxItems
    }
    if (arraySchema.minItems < Int.MAX_VALUE) {
        arraySchemaObject.minItems = arraySchema.minItems
    }
    if (arraySchema.extensions.isNotEmpty()) {
        val extensions = AnnotationsUtils.getExtensions(*arraySchema.extensions)
        if (extensions != null) {
            for (ext in extensions.keys) {
                arraySchemaObject.addExtension(ext, extensions[ext])
            }
        }
    }
    if (arraySchema.schema.implementation == Void::class.java) {
        AnnotationsUtils.getSchemaFromAnnotation(arraySchema.schema, null, null)
            .ifPresent { schema: Schema<*> ->
                if (StringUtils.isNotBlank(schema.type) || StringUtils.isNotBlank(schema.`$ref`)) {
                    arraySchemaObject.items = schema
                }
            }
    }
    AnnotationsUtils.getSchemaFromAnnotation(arraySchema.arraySchema, null, null).ifPresent { schema ->
        arraySchemaObject.apply {
            title = schema.title
            uniqueItems = schema.uniqueItems
            required = schema.required
            description = schema.description
            nullable = schema.nullable
            readOnly = schema.readOnly
            writeOnly = schema.writeOnly
            example = schema.example
            externalDocs = schema.externalDocs
            deprecated = schema.deprecated
            xml = schema.xml
            extensions = schema.extensions
        }
    }
    return arraySchemaObject
}

fun getAnnotatedSchema(prop: KProperty<*>): Schema<*>? {
    return AnnotationsUtils.getSchemaFromAnnotation(prop.findAnnotationMulti(), null).orElseGet {
        getArraySchema(prop.findAnnotationMulti())
    }
}

val <T : Any> KClass<T>.openApiSchema: Schema<*> by LazyWithReceiver<KClass<T>, Schema<*>> {
    when {
        this === List::class -> ArraySchema().also {
            it.items = typeParameters[0].upperBounds[0].classifier?.openApiSchema
                ?: throw RuntimeException("Not sure how to handle List")
        }
        java.isArray -> ArraySchema().also {
            it.items = java.componentType.kotlin.openApiSchema
        }
        else -> Schema<T>().also { schema ->
            schema.name = this.simpleName
            schema.type = "object"

            this.memberProperties.forEach { prop ->
                val reflectedSchema = prop.returnType.openApiSchema
                val annotatedSchema = getAnnotatedSchema(prop)
                val type: Schema<*> = when {
                    annotatedSchema == null -> reflectedSchema
                    reflectedSchema is ArraySchema && annotatedSchema is ArraySchema -> {
                        annotatedSchema.also {
                            it.name = reflectedSchema.name
                            if (it.items != null)
                                it.items.type = reflectedSchema.items.type //force type to be from reflection
                            else
                                it.items = reflectedSchema.items
                        }
                    }
                    reflectedSchema !is ArraySchema && annotatedSchema !is ArraySchema -> {
                        annotatedSchema.also {
                            it.name = reflectedSchema.name
                            it.type = reflectedSchema.type //force type to be from reflection
                        }
                    }
                    else ->
                        throw IllegalStateException("Reflected schema & annotation schema not same types: ${this.simpleName}")
                }
                schema.addProperties(prop.name, type)
            }
        }
    }
}

val String?.isBlank: Boolean get() = this == null || this == "" || this.trim() == ""
val String?.isNotBlank: Boolean get() = !isBlank

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Annotation> KProperty<*>.findAnnotationMulti(): T? =
    annotations.firstOrNull { it is T } as T?
        ?: javaField?.getAnnotation(T::class.java)
        ?: getter.findAnnotation()