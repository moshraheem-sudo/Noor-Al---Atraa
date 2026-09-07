import re

with open("app/src/main/java/com/example/qibla/QiblaCompassActivity.kt", "r") as f:
    content = f.read()

content = content.replace(".verticalScroll(rememberScrollState())", ".verticalScroll(androidx.compose.foundation.rememberScrollState())")

with open("app/src/main/java/com/example/qibla/QiblaCompassActivity.kt", "w") as f:
    f.write(content)
