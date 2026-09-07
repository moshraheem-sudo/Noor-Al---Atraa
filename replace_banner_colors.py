import re

with open('app/src/main/java/com/example/ui/AppUI.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                                    Text(
                                        text = if (targetShowHijri) "التاريخ الهجري" else "التاريخ الميلادي",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (targetShowHijri) hijriString else gregorianString,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )"""

replacement = """                                    Text(
                                        text = if (targetShowHijri) "التاريخ الهجري" else "التاريخ الميلادي",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = gold,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (targetShowHijri) hijriString else gregorianString,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = gold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )"""

new_content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/AppUI.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Replaced colors")
