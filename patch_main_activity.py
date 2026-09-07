import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add ThemeManager.init(this) to onCreate if not present
if "ThemeManager.init(this)" not in content:
    # Need to import ThemeManager
    imports = "import com.example.ui.theme.ThemeManager\n"
    content = content.replace("import com.example.ui.theme.MyApplicationTheme", imports + "import com.example.ui.theme.MyApplicationTheme")
    
    # insert ThemeManager.init(this) after super.onCreate(savedInstanceState)
    content = content.replace("super.onCreate(savedInstanceState)\n", "super.onCreate(savedInstanceState)\n        ThemeManager.init(this)\n")

# Replace MyApplicationTheme(themeMode = com.example.ui.theme.AppThemeMode.ICE_BLUE_LIGHT) with ThemeManager.currentThemeMode
content = re.sub(
    r'MyApplicationTheme\(themeMode = com\.example\.ui\.theme\.AppThemeMode\.ICE_BLUE_LIGHT\)',
    'MyApplicationTheme(themeMode = ThemeManager.currentThemeMode)',
    content
)

content = re.sub(
    r'MyApplicationTheme\(darkTheme = false\)',
    'MyApplicationTheme(themeMode = ThemeManager.currentThemeMode)',
    content
)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
print("MainActivity patched successfully.")

