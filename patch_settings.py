import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r") as f:
    content = f.read()

# Fix 1: Number of notifications text color
pattern_num = r"""(Text\("\$\{notificationsPerHour\.toInt\(\)\} إشعارات", style = MaterialTheme\.typography\.titleSmall, fontWeight = FontWeight\.Bold, color = )MaterialTheme\.colorScheme\.primary(\))"""
replacement_num = r"\1if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary\2"
content = re.sub(pattern_num, replacement_num, content, count=1)

# Fix 2: Hijri buttons
pattern_hijri = r"""Row\(\s*modifier = Modifier\.fillMaxWidth\(\),\s*horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\s*\)\s*\{\s*androidx\.compose\.material3\.FilterChip\(.*?androidx\.compose\.material3\.FilterChip\(.*?\}\s*if \(hijriSyncMode == "manual"\) \{"""

def fix_hijri(match):
    return """
                val isDarkTheme = MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White
                val filterChipBorder = if (isDarkTheme) androidx.compose.material3.FilterChipDefaults.filterChipBorder(borderColor = androidx.compose.ui.graphics.Color(0xFFD4AF37), borderWidth = 1.5.dp) else androidx.compose.material3.FilterChipDefaults.filterChipBorder()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.FilterChip(
                        selected = hijriSyncMode == "auto",
                        onClick = { 
                            hijriSyncMode = "auto" 
                            sharedPref.edit().putString("hijri_sync_mode", "auto").apply()
                        },
                        label = { Text("تلقائي (عبر الإنترنت)") },
                        modifier = Modifier.weight(1f),
                        border = filterChipBorder
                    )
                    
                    androidx.compose.material3.FilterChip(
                        selected = hijriSyncMode == "manual",
                        onClick = { 
                            hijriSyncMode = "manual" 
                            sharedPref.edit().putString("hijri_sync_mode", "manual").apply()
                        },
                        label = { Text("تعديل يدوي") },
                        modifier = Modifier.weight(1f),
                        border = filterChipBorder
                    )
                }
                if (hijriSyncMode == "manual") {"""

content = re.sub(pattern_hijri, fix_hijri, content, flags=re.DOTALL)

pattern_btn = r"""Button\(\s*onClick = \{(.*?)\},\s*modifier = Modifier\.fillMaxWidth\(\),\s*colors = ButtonDefaults\.buttonColors\(containerColor = MaterialTheme\.colorScheme\.primary\)\s*\) \{"""

def fix_btn(match):
    return """Button(
                        onClick = {""" + match.group(1) + """},
                        modifier = if (isDarkTheme) Modifier.fillMaxWidth().border(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37), RoundedCornerShape(24.dp)) else Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {"""

content = re.sub(pattern_btn, fix_btn, content, flags=re.DOTALL)


with open("app/src/main/java/com/example/ui/Screens.kt", "w") as f:
    f.write(content)
