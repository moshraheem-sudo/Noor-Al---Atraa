package com.example.data.model

enum class AdhanSoundMode(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String
) {
    FULL_ADHAN(
        id = "full_adhan",
        titleAr = "أذان كامل بصوت المؤذن",
        titleEn = "Full Adhan Voice",
        subtitleAr = "تشغيل صوت الأذان كاملاً بصوت المؤذن المختار",
        subtitleEn = "Play full Adhan recitation by chosen muezzin"
    ),
    SHORT_TAKBIR(
        id = "short_takbir",
        titleAr = "تكبيرات فقط (مختصر)",
        titleEn = "Short Takbir Only",
        subtitleAr = "تشغيل التكبيرات الأولى فقط لتنبيه سريع وموجز",
        subtitleEn = "Play opening Takbeerat only for a brief reminder"
    ),
    BEEP_ALERT(
        id = "beep_alert",
        titleAr = "نغمة تنبيه لطيفة",
        titleEn = "Soft Ringtone / Tone",
        subtitleAr = "رنين نغمة هادئة بدون صوت بشري",
        subtitleEn = "Play gentle notification chime without voice"
    ),
    VIBRATE_ONLY(
        id = "vibrate_only",
        titleAr = "اهتزاز فقط (صامت)",
        titleEn = "Vibrate Only",
        subtitleAr = "تنبيه عبر الاهتزاز والشاشة بدون إصدار أي صوت",
        subtitleEn = "Vibrate and screen alert without sound"
    );

    companion object {
        fun fromId(id: String?): AdhanSoundMode {
            return entries.find { it.id == id } ?: FULL_ADHAN
        }
    }
}

enum class AlarmRepeatMode(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val count: Int,
    val intervalMinutes: Int,
    val subtitleAr: String
) {
    ONCE(
        id = "once",
        titleAr = "مرة واحدة (عند الوقت تماماً)",
        titleEn = "Once (At exact time)",
        count = 1,
        intervalMinutes = 0,
        subtitleAr = "تنبيه فوري واحد عند حلول الوقت"
    ),
    REPEAT_TWICE(
        id = "repeat_twice",
        titleAr = "تكرار التنبيه بعد 5 دقائق",
        titleEn = "Repeat once after 5 mins",
        count = 2,
        intervalMinutes = 5,
        subtitleAr = "تنبيه ثانٍ بعد 5 دقائق لضمان الانتباه والاستيقاظ"
    ),
    REPEAT_THREE(
        id = "repeat_three",
        titleAr = "تكرار مرتين (كل 5 دقائق)",
        titleEn = "Repeat twice (every 5 mins)",
        count = 3,
        intervalMinutes = 5,
        subtitleAr = "تكرار التنبيه مرتين كل 5 دقائق لصلوات الفجر والصدمات"
    ),
    REMIND_BEFORE_10(
        id = "remind_before_10",
        titleAr = "تنبيه مسبق (قبل 10 دقائق) + عند الوقت",
        titleEn = "10 mins before + At time",
        count = 2,
        intervalMinutes = -10,
        subtitleAr = "تنبيه تمهيدي قبل الوقت بـ 10 دقائق للاستعداد"
    );

    companion object {
        fun fromId(id: String?): AlarmRepeatMode {
            return entries.find { it.id == id } ?: ONCE
        }
    }
}

data class PrayerCustomAlarmConfig(
    val prayerType: PrayerType,
    val isEnabled: Boolean = true,
    val soundMode: AdhanSoundMode = AdhanSoundMode.FULL_ADHAN,
    val specificMuezzinId: String? = null, // null means use global selected muezzin
    val repeatMode: AlarmRepeatMode = AlarmRepeatMode.ONCE,
    val customVolumePercent: Int = 100 // 0 to 100
)
