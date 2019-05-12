package com.thiakil.mcp.data

import java.io.File

/**
 * Created by Thiakil on 12/05/2019.
 */
class TSRGLoader(lines: List<String>):StableNameDB() {
    init {
        val mapper = SRGRemapper(this)
        var lastClazz: SRGClass? = null
        for (line in lines) {
            if (line.trim() == "") {
                continue
            }
            val parts = when(line[0]){
                '\t' -> line.substring(1)
                else -> line
            }.split(' ')

            when {
                line[0] != '\t' -> {
                    lastClazz = SRGClass(parts[0], parts[1])
                    addClass(lastClazz)
                    //println(lastClazz.stableName)
                }
                parts.size == 2 -> lastClazz!!.fields.add(SRGField(parts[0], parts[1]))
                parts.size == 3 -> lastClazz!!.methods.add(SRGMethod(parts[0], parts[2], parts[1], mapper))
                else -> println("Ignoring tsrg line: $line")
            }
        }
    }
}

fun main(){
    val loader = TSRGLoader(File("test/1.14.tsrg").readLines())
    println(loader.classesByObf)
}