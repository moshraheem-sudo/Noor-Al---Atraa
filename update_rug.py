import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

new_shape = """@Composable
fun PrayerRugShape(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Outer border
        drawRect(
            color = color,
            topLeft = Offset(0f, h * 0.05f),
            size = Size(w, h * 0.9f),
            style = Stroke(width = 8f)
        )
        
        // Inner border
        drawRect(
            color = color.copy(alpha = 0.5f),
            topLeft = Offset(w * 0.05f, h * 0.08f),
            size = Size(w * 0.9f, h * 0.84f),
            style = Stroke(width = 4f)
        )

        // Arch (Mihrab) inside the rug
        val archPath = Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.15f, h * 0.85f)
            lineTo(w * 0.85f, h * 0.85f)
            lineTo(w * 0.85f, h * 0.35f)
            // Arch peak
            lineTo(w * 0.5f, h * 0.15f)
            close()
        }
        drawPath(
            path = archPath,
            color = color,
            style = Stroke(width = 6f, join = StrokeJoin.Round)
        )
        
        // Fringes (Tassels) Top and Bottom
        val fringeSpacing = w / 20
        for (i in 1..19) {
            val x = i * fringeSpacing
            // Top fringes
            drawLine(
                color = color,
                start = Offset(x, h * 0.05f),
                end = Offset(x, 0f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Bottom fringes
            drawLine(
                color = color,
                start = Offset(x, h * 0.95f),
                end = Offset(x, h),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}"""

content = re.sub(r"@Composable\nfun MihrabShape.*?}\n}", new_shape, content, flags=re.DOTALL)
content = content.replace("MihrabShape(color = goldColor", "PrayerRugShape(color = goldColor")
content = content.replace("// Mihrab background", "// Prayer Rug background")

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
