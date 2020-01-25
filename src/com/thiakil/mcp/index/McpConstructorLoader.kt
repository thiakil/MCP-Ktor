package com.thiakil.mcp.index

import cuchaz.enigma.translation.representation.MethodDescriptor
import cuchaz.enigma.translation.representation.entry.ClassEntry
import cuchaz.enigma.translation.representation.entry.MethodEntry
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Created by Thiakil on 25/01/2020.
 */
object McpConstructorLoader {
    fun load(constructorFile: Path): Map<MethodEntry, String> {
        return mutableMapOf<MethodEntry, String>().also { output ->
            val constructorsLines = Files.readAllLines(constructorFile, StandardCharsets.UTF_8)
            for (line in constructorsLines) {
                val (idx, clazz, desc) = line.split(" ")
                val methodEntry = MethodEntry(ClassEntry(clazz), "<init>", MethodDescriptor(desc))
                output[methodEntry] = idx
            }
        }
    }
}