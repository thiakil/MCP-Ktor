package com.thiakil.mcp.api

abstract class MappedObject<out T: MappedName>{
    /**
     * Get all the objects in this collection as an array of stable names
     */
    abstract suspend fun getAll(): Array<String>

    /**
     * Get the matching member by stable name
     */
    abstract suspend fun getOne(name: String): T?

    /**
     * Get the history for the member by stable name
     */
    abstract suspend fun getHistory(name: String): Array<String>?
}
