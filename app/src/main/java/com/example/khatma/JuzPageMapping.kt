package com.example.khatma

/**
 * جدول صفحات بداية كل جزء وفق طبعة مجمع الملك فهد (المصحف المدني - 604 صفحة).
 * إذا كان تطبيقك يستخدم طبعة مختلفة (عدد صفحات مختلف)، عدّل هذه القيم فقط
 * وباقي الكود سيعمل بدون أي تغيير.
 */
object JuzPageMapping {

    // الصفحة التي يبدأ منها كل جزء (index 0 = الجزء 1)
    val juzStartPages = intArrayOf(
        1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
        201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
        402, 422, 442, 462, 482, 502, 522, 542, 562, 582
    )

    const val TOTAL_PAGES = 604
    const val TOTAL_JUZ = 30

    /** يرجع رقم الجزء (1..30) الذي تقع فيه صفحة معينة */
    fun juzOf(page: Int): Int {
        val safePage = page.coerceIn(1, TOTAL_PAGES)
        for (i in juzStartPages.indices.reversed()) {
            if (safePage >= juzStartPages[i]) return i + 1
        }
        return 1
    }

    /** صفحة بداية الجزء */
    fun startPage(juz: Int): Int = juzStartPages[(juz - 1).coerceIn(0, TOTAL_JUZ - 1)]

    /** صفحة نهاية الجزء (آخر صفحة قبل بداية الجزء التالي) */
    fun endPage(juz: Int): Int {
        return if (juz >= TOTAL_JUZ) TOTAL_PAGES
        else juzStartPages[juz] - 1
    }

    /** عدد صفحات الجزء */
    fun pageCount(juz: Int): Int = endPage(juz) - startPage(juz) + 1

    /** النسبة المئوية للتقدم داخل الجزء بناءً على آخر صفحة مقروءة */
    fun progressPercent(juz: Int, lastReadPage: Int): Int {
        val start = startPage(juz)
        val count = pageCount(juz)
        val readInJuz = (lastReadPage - start + 1).coerceIn(0, count)
        return ((readInJuz.toFloat() / count.toFloat()) * 100).toInt()
    }
}
