import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Remove showSettingsDialog var
content = re.sub(r'var showSettingsDialog by remember \{ mutableStateOf\(false\) \}\n?', '', content)
# Remove the if (showSettingsDialog) block
content = re.sub(r'if \(showSettingsDialog\) \{\s*SettingsDialog\(onDismiss = \{ showSettingsDialog = false \}\)\s*\}\n?', '', content)

# Replace the Row representing the topBar
old_top_bar = """            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                            contentDescription = "تحميل المصحف كاملاً",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showBookmarksSheet = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Bookmark,
                            contentDescription = "العلامات المحفوظة",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Text(
                    text = "صوت القرءان",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }"""

new_top_bar = """            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "القرآن الكريم",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                            contentDescription = "تحميل المصحف كاملاً",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showBookmarksSheet = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Bookmark,
                            contentDescription = "العلامات المحفوظة",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }"""

if old_top_bar in content:
    content = content.replace(old_top_bar, new_top_bar)
    print("Top bar patched successfully.")
else:
    print("Top bar pattern not found.")
    
# Import automirrored back arrow if needed (it usually is available from Material3)
imports = "import androidx.compose.material.icons.automirrored.filled.ArrowBack\n"
if "ArrowBack" not in content:
    content = content.replace("import androidx.compose.material.icons.Icons", imports + "import androidx.compose.material.icons.Icons")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

