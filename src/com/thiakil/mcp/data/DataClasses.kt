package com.thiakil.mcp.data

import com.thiakil.mcp.api.*

/**
 * Created by Thiakil on 5/05/2019.
 */
data class MCVersion(
    override val major: Int,
    override val minor: Int,
    override val patch: Int
) : SemVer {
    override fun toString(): String {
        return "$major.$minor${if (patch != 0) ".$patch" else "" }"
    }
}

data class ClassMapping(
                        override val unstableName: String,
                        override var mappedName: String?,
                        override var documentation: String?,
                        override val type: MemberType,
                        override val version: SemVer
): MappedClass {
    override val memberType: MemberType
        get() = MemberType.CLASS
}

data class MethodMapping(
    override val stableName: String,
    override var mappedName: String?,
    override var documentation: String?,
    override val type: MemberType,
    override val version: SemVer,
    override val owners: MutableList<MappedClass> = mutableListOf(),
    override val params: MutableList<MappedParam> = mutableListOf(),
    override var isStatic: Boolean
): MappedMethod {
    override val memberType: MemberType
        get() = MemberType.METHOD
}

data class FieldMapping(
    override val stableName: String,
    override var mappedName: String?,
    override var documentation: String?,
    override val type: MemberType,
    override val version: SemVer,
    override val owner: ClassMapping
) : MappedField {
    override val memberType: MemberType
        get() = MemberType.FIELD
}

data class ParamMapping(
    override val stableName: String,
    override var mappedName: String?,
    override var documentation: String?,
    override val type: MemberType,
    override val version: SemVer,
    override val owner: MappedMethod
) : MappedParam {
    override val memberType: MemberType
        get() = MemberType.PARAM
}