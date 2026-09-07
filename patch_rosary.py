import re

with open("app/src/main/java/com/example/ui/AppUI.kt", "r") as f:
    content = f.read()

pattern = r"""// Masbaha\s*item \{\s*Card\(\s*modifier = Modifier\.fillMaxWidth\(\)\.clickable\(onClick = onNavigateToTasbeeh\),\s*shape = RoundedCornerShape\(24\.dp\),\s*colors = CardDefaults\.cardColors\(containerColor = cardCream\),\s*elevation = CardDefaults\.cardElevation\(2\.dp\)\s*\) \{\s*Row\(\s*modifier = Modifier\.padding\(20\.dp\)\.fillMaxWidth\(\),\s*verticalAlignment = Alignment\.CenterVertically,\s*horizontalArrangement = Arrangement\.SpaceBetween\s*\) \{\s*Row\(verticalAlignment = Alignment\.CenterVertically\) \{\s*Text\("المسبحة", style = MaterialTheme\.typography\.titleLarge, fontWeight = FontWeight\.Bold, color = darkGreen\)\s*Spacer\(modifier = Modifier\.width\(16\.dp\)\)\s*Box\(modifier = Modifier\.size\(56\.dp\)\.background\(darkGreenCardBg, CircleShape\)\.padding\(8\.dp\), contentAlignment = Alignment\.Center\) \{\s*Text\("📿", style = MaterialTheme\.typography\.headlineSmall\)\s*\}\s*\}\s*Box\(modifier = Modifier\.size\(36\.dp\)\.background\(darkGreenCardBg, CircleShape\), contentAlignment = Alignment\.Center\) \{\s*Icon\(Icons\.AutoMirrored\.Filled\.ArrowBack, contentDescription = null, tint = androidx\.compose\.ui\.graphics\.Color\.White, modifier = Modifier\.size\(20\.dp\)\)\s*\}\s*\}\s*\}\s*\}"""

replacement = """// Masbaha
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToTasbeeh),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = darkGreenCardBg),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("المسبحة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(modifier = Modifier.size(56.dp).border(2.dp, gold, CircleShape).padding(8.dp), contentAlignment = Alignment.Center) {
                                    Text("📿", style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                            Box(modifier = Modifier.size(36.dp).background(androidx.compose.ui.graphics.Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF0F4C41), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/AppUI.kt", "w") as f:
    f.write(content)
