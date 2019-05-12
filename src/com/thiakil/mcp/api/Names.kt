package com.thiakil.mcp.api

enum class MemberType(val prefix: String) {
    METHOD("func_"),
    FIELD("field_"),
    PARAM("p_"),
    CLASS("C_")
}

/**
 * Semantic version-like version number. In string format separated by periods (.)
 *
 * major.minor.patch
 * e.g. 1.2.3
 * major  = 1
 * minor  = 2
 * patch  = 3
 */
interface SemVer {
    val major: Int

    val minor: Int

    val patch: Int
}

interface UnstableName {
    /**
     * Unstable or Obfuscated name
     */
    val unstableName: String

    /**
     * The Minecraft version the unstable name references
     */
    val version: SemVer
}

interface MappedName {
    /**
     * SRG or Stable name for this member.
     * e.g. func_1234_ab, field_1234_ab, p_1234_1_, p_i1234_1_
     */
    val stableName: String

    /**
     * Human readable, mapped name.
     */
    val mappedName: String?

    /**
     * If this member has been named
     */
    val hasMappedName: Boolean get() = mappedName != null

    /**
     * Any Javadoc attached. For field/method members the main Javadoc comment,
     * for params the <code>@param</code> line.
     */
    val documentation: String?

    /**
     * What kind of member this represents
     */
    val type: MemberType

    /**
     * The Minecraft version this mapped name is assigned to
     */
    val version: SemVer

    /**
     * Marker value for what type this instance is
     */
    val memberType: MemberType
}

interface MappedMethod: MappedName {
    /**
     * Classes that contain this method (base and overrides)
     */
    val owners: List<MappedClass>

    //todo base class - the root class containing this method

    val params: List<MappedParam>

    /**
     * If this method is static; affects param index start point
     */
    val isStatic: Boolean
}

interface MappedField: MappedName {
    /**
     * The Class that this field is owned by
     */
    val owner: MappedClass
}

interface MappedParam: MappedName {
    /**
     * The method this is a param to
     */
    val owner: MappedMethod
}

interface MappedClass: MappedName, UnstableName {
    /**
     * MCP Stable names for classes are only used before a name has been given
     * They are not really stable between versions.
     * Will be equal to mapped name when given a name
     */
    override val stableName: String get() = if (hasMappedName) mappedName!! else unstableName
}