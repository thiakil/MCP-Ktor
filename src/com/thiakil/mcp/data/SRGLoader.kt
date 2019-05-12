package com.thiakil.mcp.data

import java.io.File

/**
 * Created by Thiakil on 5/05/2019.
 */
private operator fun MatchResult.get(group: String):String = groups[group]!!.value

class SRGLoader(lines: List<String>):StableNameDB(){
    init {
        for (line in lines){
            if (line.trim() == "") {
                continue
            }
            val parts = line.split(' ')
            when (parts[0]){
                "CL:" -> addClass(SRGClass(parts[1], parts[2]))
                "FD:" -> {
                    val obfOwner = parts[1].substringBeforeLast('/')
                    val obfName = parts[1].substringAfterLast('/')

                    val deobfName = parts[2].substringAfterLast('/')

                    this.classesByObf.getValue(obfOwner).fields.add(SRGField(obfName, deobfName))
                }
                "MD:" -> {//MD: a/a (I)La; net/minecraft/util/text/TextFormatting/func_175744_a (I)Lnet/minecraft/util/text/TextFormatting;
                    val obfNameGroup = parts[1]
                    val obfOwner = obfNameGroup.substringBeforeLast('/')
                    val obfName = obfNameGroup.substringAfterLast('/')

                    val deobfName = parts[3].substringAfterLast('/')

                    this.classesByObf.getValue(obfOwner).methods.add(SRGMethod(obfName, deobfName, parts[2], parts[4]))
                }
                "PK:" -> {}
                else -> error("unrecognised line: $line")
            }
        }
        /*lines.mapNotNull { CLASS_LINE.matchEntire(it) }.forEach { line ->
            addClass(SRGClass(line.groupValues[1], line.groupValues[2]))
        }
        lines.mapNotNull { FIELD_LINE.matchEntire(it) }.forEach { line ->
            val obfOwner = line.groupValues[1].substringBeforeLast('/')
            val obfName = line.groupValues[1].substringAfterLast('/')

            val deobfName = line.groupValues[2].substringAfterLast('/')

            this.classesByObf.getValue(obfOwner).fields.add(SRGField(obfName, deobfName))
        }
        lines.mapNotNull { METHOD_LINE.matchEntire(it) }.forEach { line ->
            val obfNameGroup = line["obfName"]
            val obfOwner = obfNameGroup.substringBeforeLast('/')
            val obfName = obfNameGroup.substringAfterLast('/')

            val deobfName = line["stableName"].substringAfterLast('/')

            this.classesByObf.getValue(obfOwner).methods.add(SRGMethod(obfName, deobfName, line["obfDesc"], line["stableDesc"]))
        }*/
    }

    companion object {
        val CLASS_LINE = Regex("^CL: (\\S+) (\\S+)$")
        //FD: aos$b/i net/minecraft/block/BlockRailBase$EnumRailDirection/NORTH_WEST
        val FIELD_LINE = Regex("^FD: (\\S+) (\\S+)$")
        //MD: a/a (I)La; net/minecraft/util/text/TextFormatting/func_175744_a (I)Lnet/minecraft/util/text/TextFormatting;
        val METHOD_LINE = Regex("^MD: (?<obfName>\\S+) (?<obfDesc>\\(\\S*\\)\\S+) (?<stableName>\\S+) (?<stableDesc>\\(\\S*\\)\\S+)$")
    }
}

fun main(){
    val loader = SRGLoader(File("test/1.12.srg").readLines())
    println(loader.classesByObf)
}