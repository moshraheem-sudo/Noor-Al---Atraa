import re

with open('app/src/main/java/com/example/ui/AppUI.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = """                                    Text(
                                        text = event.event.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val daysText = if (event.daysUntil == 0) "اليوم" else "متبقي ${event.daysUntil} يوم"
                                    val color = if (event.event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFFF8A80) else androidx.compose.ui.graphics.Color(0xFFA5D6A7)
                                    Text(
                                        text = daysText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )"""

replacement = """                                    Text(
                                        text = event.event.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = textColor,
                                        maxLines = 2,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val daysText = if (event.daysUntil == 0) "اليوم (${event.gregorianDateStr})" else "يصادف ${event.gregorianDateStr} (متبقي ${event.daysUntil} يوم)"
                                    val color = if (event.event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFFF8A80) else androidx.compose.ui.graphics.Color(0xFFA5D6A7)
                                    Text(
                                        text = daysText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )"""

new_content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/AppUI.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Updated banner")
