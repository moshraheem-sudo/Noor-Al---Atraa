import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r") as f:
    content = f.read()

# Replace WorshipCategory("صلوات\nأهل البيت" with WorshipCategory("صلوات أهل البيت"
content = content.replace('WorshipCategory("صلوات\\nأهل البيت"', 'WorshipCategory("صلوات أهل البيت"')

# Change Text styling in grid
text_block_old = """                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )"""
text_block_new = """                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp, lineHeight = 14.sp),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )"""
content = content.replace(text_block_old, text_block_new)

with open("app/src/main/java/com/example/ui/Screens.kt", "w") as f:
    f.write(content)
