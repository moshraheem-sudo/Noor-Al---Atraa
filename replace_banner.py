import re

with open('app/src/main/java/com/example/ui/AppUI.kt', 'r', encoding='utf-8') as f:
    content = f.read()

target = r"@OptIn\(androidx\.compose\.foundation\.ExperimentalFoundationApi::class\)\s*@Composable\s*fun AutoScrollingEventsBanner.*?\}\s*\}\s*\}"

replacement = """@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
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

    val nextEvent = upcomingEvents.firstOrNull()

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 2 })

    LaunchedEffect(pagerState) {
        while (true) {
            kotlinx.coroutines.delay(4000)
            val nextPage = (pagerState.currentPage + 1) % 2
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
                        // Upcoming Event
                        if (nextEvent != null) {
                            val isPainful = nextEvent.event.isMartyrdom
                            val titleText = if (isPainful) "ذكرى أليمة" else "ولادة أو ذكرى مفرحة"
                            val titleColor = if (isPainful) androidx.compose.ui.graphics.Color(0xFFFF8A80) else androidx.compose.ui.graphics.Color(0xFFA5D6A7)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(titleText, style = MaterialTheme.typography.titleMedium, color = titleColor, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(nextEvent.event.name, style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                val daysText = if (nextEvent.daysUntil == 0) "اليوم" else "متبقي ${nextEvent.daysUntil} يوم"
                                Text(daysText, style = MaterialTheme.typography.bodyMedium, color = androidx.compose.ui.graphics.Color.LightGray)
                            }
                        } else {
                            Text("لا توجد أحداث قريبة", color = androidx.compose.ui.graphics.Color.White)
                        }
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
