package com.example.data.model

enum class CalculationMethod(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val shortNameAr: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val fajrAngle: Double,
    val maghribAngle: Double
) {
    JAFARI_KAFEEL(
        id = "jafari_kafeel",
        titleAr = "جعفري (مركز الكفيل / السيد السيستاني)",
        titleEn = "Jafari (Al-Kafeel / Ayatollah Sistani)",
        shortNameAr = "جعفري",
        descriptionAr = "زاوية الفجر 18.0° • زاوية المغرب 4.0° (المعتمد في العراق)",
        descriptionEn = "Fajr angle 18.0° • Maghrib angle 4.0° (Standard in Iraq)",
        fajrAngle = 18.0,
        maghribAngle = 4.0
    ),
    JAFARI_TEHRAN(
        id = "jafari_tehran",
        titleAr = "جعفري (جامعة طهران)",
        titleEn = "Jafari (Tehran University)",
        shortNameAr = "جامعة طهران",
        descriptionAr = "زاوية الفجر 17.7° • زاوية المغرب 4.5°",
        descriptionEn = "Fajr angle 17.7° • Maghrib angle 4.5°",
        fajrAngle = 17.7,
        maghribAngle = 4.5
    ),
    JAFARI_LEVA(
        id = "jafari_leva",
        titleAr = "جعفري (معهد ليفا، قم)",
        titleEn = "Jafari (Leva Institute, Qom)",
        shortNameAr = "معهد ليفا (قم)",
        descriptionAr = "زاوية الفجر 16.0° • زاوية المغرب 4.0°",
        descriptionEn = "Fajr angle 16.0° • Maghrib angle 4.0°",
        fajrAngle = 16.0,
        maghribAngle = 4.0
    );

    companion object {
        fun fromId(id: String?): CalculationMethod {
            return entries.find { it.id == id } ?: JAFARI_KAFEEL
        }
    }
}

enum class MidnightMethod(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val shortNameAr: String,
    val descriptionAr: String,
    val descriptionEn: String
) {
    SUNSET_TO_FAJR(
        id = "sunset_to_fajr",
        titleAr = "من الغروب إلى الفجر",
        titleEn = "From Sunset to Fajr",
        shortNameAr = "من الغروب إلى الفجر",
        descriptionAr = "المعتمد فقهياً في المذهب الجعفري (المنتصف بين الغروب وطلوع الفجر)",
        descriptionEn = "Standard in Jafari Fiqh (Midpoint between Sunset and Fajr dawn)"
    ),
    SUNSET_TO_SUNRISE(
        id = "sunset_to_sunrise",
        titleAr = "من الغروب إلى الشروق",
        titleEn = "From Sunset to Sunrise",
        shortNameAr = "من الغروب إلى الشروق",
        descriptionAr = "المنتصف الفلكي العام (المنتصف بين غروب الشمس وشروقها)",
        descriptionEn = "Standard Astronomical Midpoint (Between Sunset and Sunrise)"
    );

    companion object {
        fun fromId(id: String?): MidnightMethod {
            return entries.find { it.id == id } ?: SUNSET_TO_FAJR
        }
    }
}
