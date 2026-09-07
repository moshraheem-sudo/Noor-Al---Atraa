import re

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "r") as f:
    content = f.read()

pattern = re.compile(r"@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun PrayerSelectionScreen.*?\}\s*\}", re.DOTALL)

new_func = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSelectionScreen(onBack: () -> Unit, onSelect: (PrayerOption) -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.let { color ->
        (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) < 0.5f
    }
    val titleColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary
    val cardBg = if (isDark) Color.Black else Color(0xFFFDF7E3)
    val textColor = if (isDark) Color.White else Color(0xFF1E1E1E)
    val goldColor = Color(0xFFD4AF37)

    val options = listOf(
        PrayerOption("صلاة الصبح", 2),
        PrayerOption("صلاة الظهر", 4),
        PrayerOption("صلاة العصر", 4),
        PrayerOption("صلاة المغرب", 3),
        PrayerOption("صلاة العشاء", 4),
        PrayerOption("عداد حر مفتوح", null)
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عداد الركع", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = titleColor,
                    navigationIconContentColor = titleColor
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "اختر الصلاة",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(options) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(option) }
                        .border(1.5.dp, goldColor, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}"""

content = pattern.sub(new_func, content)

with open("app/src/main/java/com/example/ui/RakaaCounterScreen.kt", "w") as f:
    f.write(content)
