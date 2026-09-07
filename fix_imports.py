import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

new_imports = """
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.drawscope.scale
"""

# Insert right after package
content = content.replace("package com.example.ui\n", "package com.example.ui\n" + new_imports)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
