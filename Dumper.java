import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Dumper {
    public static void main(String[] args) throws Exception {
        Class<?> wdClass = Class.forName("com.example.data.WorshipData");
        Object instance = wdClass.getField("INSTANCE").get(null);
        
        FileWriter out = new FileWriter("app/src/main/java/com/example/data/WorshipData.kt");
        out.write("package com.example.data\n\n");
        out.write("import java.time.DayOfWeek\n\n");
        out.write("object WorshipData {\n");
        
        // dailyPrayers
        out.write("    val dailyPrayers = mapOf(\n");
        Map<?, ?> daily = (Map<?, ?>) wdClass.getMethod("getDailyPrayers").invoke(instance);
        for (Map.Entry<?, ?> entry : daily.entrySet()) {
            Object pair = entry.getValue();
            String first = (String) pair.getClass().getMethod("getFirst").invoke(pair);
            String second = (String) pair.getClass().getMethod("getSecond").invoke(pair);
            out.write("        DayOfWeek." + entry.getKey().toString() + " to Pair(\"\"\"" + first + "\"\"\", \"\"\"" + second + "\"\"\"),\n");
        }
        out.write("    )\n");
        
        String[] lists = {"Taqeebat", "Munajat", "Tasbeehat", "AzkarList", "Ziyarats", "ZiyaratsOfDays", "SahifaSajjadiya", "GeneralPrayers", "SalawatsOnHujaj", "AhlAlBaytPrayers", "ConcisePrayers"};
        for (String name : lists) {
            String lowerName = name.substring(0, 1).toLowerCase() + name.substring(1);
            if (name.equals("SahifaSajjadiya")) {
                // monthlyDeeds is before SahifaSajjadiya
                out.write("    val monthlyDeeds = mapOf(\n");
                Map<?, ?> monthly = (Map<?, ?>) wdClass.getMethod("getMonthlyDeeds").invoke(instance);
                for (Map.Entry<?, ?> entry : monthly.entrySet()) {
                    out.write("        \"\"\"" + entry.getKey() + "\"\"\" to listOf(\n");
                    List<?> list = (List<?>) entry.getValue();
                    for (Object pair : list) {
                        String first = (String) pair.getClass().getMethod("getFirst").invoke(pair);
                        String second = (String) pair.getClass().getMethod("getSecond").invoke(pair);
                        out.write("            Pair(\"\"\"" + first + "\"\"\", \"\"\"" + second + "\"\"\"),\n");
                    }
                    out.write("        ),\n");
                }
                out.write("    )\n");
            }
            
            out.write("    val " + lowerName + " = listOf(\n");
            List<?> list = (List<?>) wdClass.getMethod("get" + name).invoke(instance);
            for (Object pair : list) {
                String first = (String) pair.getClass().getMethod("getFirst").invoke(pair);
                String second = (String) pair.getClass().getMethod("getSecond").invoke(pair);
                out.write("        Pair(\"\"\"" + first + "\"\"\", \"\"\"" + second + "\"\"\"),\n");
            }
            out.write("    )\n");
        }
        
        out.write("}\n");
        out.close();
        System.out.println("Dumped to app/src/main/java/com/example/data/WorshipData.kt successfully!");
    }
}
