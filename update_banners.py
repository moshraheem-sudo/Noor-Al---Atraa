import re

with open('app/src/main/java/com/example/ui/AppUI.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# We want to replace the "// Events Bottom" item block with our new AutoScrollingEventsBanner call.
# Actually, let's just replace the item { ... } block directly.

pattern = re.compile(r'// Events Bottom\s*item \{\s*var isEventsExpanded by remember \{ mutableStateOf\(false\) \}.*?\}\s*\}\s*\}', re.DOTALL)
match = pattern.search(content)

if match:
    replacement = """// Events Bottom
            item {
                AutoScrollingEventsBanner(hijriOffset = hijriOffset)
            }"""
    content = content.replace(match.group(0), replacement)
    print("Replaced Events Bottom block.")
else:
    print("Events Bottom block not found.")

# Now we need to define AutoScrollingEventsBanner. We can append it to the file.
auto_scrolling_banner = """
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun AutoScrollingEventsBanner(hijriOffset: Int) {
    val adjustedHijriDate = remember(hijriOffset) {
        java.time.chrono.HijrahDate.now().plus(hijriOffset.toLong(), java.time.temporal.ChronoUnit.DAYS)
    }
    val currentYear = adjustedHijriDate.get(java.time.temporal.ChronoField.YEAR_OF_ERA)
    val currentMonth = adjustedHijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
    val currentDay = adjustedHijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
    val monthName = com.example.data.getHijriMonthName(currentMonth)
    val weekdayAr = getArabicWeekdayName(java.time.LocalDate.now().dayOfWeek)
    val hijriString = "$weekdayAr $currentDay $monthName $currentYear هـ"
    
    val localDate = java.time.LocalDate.now()
    val gregString = "$weekdayAr، ${localDate.dayOfMonth} ${getLevantineMonthName(localDate.monthValue)} ${localDate.year}"

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

    val painfulEvents = upcomingEvents.filter { it.event.isMartyrdom }
    val happyEvents = upcomingEvents.filter { !it.event.isMartyrdom }

    val banner1_painful = painfulEvents.firstOrNull()
    val banner2_happy = happyEvents.firstOrNull()
    val banner3_happy = happyEvents.drop(1).firstOrNull()

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 })

    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            val nextPage = (pagerState.currentPage + 1) % 4
            pagerState.animateScrollToPage(nextPage)
        }
    }

    androidx.compose.foundation.pager.HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().height(140.dp),
        contentPadding = PaddingValues(horizontal = 24.dp),
        pageSpacing = 16.dp
    ) { page ->
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFF0F4C41)), // Match dark green theme
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                when (page) {
                    0 -> {
                        // Date Banner
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("التقويم", style = MaterialTheme.typography.titleMedium, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text(hijriString, style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(gregString, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.LightGray)
                        }
                    }
                    1 -> {
                        // Painful Event
                        if (banner1_painful != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("ذكرى أليمة", style = MaterialTheme.typography.titleMedium, color = androidx.compose.ui.graphics.Color(0xFFFF8A80), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(banner1_painful.event.name, style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                val daysText = if (banner1_painful.daysUntil == 0) "اليوم" else "متبقي ${banner1_painful.daysUntil} يوم"
                                Text(daysText, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.LightGray)
                            }
                        } else {
                            Text("لا توجد أحداث أليمة قريبة", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                    2 -> {
                        // Happy Event 1
                        if (banner2_happy != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("ولادة أو ذكرى مفرحة", style = MaterialTheme.typography.titleMedium, color = androidx.compose.ui.graphics.Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(banner2_happy.event.name, style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                val daysText = if (banner2_happy.daysUntil == 0) "اليوم" else "متبقي ${banner2_happy.daysUntil} يوم"
                                Text(daysText, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.LightGray)
                            }
                        } else {
                            Text("لا توجد أحداث مفرحة قريبة", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                    3 -> {
                        // Happy Event 2
                        if (banner3_happy != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text("ولادة أو ذكرى مفرحة", style = MaterialTheme.typography.titleMedium, color = androidx.compose.ui.graphics.Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(banner3_happy.event.name, style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                val daysText = if (banner3_happy.daysUntil == 0) "اليوم" else "متبقي ${banner3_happy.daysUntil} يوم"
                                Text(daysText, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.LightGray)
                            }
                        } else {
                            Text("لا توجد أحداث مفرحة أخرى", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }
        }
    }
}
"""

content += "\n" + auto_scrolling_banner

with open('app/src/main/java/com/example/ui/AppUI.kt', 'w', encoding='utf-8') as f:
    f.write(content)
print("Added AutoScrollingEventsBanner.")

