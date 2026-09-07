package com.example.data.model

import java.util.Locale

data class CityLocation(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val countryAr: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String, // e.g. "+3", "+3.5", "+4", "-4"
    val isHolyCity: Boolean = false,
    val province: String = ""
) {
    val displayName: String
        get() = if (province.isNotEmpty() && province != nameAr && !nameAr.contains(province)) "$nameAr ($province)" else nameAr

    val formattedCoordinates: String
        get() = "${String.format(Locale.ENGLISH, "%.4f", latitude)}°N, ${String.format(Locale.ENGLISH, "%.4f", longitude)}°E (GMT $timezone)"
}

object PredefinedCities {
    val list = listOf(
        // Holy Shrine Cities in Iraq
        CityLocation("karbala", "كربلاء المقدسة", "Karbala", "العراق", 32.6143, 44.0228, "+3", true, "كربلاء"),
        CityLocation("najaf", "النجف الأشرف", "Najaf", "العراق", 32.0000, 44.3333, "+3", true, "النجف"),
        CityLocation("kufa", "الكوفة المعظمة", "Kufa", "العراق", 32.0300, 44.4000, "+3", true, "النجف"),
        CityLocation("kadhimya", "الكاظمية المقدسة", "Kadhimiya", "العراق", 33.3800, 44.3400, "+3", true, "بغداد"),
        CityLocation("samarra", "سامراء المقدسة", "Samarra", "العراق", 34.1983, 43.8742, "+3", true, "صلاح الدين"),
        CityLocation("balad", "بلد (مرقد السيد محمد ع)", "Balad", "العراق", 34.0142, 44.1436, "+3", true, "صلاح الدين"),

        // Famous Iraqi Districts (أقضية العراق المشهورة)
        // Karbala Districts
        CityLocation("dist_hindiyah", "قضاء طويريج (الهندية)", "Al-Hindiyah", "العراق", 32.5539, 44.2250, "+3", false, "كربلاء"),
        CityLocation("dist_ain_tamr", "قضاء عين التمر (شثاثا)", "Ain Al-Tamr", "العراق", 32.5694, 43.4864, "+3", false, "كربلاء"),
        CityLocation("dist_al_hurr", "قضاء الحر (مرقد الحر ع)", "Al-Hurr", "العراق", 32.6500, 44.0000, "+3", true, "كربلاء"),

        // Najaf Districts
        CityLocation("dist_manathera", "قضاء المناذرة (أبو صخير)", "Al-Manathera", "العراق", 31.8667, 44.4833, "+3", false, "النجف"),
        CityLocation("dist_mishkhab", "قضاء المشخاب (عنبر العراق)", "Al-Mishkhab", "العراق", 31.8000, 44.5000, "+3", false, "النجف"),
        CityLocation("dist_heera", "قضاء الحيرة التاريخية", "Al-Heera", "العراق", 31.8833, 44.4333, "+3", false, "النجف"),

        // Baghdad Districts
        CityLocation("dist_adhamiya", "قضاء الأعظمية", "Al-Adhamiya", "العراق", 33.3667, 44.3667, "+3", false, "بغداد"),
        CityLocation("dist_madain", "قضاء المدائن (سلمان باك)", "Al-Mada'in", "العراق", 33.1000, 44.5833, "+3", true, "بغداد"),
        CityLocation("dist_mahmoudiyah", "قضاء المحمودية", "Al-Mahmoudiyah", "العراق", 33.0600, 44.3500, "+3", false, "بغداد"),
        CityLocation("dist_abu_ghraib", "قضاء أبو غريب", "Abu Ghraib", "العراق", 33.3167, 44.1833, "+3", false, "بغداد"),
        CityLocation("dist_taji", "قضاء التاجي", "Al-Taji", "العراق", 33.5333, 44.2833, "+3", false, "بغداد"),

        // Babil (Babylon) Districts
        CityLocation("dist_musayyib", "قضاء المسيب (أولاد مسلم ع)", "Al-Musayyib", "العراق", 32.7833, 44.3000, "+3", true, "بابل"),
        CityLocation("dist_qasim", "قضاء القاسم (الإمام القاسم ع)", "Al-Qasim", "العراق", 32.3000, 44.6833, "+3", true, "بابل"),
        CityLocation("dist_hashimiyah", "قضاء الهاشمية", "Al-Hashimiyah", "العراق", 32.3667, 44.6333, "+3", false, "بابل"),
        CityLocation("dist_mahaweel", "قضاء المحاويل", "Al-Mahaweel", "العراق", 32.6500, 44.4167, "+3", false, "بابل"),
        CityLocation("dist_kifl", "قضاء الكفل (مرقد ذي الكفل ع)", "Al-Kifl", "العراق", 32.2200, 44.3700, "+3", true, "بابل"),
        CityLocation("dist_iskandariya", "قضاء الإسكندرية", "Al-Iskandariya", "العراق", 32.8833, 44.3333, "+3", false, "بابل"),

        // Dhi Qar Districts
        CityLocation("dist_suq_shuyukh", "قضاء سوق الشيوخ", "Suq Al-Shuyukh", "العراق", 30.8833, 46.5000, "+3", false, "ذي قار"),
        CityLocation("dist_shatrah", "قضاء الشطرة", "Al-Shatrah", "العراق", 31.4167, 46.1667, "+3", false, "ذي قار"),
        CityLocation("dist_rifai", "قضاء الرفاعي", "Al-Rifa'i", "العراق", 31.6500, 46.1000, "+3", false, "ذي قار"),
        CityLocation("dist_chibayish", "قضاء الجبايش (الأهوار)", "Al-Chibayish", "العراق", 30.9500, 46.9833, "+3", false, "ذي قار"),
        CityLocation("dist_qalatsukkar", "قضاء قلعة سكر", "Qalat Sukkar", "العراق", 31.8667, 46.0667, "+3", false, "ذي قار"),

        // Basra Districts
        CityLocation("dist_faw", "قضاء الفاو (شاطئ الخليج)", "Al-Faw", "العراق", 29.9742, 48.4731, "+3", false, "البصرة"),
        CityLocation("dist_zubair", "قضاء الزبير (خطوة الإمام علي ع)", "Al-Zubair", "العراق", 30.3889, 47.7000, "+3", true, "البصرة"),
        CityLocation("dist_qurna", "قضاء القرنة (ملتقى الرافدين)", "Al-Qurna", "العراق", 31.0167, 47.4333, "+3", false, "البصرة"),
        CityLocation("dist_shatt_arab", "قضاء شط العرب", "Shatt Al-Arab", "العراق", 30.5333, 47.8500, "+3", false, "البصرة"),
        CityLocation("dist_abu_khasib", "قضاء أبي الخصيب", "Abu Al-Khasib", "العراق", 30.4500, 47.9833, "+3", false, "البصرة"),
        CityLocation("dist_madina", "قضاء المدينة", "Al-Madina", "العراق", 30.9333, 47.2667, "+3", false, "البصرة"),

        // Maysan Districts
        CityLocation("dist_majar_kabir", "قضاء المجر الكبير", "Al-Majar Al-Kabir", "العراق", 31.5833, 47.1667, "+3", false, "ميسان"),
        CityLocation("dist_ali_gharbi", "قضاء علي الغربي", "Ali Al-Gharbi", "العراق", 32.4667, 46.6833, "+3", false, "ميسان"),
        CityLocation("dist_qalat_saleh", "قضاء قلعة صالح (العزير ع)", "Qalat Saleh", "العراق", 31.5167, 47.2833, "+3", true, "ميسان"),
        CityLocation("dist_maymouna", "قضاء الميمونة", "Al-Maymouna", "العراق", 31.7500, 46.9667, "+3", false, "ميسان"),

        // Wasit Districts
        CityLocation("dist_hayy", "قضاء الحي (سعيد بن جبير)", "Al-Hayy", "العراق", 32.1667, 46.0500, "+3", true, "واسط"),
        CityLocation("dist_numaniyah", "قضاء النعمانية (المتنبي)", "Al-Numaniyah", "العراق", 32.5500, 45.4167, "+3", false, "واسط"),
        CityLocation("dist_suwaira", "قضاء الصويرة", "Al-Suwaira", "العراق", 32.9167, 44.7667, "+3", false, "واسط"),
        CityLocation("dist_aziziya", "قضاء العزيزية", "Al-Aziziya", "العراق", 32.9000, 45.0667, "+3", false, "واسط"),
        CityLocation("dist_badra", "قضاء بدرة وجصان", "Badra", "العراق", 33.1000, 45.9667, "+3", false, "واسط"),

        // Qadisiyah (Diwaniyah) Districts
        CityLocation("dist_shamiyah", "قضاء الشامية", "Al-Shamiyah", "العراق", 31.9667, 44.6000, "+3", false, "القادسية"),
        CityLocation("dist_hamza_sharqi", "قضاء الحمزة الشرقي", "Al-Hamza Al-Sharqi", "العراق", 31.7333, 44.9833, "+3", true, "القادسية"),
        CityLocation("dist_afak", "قضاء عفك (آثار نِفَّر)", "Afak", "العراق", 32.0667, 45.2500, "+3", false, "القادسية"),
        CityLocation("dist_ghammas", "قضاء غماس", "Ghammas", "العراق", 31.7333, 44.6000, "+3", false, "القادسية"),

        // Muthanna Districts
        CityLocation("dist_rumaitha", "قضاء الرميثة (ثورة العشرين)", "Al-Rumaitha", "العراق", 31.5333, 45.2000, "+3", false, "المثنى"),
        CityLocation("dist_khidhir", "قضاء الخضر (السيد الخضر ع)", "Al-Khidhir", "العراق", 31.2833, 45.5500, "+3", true, "المثنى"),
        CityLocation("dist_salman", "قضاء السلمان (البادية)", "Al-Salman", "العراق", 30.5000, 44.7500, "+3", false, "المثنى"),

        // Salah Al-Din Districts
        CityLocation("dist_dujail", "قضاء الدجيل", "Al-Dujail", "العراق", 33.8833, 44.2333, "+3", false, "صلاح الدين"),
        CityLocation("dist_tuz_khurmatu", "قضاء طوزخورماتو", "Tuz Khurmatu", "العراق", 34.8833, 44.6333, "+3", false, "صلاح الدين"),
        CityLocation("dist_baiji", "قضاء بيجي", "Baiji", "العراق", 34.9333, 43.4833, "+3", false, "صلاح الدين"),
        CityLocation("dist_shirqat", "قضاء الشرقاط (آشور)", "Al-Shirqat", "العراق", 35.5000, 43.2333, "+3", false, "صلاح الدين"),

        // Kirkuk Districts
        CityLocation("dist_hawija", "قضاء الحويجة", "Al-Hawija", "العراق", 35.3167, 43.7667, "+3", false, "كركوك"),
        CityLocation("dist_daquq", "قضاء داقوق", "Daquq", "العراق", 35.1333, 44.4500, "+3", false, "كركوك"),

        // Diyala Districts
        CityLocation("dist_khanaqin", "قضاء خانقين", "Khanaqin", "العراق", 34.3500, 45.3833, "+3", false, "ديالى"),
        CityLocation("dist_miqdadiyah", "قضاء المقدادية (شهربان)", "Al-Miqdadiyah", "العراق", 33.9833, 44.9333, "+3", false, "ديالى"),
        CityLocation("dist_khalis", "قضاء الخالص", "Al-Khalis", "العراق", 33.8500, 44.5333, "+3", false, "ديالى"),
        CityLocation("dist_baladruz", "قضاء بلدروز", "Baladruz", "العراق", 33.6833, 45.0500, "+3", false, "ديالى"),

        // Nineveh Districts
        CityLocation("dist_sinjar", "قضاء سنجار", "Sinjar", "العراق", 36.3167, 41.8667, "+3", false, "نينوى"),
        CityLocation("dist_hamdaniyah", "قضاء الحمدانية (بخديدا)", "Al-Hamdaniyah", "العراق", 36.2667, 43.3667, "+3", false, "نينوى"),
        CityLocation("dist_sheikhan", "قضاء الشيخان", "Sheikhan", "العراق", 36.6833, 43.3500, "+3", false, "نينوى"),

        // Anbar Districts
        CityLocation("dist_hit", "قضاء هيت", "Hit", "العراق", 33.6400, 42.8200, "+3", false, "الأنبار"),
        CityLocation("dist_haditha", "قضاء حديثة", "Haditha", "العراق", 34.1333, 42.3667, "+3", false, "الأنبار"),
        CityLocation("dist_qaim", "قضاء القائم", "Al-Qaim", "العراق", 34.3667, 41.0833, "+3", false, "الأنبار"),

        // Kurdistan Region Districts
        CityLocation("dist_amadiya", "قضاء العمادية", "Amadiya", "العراق", 37.0833, 43.4833, "+3", false, "دهوك"),
        CityLocation("dist_aqrah", "قضاء عقرة", "Aqrah", "العراق", 36.7500, 43.8833, "+3", false, "دهوك"),
        CityLocation("dist_soran", "قضاء سوران", "Soran", "العراق", 36.6500, 44.5333, "+3", false, "أربيل"),
        CityLocation("dist_koya", "قضاء كويسنجق", "Koya", "العراق", 36.0833, 44.6333, "+3", false, "أربيل"),
        CityLocation("dist_halabja", "قضاء حلبجة الشهيدة", "Halabja", "العراق", 35.1833, 45.9833, "+3", false, "حلبجة"),
        CityLocation("dist_ranya", "قضاء رانية", "Ranya", "العراق", 36.2500, 44.8833, "+3", false, "السليمانية"),
        CityLocation("dist_kalar", "قضاء كلار", "Kalar", "العراق", 34.7333, 45.3167, "+3", false, "السليمانية"),

        // Iraqi Governorates & Major Capitals
        CityLocation("baghdad", "بغداد (العاصمة)", "Baghdad", "العراق", 33.3152, 44.3661, "+3", false, "بغداد"),
        CityLocation("basra", "البصرة (الفيحاء)", "Basra", "العراق", 30.5081, 47.7835, "+3", false, "البصرة"),
        CityLocation("hillah", "الحلة (بابل)", "Hillah", "العراق", 32.4833, 44.4333, "+3", false, "بابل"),
        CityLocation("nasiriyah", "الناصرية (ذي قار)", "Nasiriyah", "العراق", 31.0500, 46.2573, "+3", false, "ذي قار"),
        CityLocation("amarah", "العمارة (ميسان)", "Amarah", "العراق", 31.8439, 47.1450, "+3", false, "ميسان"),
        CityLocation("kut", "الكوت (واسط)", "Kut", "العراق", 32.5128, 45.8194, "+3", false, "واسط"),
        CityLocation("diwaniyah", "الديوانية (القادسية)", "Diwaniyah", "العراق", 31.9933, 44.9250, "+3", false, "القادسية"),
        CityLocation("samawah", "السماوة (المثنى)", "Samawah", "العراق", 31.3167, 45.2833, "+3", false, "المثنى"),
        CityLocation("kirkuk", "كركوك", "Kirkuk", "العراق", 35.4681, 44.3922, "+3", false, "كركوك"),
        CityLocation("mosul", "الموصل (نينوى)", "Mosul", "العراق", 36.3400, 43.1300, "+3", false, "نينوى"),
        CityLocation("erbil", "أربيل", "Erbil", "العراق", 36.1911, 44.0092, "+3", false, "أربيل"),
        CityLocation("sulaymaniyah", "السليمانية", "Sulaymaniyah", "العراق", 35.5568, 45.4371, "+3", false, "السليمانية"),
        CityLocation("duhok", "دهوك", "Duhok", "العراق", 36.8679, 42.9886, "+3", false, "دهوك"),
        CityLocation("ramadi", "الرمادي (الأنبار)", "Ramadi", "العراق", 33.4244, 43.2989, "+3", false, "الأنبار"),
        CityLocation("fallujah", "الفلوجة", "Fallujah", "العراق", 33.3533, 43.7844, "+3", false, "الأنبار"),
        CityLocation("baqubah", "بعقوبة (ديالى)", "Baqubah", "العراق", 33.7439, 44.6442, "+3", false, "ديالى"),
        CityLocation("tikrit", "تكريت", "Tikrit", "العراق", 34.6074, 43.6784, "+3", false, "صلاح الدين"),
        CityLocation("zakho", "زاخو", "Zakho", "العراق", 37.1436, 42.6869, "+3", false, "دهوك"),
        CityLocation("talafar", "تلعفر", "Tal Afar", "العراق", 36.3756, 42.4539, "+3", false, "نينوى"),

        // Islamic Holy & Capital Cities in Arab & Islamic World
        CityLocation("mecca", "مكة المكرمة", "Mecca", "السعودية", 21.4225, 39.8262, "+3", true, "مكة"),
        CityLocation("medina", "المدينة المنورة", "Medina", "السعودية", 24.5247, 39.5692, "+3", true, "المدينة"),
        CityLocation("mashhad", "مشهد المقدسة (الإمام الرضا ع)", "Mashhad", "إيران", 36.2972, 59.6067, "+3.5", true, "خراسان"),
        CityLocation("qom", "قم المقدسة (السيدة معصومة ع)", "Qom", "إيران", 34.6401, 50.8764, "+3.5", true, "قم"),
        CityLocation("jerusalem", "القدس الشريف", "Jerusalem", "فلسطين", 31.7683, 35.2137, "+3", true, "القدس"),
        CityLocation("damascus", "دمشق (السيدة زينب ع)", "Damascus", "سوريا", 33.5138, 36.2765, "+3", true, "دمشق"),
        CityLocation("kuwait", "مدينة الكويت", "Kuwait City", "الكويت", 29.3759, 47.9774, "+3", false, "الكويت"),
        CityLocation("manama", "المنامة", "Manama", "البحرين", 26.2285, 50.5860, "+3", false, "المنامة"),
        CityLocation("doha", "الدوحة", "Doha", "قطر", 25.2854, 51.5310, "+3", false, "الدوحة"),
        CityLocation("riyadh", "الرياض", "Riyadh", "السعودية", 24.7136, 46.6753, "+3", false, "الرياض"),
        CityLocation("dubai", "دبي", "Dubai", "الإمارات", 25.2048, 55.2708, "+4", false, "دبي"),
        CityLocation("abudhabi", "أبوظبي", "Abu Dhabi", "الإمارات", 24.4539, 54.3773, "+4", false, "أبوظبي"),
        CityLocation("muscat", "مسقط", "Muscat", "عمان", 23.5880, 58.3829, "+4", false, "مسقط"),
        CityLocation("beirut", "بيروت", "Beirut", "لبنان", 33.8938, 35.5018, "+3", false, "بيروت"),
        CityLocation("cairo", "القاهرة", "Cairo", "مصر", 30.0444, 31.2357, "+3", false, "القاهرة"),
        CityLocation("amman", "عَمّان", "Amman", "الأردن", 31.9539, 35.9106, "+3", false, "عمان"),
        CityLocation("tehran", "طهران", "Tehran", "إيران", 35.6892, 51.3890, "+3.5", false, "طهران"),
        CityLocation("istanbul", "إسطنبول", "Istanbul", "تركيا", 41.0082, 28.9784, "+3", false, "إسطنبول"),

        // Global Diaspora Cities
        CityLocation("dearborn", "ديربورن / ديترويت", "Dearborn", "أمريكا", 42.3223, -83.1763, "-4", false, "ميشيغان"),
        CityLocation("london", "لندن", "London", "بريطانيا", 51.5074, -0.1278, "+1", false, "إنجلترا"),
        CityLocation("paris", "باريس", "Paris", "فرنسا", 48.8566, 2.3522, "+2", false, "إيل دو فرانس"),
        CityLocation("berlin", "برلين", "Berlin", "ألمانيا", 52.5200, 13.4050, "+2", false, "برلين"),
        CityLocation("stockholm", "ستوكهولم", "Stockholm", "السويد", 59.3293, 18.0686, "+2", false, "ستوكهولم"),
        CityLocation("toronto", "تورونتو", "Toronto", "كندا", 43.6532, -79.3832, "-4", false, "أونتاريو"),
        CityLocation("sydney", "سيدني", "Sydney", "أستراليا", -33.8688, 151.2093, "+10", false, "نيوساوث ويلز"),
        CityLocation("kualalumpur", "كوالالمبور", "Kuala Lumpur", "ماليزيا", 3.1390, 101.6869, "+8", false, "كوالالمبور")
    )

    val defaultCity = list.first() // Karbala Holy City
}
