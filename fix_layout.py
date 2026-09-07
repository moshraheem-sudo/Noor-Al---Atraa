import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Fix MosqueBackground by removing the arch
old_mosque = """@Composable
fun MosqueBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Base dark warm color
        drawRect(Color(0xFF141210))
        
        // Background wall texture
        val archPath = Path().apply {
            moveTo(w * 0.05f, h)
            lineTo(w * 0.05f, h * 0.4f)
            
            // Pointed arch
            quadraticBezierTo(w * 0.05f, h * 0.2f, w * 0.2f, h * 0.15f)
            quadraticBezierTo(w * 0.5f, h * 0.05f, w * 0.8f, h * 0.15f)
            quadraticBezierTo(w * 0.95f, h * 0.2f, w * 0.95f, h * 0.4f)
            
            lineTo(w * 0.95f, h)
            close()
        }
        
        drawPath(
            path = archPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F1A17), Color(0xFF132A25), Color(0xFF132A25))
            )
        )
        
        // Inner glowing border of the arch
        drawPath(
            path = archPath,
            color = Color(0xFFC8A261).copy(alpha = 0.4f),
            style = Stroke(width = 8f)
        )
        drawPath(
            path = archPath,
            color = Color(0xFFC8A261).copy(alpha = 0.2f),
            style = Stroke(width = 16f)
        )
        
        // A floor line
        drawLine(
            color = Color(0xFFC8A261).copy(alpha = 0.2f),
            start = Offset(0f, h * 0.85f),
            end = Offset(w, h * 0.85f),
            strokeWidth = 4f
        )
        
        // Simple rug shape
        val rugPath = Path().apply {
            moveTo(w * 0.2f, h)
            lineTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.8f, h)
            close()
        }
        drawPath(
            path = rugPath,
            color = Color(0xFF0D1B17).copy(alpha = 0.8f)
        )
        
        // Rug border
        drawPath(
            path = rugPath,
            color = Color(0xFFC8A261).copy(alpha = 0.3f),
            style = Stroke(width = 4f)
        )
    }
}"""
new_mosque = """@Composable
fun MosqueBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Base dark warm color
        drawRect(Color(0xFF141210))
        
        // A floor line
        drawLine(
            color = Color(0xFFC8A261).copy(alpha = 0.2f),
            start = Offset(0f, h * 0.85f),
            end = Offset(w, h * 0.85f),
            strokeWidth = 4f
        )
        
        // Simple rug shape
        val rugPath = Path().apply {
            moveTo(w * 0.2f, h)
            lineTo(w * 0.35f, h * 0.85f)
            lineTo(w * 0.65f, h * 0.85f)
            lineTo(w * 0.8f, h)
            close()
        }
        drawPath(
            path = rugPath,
            color = Color(0xFF0D1B17).copy(alpha = 0.8f)
        )
        
        // Rug border
        drawPath(
            path = rugPath,
            color = Color(0xFFC8A261).copy(alpha = 0.3f),
            style = Stroke(width = 4f)
        )
    }
}"""

content = content.replace(old_mosque, new_mosque)

# Make components smaller so they fit
# 1. Rakaa circle and text
content = content.replace("CircleCounter(color = lightGold, borderColor = goldColor, size = 180f)", "CircleCounter(color = lightGold, borderColor = goldColor, size = 130f)")
content = content.replace("fontSize = 90.sp,", "fontSize = 64.sp,")

# 2. Sujood circle and text
content = content.replace("CircleCounter(color = lightGold, borderColor = goldColor, size = 140f)", "CircleCounter(color = lightGold, borderColor = goldColor, size = 100f)")
content = content.replace("fontSize = 70.sp,", "fontSize = 48.sp,")

# 3. Spacers reduction
content = content.replace("Spacer(modifier = Modifier.height(24.dp))", "Spacer(modifier = Modifier.height(12.dp))")

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
