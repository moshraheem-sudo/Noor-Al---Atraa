import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Replace TouchApp with Refresh in imports
content = content.replace("import androidx.compose.material.icons.filled.TouchApp", "import androidx.compose.material.icons.filled.Refresh")

# Update icon and action
old_action = """                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.TouchApp, contentDescription = "Touch", tint = Color.White)
                    }
                },"""
new_action = """                actions = {
                    IconButton(onClick = { 
                        rakaaCount = 0
                        currentSujood = 0
                        isStarted = false
                        try {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                        } catch (e: Exception) {}
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                },"""
content = content.replace(old_action, new_action)

# Update Rakaa Section
old_rakaa = """                // Rakaa Section
                Box(
                    modifier = Modifier
                        .background(darkElementColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 6.dp)
                ) {
                    Text("ركعة", color = Color.White, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    IslamicStar(color = goldColor, size = 120f)
                    Text(
                        text = rakaaCount.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }"""
new_rakaa = """                // Rakaa Section
                Text("ركعة", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircleCounter(color = goldColor, size = 120f)
                    Text(
                        text = rakaaCount.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }"""
content = content.replace(old_rakaa, new_rakaa)

# Update Sujood Section
old_sujood = """                // Sujood Section
                Box(
                    modifier = Modifier
                        .background(darkElementColor, RoundedCornerShape(24.dp))
                        .padding(horizontal = 32.dp, vertical = 6.dp)
                ) {
                    Text("سجدة", color = Color.White, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    IslamicStar(color = goldColor, size = 100f)
                    Text(
                        text = currentSujood.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }"""
new_sujood = """                // Sujood Section
                Text("سجدة", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircleCounter(color = goldColor, size = 100f)
                    Text(
                        text = currentSujood.toString(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = bgColor
                    )
                }"""
content = content.replace(old_sujood, new_sujood)

# Replace IslamicStar definition with CircleCounter
old_star = """@Composable
fun IslamicStar(color: Color, size: Float) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2
        val cy = h / 2
        val outerRadius = w / 2
        val innerRadius = w / 2 * 0.7f

        val path = Path().apply {
            for (i in 0 until 8) {
                val angle = i * Math.PI / 4 - Math.PI / 8
                val x = cx + outerRadius * cos(angle).toFloat()
                val y = cy + outerRadius * sin(angle).toFloat()
                
                if (i == 0) moveTo(x, y) else lineTo(x, y)
                
                val innerAngle = angle + Math.PI / 8
                val ix = cx + innerRadius * cos(innerAngle).toFloat()
                val iy = cy + innerRadius * sin(innerAngle).toFloat()
                lineTo(ix, iy)
            }
            close()
        }
        drawPath(
            path = path,
            color = color
        )
    }
}"""
new_circle = """@Composable
fun CircleCounter(color: Color, size: Float) {
    Canvas(modifier = Modifier.size(size.dp)) {
        drawCircle(
            color = color,
            radius = size.dp.toPx() / 2f
        )
    }
}"""
content = content.replace(old_star, new_circle)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
