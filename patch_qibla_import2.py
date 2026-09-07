import re

with open("app/src/main/java/com/example/qibla/QiblaCompassActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.foundation.background", "import androidx.compose.foundation.background\nimport androidx.compose.foundation.verticalScroll")

with open("app/src/main/java/com/example/qibla/QiblaCompassActivity.kt", "w") as f:
    f.write(content)
