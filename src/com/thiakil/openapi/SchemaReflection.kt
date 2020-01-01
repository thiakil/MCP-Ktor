package com.thiakil.openapi

import io.swagger.v3.core.util.AnnotationsUtils
import io.swagger.v3.oas.models.media.*
import java.math.BigInteger
import java.util.*
import kotlin.reflect.*
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
        is KClass<*> -> this.openApiSchema
        else -> throw RuntimeException("Classifier isn't KClass??")
    }

val KType.openApiSchema : Schema<*> by LazyWithReceiver<KType, Schema<*>> {
    when (val classifier = classifier) {
        String::class -> StringSchema()
        Int::class, Long::class -> IntegerSchema()
        Float::class, Double::class, BigInteger::class -> NumberSchema()
        Boolean::class -> BooleanSchema()
        List::class -> arrayType()
        Date::class -> DateTimeSchema()
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
                val type: Schema<*> = AnnotationsUtils.getSchemaFromAnnotation(prop.findAnnotationMulti(), null).map {
                    if (it.type.isBlank || it.name.isBlank) {
                        val reflectedSchema = prop.returnType.openApiSchema
                        if (it.type.isBlank) {
                            it.type = reflectedSchema.type
                        }
                        if (it.name.isBlank) {
                            it.name = reflectedSchema.name
                        }
                    }
                    return@map it
                }.orElseGet { prop.returnType.openApiSchema }
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