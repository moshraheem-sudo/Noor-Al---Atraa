cat << 'INNER_EOF' > /tmp/CalendarPatch.kt
fun HijriCalendarCard(hijriOffset: Int, showEvents: Boolean = false) {
    val isDark = MaterialTheme.colorScheme.background.let { color ->
        (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) < 0.5f
    }
    val darkGreen = if (isDark) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color(0xFF0F4C41)
    
    val todayHijri = androidx.compose.runtime.remember(hijriOffset) { java.time.chrono.HijrahDate.now().plus(hijriOffset.toLong(), java.time.temporal.ChronoUnit.DAYS) }
    var currentYear by androidx.compose.runtime.remember(todayHijri) { androidx.compose.runtime.mutableStateOf(todayHijri.get(java.time.temporal.ChronoField.YEAR_OF_ERA)) }
    var currentMonth by androidx.compose.runtime.remember(todayHijri) { androidx.compose.runtime.mutableStateOf(todayHijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)) }
    
    val monthName = com.example.data.getHijriMonthName(currentMonth)
    
    val firstDayStandard = androidx.compose.runtime.remember(currentYear, currentMonth) { java.time.chrono.HijrahDate.of(currentYear, currentMonth, 1) }
    val lengthOfMonth = firstDayStandard.lengthOfMonth()
    
    val firstDayObservedGregorian = androidx.compose.runtime.remember(firstDayStandard, hijriOffset) {
        java.time.LocalDate.from(firstDayStandard).minusDays(hijriOffset.toLong())
    }
    
    val firstDayOfWeek = firstDayObservedGregorian.dayOfWeek.value // 1=Mon, 7=Sun
    val startDayIndex = if (firstDayOfWeek == 7) 0 else firstDayOfWeek // 0=Sun, 1=Mon...
    
    fun nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
    }
    
    fun prevMonth() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
    }
    
    val currentMonthEvents = androidx.compose.runtime.remember(currentMonth) {
        com.example.data.AhlAlBaytRepository.events.filter { it.month == currentMonth }.sortedBy { it.day }
    }

    androidx.compose.material3.Card(
        modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(horizontal = if (showEvents) 24.dp else 0.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.White),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(2.dp)
    ) {
        androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(16.dp)) {
            // Header
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.IconButton(onClick = { prevMonth() }) {
                    @Suppress("DEPRECATION")
                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Filled.KeyboardArrowRight, contentDescription = "Previous Month", tint = darkGreen)
                }
                androidx.compose.material3.Text(
                    text = "$monthName ${currentYear.toArabicNumerals()}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isDark) MaterialTheme.colorScheme.onSurface else androidx.compose.ui.graphics.Color.Black
                )
                androidx.compose.material3.IconButton(onClick = { nextMonth() }) {
                    @Suppress("DEPRECATION")
                    androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Filled.KeyboardArrowLeft, contentDescription = "Next Month", tint = darkGreen)
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            
            val daysOfWeek = listOf("ح", "ن", "ث", "ر", "خ", "ج", "س")
            androidx.compose.foundation.layout.Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround) {
                daysOfWeek.forEach { day ->
                    androidx.compose.material3.Text(
                        text = day,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.ui.graphics.Color.Gray,
                        modifier = androidx.compose.ui.Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            
            val totalCells = startDayIndex + lengthOfMonth
            val rows = kotlin.math.ceil(totalCells / 7.0).toInt()
            
            for (row in 0 until rows) {
                androidx.compose.foundation.layout.Row(modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceAround) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startDayIndex + 1
                        
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .border(
                                    width = 0.5.dp,
                                    color = if (isDark) androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.4f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            if (dayNumber in 1..lengthOfMonth) {
                                val isToday = currentYear == todayHijri.get(java.time.temporal.ChronoField.YEAR_OF_ERA) &&
                                              currentMonth == todayHijri.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) &&
                                              dayNumber == todayHijri.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                                val hasEvent = currentMonthEvents.any { it.day == dayNumber }
                                
                                androidx.compose.foundation.layout.Box(
                                    modifier = androidx.compose.ui.Modifier
                                        .fillMaxSize()
                                        .background(if (isToday) androidx.compose.ui.graphics.Color(0xFFE50000) else androidx.compose.ui.graphics.Color.Transparent, androidx.compose.foundation.shape.CircleShape),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                                        androidx.compose.material3.Text(
                                            text = dayNumber.toArabicNumerals(),
                                            color = if (isToday) androidx.compose.ui.graphics.Color.White else if (col == 5) androidx.compose.ui.graphics.Color(0xFF2962FF) else androidx.compose.ui.graphics.Color.Gray,
                                            fontWeight = if (isToday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                                        )
                                        if (showEvents && hasEvent && !isToday) {
                                            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.size(4.dp).background(if (currentMonthEvents.find { it.day == dayNumber }?.isMartyrdom == true) androidx.compose.ui.graphics.Color(0xFFEF5350) else androidx.compose.ui.graphics.Color(0xFF66BB6A), androidx.compose.foundation.shape.CircleShape))
                                        } else if (showEvents && hasEvent && isToday) {
                                            androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.size(4.dp).background(androidx.compose.ui.graphics.Color.White, androidx.compose.foundation.shape.CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if (showEvents && currentMonthEvents.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.HorizontalDivider(color = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f))
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                androidx.compose.material3.Text(
                    "أحداث هذا الشهر",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = darkGreen,
                    modifier = androidx.compose.ui.Modifier.padding(vertical = 8.dp)
                )
                currentMonthEvents.forEach { event ->
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = androidx.compose.ui.Modifier.size(8.dp).background(
                                color = if (event.isMartyrdom) androidx.compose.ui.graphics.Color(0xFFEF5350) else androidx.compose.ui.graphics.Color(0xFF66BB6A),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(12.dp))
                        androidx.compose.material3.Text(
                            text = "${event.day.toArabicNumerals()} $monthName - ${event.name}",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = if (isDark) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
INNER_EOF
