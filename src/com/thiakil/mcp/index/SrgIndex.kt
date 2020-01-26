package com.thiakil.mcp.index

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.SetMultimap
import com.google.common.collect.TreeMultimap
import cuchaz.enigma.analysis.ClassCache
import cuchaz.enigma.analysis.index.JarIndexer
import cuchaz.enigma.translation.representation.TypeDescriptor.Primitive.DOUBLE
import cuchaz.enigma.translation.representation.TypeDescriptor.Primitive.LONG
import cuchaz.enigma.translation.representation.entry.ClassDefEntry
import cuchaz.enigma.translation.representation.entry.ClassEntry
import cuchaz.enigma.translation.representation.entry.FieldDefEntry
import cuchaz.enigma.translation.representation.entry.MethodDefEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import net.minecraftforge.api.distmarker.Dist
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.nio.file.Paths

/**
 * Created by Thiakil on 25/01/2020.
 */
@Suppress("MemberVisibilityCanBePrivate")
class SrgIndex(val constructorIndices: Map<MethodEntry, String>) : JarIndexer {
    val classes: MutableMap<ClassEntry, ClassSrgEntry> = mutableMapOf()
    /** Method name -> method instances */
    val methods: ListMultimap<String, MethodDefEntry> = ArrayListMultimap.create()
    /** method srg index -> instances. containts constructors too */
    val methodsByIndex: ListMultimap<Int, MethodDefEntry> = ArrayListMultimap.create()
    /** method name -> param names*/
    val methodParams: SetMultimap<MethodEntry, String> = TreeMultimap.create()
    /** Param name -> method instances */
    val paramsIndex: ListMultimap<String, MethodDefEntry> = ArrayListMultimap.create()
    /** field name -> field entry */
    val fields: MutableMap<String, FieldDefEntry> = mutableMapOf()
    /** owner class -> method def */
    val constructors: ListMultimap<ClassEntry, MethodDefEntry> = ArrayListMultimap.create()

    override fun indexClass(classEntry: ClassDefEntry) {
        classes[classEntry] = classEntry as ClassSrgEntry
    }

    override fun indexMethod(methodEntry: MethodDefEntry) {
        var doParams = false
        if (methodEntry.name.startsWith("func_")) {
            methods.put(methodEntry.name, methodEntry)
            methodsByIndex.put(methodEntry.name.split("_")[1].toInt(), methodEntry)
            doParams = true
        } else if (methodEntry.name == "<init>") {
            constructors.put(methodEntry.containingClass!!, methodEntry)
            constructorIndices[methodEntry]?.let { idx ->
                methodsByIndex.put(idx.toInt(), methodEntry)
                doParams = true
            }
        }
        if (doParams && methodEntry.desc.argumentDescs.isNotEmpty()) {
            makeParams(methodEntry).forEach {
                paramsIndex.put(it, methodEntry)
                methodParams.put(methodEntry, it)
            }
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
        private var classEntry: ClassSrgEntry? = null
        private val classEntryNN: ClassSrgEntry get() = classEntry ?: error("visit not called first")

        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<String>?
        ) {
            classEntry = ClassSrgEntry.parse(access, name, signature, superName, interfaces).also { indexClass(it) }
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitField(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            value: Any?
        ): FieldVisitor? {
            indexField(FieldDefEntry.parse(classEntryNN, access, name, desc, signature))
            return super.visitField(access, name, desc, signature, value)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<String>?
        ): MethodVisitor? {
            indexMethod(MethodDefEntry.parse(classEntryNN, access, name, desc, signature))
            return super.visitMethod(access, name, desc, signature, exceptions)
        }

        override fun visitEnd() {
            super.visitEnd()
            classEntry = null
        }

        override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
            if (descriptor == "Lnet/minecraftforge/api/distmarker/OnlyIn;") {
                return object : AnnotationVisitor(Opcodes.ASM7) {
                    var dist: Dist? = null
                    var interf: Type? = null
                    override fun visitEnum(name: String?, descriptor: String?, value: String?) {
                        if (name == "value" && descriptor == "Lnet/minecraftforge/api/distmarker/Dist;") {
                            if (value != null) {
                                dist = Dist.valueOf(value)
                            }
                        }
                    }

                    override fun visit(name: String?, value: Any?) {
                        if (name == "_interface" && value is Type) {
                            interf = value
                        }
                    }

                    override fun visitEnd() {
                        if (interf == null) {
                            classEntryNN.dist = dist
                        }
                    }
                }
            }
            return null
        }
    }

    companion object {
        fun load(mcpPath: String, mcVersion: String): SrgIndex {
            val cache = ClassCache.of(Paths.get("$mcpPath/build/versions/$mcVersion/$mcVersion.joined.mapped.jar"))
            return SrgIndex(McpConstructorLoader.load(Paths.get("$mcpPath/versions/$mcVersion/constructors.txt"))).also { it.index(cache) }
        }
    }
}