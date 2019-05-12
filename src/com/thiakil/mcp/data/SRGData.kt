package com.thiakil.mcp.data

import org.objectweb.asm.commons.Remapper

/**
 * Created by Thiakil on 5/05/2019.
 */
interface StableMapping {
    val obfName: String
    val stableName: String
}

open class StableNameDB {
    /** unstable name to entry **/
    private val _classesByObf:MutableMap<String, SRGClass> = mutableMapOf()
    val classesByObf: Map<String, SRGClass> get() = _classesByObf

    private val _classesByStable: MutableMap<String, SRGClass> = mutableMapOf()
    val classesByStable: Map<String, SRGClass> get() = _classesByStable

    protected fun addClass(clazz: SRGClass) {
        _classesByObf[clazz.obfName] = clazz
        _classesByStable[clazz.stableName] = clazz
    }
}

data class SRGClass(
    override val obfName: String,
    override val stableName: String,
    val methods: MutableList<SRGMethod> = mutableListOf(),
    val fields: MutableList<SRGField> = mutableListOf()
): StableMapping {
    fun getMethodByObf(obfName: String, obfDesc:String): SRGMethod? {
        return methods.first { it.obfName == obfName && it.obfSignature == obfDesc }
    }

    fun getFieldByObf(obfName: String): SRGField? {
        return fields.first { it.obfName == obfName }
    }
}

data class SRGField(
    override val obfName: String,
    override val stableName: String
): StableMapping

data class SRGMethod(
    override val obfName: String,
    override val stableName: String,
    val obfSignature: String,
    var _stableSignature: String? = null
): StableMapping {
    constructor(obfName: String, stableName: String, obfSignature: String, mapper: Remapper): this(obfName, stableName, obfSignature) {
        this.mapper = mapper
    }

    private var mapper: Remapper? = null

    val stableSignature: String get() = when (_stableSignature) {
        null -> {
            _stableSignature = mapper!!.mapMethodDesc(obfSignature)
            _stableSignature!!
        }
        else -> _stableSignature!!
    }
}

class SRGRemapper(private val db: StableNameDB):Remapper() {
    override fun map(internalName: String): String {
        return db.classesByObf[internalName]?.stableName ?: internalName
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        return db.classesByObf[owner]?.getMethodByObf(name, descriptor)?.stableName ?: name
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        return db.classesByObf[owner]?.getFieldByObf(name)?.stableName ?: name
    }
}