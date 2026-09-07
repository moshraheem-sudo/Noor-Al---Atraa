import re

with open("app/src/main/java/com/example/ui/AppUI.kt", "r") as f:
    content = f.read()

# Add getLevantineMonthName helper if not exists
helper = """
fun getLevantineMonthName(month: Int): String {
    return when(month) {
        1 -> "كانون الثاني"
        2 -> "شباط"
        3 -> "آذار"
        4 -> "نيسان"
        5 -> "أيار"
        6 -> "حزيران"
        7 -> "تموز"
        8 -> "آب"
        9 -> "أيلول"
        10 -> "تشرين الأول"
        11 -> "تشرين الثاني"
        12 -> "كانون الأول"
        else -> ""
    }
}
"""
if "fun getLevantineMonthName" not in content:
    content = content.replace("fun getArabicWeekdayName", helper + "\nfun getArabicWeekdayName")

# Replace line 560
content = re.sub(
    r'val gregDateStr = remember \{ localDate.format\(java.time.format.DateTimeFormatter.ofPattern\("d MMMM yyyy", java.util.Locale.forLanguageTag\("ar"\)\)\) \}',
    r'val gregDateStr = remember { "${localDate.dayOfMonth} ${getLevantineMonthName(localDate.monthValue)} ${localDate.year}" }',
    content
)

# Replace line 1134
content = re.sub(
    r'val gregString = localDate.format\(java.time.format.DateTimeFormatter.ofPattern\("EEEE, d MMMM yyyy", java.util.Locale.forLanguageTag\("ar"\)\)\)',
    r'val gregString = "$weekdayAr، ${localDate.dayOfMonth} ${getLevantineMonthName(localDate.monthValue)} ${localDate.year}"',
    content
)

# Replace line 1222
content = re.sub(
    r'ev.eventDate.format\(java.time.format.DateTimeFormatter.ofPattern\("EEEE، d MMMM yyyy", java.util.Locale.forLanguageTag\("ar"\)\)\)',
    r'"${getArabicWeekdayName(ev.eventDate.dayOfWeek)}، ${ev.eventDate.dayOfMonth} ${getLevantineMonthName(ev.eventDate.monthValue)} ${ev.eventDate.year}"',
    content
)

with open("app/src/main/java/com/example/ui/AppUI.kt", "w") as f:
    f.write(content)
