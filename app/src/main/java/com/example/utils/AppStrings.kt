package com.example.utils

import com.example.data.model.AppLanguage
import com.example.data.model.PredefinedCities
import com.example.data.model.PrayerType

object AppStrings {
    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مواقيت الصلاة"
        AppLanguage.ENGLISH -> "Prayer Times"
    }

    fun tabPrayerTimes(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المواقيت"
        AppLanguage.ENGLISH -> "Times"
    }

    fun tabSettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإعدادات"
        AppLanguage.ENGLISH -> "Settings"
    }

    fun currentLocationBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "موقعك الحالي"
        AppLanguage.ENGLISH -> "Current Location"
    }

    fun gpsLocateButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحديد GPS"
        AppLanguage.ENGLISH -> "GPS Locate"
    }

    fun citiesButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المدن"
        AppLanguage.ENGLISH -> "Cities"
    }

    fun offlineBanner(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أنت في وضع عدم الاتصال. يتم استخدام الحسابات الفلكية الشرعية المعتمدة بدقة."
        AppLanguage.ENGLISH -> "You are offline. Precise astronomical calculations according to Shia jurisprudential rules are used."
    }

    fun nextPrayerLabel(lang: AppLanguage, prayerName: String): String = when (lang) {
        AppLanguage.ARABIC -> "الصلاة القادمة: $prayerName"
        AppLanguage.ENGLISH -> "Next Prayer: $prayerName"
    }

    fun countdownRemaining(lang: AppLanguage, timeFormatted: String): String = when (lang) {
        AppLanguage.ARABIC -> "يتبقى $timeFormatted لرفع الأذان"
        AppLanguage.ENGLISH -> "$timeFormatted remaining until Adhan"
    }

    fun prayerSectionTitle(lang: AppLanguage, cityName: String): String = when (lang) {
        AppLanguage.ARABIC -> {
            val prefix = if (cityName.startsWith("مدينة")) "" else "مدينة "
            "مواقيت الصلوات الخمسة حسب $prefix$cityName"
        }
        AppLanguage.ENGLISH -> "Five Prayer Times for $cityName"
    }

    fun updatingStatus(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحديث..."
        AppLanguage.ENGLISH -> "Updating..."
    }

    fun prayerName(type: PrayerType, lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> type.arName
        AppLanguage.ENGLISH -> type.enName
    }

    fun nextPrayerBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الصلاة القادمة"
        AppLanguage.ENGLISH -> "Next Prayer"
    }

    fun prayerDescription(type: PrayerType, lang: AppLanguage): String = when (type) {
        PrayerType.FAJR -> when (lang) {
            AppLanguage.ARABIC -> "أذان الفجر الشرعي"
            AppLanguage.ENGLISH -> "Fajr Adhan & Start of Dawn"
        }
        PrayerType.SUNRISE -> when (lang) {
            AppLanguage.ARABIC -> "شروق الشمس ونهاية وقت الفجر"
            AppLanguage.ENGLISH -> "Sunrise & End of Fajr Time"
        }
        PrayerType.DHUHR -> when (lang) {
            AppLanguage.ARABIC -> "أذان الظهرين (الظهر والعصر)"
            AppLanguage.ENGLISH -> "Dhuhr & Asr Adhan (Solar Noon)"
        }
        PrayerType.ASR -> when (lang) {
            AppLanguage.ARABIC -> "وقت فضيلة صلاة العصر"
            AppLanguage.ENGLISH -> "Asr Prayer (Fadheelah Time)"
        }
        PrayerType.SUNSET -> when (lang) {
            AppLanguage.ARABIC -> "غروب قرص الشمس"
            AppLanguage.ENGLISH -> "Astronomical Sunset"
        }
        PrayerType.MAGHRIB -> when (lang) {
            AppLanguage.ARABIC -> "أذان المغربين (المغرب والعشاء)"
            AppLanguage.ENGLISH -> "Maghrib & Isha Adhan"
        }
        PrayerType.ISHA -> when (lang) {
            AppLanguage.ARABIC -> "وقت فضيلة صلاة العشاء"
            AppLanguage.ENGLISH -> "Isha Prayer (Fadheelah Time)"
        }
        PrayerType.MIDNIGHT -> when (lang) {
            AppLanguage.ARABIC -> "منتصف الليل الشرعي (نهاية وقت العشائين)"
            AppLanguage.ENGLISH -> "Midnight (End of Maghrib & Isha)"
        }
    }

    // Jafari Calculation Settings translations
    fun jafariCalcSettingsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إعدادات الحساب الجعفري والمواقيت الشرعية"
        AppLanguage.ENGLISH -> "Jafari Calculation & Fiqh Settings"
    }

    fun jafariCalcSettingsSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "طرق حساب زوايا الفجر والمغرب ومنتصف الليل وفق معايير الفقه الجعفري"
        AppLanguage.ENGLISH -> "Calculation methods for Fajr, Maghrib, and Midnight angles"
    }

    fun calcMethodLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "طريقة حساب وقت الفجر والمغرب:"
        AppLanguage.ENGLISH -> "Fajr & Maghrib Calculation Method:"
    }

    fun midnightMethodLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "طريقة حساب منتصف الليل الشرعي:"
        AppLanguage.ENGLISH -> "Midnight Calculation Rule:"
    }

    fun showAsrSeparateLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عرض وقت صلاة العصر بشكل منفصل"
        AppLanguage.ENGLISH -> "Display Asr prayer separately"
    }

    fun showIshaSeparateLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عرض وقت صلاة العشاء بشكل منفصل"
        AppLanguage.ENGLISH -> "Display Isha prayer separately"
    }

    // Settings translations
    fun settingsLanguageTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "لغة التطبيق (Language)"
        AppLanguage.ENGLISH -> "App Language (اللغة)"
    }

    fun settingsLanguageSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اختر لغة الواجهة المفضلة (العربية أو الإنجليزية)"
        AppLanguage.ENGLISH -> "Select interface language (Arabic or English)"
    }

    fun settingsThemeTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مظهر التطبيق (الثيم)"
        AppLanguage.ENGLISH -> "App Theme"
    }

    fun settingsThemeSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اختر بين الوضع الليلي الداكن أو النهاري الفاتح"
        AppLanguage.ENGLISH -> "Choose between Dark mode or Light mode"
    }

    fun themeDark(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الوضع الليلي (الداكن)"
        AppLanguage.ENGLISH -> "Dark Mode"
    }

    fun themeDarkSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ألوان داكنة فاخرة مريحة للعين ليلاً"
        AppLanguage.ENGLISH -> "Comfortable dark colors for low-light"
    }

    fun themeLight(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الوضع النهاري (الفاتح)"
        AppLanguage.ENGLISH -> "Light Mode"
    }

    fun themeLightSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ألوان نهارية نقية وعالية الوضوح في ضوء النهار"
        AppLanguage.ENGLISH -> "Clean bright design for daytime"
    }

    fun themeSystem(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تلقائي (حسب النظام)"
        AppLanguage.ENGLISH -> "System Default"
    }

    fun themeSystemSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "يتغير تلقائياً بحسب إعدادات الهاتف العامة"
        AppLanguage.ENGLISH -> "Follows device system theme"
    }

    fun hijriSyncTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مزامنة وتحديث التقويم الهجري"
        AppLanguage.ENGLISH -> "Hijri Calendar Sync & Update"
    }

    fun hijriSyncSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مزامنة التاريخ الهجري وتحديثه تلقائياً لضمان الدقة الشرعية"
        AppLanguage.ENGLISH -> "Auto-sync Hijri date for guaranteed astronomical accuracy"
    }

    fun syncHijriButton(lang: AppLanguage, isSyncing: Boolean): String = when {
        isSyncing -> when (lang) {
            AppLanguage.ARABIC -> "جاري جلب وتحديث التاريخ الهجري..."
            AppLanguage.ENGLISH -> "Fetching and updating Hijri calendar..."
        }
        else -> when (lang) {
            AppLanguage.ARABIC -> "مزامنة وتحديث التاريخ الهجري الآن"
            AppLanguage.ENGLISH -> "Sync Hijri Calendar Now"
        }
    }

    fun githubAutoSyncLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المزامنة والتحديث التلقائي في الخلفية"
        AppLanguage.ENGLISH -> "Automatic Background Sync"
    }

    fun githubAutoSyncSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التحقق التلقائي عند تشغيل التطبيق وتحديث التاريخ في الخلفية بدون رنين"
        AppLanguage.ENGLISH -> "Check for updates on app launch and update silently in background"
    }

    fun githubTriggerDispatchButton(lang: AppLanguage, isSending: Boolean): String = when {
        isSending -> when (lang) {
            AppLanguage.ARABIC -> "جارٍ إرسال إشعار التحديث..."
            AppLanguage.ENGLISH -> "Sending Update Trigger..."
        }
        else -> when (lang) {
            AppLanguage.ARABIC -> "إرسال إشعار التحديث التلقائي"
            AppLanguage.ENGLISH -> "Send Automatic Update Trigger"
        }
    }

    fun githubRepoSettingsLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "معلومات المزامنة التلقائية"
        AppLanguage.ENGLISH -> "Auto-Sync Info"
    }

    fun notificationsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تنبيهات أوقات الصلاة والأذان"
        AppLanguage.ENGLISH -> "Prayer Alerts & Adhan"
    }

    fun notificationsSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إشعار في شريط التنبيهات مع الرنين عند حلول وقت كل صلاة"
        AppLanguage.ENGLISH -> "Status bar alerts and audio ring on prayer time"
    }

    fun testNotificationButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تجربة رنين وإشعار الأذان الآن"
        AppLanguage.ENGLISH -> "Test Adhan Notification & Sound Now"
    }

    fun appVersion(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الإصدار ${com.example.BuildConfig.VERSION_NAME}"
        AppLanguage.ENGLISH -> "Version ${com.example.BuildConfig.VERSION_NAME}"
    }

    fun appDescription(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تطبيق إسلامي شامل وموثوق لعرض مواقيت الصلاة والظواهر الفلكية والاتجاهات الشرعية بدقة عالية وفق التقويم الشرعي المعتمد."
        AppLanguage.ENGLISH -> "Comprehensive Islamic app providing precise Shia prayer times, astronomical phenomena, and Qibla directions."
    }

    fun copyrightText(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جميع الحقوق محفوظة © 2026 - تطبيق مواقيت الصلاة"
        AppLanguage.ENGLISH -> "All Rights Reserved © 2026 - Prayer Times App"
    }

    // Prayer Visibility Settings
    fun prayerVisibilityTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إظهار وإخفاء المواقيت"
        AppLanguage.ENGLISH -> "Show / Hide Prayer Times"
    }

    fun prayerVisibilitySubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحديد المواقيت والظواهر الفلكية المعروضة في الشاشة الرئيسية"
        AppLanguage.ENGLISH -> "Choose which prayer times are displayed on the home screen"
    }

    fun hijriDateLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التقويم الهجري"
        AppLanguage.ENGLISH -> "Hijri Date"
    }

    fun gregorianDateLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التقويم الميلادي"
        AppLanguage.ENGLISH -> "Gregorian Date"
    }

    // City Selection Dialog
    fun selectCityTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اختيار المدينة والموقع"
        AppLanguage.ENGLISH -> "Select City & Location"
    }

    fun searchPlaceholder(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "ابحث عن قضاء، محافظة، أو مدينة..."
        AppLanguage.ENGLISH -> "Search district, province, or city..."
    }

    fun filterAll(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "الكل"
        AppLanguage.ENGLISH -> "All"
    }

    fun filterIraq(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "أقضية العراق"
        AppLanguage.ENGLISH -> "Iraq Districts"
    }

    fun filterHoly(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "المقدسات"
        AppLanguage.ENGLISH -> "Holy Shrines"
    }

    fun filterGlobal(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "عالمية"
        AppLanguage.ENGLISH -> "Global"
    }

    fun filterCustom(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "مخصص"
        AppLanguage.ENGLISH -> "Custom"
    }

    fun customLocationTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إدخال إحداثيات مخصصة"
        AppLanguage.ENGLISH -> "Enter Custom Coordinates"
    }

    fun customNameLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اسم الموقع / المدينة"
        AppLanguage.ENGLISH -> "Location / City Name"
    }

    fun customLatLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "خط العرض (Latitude)"
        AppLanguage.ENGLISH -> "Latitude (e.g. 32.6143)"
    }

    fun customLonLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "خط الطول (Longitude)"
        AppLanguage.ENGLISH -> "Longitude (e.g. 44.0228)"
    }

    fun saveCustomButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "حفظ واعتماد الإحداثيات"
        AppLanguage.ENGLISH -> "Save & Apply Coordinates"
    }

    fun manualPrayerOffsetsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التعديل اليدوي لمواقيت الصلاة"
        AppLanguage.ENGLISH -> "Manual Prayer Time Adjustments"
    }

    fun manualPrayerOffsetsSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تقديم أو تأخير دقائق كل صلاة بشكل فردي ومستقل"
        AppLanguage.ENGLISH -> "Independently adjust minutes forward or backward for each prayer"
    }

    fun manualHijriAdjustmentTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "التعديل اليدوي للتقويم الهجري"
        AppLanguage.ENGLISH -> "Manual Hijri Date Adjustment"
    }

    fun manualHijriAdjustmentSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تعديل يوم التقويم بالتقديم أو التأخير يدوياً أو كتابة تاريخ مخصص"
        AppLanguage.ENGLISH -> "Adjust Hijri day forward/backward or set a custom Hijri date"
    }

    fun resetToAutoButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إعادة الضبط للتلقائي"
        AppLanguage.ENGLISH -> "Reset to Default"
    }

    fun resetAllOffsetsButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إعادة ضبط جميع أوقات الصلوات (0 دقيقة)"
        AppLanguage.ENGLISH -> "Reset All Prayer Offsets (0 min)"
    }

    fun closeButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إغلاق"
        AppLanguage.ENGLISH -> "Close"
    }

    fun muezzinSectionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "صوت الأذان"
        AppLanguage.ENGLISH -> "Adhan Sound"
    }

    fun muezzinSectionSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "اختيار المؤذن وتحميل الصوتيات للعمل بدون إنترنت"
        AppLanguage.ENGLISH -> "Select Muezzin & download for offline playback"
    }

    fun muezzinAutoPlayLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تشغيل صوت الأذان تلقائياً"
        AppLanguage.ENGLISH -> "Auto-Play Adhan Audio"
    }

    fun muezzinAutoPlayDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "يقوم التطبيق بتشغيل صوت الأذان للمؤذن المختار تلقائياً عند حلول وقت كل صلاة. يتم تحميل الصوتيات بالخلفية لتعمل حتى عند انقطاع الإنترنت."
        AppLanguage.ENGLISH -> "Automatically plays the selected Muezzin's Adhan at prayer times. Audios are downloaded in background to work offline."
    }

    fun previewAdhanButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "استماع / معاينة"
        AppLanguage.ENGLISH -> "Listen / Preview"
    }

    fun stopAdhanButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "إيقاف التشغيل"
        AppLanguage.ENGLISH -> "Stop Playback"
    }

    fun bufferingAdhanText(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جارٍ تجهيز الصوت..."
        AppLanguage.ENGLISH -> "Buffering audio..."
    }

    fun playingAdhanText(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جارٍ تشغيل الأذان الآن 🔊"
        AppLanguage.ENGLISH -> "Playing Adhan now 🔊"
    }

    fun downloadAllButton(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تحميل كافة الأصوات للعمل أوفلاين"
        AppLanguage.ENGLISH -> "Download All for Offline Use"
    }

    fun downloadedOfflineTag(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "✓ جاهز للعمل بدون إنترنت"
        AppLanguage.ENGLISH -> "✓ Ready for offline use"
    }

    fun downloadingTag(progress: Int, lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> if (progress > 0) "جارٍ التحميل $progress%..." else "جارٍ التحميل بالخلفية..."
        AppLanguage.ENGLISH -> if (progress > 0) "Downloading $progress%..." else "Downloading in background..."
    }

    fun notDownloadedTag(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "سيتم التحميل تلقائياً عند توفر الإنترنت"
        AppLanguage.ENGLISH -> "Will auto-download when online"
    }

    fun offlineStreamingNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "⚡ يتم تحميل أصوات المؤذنين تلقائياً في الخلفية فور توفر الإنترنت لتعمل المواقيت وصوت الأذان بشكل كامل حتى عند إطفاء الإنترنت لاحقاً."
        AppLanguage.ENGLISH -> "⚡ Muezzin audios are automatically downloaded in the background so Adhan sounds work seamlessly even when offline later."
    }

    // GPS Status Messages & Localization
    fun gpsStatusDisabled(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "خدمة GPS مغلقة، تم فتح إعدادات الهاتف لتفعيلها مباشرة..."
        AppLanguage.ENGLISH -> "GPS service is disabled. Opened device settings to enable it..."
    }

    fun gpsStatusOpeningSettings(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم فتح إعدادات الموقع، يرجى تفعيل GPS لتحديد مكانك بدقة."
        AppLanguage.ENGLISH -> "Location settings opened, please enable GPS to locate accurately."
    }

    fun gpsStatusLocating(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "جاري تحديد الموقع الدقيق عبر GPS والإنترنت..."
        AppLanguage.ENGLISH -> "Locating precise position via GPS and network..."
    }

    fun gpsStatusDetermined(cityName: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم تحديد الموقع بنجاح: $cityName"
        AppLanguage.ENGLISH -> "Location determined successfully: $cityName"
    }

    fun gpsStatusSetTo(cityName: String, lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "تم تحديد الموقع على: $cityName"
        AppLanguage.ENGLISH -> "Location set to: $cityName"
    }

    fun gpsToastEnable(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "يرجى تفعيل خدمة الموقع (GPS) في الهاتف"
        AppLanguage.ENGLISH -> "Please enable Location service (GPS) on your phone"
    }

    fun translateGpsMessage(rawMessage: String?, lang: AppLanguage): String {
        if (rawMessage.isNullOrBlank()) return ""
        val isAr = lang == AppLanguage.ARABIC
        if (isAr) {
            return when {
                rawMessage.contains("GPS service is disabled", ignoreCase = true) ||
                rawMessage.contains("GPS service is turned off", ignoreCase = true) ->
                    "خدمة GPS مغلقة، تم فتح إعدادات الهاتف لتفعيلها مباشرة..."
                rawMessage.contains("Location settings opened", ignoreCase = true) ->
                    "تم فتح إعدادات الموقع، يرجى تفعيل GPS لتحديد مكانك بدقة."
                rawMessage.contains("Locating precise position", ignoreCase = true) ->
                    "جاري تحديد الموقع الدقيق عبر GPS والإنترنت..."
                rawMessage.startsWith("Location determined successfully:", ignoreCase = true) -> {
                    val cityPart = rawMessage.substringAfter(":").trim()
                    val cityObj = PredefinedCities.list.find { it.nameEn.equals(cityPart, ignoreCase = true) || it.nameAr == cityPart }
                    "تم تحديد الموقع بنجاح: ${cityObj?.nameAr ?: cityPart}"
                }
                rawMessage.startsWith("Location set to:", ignoreCase = true) -> {
                    val cityPart = rawMessage.substringAfter(":").trim()
                    val cityObj = PredefinedCities.list.find { it.nameEn.equals(cityPart, ignoreCase = true) || it.nameAr == cityPart }
                    "تم تحديد الموقع على: ${cityObj?.nameAr ?: cityPart}"
                }
                else -> rawMessage
            }
        } else {
            return when {
                rawMessage.contains("خدمة GPS مغلقة") ->
                    "GPS service is disabled. Opened device settings to enable it..."
                rawMessage.contains("تم فتح إعدادات الموقع") ->
                    "Location settings opened, please enable GPS to locate accurately."
                rawMessage.contains("جاري تحديد الموقع الدقيق") ->
                    "Locating precise position via GPS and network..."
                rawMessage.startsWith("تم تحديد الموقع بنجاح:") -> {
                    val cityPart = rawMessage.substringAfter("تم تحديد الموقع بنجاح:").trim()
                    val cityObj = PredefinedCities.list.find { it.nameAr == cityPart || it.nameEn.equals(cityPart, ignoreCase = true) }
                    "Location determined successfully: ${cityObj?.nameEn ?: cityPart}"
                }
                rawMessage.startsWith("تم تحديد الموقع على:") -> {
                    val cityPart = rawMessage.substringAfter("تم تحديد الموقع على:").trim()
                    val cityObj = PredefinedCities.list.find { it.nameAr == cityPart || it.nameEn.equals(cityPart, ignoreCase = true) }
                    "Location set to: ${cityObj?.nameEn ?: cityPart}"
                }
                else -> rawMessage
            }
        }
    }
}
