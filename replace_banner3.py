import re

with open('app/src/main/java/com/example/ui/AppUI.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = r"@Composable\s*fun AutoScrollingEventsBanner.*?\}\s*\}\s*\}\s*\}\s*\}"

replacement = """@Composable
fun AutoScrollingEventsBanner(hijriOffset: Int) {
    val isDark = MaterialTheme.colorScheme.background.let { color ->
        (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) < 0.5f
    }
    val cardBg = if (isDark) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color(0xFF0F4C41)
    val titleColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFD4AF37) else androidx.compose.ui.graphics.Color.White
    val textColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else androidx.compose.ui.graphics.Color.White

    val adjustedHijriDate = remember(hijriOffset) {
        java.time.chrono.HijrahDate.now().plus(hijriOffset.toLong(), java.time.temporal.ChronoUnit.DAYS)
    }
    val currentYear = adjustedHijriDate.get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    val currentMonth = adjustedHijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    val currentDay = adjustedHijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)

    val upcomingEvents = remember { mutableStateListOf<UpcomingEvent>() }
    LaunchedEffect(currentYear, currentMonth, currentDay, hijriOffset) {
        upcomingEvents.clear()
        for (event in AhlAlBaytRepository.events) {
            val eventMonth = event.month
            val eventDay = event.day
            var eventYear = currentYear
            if (eventMonth < currentMonth || (eventMonth == currentMonth && eventDay < currentDay)) {
                eventYear = currentYear + 1
            }
            try {
                val eventHijrahDate = java.time.chrono.HijrahDate.of(eventYear, eventMonth, eventDay)
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(adjustedHijriDate, eventHijrahDate).toInt()
                val eventLocalDate = java.time.LocalDate.now().plusDays(daysBetween.toLong())
                val gregorianDateStr = eventLocalDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                upcomingEvents.add(UpcomingEvent(event, daysBetween, gregorianDateStr, eventLocalDate, eventYear))
            } catch(e: Exception) {}
        }
        upcomingEvents.sortBy { it.daysUntil }
    }

    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(upcomingEvents.size) {
        if (upcomingEvents.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay(5000)
                currentIndex = (currentIndex + 1) % upcomingEvents.size
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("الأحداث", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = titleColor)
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.weight(1f).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    if (upcomingEvents.isNotEmpty()) {
                        androidx.compose.animation.AnimatedContent(
                            targetState = currentIndex,
                            transitionSpec = {
                                (androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)) + 
                                androidx.compose.animation.slideInVertically(animationSpec = androidx.compose.animation.core.tween(500)) { height -> height }).togetherWith(
                                    androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)) + 
                                    androidx.compose.animation.slideOutVertically(animationSpec = androidx.compose.animation.core.tween(500)) { height -> -height }
                                )
                            },
                            label = "events_banner"
                        ) { index ->
                            val event = upcomingEvents.getOrNull(index)
                            if (event != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(
                                        text = event.event.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val daysText = if (event.daysUntil == 0) "اليوم" else "متبقي ${event.daysUntil} يوم"
                                    val color = if (event.event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFFF8A80) else androidx.compose.ui.graphics.Color(0xFFA5D6A7)
                                    Text(
                                        text = daysText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = color,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        Text("لا توجد أحداث قريبة", color = textColor, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}"""

new_content = re.sub(target, replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/AppUI.kt', 'w', encoding='utf-8') as f:
    f.write(new_content)

print("Replaced AutoScrollingEventsBanner")
