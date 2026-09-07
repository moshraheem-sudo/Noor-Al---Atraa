import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Fix bottom bar padding
old_bottom_padding = """                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp),"""
new_bottom_padding = """                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),"""
content = content.replace(old_bottom_padding, new_bottom_padding)

# Fix path to remove top arch
old_path = """                            val path = Path().apply {
                                // Top point and top edge
                                moveTo(cw/2, 0f)
                                quadraticBezierTo(cw/2 + pointWidth/4, pointHeight, cw/2 + pointWidth/2, pointHeight)
                                lineTo(cw - cornerRadius, pointHeight)
                                
                                // Top right corner"""

new_path = """                            val path = Path().apply {
                                // Top edge (straight)
                                moveTo(cornerRadius, pointHeight)
                                lineTo(cw - cornerRadius, pointHeight)
                                
                                // Top right corner"""

content = content.replace(old_path, new_path)

# Update deprecations
content = content.replace("quadraticBezierTo", "quadraticTo")

# Fix text sizes and circle sizes to fit the screen
old_sizes_1 = """                                CircleCounter(color = lightGold, borderColor = goldColor, size = 130f)
                                Text(
                                    text = rakaaCount.toString(),
                                    fontSize = 64.sp,"""
new_sizes_1 = """                                CircleCounter(color = lightGold, borderColor = goldColor, size = 110f)
                                Text(
                                    text = rakaaCount.toString(),
                                    fontSize = 56.sp,"""
content = content.replace(old_sizes_1, new_sizes_1)

old_sizes_2 = """                                CircleCounter(color = lightGold, borderColor = goldColor, size = 100f)
                                Text(
                                    text = currentSujood.toString(),
                                    fontSize = 48.sp,"""
new_sizes_2 = """                                CircleCounter(color = lightGold, borderColor = goldColor, size = 80f)
                                Text(
                                    text = currentSujood.toString(),
                                    fontSize = 40.sp,"""
content = content.replace(old_sizes_2, new_sizes_2)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
