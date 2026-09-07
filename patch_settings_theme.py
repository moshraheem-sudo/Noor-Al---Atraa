import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r") as f:
    content = f.read()

# Replace the Sawt Al Quran Banner with the Theme Selector
pattern = r'// Notification Banner for Sawt AL Quran App Availability.*?Card\([\s\S]*?border = BorderStroke\([\s\S]*?\n\s*\)\s*\{\s*Row\([\s\S]*?\}\s*\}\s*\}\s*\}\s*\}'
replacement = """// Theme Selector
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("مظهر التطبيق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("اختر الثيم المفضل لك", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                com.example.ui.theme.AppThemeMode.values().forEach { mode ->
                    val isSelected = com.example.ui.theme.ThemeManager.currentThemeMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable {
                                com.example.ui.theme.ThemeManager.setTheme(context, mode)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(mode.primaryColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode.titleAr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(mode.subtitleAr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }"""

if re.search(pattern, content):
    content = re.sub(pattern, replacement, content)
    print("Settings banner replaced.")
else:
    print("Settings banner pattern not found.")

with open("app/src/main/java/com/example/ui/Screens.kt", "w") as f:
    f.write(content)
