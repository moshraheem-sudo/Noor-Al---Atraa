package com.example

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.test.core.app.ActivityScenario
import com.example.data.WorshipData

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testWorshipDataInitialization() {
    println("Initializing WorshipData...")
    println("dailyPrayers size: ${WorshipData.dailyPrayers.size}")
    println("taqeebat size: ${WorshipData.taqeebat.size}")
    println("munajat size: ${WorshipData.munajat.size}")
    println("tasbeehat size: ${WorshipData.tasbeehat.size}")
    println("azkarList size: ${WorshipData.azkarList.size}")
    println("ziyarats size: ${WorshipData.ziyarats.size}")
    println("ziyaratsOfDays size: ${WorshipData.ziyaratsOfDays.size}")
    println("monthlyDeeds size: ${WorshipData.monthlyDeeds.size}")
    println("sahifaSajjadiya size: ${WorshipData.sahifaSajjadiya.size}")
    println("generalPrayers size: ${WorshipData.generalPrayers.size}")
    println("salawatsOnHujaj size: ${WorshipData.salawatsOnHujaj.size}")
    println("ahlAlBaytPrayers size: ${WorshipData.ahlAlBaytPrayers.size}")
    println("WorshipData initialized successfully!")
  }

  @Test
  fun testAllHardcodedWorshipTitles() {
    val allLists = listOf(
        WorshipData.dailyPrayers.values.toList(),
        WorshipData.taqeebat,
        WorshipData.munajat,
        WorshipData.tasbeehat,
        WorshipData.azkarList,
        WorshipData.ziyarats,
        WorshipData.ziyaratsOfDays,
        WorshipData.sahifaSajjadiya,
        WorshipData.generalPrayers,
        WorshipData.salawatsOnHujaj,
        WorshipData.ahlAlBaytPrayers,
        WorshipData.concisePrayers,
        WorshipData.obligatoryAndRecommendedPrayers,
        WorshipData.fastingRules,
        WorshipData.hajjRules
    ).flatten()

    val allDeeds = WorshipData.monthlyDeeds.flatMap { (month, deeds) -> 
        deeds.map { Pair("${it.first} - $month", it.second) } 
    }

    val titlesToVerify = listOf(
        // General Suggestions & Banners
        "دعاء كميل بن زياد رضوان الله عليه",
        "دعاء التوسل بالأئمة (ع)",
        "دعاء الندبة",
        "زيارة عاشوراء",
        "تسبيح الزهراء (عليها السلام)",
        "دعاء العهد",
        "مناجاة التائبين",
        "أذكار الصباح",
        "تعقيب صلاة الصبح",
        "مناجاة الشاكرين",
        "الصلاة على محمد وآل محمد",
        "تعقيب صلاة الظهر",
        "المناجاة المنظومة",
        "دعاء يوم السبت",
        "دعاء يوم الأحد",
        "تسبيح يوم السبت",
        "دعاء الصباح",
        "الاستغفار وطلب التوبة",
        "تعقيب صلاة المغرب",
        "أذكار المساء",
        "مناجاة الخائفين",
        "تعقيب صلاة العشاء",
        "مناجاة المحبين",
        "تسبيح يوم الجمعة",
        "مناجاة الراجين",
        "دعاء يوم الاثنين",
        "دعاء يوم الثلاثاء",

        // Day of Week dynamic items
        "تسبيح يوم السبت", "دعاء يوم السبت", "زيارة يوم السبت",
        "تسبيح يوم الأحد", "دعاء يوم الأحد", "زيارة يوم الأحد",
        "تسبيح يوم الاثنين", "دعاء يوم الاثنين", "زيارة يوم الاثنين",
        "تسبيح يوم الثلاثاء", "دعاء يوم الثلاثاء", "زيارة يوم الثلاثاء",
        "تسبيح يوم الأربعاء", "دعاء يوم الأربعاء", "زيارة يوم الأربعاء",
        "تسبيح يوم الخميس", "دعاء يوم الخميس", "زيارة يوم الخميس",
        "تسبيح يوم الجمعة", "دعاء يوم الجمعة", "زيارة يوم الجمعة",

        // Ahl Al-Bayt Prayers / Friday Deeds (rightCol)
        "صلاة النبي (ص)",
        "صلاة السيدة فاطمة (س)",
        "صلاة الإمام الحسين (ع)",
        "صلاة الإمام الباقر (ع)",
        "صلاة الإمام الكاظم (ع)",
        "صلاة الإمام الجواد (ع)",
        "صلاة الإمام العسكري (ع)",
        "صلاة جعفر الطيّار (ع)",

        // Ahl Al-Bayt Prayers / Friday Deeds (leftCol)
        "أعمال نهار الجُمعة",
        "صلاة أمير المؤمنين (ع)",
        "صلاة الإمام الحسن (ع)",
        "صلاة الإمام زين العابدين (ع)",
        "صلاة الإمام الصادق (ع)",
        "صلاة الإمام الرضا (ع)",
        "صلاة الإمام الهادي (ع)",
        "صلاة الإمام الحجّة (عج)",
        "أعمال يوم الجمعة",
        "أعمال ليلة الجُمعة",

        // Other hardcoded screens
        "أحكام الحجّ",
        "صلاة العيدين (عيد الفطر المبارك وعيد الأضحى المبارك)"
    )

    val missingTitles = mutableListOf<String>()
    for (title in titlesToVerify) {
        val found = allLists.any { it.first == title } || allDeeds.any { it.first == title }
        if (!found) {
            missingTitles.add(title)
        }
    }

    assertTrue("The following titles are missing or mismatched in WorshipData: $missingTitles", missingTitles.isEmpty())
  }

  @Test
  fun testMainActivityLaunch() {
    println("Launching MainActivity via Robolectric...")
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
      scenario.onActivity { activity ->
        println("MainActivity launched successfully under Robolectric: $activity")
      }
    }
  }

  @Test
  fun testQuranDatabaseDiagnosis() {
    println("=== DIAGNOSIS START ===")
    val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
    
    // 1. Check if the asset file exists and try to read it
    val assetPath = "quran/quran_hafs_uthmani_v12.db"
    var assetExists = false
    var assetSize = -1L
    try {
      context.assets.open(assetPath).use { stream ->
        assetExists = true
        assetSize = stream.available().toLong()
        println("1. Asset '$assetPath' exists. Size: $assetSize bytes")
      }
    } catch (e: Exception) {
      println("1. Asset '$assetPath' does NOT exist or failed to open: ${e.message}")
    }

    // 2. Try copying and opening the database via Room
    var copySuccess = false
    var copyError: String? = null
    var surahCount = -1
    try {
      val db = com.example.data.QuranDatabase.getDatabase(context)
      println("2. Room Database object obtained successfully.")
      
      // Force database creation / copying by performing a query
      val lock = kotlinx.coroutines.runBlocking { db.quranDao().getIntegrityLock() }
      println("3. integrity_lock obtained: $lock")
      
      // Let's get surahs flow and try to collect it
      val surahsList = db.quranDao().getAllSurahs()
      // Since it's a Flow, we can't block easily unless we use runBlocking
      kotlinx.coroutines.runBlocking {
        surahsList.collect { list ->
          surahCount = list.size
          println("3. surahs table row count collected: $surahCount")
          throw Exception("STOP_FLOW") // exception to stop collecting
        }
      }
    } catch (e: Exception) {
      if (e.message == "STOP_FLOW") {
        copySuccess = true
      } else {
        copySuccess = false
        copyError = e.message ?: e.toString()
        println("2/3. Failed during database copy or query: ${e.message}")
        e.printStackTrace()
      }
    }

    println("=== DIAGNOSIS SUMMARY ===")
    println("Asset Exists: $assetExists")
    println("Asset Size: $assetSize bytes")
    println("Copy Success: $copySuccess")
    println("Copy Error: $copyError")
    println("Surahs Row Count: $surahCount")
    println("=== DIAGNOSIS END ===")
  }
}
