with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

content = content.replace("androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack", "Icons.AutoMirrored.Filled.ArrowBack")
content = content.replace("import androidx.compose.material.icons.Icons\n", "import androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack\n")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
