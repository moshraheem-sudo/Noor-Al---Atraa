import com.example.data.WorshipData
import java.io.File
import java.time.DayOfWeek

fun main() {
    val data = WorshipData.INSTANCE
    val sb = java.lang.StringBuilder()
    sb.append("package com.example.data\n\n")
    sb.append("import java.time.DayOfWeek\n\n")
    sb.append("object WorshipData {\n")
    
    fun escapeString(s: String): String {
        return "\"\"\"" + s + "\"\"\""
    }
    
    sb.append("    val dailyPrayers = mapOf(\n")
    data.dailyPrayers.forEach { (k, v) ->
        sb.append("        DayOfWeek.${k.name} to Pair(${escapeString(v.first)}, ${escapeString(v.second)}),\n")
    }
    sb.append("    )\n")
    
    fun dumpList(name: String, list: List<Pair<String, String>>) {
        sb.append("    val $name = listOf(\n")
        list.forEach { v ->
            sb.append("        Pair(${escapeString(v.first)}, ${escapeString(v.second)}),\n")
        }
        sb.append("    )\n")
    }
    
    dumpList("taqeebat", data.taqeebat)
    dumpList("munajat", data.munajat)
    dumpList("tasbeehat", data.tasbeehat)
    dumpList("azkarList", data.azkarList)
    dumpList("ziyarats", data.ziyarats)
    dumpList("ziyaratsOfDays", data.ziyaratsOfDays)
    
    sb.append("    val monthlyDeeds = mapOf(\n")
    data.monthlyDeeds.forEach { (k, vList) ->
        sb.append("        ${escapeString(k)} to listOf(\n")
        vList.forEach { v ->
            sb.append("            Pair(${escapeString(v.first)}, ${escapeString(v.second)}),\n")
        }
        sb.append("        ),\n")
    }
    sb.append("    )\n")
    
    dumpList("sahifaSajjadiya", data.sahifaSajjadiya)
    dumpList("generalPrayers", data.generalPrayers)
    dumpList("salawatsOnHujaj", data.salawatsOnHujaj)
    dumpList("ahlAlBaytPrayers", data.ahlAlBaytPrayers)
    dumpList("concisePrayers", data.concisePrayers)
    
    sb.append("}\n")
    
    File("app/src/main/java/com/example/data/WorshipData.kt").writeText(sb.toString())
}
