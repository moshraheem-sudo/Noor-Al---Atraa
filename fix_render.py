with open('app/src/main/java/com/example/ui/Screens.kt', 'r', encoding='utf-8') as f:
    content = f.read()

start_idx = content.find("    Column(\n        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)")
if start_idx == -1:
    print("Could not find start_idx")
    exit(1)

# Find the matching closing brace for this Column
brace_count = 0
end_idx = -1
for i in range(start_idx, len(content)):
    if content[i] == '{':
        brace_count += 1
    elif content[i] == '}':
        brace_count -= 1
        if brace_count == 0 and end_idx == -1: # the first { was at the Column block
            end_idx = i + 1
            break

new_render = """    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(modifier = Modifier.width(4.dp).height(48.dp).background(androidx.compose.ui.graphics.Color(0xFFE2C275))) // Vertical line
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "العبادات",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF0A3622)
                )
            }
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_ornate_divider),
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth().height(16.dp)
            )

            val rows = gridItems.chunked(4)
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                rows.forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { item ->
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f).clickable { onCategorySelected(item.title.replace("\\n", " ")) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(item.color, shape = CircleShape)
                                        .border(2.dp, androidx.compose.ui.graphics.Color(0xFFE2C275), CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = item.iconRes),
                                        contentDescription = item.title,
                                        tint = item.iconTint ?: androidx.compose.ui.graphics.Color(0xFFE2C275),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        repeat(4 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }"""

if end_idx != -1:
    content = content[:start_idx] + new_render + content[end_idx:]
    with open('app/src/main/java/com/example/ui/Screens.kt', 'w', encoding='utf-8') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Could not find end brace")

