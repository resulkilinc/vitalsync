package com.vitalsync.app.domain.model

data class AggregateAnalysisResult(
    val overallStatus: HealthStatus,
    val personalizedMessage: String,
    val complianceRate: Float, // 0.0 to 100.0 (Yüzde uyum)
    val peakValue: Float,
    val peakDate: Long,
    val troughValue: Float,
    val troughDate: Long,
    /** Birincil özet metrik (KB için ortalama sistolik; diğerleri için ortalama değer). */
    val averageValue: Float,
    /** KB toplu analizinde ortalama diyastolik (UI’da “ortalama” kartında kullanılır). */
    val averageSecondaryValue: Float? = null,
    val weeklyTrend: String, // "Artıyor", "Azalıyor", "Stabil"
    val recommendations: List<String>,
    val sourceRefs: List<String> = emptyList(),
    val disclaimer: String = "Bu analiz yalnızca kayıtlı ölçümlere dayalı bilgilendirme özetidir; tanı koymaz. Şüphe, risk veya belirti varsa sağlık profesyoneline başvurun."
)
