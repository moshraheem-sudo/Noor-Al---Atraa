import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r") as f:
    content = f.read()

pattern = r"""colors = androidx\.compose\.material3\.SliderDefaults\.colors\(\s*thumbColor = androidx\.compose\.ui\.graphics\.Color\.White,\s*activeTrackColor = androidx\.compose\.ui\.graphics\.Color\.White,\s*inactiveTrackColor = androidx\.compose\.ui\.graphics\.Color\.White\.copy\(alpha = 0\.5f\),\s*activeTickColor = androidx\.compose\.ui\.graphics\.Color\.Black,\s*inactiveTickColor = androidx\.compose\.ui\.graphics\.Color\.Black\s*\)"""

replacement = """colors = androidx.compose.material3.SliderDefaults.colors(
                                thumbColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                                activeTrackColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                activeTickColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.Black else MaterialTheme.colorScheme.onPrimary,
                                inactiveTickColor = if (MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White) androidx.compose.ui.graphics.Color.Black else MaterialTheme.colorScheme.primary
                            )"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/Screens.kt", "w") as f:
    f.write(content)
