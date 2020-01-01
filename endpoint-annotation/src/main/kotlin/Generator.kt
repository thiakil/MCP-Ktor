package com.thiakil.mcp.endpoints

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class Generator: AbstractProcessor() {

    val kaptKotlinGeneratedDir: String
        get() = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        println("getSupportedAnnotationTypes")
        return mutableSetOf(Endpoint::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        when (processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]) {
            null, "" -> {
                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Required option $KAPT_KOTLIN_GENERATED_OPTION_NAME is blank")
                throw RuntimeException("Required option $KAPT_KOTLIN_GENERATED_OPTION_NAME is blank")
            }
        }
    }

    override fun process(set: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (set.isEmpty()){
            return false
        }
        val classes = roundEnv.getElementsAnnotatedWith(Endpoint::class.java)
            .map {
                val className = it.simpleName.toString()
                val pack = processingEnv.elementUtils.getPackageOf(it).toString()
                pack + "." + className
            }
        if (classes.isNotEmpty()) {
            generateClass("GeneratedEndpointList", "com.thiakil.mcp.endpoints", classes)
        } else {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "No classes found")
        }
        return true
    }

    private fun generateClass(className: String, pack: String, classes: List<String>) {
        val file = FileSpec.builder(pack, className)
            .addType(
                TypeSpec.objectBuilder(className)
                    .addProperty(
                        PropertySpec.builder(
                            "ENDPOINTS",
                            ParameterizedTypeName.get(
                                ClassName.bestGuess("kotlin.Array"),
                                ParameterizedTypeName.get(
                                    ClassName.bestGuess("com.thiakil.mcp.endpoints.EndpointHandler"),
                                    WildcardTypeName.subtypeOf(Any::class)
                                )
                            )
                        ).initializer(
                            CodeBlock.of("arrayOf(%L)", classes.map { CodeBlock.of("%T", ClassName.bestGuess(it)) }.joinToCode(separator = ",%W"))
                        ).addKdoc("Objects annotated with [Endpoint]\n").build()
                    )
                    .addKdoc("This is a generated class, do not edit directly. Use Gradle task 'kaptKotlin' to regenerate\n")
                    .build()
            )
            .build()

        file.writeTo(Paths.get(kaptKotlinGeneratedDir))
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}