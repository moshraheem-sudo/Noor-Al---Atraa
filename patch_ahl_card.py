import re

with open("app/src/main/java/com/example/ui/Screens.kt", "r") as f:
    content = f.read()

# Replace AhlAlBaytGridCard
pattern = r"""(@Composable\s*)?fun AhlAlBaytGridCard\(.*?onClick: \(\) -> Unit\n\) \{.*?\n\}\n(?=@|fun|\s*$)"""
replacement = """@Composable
fun AhlAlBaytGridCard(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.onBackground == androidx.compose.ui.graphics.Color.White
    val textColor = if (isDark) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.primary
    val cardBg = if (isDark) androidx.compose.ui.graphics.Color.Black else MaterialTheme.colorScheme.surface
    var baseModifier = modifier.height(80.dp).clickable { onClick() }
    
    val borderStroke = if (isDark) {
        androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.ui.graphics.Color(0xFFD4AF37))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
    
    Card(
        modifier = baseModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    fontSize = 15.sp
                ),
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
"""
content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/Screens.kt", "w") as f:
    f.write(content)

