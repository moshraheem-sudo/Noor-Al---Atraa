import re

with open('app/src/main/java/com/example/UpdateChecker.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Replace showUpdateDialog
target = r"private fun showUpdateDialog\(context: Context, newVersion: String, apkUrl: String\)\s*\{.*?\}\s*private fun startDownload"

replacement = """private fun showUpdateDialog(context: Context, newVersion: String, apkUrl: String) {
        try {
            val intent = Intent(context, UpdateActivity::class.java).apply {
                putExtra("APK_URL", apkUrl)
                putExtra("NEW_VERSION", newVersion)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startDownload"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/UpdateChecker.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Updated showUpdateDialog")
