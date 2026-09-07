import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

new_topbar = """        topBar = {
            TopAppBar(
                title = { 
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("عداد الركع", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.TouchApp, contentDescription = "Touch", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },"""

content = re.sub(r"        topBar = \{.*?        \},", new_topbar, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
