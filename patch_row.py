import re

with open("app/src/main/java/com/example/ui/AppUI.kt", "r") as f:
    content = f.read()

# Replace the row modifier
content = content.replace(
    'Row(\n                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),\n                    horizontalArrangement = Arrangement.spacedBy(12.dp)\n                )',
    'Row(\n                    modifier = Modifier.fillMaxWidth(),\n                    horizontalArrangement = Arrangement.spacedBy(12.dp)\n                )'
)

# Replace .fillMaxHeight() with .aspectRatio(1f)
content = content.replace(
    'modifier = Modifier.weight(1f).fillMaxHeight().clickable { navController.navigate("worship_text/زيارة يوم $weekdayAr") }',
    'modifier = Modifier.weight(1f).aspectRatio(0.9f).clickable { navController.navigate("worship_text/زيارة يوم $weekdayAr") }'
)
content = content.replace(
    'modifier = Modifier.weight(1f).fillMaxHeight().clickable { navController.navigate("worship_text/دعاء يوم $weekdayAr") }',
    'modifier = Modifier.weight(1f).aspectRatio(0.9f).clickable { navController.navigate("worship_text/دعاء يوم $weekdayAr") }'
)
content = content.replace(
    'modifier = Modifier.weight(1f).fillMaxHeight().clickable { navController.navigate("worship_text/تسبيح يوم $weekdayAr") }',
    'modifier = Modifier.weight(1f).aspectRatio(0.9f).clickable { navController.navigate("worship_text/تسبيح يوم $weekdayAr") }'
)

with open("app/src/main/java/com/example/ui/AppUI.kt", "w") as f:
    f.write(content)
