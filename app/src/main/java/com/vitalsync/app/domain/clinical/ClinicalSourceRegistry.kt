package com.vitalsync.app.domain.clinical

/**
 * Uygulama içi **sürümlü kaynak kimlikleri** (URL üretmez; hukuk/klinik izlenebilirlik için sabit kod).
 * Metinlerde kısa atıf: "Kaynak: ESC 2024" gibi [ClinicalSourceRegistry.ESC_2024_BP.displayName] kullanılır.
 */
object ClinicalSourceRegistry {
    const val ESC_2024_BP = "ESC_2024_BP"
    const val TEMD_2026_DIABETES = "TEMD_2026_DIABETES"
    const val ADA_2026_DIABETES = "ADA_2026_DIABETES"
    const val AHA_RESTING_HR = "AHA_RESTING_HR"
    const val AHA_EXERCISE_HR = "AHA_EXERCISE_HR"
    const val AHA_ACC_2025_BP_SEVERE = "AHA_ACC_2025_BP_SEVERE"
    const val FDA_PULSE_OX_LIMITATIONS = "FDA_PULSE_OX"
    const val MEDLINEPLUS_PULSE_OX = "MEDLINEPLUS_PULSE_OX"
    const val BTS_OXYGEN_TARGETS = "BTS_OXYGEN_TARGETS"
    const val WHO_OXYGEN_TRAINING = "WHO_OXYGEN_TRAINING"
    const val TURK_KARDIYOLOJI_DERNEGI_POP = "TKD_POP_EDUCATION"

    /** Kullanıcıya gösterilecek kısa etiket (satır içi). */
    fun displayLabels(ids: Collection<String>): String =
        ids.distinct().joinToString(" · ") { shortLabel(it) }

    private fun shortLabel(id: String): String = when (id) {
        ESC_2024_BP -> "ESC 2024"
        TEMD_2026_DIABETES -> "TEMD 2026"
        ADA_2026_DIABETES -> "ADA 2026"
        AHA_RESTING_HR -> "AHA (istirahat nabız)"
        AHA_EXERCISE_HR -> "AHA (egzersiz nabız)"
        AHA_ACC_2025_BP_SEVERE -> "AHA/ACC 2025 (şiddetli KB)"
        FDA_PULSE_OX_LIMITATIONS -> "FDA (pulse oksimetre)"
        MEDLINEPLUS_PULSE_OX -> "MedlinePlus (SpO₂)"
        BTS_OXYGEN_TARGETS -> "BTS (oksijen hedefleri)"
        WHO_OXYGEN_TRAINING -> "WHO (pulse oksimetri eğitimi)"
        TURK_KARDIYOLOJI_DERNEGI_POP -> "TKD (toplum bilgilendirme)"
        else -> id
    }
}
