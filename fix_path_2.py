import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

old_path = """                            val path = Path().apply {
                                // Top edge (straight)
                                moveTo(cornerRadius, pointHeight)
                                lineTo(cw - cornerRadius, pointHeight)
                                
                                // Top right corner
                                arcTo(
                                    rect = Rect(cw - cornerRadius * 2, pointHeight, cw, pointHeight + cornerRadius * 2),
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
                                lineTo(0f, pointHeight + cornerRadius)
                                
                                // Top left corner
                                arcTo(
                                    rect = Rect(0f, pointHeight, cornerRadius * 2, pointHeight + cornerRadius * 2),
                                    startAngleDegrees = 180f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                
                                close()
                            }"""

new_path = """                            val path = Path().apply {
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
                            }"""

content = content.replace(old_path, new_path)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
