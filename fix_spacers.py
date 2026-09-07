import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Reduce circle sizes and text sizes slightly more
content = content.replace("CircleCounter(color = lightGold, borderColor = goldColor, size = 110f)", "CircleCounter(color = lightGold, borderColor = goldColor, size = 100f)")
content = content.replace("fontSize = 56.sp,", "fontSize = 48.sp,")

content = content.replace("CircleCounter(color = lightGold, borderColor = goldColor, size = 80f)", "CircleCounter(color = lightGold, borderColor = goldColor, size = 70f)")
content = content.replace("fontSize = 40.sp,", "fontSize = 32.sp,")

# Reduce internal spacers
content = content.replace("Spacer(modifier = Modifier.height(16.dp))", "Spacer(modifier = Modifier.height(8.dp))")
content = content.replace("Spacer(modifier = Modifier.height(12.dp))", "Spacer(modifier = Modifier.height(6.dp))")
content = content.replace("Spacer(modifier = Modifier.height(24.dp))", "Spacer(modifier = Modifier.height(12.dp))")

# Also change the path pointHeight to be smaller, so it doesn't take too much vertical space
content = content.replace("val pointHeight = 50f", "val pointHeight = 30f")

# Wrap the main Column in verticalScroll just in case it doesn't fit
if "import androidx.compose.foundation.verticalScroll" not in content:
    content = content.replace("import androidx.compose.foundation.layout.BoxWithConstraints", "import androidx.compose.foundation.layout.BoxWithConstraints\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.foundation.rememberScrollState")

old_column = """                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        )"""
new_column = """                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        )"""
content = content.replace(old_column, new_column)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
