import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r") as f:
    content = f.read()

pattern = r"""val filterChipBorder = .*?val isDarkTheme = MaterialTheme\.colorScheme\.onBackground == androidx\.compose\.ui\.graphics\.Color\.White\n\s*val filterChipBorder = .*?\n"""

def remove_filter(match):
    return "val isDarkTheme = MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White\n"

content = re.sub(r"val filterChipBorder = if \(isDarkTheme\).*?filterChipBorder\(\)\s*\n", "", content)

pattern_chip1 = r"""androidx\.compose\.material3\.FilterChip\(\s*selected = hijriSyncMode == "auto",\s*onClick = \{\s*hijriSyncMode = "auto"\s*sharedPref\.edit\(\)\.putString\("hijri_sync_mode", "auto"\)\.apply\(\)\s*\},\s*label = \{ Text\("تلقائي \(عبر الإنترنت\)"\) \},\s*modifier = Modifier\.weight\(1f\),\s*border = filterChipBorder\s*\)"""

replacement_chip1 = """androidx.compose.material3.FilterChip(
                        selected = hijriSyncMode == "auto",
                        onClick = { 
                            hijriSyncMode = "auto" 
                            sharedPref.edit().putString("hijri_sync_mode", "auto").apply()
                        },
                        label = { Text("تلقائي (عبر الإنترنت)") },
                        modifier = if (isDarkTheme) Modifier.weight(1f).border(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37), RoundedCornerShape(8.dp)) else Modifier.weight(1f),
                        border = null
                    )"""
content = re.sub(pattern_chip1, replacement_chip1, content)


pattern_chip2 = r"""androidx\.compose\.material3\.FilterChip\(\s*selected = hijriSyncMode == "manual",\s*onClick = \{\s*hijriSyncMode = "manual"\s*sharedPref\.edit\(\)\.putString\("hijri_sync_mode", "manual"\)\.apply\(\)\s*\},\s*label = \{ Text\("تعديل يدوي"\) \},\s*modifier = Modifier\.weight\(1f\),\s*border = filterChipBorder\s*\)"""

replacement_chip2 = """androidx.compose.material3.FilterChip(
                        selected = hijriSyncMode == "manual",
                        onClick = { 
                            hijriSyncMode = "manual" 
                            sharedPref.edit().putString("hijri_sync_mode", "manual").apply()
                        },
                        label = { Text("تعديل يدوي") },
                        modifier = if (isDarkTheme) Modifier.weight(1f).border(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37), RoundedCornerShape(8.dp)) else Modifier.weight(1f),
                        border = null
                    )"""
content = re.sub(pattern_chip2, replacement_chip2, content)

with open("app/src/main/java/com/example/ui/Screens.kt", "w") as f:
    f.write(content)
