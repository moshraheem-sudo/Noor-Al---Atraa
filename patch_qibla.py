import re

with open("app/src/main/java/com/example/qibla/QiblaCompassActivity.kt", "r") as f:
    content = f.read()

content = content.replace("Spacer(modifier = Modifier.weight(1f))", "Spacer(modifier = Modifier.height(16.dp))")
content = content.replace("Spacer(modifier = Modifier.weight(1.2f))", "Spacer(modifier = Modifier.height(24.dp))")
content = content.replace("Spacer(modifier = Modifier.weight(1.5f))", "Spacer(modifier = Modifier.height(32.dp))")

# Add verticalScroll to Column
old_column = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0073E6)), // Perfect vivid royal blue from screenshot
        horizontalAlignment = Alignment.CenterHorizontally
    ) {"""

new_column = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0073E6)) // Perfect vivid royal blue from screenshot
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {"""

content = content.replace(old_column, new_column)

with open("app/src/main/java/com/example/qibla/QiblaCompassActivity.kt", "w") as f:
    f.write(content)
