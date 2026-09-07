import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports_to_add = """import android.media.AudioManager
import android.media.ToneGenerator
"""
content = content.replace("import android.hardware.SensorManager\n", "import android.hardware.SensorManager\n" + imports_to_add)

# Find ActiveCounterScreen and add tone generator
old_setup = """    val haptic = LocalHapticFeedback.current

    var rakaaCount"""

new_setup = """    val haptic = LocalHapticFeedback.current
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100) }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    var rakaaCount"""

content = content.replace(old_setup, new_setup)

# Add sound when registered
old_feedback = """                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (currentSujood == 1) {"""

new_feedback = """                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                            } catch (e: Exception) {
                                // Ignore
                            }
                            if (currentSujood == 1) {"""

content = content.replace(old_feedback, new_feedback)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
