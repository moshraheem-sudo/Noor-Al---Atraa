#!/bin/bash
FILE="/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt"

# Replace Date & Location Header Line with an integrated App Title & Location header
sed -i '/\/\/ Date & Location Header Line/,/}\n        }/c\
        item {\
            Column(\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .padding(top = 16.dp, bottom = 8.dp),\
                horizontalAlignment = Alignment.CenterHorizontally\
            ) {\
                Text(\
                    text = "مواقيت الصلاة",\
                    style = MaterialTheme.typography.headlineMedium,\
                    fontWeight = FontWeight.Black,\
                    color = AppColors.current.textTitle,\
                    fontSize = 28.sp\
                )\
                Spacer(modifier = Modifier.height(4.dp))\
                Row(verticalAlignment = Alignment.CenterVertically) {\
                    Icon(\
                        imageVector = Icons.Default.LocationOn,\
                        contentDescription = "Location",\
                        tint = AppColors.current.tealAccentLight,\
                        modifier = Modifier.size(16.dp)\
                    )\
                    Spacer(modifier = Modifier.width(4.dp))\
                    Text(\
                        text = selectedCity.nameAr,\
                        style = MaterialTheme.typography.titleMedium,\
                        color = AppColors.current.textMain,\
                        fontWeight = FontWeight.Bold,\
                        fontSize = 16.sp\
                    )\
                }\
                Spacer(modifier = Modifier.height(12.dp))\
                Row(\
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),\
                    horizontalArrangement = Arrangement.SpaceBetween,\
                    verticalAlignment = Alignment.CenterVertically\
                ) {\
                    val hijriDisplay = prayerData?.hijriDate?.takeIf { !it.contains("الكفيل") } ?: PrayerCalculator.getFormattedHijriDate()\
                    Text(\
                        text = hijriDisplay,\
                        style = MaterialTheme.typography.bodyMedium,\
                        fontWeight = FontWeight.Medium,\
                        color = AppColors.current.textMuted,\
                        fontSize = 14.sp\
                    )\
                    Text(\
                        text = prayerData?.gregorianDate ?: PrayerCalculator.getFormattedGregorianDate(),\
                        style = MaterialTheme.typography.bodySmall,\
                        color = AppColors.current.textSubtle,\
                        fontSize = 12.sp\
                    )\
                }\
            }\
        }' $FILE
