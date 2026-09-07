package com.example.khatma

import kotlinx.coroutines.flow.Flow

class KhatmaRepository(private val dao: KhatmaDao) {

    fun observeAllJuz(): Flow<List<KhatmaProgress>> = dao.observeAllJuz()

    /**
     * ★ الدالة الأهم: نادِها من نفس المكان الذي يحفظ فيه تطبيقك حالياً
     * "آخر صفحة مقروءة" (عند تقليب الصفحة، أو الخروج من شاشة القراءة، إلخ).
     *
     * currentPage = رقم الصفحة المطلقة في المصحف (1..604)
     */
    suspend fun updateProgress(currentPage: Int) {
        val juz = JuzPageMapping.juzOf(currentPage)
        val percent = JuzPageMapping.progressPercent(juz, currentPage)
        val completed = percent >= 100

        val existing = dao.getJuz(juz)
        // لا نرجّع النسبة للخلف لو المستخدم رجع يراجع صفحة سابقة ضمن نفس الجزء
        val bestPercent = maxOf(percent, existing?.progressPercent ?: 0)
        val bestPage = maxOf(currentPage, existing?.lastReadPage ?: 0)

        dao.upsert(
            KhatmaProgress(
                juzNumber = juz,
                lastReadPage = bestPage,
                progressPercent = bestPercent,
                isCompleted = completed || (existing?.isCompleted == true)
            )
        )
    }

    /** يرجع الجزء الذي يجب أن يفتح فيه القارئ تلقائياً (أول جزء غير مكتمل) */
    suspend fun getNextJuzToOpen(): Int = dao.getFirstIncompleteJuz() ?: 1
    
    suspend fun getJuz(juz: Int): KhatmaProgress? = dao.getJuz(juz)

    suspend fun startNewKhatma() = dao.resetAll()

    suspend fun resetJuz(juz: Int) = dao.resetJuz(juz)
}
