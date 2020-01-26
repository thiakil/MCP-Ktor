package com.thiakil.mcp.index

import cuchaz.enigma.translation.representation.AccessFlags
import cuchaz.enigma.translation.representation.Signature
import cuchaz.enigma.translation.representation.entry.ClassDefEntry
import cuchaz.enigma.translation.representation.entry.ClassEntry
import net.minecraftforge.api.distmarker.Dist

/**
 * Created by Thiakil on 26/01/2020.
 */
class ClassSrgEntry(
    parent: ClassEntry?,
    className: String,
    signature: Signature?,
    access: AccessFlags,
    superClass: ClassEntry?,
    interfaces: Array<out ClassEntry>?
) : ClassDefEntry(parent, className, signature, access, superClass, interfaces) {
    constructor(className: String, signature: Signature?, access:AccessFlags, superClass: ClassEntry?, interfaces: Array<ClassEntry>?) :
        this(getOuterClass(className), getInnerName(className), signature, access, superClass, interfaces)

    var dist: Dist? = null

    companion object {
        fun parse(
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<String>?
        ): ClassSrgEntry {
            val superClass = superName?.let { ClassEntry(it) }
            val interfaceClasses = interfaces?.map { className -> ClassEntry(className) }?.toTypedArray()
            return ClassSrgEntry(
                name,
                Signature.createSignature(signature),
                AccessFlags(access),
                superClass,
                interfaceClasses
            )
        }
    }
}