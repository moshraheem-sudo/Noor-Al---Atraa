import re

with open("app/src/main/java/com/example/ui/AppUI.kt", "r") as f:
    content = f.read()

# For Ziyarat
content = content.replace(
    'Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)',
    'Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)'
)

content = content.replace(
    'Box(modifier = Modifier.size(48.dp).background(cardMint, CircleShape), contentAlignment = Alignment.Center)',
    'Box(modifier = Modifier.size(40.dp).background(cardMint, CircleShape), contentAlignment = Alignment.Center)'
)

content = content.replace(
    'Spacer(modifier = Modifier.height(12.dp))',
    'Spacer(modifier = Modifier.height(6.dp))'
)

with open("app/src/main/java/com/example/ui/AppUI.kt", "w") as f:
    f.write(content)
