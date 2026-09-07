import re

with open("app/src/main/java/com/example/ui/AppUI.kt", "r") as f:
    content = f.read()

# Fix 1: تعقيبات الصلاة
pattern1 = r"""(Box\(modifier = Modifier.size\(36.dp\).background\(androidx.compose.ui.graphics.Color.White, CircleShape\), contentAlignment = Alignment.Center\) \{\s*Icon\(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color\(0xFF0F4C41\), modifier = Modifier.size\(20.dp\)\)\s*\})\s*(Row\(verticalAlignment = Alignment.CenterVertically\) \{\s*Text\("تعقيبات الصلاة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White\)\s*Spacer\(modifier = Modifier.width\(16.dp\)\)\s*Box\(modifier = Modifier.size\(56.dp\).border\(2.dp, gold, CircleShape\).padding\(8.dp\), contentAlignment = Alignment.Center\) \{\s*Text\("🤲", style = MaterialTheme.typography.headlineSmall\)\s*\}\s*\})"""
replacement1 = r"\2\n                            \1"
content = re.sub(pattern1, replacement1, content, flags=re.DOTALL)

# Fix 2: المسبحة
pattern2 = r"""(Box\(modifier = Modifier.size\(36.dp\).background\(darkGreenCardBg, CircleShape\), contentAlignment = Alignment.Center\) \{\s*Icon\(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size\(20.dp\)\)\s*\})\s*(Row\(verticalAlignment = Alignment.CenterVertically\) \{\s*Text\("المسبحة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = darkGreen\)\s*Spacer\(modifier = Modifier.width\(16.dp\)\)\s*Box\(modifier = Modifier.size\(56.dp\).background\(darkGreenCardBg, CircleShape\).padding\(8.dp\), contentAlignment = Alignment.Center\) \{\s*Text\("📿", style = MaterialTheme.typography.headlineSmall\)\s*\}\s*\})"""
replacement2 = r"\2\n                            \1"
content = re.sub(pattern2, replacement2, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/AppUI.kt", "w") as f:
    f.write(content)
