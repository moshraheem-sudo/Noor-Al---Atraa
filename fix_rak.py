import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

pattern = r"// Ornate Counter Container.*?Spacer\(modifier = Modifier\.height\(8\.dp\)\)\n            \}"
new_container = """// Ornate Counter Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 16.dp)
                ) {
                    // Content inside the ornate shape
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                val cw = size.width
                                val ch = size.height
                                
                                val cornerRadius = 60f
                                val pointWidth = 120f
                                val pointHeight = 30f
                                
                                val path = Path().apply {
                                    // Top edge (straight)
                                    moveTo(cornerRadius, 0f)
                                    lineTo(cw - cornerRadius, 0f)
                                    
                                    // Top right corner
                                    arcTo(
                                        rect = Rect(cw - cornerRadius * 2, 0f, cw, cornerRadius * 2),
                                        startAngleDegrees = -90f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )
                                    
                                    // Right edge
                                    lineTo(cw, ch - pointHeight - cornerRadius)
                                    
                                    // Bottom right corner
                                    arcTo(
                                        rect = Rect(cw - cornerRadius * 2, ch - pointHeight - cornerRadius * 2, cw, ch - pointHeight),
                                        startAngleDegrees = 0f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )
                                    
                                    // Bottom edge and bottom point
                                    lineTo(cw/2 + pointWidth/2, ch - pointHeight)
                                    quadraticTo(cw/2 + pointWidth/4, ch - pointHeight, cw/2, ch)
                                    quadraticTo(cw/2 - pointWidth/4, ch - pointHeight, cw/2 - pointWidth/2, ch - pointHeight)
                                    
                                    // Bottom edge left
                                    lineTo(cornerRadius, ch - pointHeight)
                                    
                                    // Bottom left corner
                                    arcTo(
                                        rect = Rect(0f, ch - pointHeight - cornerRadius * 2, cornerRadius * 2, ch - pointHeight),
                                        startAngleDegrees = 90f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )
                                    
                                    // Left edge
                                    lineTo(0f, cornerRadius)
                                    
                                    // Top left corner
                                    arcTo(
                                        rect = Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 90f,
                                        forceMoveTo = false
                                    )
                                    
                                    close()
                                }
                                
                                // Outer dark fill
                                drawPath(path = path, color = darkShapeBg)
                                
                                // Outer gold border
                                drawPath(path = path, color = goldColor, style = Stroke(width = 6f))
                                
                                // Inner gold border
                                val innerScale = 0.94f
                                scale(innerScale, innerScale) {
                                    drawPath(path = path, color = goldColor.copy(alpha = 0.5f), style = Stroke(width = 3f))
                                }
                            }
                    ) {
                        Spacer(modifier = Modifier.weight(0.1f))
                        
                        // Rakaa Section
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Canvas(modifier = Modifier.size(12.dp)) { drawPath(createDiamondPath(6.dp.toPx(), 6.dp.toPx(), 6.dp.toPx()), goldColor) }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("ركعة", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(16.dp))
                            Canvas(modifier = Modifier.size(12.dp)) { drawPath(createDiamondPath(6.dp.toPx(), 6.dp.toPx(), 6.dp.toPx()), goldColor) }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            // Diamonds on left/right of circle
                            Canvas(modifier = Modifier.fillMaxWidth()) {
                                drawPath(createDiamondPath(size.width * 0.15f, size.height / 2, 10f), goldColor)
                                drawPath(createDiamondPath(size.width * 0.85f, size.height / 2, 10f), goldColor)
                            }
                            CircleCounter(color = lightGold, borderColor = goldColor, size = 110f)
                            Text(
                                text = rakaaCount.toString(),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(0.1f))
                        
                        // Separator line
                        Canvas(modifier = Modifier.fillMaxWidth(0.6f).height(1.dp)) {
                            drawLine(color = goldColor.copy(alpha=0.5f), start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 2f)
                        }
                        
                        Spacer(modifier = Modifier.weight(0.1f))
                        
                        // Sujood Section
                        Text("سجدة", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            CircleCounter(color = lightGold, borderColor = goldColor, size = 80f)
                            Text(
                                text = currentSujood.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = textDark,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(0.15f))
                    }
                }
            }"""

if re.search(pattern, content, flags=re.DOTALL):
    content = re.sub(pattern, new_container, content, flags=re.DOTALL)
else:
    print("Pattern not found!")

if "import androidx.compose.ui.draw.drawBehind" not in content:
    content = content.replace("import androidx.compose.ui.Alignment", "import androidx.compose.ui.draw.drawBehind\nimport androidx.compose.ui.Alignment")

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
