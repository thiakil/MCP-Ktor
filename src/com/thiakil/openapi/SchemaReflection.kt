package com.thiakil.openapi

import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.media.*
import java.math.BigInteger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

/**
 * Created by Thiakil on 31/12/2019.
 */
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

val KType.openApiType : Schema<*> by LazyWithReceiver<KType, Schema<*>> {
    when (val classifier = classifier) {
        String::class -> StringSchema()
        Int::class, Long::class -> IntegerSchema()
        Float::class, Double::class, BigInteger::class -> NumberSchema()
        Boolean::class -> BooleanSchema()
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
                val type: Schema<*> = prop.returnType.openApiType
                prop.findAnnotation<Description>()?.let {
                        if (it.value != "") {
                            type.description = it.value
                        }
                    }
                schema.addProperties(prop.name, type)
            }
        }
    }
}

@Target(AnnotationTarget.PROPERTY)
annotation class Description(val value: String)