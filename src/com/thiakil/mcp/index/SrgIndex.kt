package com.thiakil.mcp.index

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import cuchaz.enigma.analysis.ClassCache
import cuchaz.enigma.analysis.ReferenceTargetType
import cuchaz.enigma.analysis.index.JarIndexer
import cuchaz.enigma.translation.representation.Lambda
import cuchaz.enigma.translation.representation.TypeDescriptor.Primitive.DOUBLE
import cuchaz.enigma.translation.representation.TypeDescriptor.Primitive.LONG
import cuchaz.enigma.translation.representation.entry.ClassDefEntry
import cuchaz.enigma.translation.representation.entry.ClassEntry
import cuchaz.enigma.translation.representation.entry.FieldDefEntry
import cuchaz.enigma.translation.representation.entry.MethodDefEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.nio.file.Paths

/**
 * Created by Thiakil on 25/01/2020.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SrgIndex(val constructorIndices: Map<MethodEntry, String>) : JarIndexer {
    val classes: MutableSet<ClassDefEntry> = mutableSetOf()
    /** Method name -> method instances */
    val methods: Multimap<String, MethodDefEntry> = ArrayListMultimap.create()
    /** Param name -> method instances */
    val params: Multimap<String, MethodDefEntry> = ArrayListMultimap.create()
    /** field name -> field entry */
    val fields: MutableMap<String, FieldDefEntry> = mutableMapOf()
    /** owner class -> method def */
    val constructors: Multimap<ClassEntry, MethodDefEntry> = ArrayListMultimap.create()

    override fun indexClass(classEntry: ClassDefEntry) {
        classes.add(classEntry)
    }

    override fun indexMethod(methodEntry: MethodDefEntry) {
        if (methodEntry.name.startsWith("func_")) {
            methods.put(methodEntry.name, methodEntry)
            if (methodEntry.desc.argumentDescs.isNotEmpty()) {
                makeParams(methodEntry).forEach { params.put(it, methodEntry) }
            }
        } else if (methodEntry.name == "<init>") {
            constructors.put(methodEntry.containingClass!!, methodEntry)
        }
    }

    override fun indexField(fieldEntry: FieldDefEntry) {
        if (fieldEntry.name.startsWith("field_")) {
            fields[fieldEntry.name] = fieldEntry
        }
    }

    private fun makeParams(method: MethodDefEntry): List<String> {
        var idx = if (method.access.isStatic) 0 else 1
        val prefix = if (method.isConstructor) "i" else ""
        val listOut = mutableListOf<String>()
        val srgIdx = if (method.isConstructor) {
            constructorIndices[method] ?: error("constructor not found")
        } else {
            method.name.split("_")[1]
        }
        for (param in method.desc.argumentDescs) {
            listOut.add("p_${prefix}${srgIdx}_${idx}_")
            idx += if (param.isPrimitive) {
                when (param.primitive) {
                    DOUBLE, LONG -> 2
                    else -> 1
                }
            } else {
                1
            }
        }
        return listOut
    }

    fun index(cache: ClassCache) {
        cache.visit(::IndexClassVisitor, 0)
    }

    private inner class IndexClassVisitor : ClassVisitor(Opcodes.ASM7) {
        private var classEntry: ClassDefEntry? = null
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<String>?
        ) {
            classEntry = ClassDefEntry.parse(access, name, signature, superName, interfaces).also { indexClass(it) }
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitField(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            value: Any?
        ): FieldVisitor? {
            indexField(FieldDefEntry.parse(classEntry!!, access, name, desc, signature))
            return super.visitField(access, name, desc, signature, value)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<String>?
        ): MethodVisitor? {
            indexMethod(MethodDefEntry.parse(classEntry!!, access, name, desc, signature))
            return super.visitMethod(access, name, desc, signature, exceptions)
        }
    }

    companion object {
        fun load(mcpPath: String, mcVersion: String): SrgIndex {
            val cache = ClassCache.of(Paths.get("$mcpPath/build/versions/$mcVersion/$mcVersion.joined.mapped.jar"))
            return SrgIndex(McpConstructorLoader.load(Paths.get("$mcpPath/versions/$mcVersion/constructors.txt"))).also { it.index(cache) }
        }
    }
}