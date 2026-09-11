package com.vitalsync.app.domain.engine

import com.vitalsync.app.domain.clinical.ClinicalSourceRegistry
import com.vitalsync.app.domain.model.AggregateAnalysisResult
import com.vitalsync.app.domain.model.AnalysisResult
import com.vitalsync.app.domain.model.BloodPressureRecord
import com.vitalsync.app.domain.model.GlucoseRecord
import com.vitalsync.app.domain.model.HeartRateRecord
import com.vitalsync.app.domain.model.HealthStatus
import com.vitalsync.app.domain.model.OxygenRecord
import com.vitalsync.app.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Deterministik, test edilebilir kural motoru. LLM kullanmaz.
 *
 * Kaynak önceliği (sürümlü kimlikler [ClinicalSourceRegistry]):
 * - Türkiye/Avrupa KB sınıflandırması: ESC 2024 ofis; ev ölçümü (HBPM) eşikleri ayrı ele alınır.
 * - Diyabet hedef/eşik: TEMD 2026 (birincil), ADA 2026 (destekleyici notlar).
 * - Şiddetli KB / acil dil: AHA/ACC 2025 toplumu bilgilendirme çerçevesi (uyarı metni; ilaç önerisi yok).
 * - Nabız: AHA istirahat ve egzersiz nabız bölgesi (220−yaş yaklaşımı).
 * - SpO₂: BTS oksijen hedefleri (KOAH bandı), FDA/MedlinePlus pulse oksimetre sınırlamaları (kalite uyarısı).
 */
object HealthAnalysisEngine {

    private const val FOOTER_SINGLE =
        "Tek ölçüm tanı anlamına gelmez ve tedavi yerine geçmez. Aynı koşullarda 2–3 dk ara ile tekrar ölçün; " +
            "belirsizlikte sağlık profesyoneline danışın. Göğüs ağrısı, nefes darlığı, konuşma bozulması, yüz/kol uyuşması " +
            "veya güçsüzlük, şiddetli baş ağrısı, görme değişikliği, bayılma varsa 112’yi arayın."

    private fun withClinicalFooter(
        recommendation: String,
        sources: List<String>,
        quality: List<String>,
    ): String = buildString {
        append(recommendation.trim())
        append("\n\n")
        append(FOOTER_SINGLE)
        if (sources.isNotEmpty()) {
            append("\nKaynak: ")
            append(ClinicalSourceRegistry.displayLabels(sources))
        }
        if (quality.isNotEmpty()) {
            append("\nÖlçüm kalitesi: ")
            append(quality.joinToString(" "))
        }
    }

    private fun result(
        status: HealthStatus,
        title: String,
        description: String,
        recommendation: String,
        emoji: String,
        sources: List<String>,
        quality: List<String> = emptyList(),
    ): AnalysisResult = AnalysisResult(
        status = status,
        title = title,
        description = description,
        recommendation = withClinicalFooter(recommendation, sources, quality),
        emoji = emoji,
        sourceRefs = sources.distinct(),
        measurementQualityWarnings = quality,
    )

    private fun isOfficeContext(context: String): Boolean = context == "Klinik/Ofis"

    private fun isPostExerciseContext(context: String): Boolean = context == "Egzersiz Sonrası"

    // --- Kan basıncı (ESC 2024 ofis; ev/HBPM tek ölçüm yaklaşımı) ---

    fun analyzeBloodPressure(
        systolic: Int,
        diastolic: Int,
        hasHypertension: Boolean = false,
        context: String = "İstirahat",
        hasPregnancy: Boolean = false,
        hasEmergencySymptoms: Boolean = false,
    ): AnalysisResult {
        val office = isOfficeContext(context)
        val sources = mutableListOf(ClinicalSourceRegistry.ESC_2024_BP)
        if (!office) sources.add(ClinicalSourceRegistry.TURK_KARDIYOLOJI_DERNEGI_POP)

        if (isPostExerciseContext(context)) {
            return analyzeBloodPressurePostExercise(systolic, diastolic)
        }

        if (hasPregnancy && (systolic >= 160 || diastolic >= 110)) {
            return result(
                HealthStatus.CRITICAL,
                title = "Gebelikte ciddi yüksek değer ile uyumlu olabilir",
                description = "Ölçüm: $systolic/$diastolic mmHg. Bu eşikler gebelikte acil değerlendirme gerektirebilir.",
                recommendation = "Gebelikte bu değerler ciddi kabul edilebilir. Hemen doğum/ acil kadın doğum hattınız veya 112 ile temas kurun.",
                emoji = "🚨",
                sources = sources + ClinicalSourceRegistry.TEMD_2026_DIABETES,
            )
        }

        if (isHypotension(systolic, diastolic)) {
            return result(
                HealthStatus.LOW,
                title = "Düşük kan basıncı ile uyumlu olabilir",
                description = "Ölçüm: $systolic/$diastolic mmHg. Tek ölçüm; doğrulama gerekir.",
                recommendation = "Bol sıvı alın, dik oturun/yatın. Baş dönmesi, bayılma, göğüs ağrısı veya nefes darlığı varsa 112. Aksi halde tekrar ölçün.",
                emoji = "🔵",
                sources = sources,
            )
        }

        if (systolic >= 180 || diastolic >= 120) {
            sources.add(ClinicalSourceRegistry.AHA_ACC_2025_BP_SEVERE)
            return if (hasEmergencySymptoms) {
                result(
                    HealthStatus.CRITICAL,
                    title = "Çok yüksek kan basıncı — acil belirtiler bildirildi",
                    description = "Ölçüm: $systolic/$diastolic mmHg. Acil değerlendirme gerektirebilir.",
                    recommendation = "112’yi arayın veya en yakın acil servise başvurun. Hareketi yavaşlatın, sakin olun.",
                    emoji = "🚨",
                    sources = sources,
                )
            } else {
                result(
                    HealthStatus.HIGH,
                    title = "Çok yüksek kan basıncı (şiddetli aralık ile uyumlu)",
                    description = "Ölçüm: $systolic/$diastolic mmHg. Tanı değildir; doğrulama ve risk değerlendirmesi gerekir.",
                    recommendation = "5 dk dinlenip doğru teknikle tekrar ölçün. Yüksek kalıyorsa bugün sağlık profesyoneline ulaşın. Acil belirtiler gelişirse 112.",
                    emoji = "🔴",
                    sources = sources,
                )
            }
        }

        if (systolic >= 160 || diastolic >= 100) {
            return result(
                HealthStatus.HIGH,
                title = "Yüksek kan basıncı (ileri evre ile uyumlu olabilir)",
                description = "Ölçüm: $systolic/$diastolic mmHg. Ofis sınıflamasında genellikle daha ileri aralık olarak ele alınır.",
                recommendation = "En kısa sürede sağlık profesyoneline randevu alın. Yaşam tarzı ve ilaç planı kişiye özeldir; burada önerilmez.",
                emoji = "🔴",
                sources = sources,
            )
        }

        return if (office) classifyOfficeBp(systolic, diastolic, hasHypertension, sources)
        else classifyHomeBp(systolic, diastolic, hasHypertension, sources)
    }

    private fun isHypotension(s: Int, d: Int): Boolean = s < 90 || d < 60

    private fun classifyOfficeBp(
        s: Int,
        d: Int,
        hasHypertension: Boolean,
        sources: List<String>,
    ): AnalysisResult {
        when {
            s >= 140 || d >= 90 -> {
                val extra = if (hasHypertension) " Bilinen hipertansiyon öykünüz var; tedavi hedeflerinizi doktorunuzla gözden geçirin."
                else ""
                return result(
                    HealthStatus.HIGH,
                    title = "Yüksek kan basıncı (ofis ölçümü eşikleri ile uyumlu olabilir)",
                    description = "Ölçüm: $s/$d mmHg. ESC 2024 ofis ölçümünde genellikle ≥140 veya ≥90 yüksek kabul edilir.",
                    recommendation = "Tek ölçüm tanı koymaz; ev ölçümleri ve tekrar değerlendirme önemlidir.$extra",
                    emoji = "🟠",
                    sources = sources,
                )
            }
            (s in 120..139) || (d in 70..89) -> {
                return result(
                    HealthStatus.ATTENTION,
                    title = "Yükselmiş kan basıncı (ofis: “elevated” aralığı ile uyumlu olabilir)",
                    description = "Ölçüm: $s/$d mmHg. ESC 2024’e göre ofiste 120–139 veya 70–89 aralığı dikkat gerektirebilir.",
                    recommendation = if (hasHypertension) "Takip ve yaşam tarzı hedeflerinizi doktorunuzla netleştirin."
                    else "Tuz/alkol/kafein, uyku ve düzenli egzersiz; tekrar ölçüm serisi tutun.",
                    emoji = "🟡",
                    sources = sources,
                )
            }
            s < 120 && d < 70 -> {
                return result(
                    HealthStatus.NORMAL,
                    title = "Ofis ölçümü: hedeflenebilir aralık ile uyumlu olabilir",
                    description = "Ölçüm: $s/$d mmHg. ESC 2024 ofiste <120 ve <70 genellikle yükselmiş kabul edilmez.",
                    recommendation = "Düzenli ölçüm ve sağlıklı yaşam tarzını sürdürün.",
                    emoji = "💚",
                    sources = sources,
                )
            }
            else -> {
                return result(
                    HealthStatus.NORMAL,
                    title = "Ofis ölçümü: kabul edilebilir bant",
                    description = "Ölçüm: $s/$d mmHg.",
                    recommendation = "Ölçüm bağlamını (ofis/ev) not edin; şüphede tekrar ölçüm serisi alın.",
                    emoji = "✅",
                    sources = sources,
                )
            }
        }
    }

    private fun classifyHomeBp(
        s: Int,
        d: Int,
        hasHypertension: Boolean,
        sources: List<String>,
    ): AnalysisResult {
        val quality = listOf(
            "Ev ölçümü (HBPM) tanısı için çoğunlukla birden fazla gün ortalaması gerekir; tek ölçüm yönlendirme amaçlıdır.",
        )
        when {
            s >= 135 || d >= 85 -> {
                val extra = if (hasHypertension) " Bilinen hipertansiyon öykünüz var; hedefler kişiseldir."
                else ""
                return result(
                    HealthStatus.HIGH,
                    title = "Ev ölçümü: yüksek değer ile uyumlu olabilir (HBPM eşiği)",
                    description = "Ölçüm: $s/$d mmHg. Kılavuzlarda ev ortalaması için genellikle ≥135/85 yüksek kabul edilir.",
                    recommendation = "7 gün sabah/akşam 2’şer ölçüm serisi tutup ortalamayı hesaplayın; yüksek kalırsa sağlık profesyoneline başvurun.$extra",
                    emoji = "🟠",
                    sources = sources,
                    quality = quality,
                )
            }
            (s in 120..134) || (d in 70..84) -> {
                return result(
                    HealthStatus.ATTENTION,
                    title = "Ev ölçümü: sınır üstü ile uyumlu olabilir",
                    description = "Ölçüm: $s/$d mmHg.",
                    recommendation = "Ölçüm tekniğini kontrol edin; 1 haftalık seri ile ortalama çıkarın.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality,
                )
            }
            s < 120 && d < 70 -> {
                return result(
                    HealthStatus.NORMAL,
                    title = "Ev ölçümü: hedeflenebilir aralık ile uyumlu olabilir",
                    description = "Ölçüm: $s/$d mmHg.",
                    recommendation = "Düzenli ölçüm ve yaşam tarzı takibini sürdürün.",
                    emoji = "💚",
                    sources = sources,
                    quality = quality,
                )
            }
            else -> {
                return result(
                    HealthStatus.NORMAL,
                    title = "Ev ölçümü: kabul edilebilir bant",
                    description = "Ölçüm: $s/$d mmHg.",
                    recommendation = "Ölçüm serisi ile ortalamayı izleyin.",
                    emoji = "✅",
                    sources = sources,
                    quality = quality,
                )
            }
        }
    }

    private fun analyzeBloodPressurePostExercise(systolic: Int, diastolic: Int): AnalysisResult {
        val sources = listOf(ClinicalSourceRegistry.ESC_2024_BP, ClinicalSourceRegistry.AHA_EXERCISE_HR)
        return when {
            systolic > 210 || diastolic > 110 -> result(
                HealthStatus.CRITICAL,
                title = "Egzersiz sonrası bile çok yüksek değerler",
                description = "Ölçüm: $systolic/$diastolic mmHg.",
                recommendation = "Egzersizi durdurun, dinlenin; tekrar ölçün. Belirtiler varsa veya değer düşmüyorsa 112/acil.",
                emoji = "🚨",
                sources = sources,
            )
            systolic > 180 || diastolic > 100 -> result(
                HealthStatus.HIGH,
                title = "Egzersiz sonrası yüksek kan basıncı",
                description = "Ölçüm: $systolic/$diastolic mmHg. Genellikle 10–20 dk içinde düşmesi beklenir.",
                recommendation = "15 dk dinlenip tekrar ölçün; yüksek kalırsa sağlık profesyoneline danışın.",
                emoji = "⚠️",
                sources = sources,
            )
            else -> result(
                HealthStatus.NORMAL,
                title = "Egzersiz sonrası: beklenen bant ile uyumlu olabilir",
                description = "Ölçüm: $systolic/$diastolic mmHg.",
                recommendation = "Nefes ve genel durumunuzu izleyin; ağrı/ bayılma varsa acil değerlendirme.",
                emoji = "✅",
                sources = sources,
            )
        }
    }

    // --- Glukoz ---

    fun analyzeGlucose(
        value: Int,
        context: String,
        hasDiabetes: Boolean = false,
        measurementMethod: String = "Parmak Ucu",
        hasClassicHyperglycemicSymptoms: Boolean = false,
        usesSglt2Inhibitor: Boolean = false,
    ): AnalysisResult {
        val methodQuality = buildList {
            if (measurementMethod.contains("Parmak", ignoreCase = true)) {
                add("Parmak ucu glukoz ölçümü laboratuvara göre daha fazla hata içerebilir; düşük/ yüksek okumada tekrar ölçüm önerilir.")
            }
        }
        return when (context) {
            "Açlık" -> analyzeGlucoseFasting(value, hasDiabetes, methodQuality, usesSglt2Inhibitor)
            "Tokluk" -> analyzeGlucosePostprandial(value, hasDiabetes, methodQuality)
            "OGTT" -> analyzeGlucoseOgtt2h(value, hasDiabetes, methodQuality)
            else -> analyzeGlucoseRandom(
                value,
                hasDiabetes,
                methodQuality,
                hasClassicHyperglycemicSymptoms,
            )
        }
    }

    private fun analyzeGlucoseFasting(
        value: Int,
        hasDiabetes: Boolean,
        quality: List<String>,
        usesSglt2Inhibitor: Boolean,
    ): AnalysisResult {
        val sources = mutableListOf(ClinicalSourceRegistry.TEMD_2026_DIABETES, ClinicalSourceRegistry.ADA_2026_DIABETES)
        if (hasDiabetes) {
            return when {
                value < 54 -> result(
                    HealthStatus.CRITICAL,
                    title = "Klinik önemli düşük glukoz riski (<54 mg/dL)",
                    description = "Açlık ölçüm: $value mg/dL.",
                    recommendation = "Hızlı emilen karbonhidrat alın ve 15 dk sonra tekrar ölçün. Bilinç bulanıklığı/ nöbet varsa 112.",
                    emoji = "🚨",
                    sources = sources,
                    quality = quality,
                )
                value < 70 -> result(
                    HealthStatus.LOW,
                    title = "Düşük glukoz (<70 mg/dL)",
                    description = "Açlık ölçüm: $value mg/dL.",
                    recommendation = "15 g hızlı karbonhidrat + tekrar ölçüm. Sık tekrarlıyorsa sağlık profesyoneline başvurun.",
                    emoji = "🔵",
                    sources = sources,
                    quality = quality,
                )
                value in 70..79 -> result(
                    HealthStatus.ATTENTION,
                    title = "Açlık glukozu: hedef bandın altı ile uyumlu olabilir",
                    description = "Ölçüm: $value mg/dL. TEMD 2026’da sıkça anılan açlık/öğün öncesi hedef genelde 80–130 mg/dL bandına yaklaşır; kişiselleştirilebilir.",
                    recommendation = "Tekrar ölçüm; sık tekrarlıyorsa hipoglisemi riski ve ilaç/öğün zamanlaması için doktorunuzla görüşün.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality,
                )
                value in 80..130 -> result(
                    HealthStatus.NORMAL,
                    title = "Açlık hedef aralığı ile uyumlu olabilir (diyabetli)",
                    description = "Ölçüm: $value mg/dL. TEMD 2026’da sıkça önerilen açlık/öğün öncesi hedef bandı 80–130 mg/dL civarıdır; kişiselleştirilebilir.",
                    recommendation = "Kişisel hedefinizi doktorunuzla netleştirin.",
                    emoji = "💚",
                    sources = sources,
                    quality = quality,
                )
                value in 131..180 -> result(
                    HealthStatus.HIGH,
                    title = "Açlık glukozu hedef üstü ile uyumlu olabilir",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Öğün/ ilaç zamanlaması ve aktiviteyi gözden geçirin; planınızı doktorunuzla konuşun.",
                    emoji = "🔴",
                    sources = sources,
                    quality = quality,
                )
                else -> result(
                    HealthStatus.CRITICAL,
                    title = "Çok yüksek açlık glukozu — acil değerlendirme düşünülmeli",
                    description = "Ölçüm: $value mg/dL. Kusma, karın ağrısı, hızlı solunum, susuzluk, bilinç bulanıklığı varsa DKA/HHS açısından acil değerlendirme gerekebilir.",
                    recommendation = "112 veya acil servis değerlendirmesi düşünün. SGLT2 kullanıyorsanız euglykemik ketoasidoz riski için acil başvuru önemlidir: ${if (usesSglt2Inhibitor) "Evet bildirildi." else "Bilinmiyor; ilaç listenizi doktorunuza bildirin."}",
                    emoji = "🚨",
                    sources = sources,
                    quality = quality,
                )
            }
        } else {
            return when {
                value < 54 -> result(
                    HealthStatus.CRITICAL,
                    title = "Ciddi düşük glukoz (<54 mg/dL)",
                    description = "Açlık ölçüm: $value mg/dL.",
                    recommendation = "Hızlı karbonhidrat + tekrar ölçüm; belirtilerle 112.",
                    emoji = "🚨",
                    sources = sources,
                    quality = quality,
                )
                value < 70 -> result(
                    HealthStatus.LOW,
                    title = "Düşük glukoz (<70 mg/dL)",
                    description = "Açlık ölçüm: $value mg/dL.",
                    recommendation = "Karbonhidrat alın; tekrar ölçün.",
                    emoji = "🔵",
                    sources = sources,
                    quality = quality,
                )
                value <= 99 -> result(
                    HealthStatus.NORMAL,
                    title = "Açlık glukozu: yaygın hedef band ile uyumlu olabilir",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Sağlıklı yaşam tarzını sürdürün.",
                    emoji = "💚",
                    sources = sources,
                    quality = quality,
                )
                value in 100..125 -> result(
                    HealthStatus.ATTENTION,
                    title = "Açlık glukozu: bozulmuş açlık glukozu aralığı ile uyumlu olabilir",
                    description = "Ölçüm: $value mg/dL. Tanı için laboratuvarda doğrulama gerekir.",
                    recommendation = "Tek ölçüm tanı koymaz. Açlık tekrar ölçümü ve/veya OGTT için sağlık profesyoneline danışın.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality,
                )
                value in 126..249 -> result(
                    HealthStatus.HIGH,
                    title = "Açlık glukozu: diyabet tanı eşiği ile uyumlu olabilir",
                    description = "Ölçüm: $value mg/dL (≥126 mg/dL). Tanı yalnızca klinik/laboratuvar değerlendirme ile konur.",
                    recommendation = "En kısa sürede sağlık profesyoneline başvurun; tekrarlayan açlık ölçümleri ve/veya HbA1c değerlendirmesi önerilir.",
                    emoji = "🔴",
                    sources = sources,
                    quality = quality,
                )
                else -> result(
                    HealthStatus.CRITICAL,
                    title = "Çok yüksek açlık glukozu",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Acil değerlendirme düşünün (kusma, karın ağrısı, solunum hızlanması, bilinç değişikliği varsa 112).",
                    emoji = "🚨",
                    sources = sources,
                    quality = quality,
                )
            }
        }
    }

    private fun analyzeGlucosePostprandial(value: Int, hasDiabetes: Boolean, quality: List<String>): AnalysisResult {
        val sources = listOf(ClinicalSourceRegistry.TEMD_2026_DIABETES, ClinicalSourceRegistry.ADA_2026_DIABETES)
        val bandNote = "Tokluk ölçümde 1. saat mi 2. saat mi olduğunu not etmek yorumu netleştirir."
        if (hasDiabetes) {
            return when {
                value < 70 -> result(
                    HealthStatus.LOW,
                    title = "Tokluk hipoglisemi riski",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Hızlı karbonhidrat + tekrar ölçüm.",
                    emoji = "🔵",
                    sources = sources,
                    quality = quality + bandNote,
                )
                value < 160 -> result(
                    HealthStatus.NORMAL,
                    title = "Tokluk hedef ile uyumlu olabilir (TEMD 2. saat <160 mg/dL çizgisi)",
                    description = "Ölçüm: $value mg/dL. Bireysel hedef farklı olabilir; ADA’da sıkça 2 saat <180 mg/dL hedefi de anılır.",
                    recommendation = "Kişisel hedefinizi doktorunuzla netleştirin.",
                    emoji = "💚",
                    sources = sources,
                    quality = quality + bandNote,
                )
                value in 160..199 -> result(
                    HealthStatus.ATTENTION,
                    title = "Tokluk glukozu hedef üstü ile uyumlu olabilir",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Öğün içeriği ve aktiviteyi gözden geçirin; takip için doktorunuzla görüşün.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality + bandNote,
                )
                else -> result(
                    HealthStatus.HIGH,
                    title = "Tokluk glukozu belirgin yüksek",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Kısa sürede sağlık profesyoneline danışın.",
                    emoji = "🔴",
                    sources = sources,
                    quality = quality + bandNote,
                )
            }
        } else {
            return when {
                value < 70 -> result(
                    HealthStatus.LOW,
                    title = "Tokluk düşük glukoz",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Karbonhidrat alın; tekrar ölçün.",
                    emoji = "🔵",
                    sources = sources,
                    quality = quality + bandNote,
                )
                value < 140 -> result(
                    HealthStatus.NORMAL,
                    title = "Tokluk glukozu: genellikle hedeflenebilir bant",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Bağlam (1./2. saat) not edin.",
                    emoji = "💚",
                    sources = sources,
                    quality = quality + bandNote,
                )
                value in 140..199 -> result(
                    HealthStatus.ATTENTION,
                    title = "Tokluk glukozu: bozulmuş tolerans aralığı ile uyumlu olabilir (bağlama bağlı)",
                    description = "Ölçüm: $value mg/dL. OGTT 2. saat yorumu ile karıştırılmamalıdır.",
                    recommendation = "Sağlık profesyoneline danışın; gerekirse OGTT planlanır.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality + bandNote,
                )
                else -> result(
                    HealthStatus.HIGH,
                    title = "Tokluk glukozu yüksek",
                    description = "Ölçüm: $value mg/dL.",
                    recommendation = "Değerlendirme için sağlık profesyoneline başvurun.",
                    emoji = "🔴",
                    sources = sources,
                    quality = quality + bandNote,
                )
            }
        }
    }

    private fun analyzeGlucoseOgtt2h(value: Int, hasDiabetes: Boolean, quality: List<String>): AnalysisResult {
        val sources = listOf(ClinicalSourceRegistry.ADA_2026_DIABETES, ClinicalSourceRegistry.TEMD_2026_DIABETES)
        if (hasDiabetes) {
            return analyzeGlucosePostprandial(value, true, quality)
        }
        return when {
            value < 140 -> result(
                HealthStatus.NORMAL,
                title = "OGTT 2. saat: genellikle hedeflenebilir bant",
                description = "Ölçüm: $value mg/dL.",
                recommendation = "Sonuç yorumu test protokolüne bağlıdır; doktorunuzla paylaşın.",
                emoji = "💚",
                sources = sources,
                quality = quality,
            )
            value in 140..199 -> result(
                HealthStatus.ATTENTION,
                title = "OGTT 2. saat: bozulmuş glukoz toleransı aralığı ile uyumlu olabilir",
                description = "Ölçüm: $value mg/dL.",
                recommendation = "Tanı/ takip için sağlık profesyoneline danışın.",
                emoji = "🟡",
                sources = sources,
                quality = quality,
            )
            else -> result(
                HealthStatus.HIGH,
                title = "OGTT 2. saat: diyabet eşiği ile uyumlu olabilir",
                description = "Ölçüm: $value mg/dL (≥200 mg/dL). Tanı klinik karardır.",
                recommendation = "Sağlık profesyoneline başvurun.",
                emoji = "🔴",
                sources = sources,
                quality = quality,
            )
        }
    }

    private fun analyzeGlucoseRandom(
        value: Int,
        hasDiabetes: Boolean,
        quality: List<String>,
        hasClassicHyperglycemicSymptoms: Boolean,
    ): AnalysisResult {
        val sources = listOf(ClinicalSourceRegistry.ADA_2026_DIABETES, ClinicalSourceRegistry.TEMD_2026_DIABETES)
        if (hasDiabetes) {
            return when {
                value < 70 -> result(
                    HealthStatus.LOW,
                    title = "Rastgele ölçümde düşük glukoz",
                    description = "$value mg/dL.",
                    recommendation = "Karbonhidrat + tekrar ölçüm.",
                    emoji = "🔵",
                    sources = sources,
                    quality = quality,
                )
                value in 70..139 -> result(
                    HealthStatus.NORMAL,
                    title = "Rastgele glukoz: sık görülen bant",
                    description = "$value mg/dL.",
                    recommendation = "Rastgele ölçüm bağlamı belirsizdir; açlık/tokluk zamanı not ederek tekrar ölçün.",
                    emoji = "✅",
                    sources = sources,
                    quality = quality,
                )
                value in 140..199 -> result(
                    HealthStatus.ATTENTION,
                    title = "Rastgele glukoz yüksek — bağlam netleştirilmeli",
                    description = "$value mg/dL.",
                    recommendation = "Tek ölçüm tanı koymaz. Tokluk (1./2. saat) veya açlık ölçümü ile tamamlayın; gerekiyorsa doktorunuza danışın.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality,
                )
                else -> result(
                    HealthStatus.HIGH,
                    title = "Rastgele glukoz yüksek",
                    description = "$value mg/dL.",
                    recommendation = "Nefes darlığı, bulantı, kusma, karın ağrısı varsa acil değerlendirme düşünün.",
                    emoji = "🔴",
                    sources = sources,
                    quality = quality,
                )
            }
        }
        return when {
            value < 70 -> result(
                HealthStatus.LOW,
                title = "Düşük glukoz",
                description = "Rastgele: $value mg/dL.",
                recommendation = "Karbonhidrat + tekrar ölçüm.",
                emoji = "🔵",
                sources = sources,
                quality = quality,
            )
            value <= 139 -> result(
                HealthStatus.NORMAL,
                title = "Rastgele glukoz: yaygın bant",
                description = "$value mg/dL.",
                recommendation = "Açlık veya tokluk ölçümü ile tamamlayın.",
                emoji = "✅",
                sources = sources,
                quality = quality,
            )
            value in 140..199 -> result(
                HealthStatus.ATTENTION,
                title = "Rastgele glukoz yüksek",
                description = "$value mg/dL.",
                recommendation = "Tek ölçüm tanı koymaz. Tekrar ölçüm ve değerlendirme önerilir.",
                emoji = "🟡",
                sources = sources,
                quality = quality,
            )
            value in 200..299 -> {
                val sev = if (hasClassicHyperglycemicSymptoms) {
                    result(
                        HealthStatus.HIGH,
                        title = "Rastgele glukoz çok yüksek (klasik hiperglisemi belirtileri bildirildi)",
                        description = "$value mg/dL.",
                        recommendation = "Acil değerlendirme düşünün (112/ acil servis).",
                        emoji = "🔴",
                        sources = sources,
                        quality = quality,
                    )
                } else {
                    result(
                        HealthStatus.HIGH,
                        title = "Rastgele glukoz çok yüksek",
                        description = "$value mg/dL. Tanı için bağlam ve tekrar ölçüm gerekir.",
                        recommendation = "Aynı gün içinde açlık ölçümü veya sağlık profesyoneline başvuru planlayın.",
                        emoji = "🔴",
                        sources = sources,
                        quality = quality,
                    )
                }
                sev
            }
            else -> result(
                HealthStatus.CRITICAL,
                title = "Aşırı yüksek glukoz — acil değerlendirme düşünülmeli",
                description = "$value mg/dL.",
                recommendation = "DKA/HHS riskine karşı acil başvuruyu düşünün (özellikle kusma, karın ağrısı, solunum hızlanması, bilinç değişikliği).",
                emoji = "🚨",
                sources = sources,
                quality = quality,
            )
        }
    }

    // --- Nabız ---

    fun analyzeHeartRate(
        value: Int,
        age: Int,
        context: String = "İstirahat",
        hasFever: Boolean = false,
        hasPalpitations: Boolean = false,
        hasHeartFailure: Boolean = false,
    ): AnalysisResult {
        return when (context) {
            "Uyku" -> analyzeHeartRateSleep(value, hasFever, hasPalpitations)
            "Egzersiz" -> analyzeHeartRateExercise(value, age)
            else -> analyzeHeartRateRest(value, hasFever, hasPalpitations, hasHeartFailure)
        }
    }

    private fun analyzeHeartRateExercise(value: Int, age: Int): AnalysisResult {
        val maxHr = (220 - age).coerceAtLeast(100)
        val sources = listOf(ClinicalSourceRegistry.AHA_EXERCISE_HR)
        return when {
            value > maxHr -> result(
                HealthStatus.CRITICAL,
                title = "Tahmini maksimum nabız üstü",
                description = "$value bpm (yaklaşık maks: $maxHr bpm).",
                recommendation = "Yoğunluğu azaltın; göğüs ağrısı/bayılma varsa 112.",
                emoji = "🚨",
                sources = sources,
            )
            value > (maxHr * 0.85f).roundToInt() -> result(
                HealthStatus.ATTENTION,
                title = "Yüksek yoğunluk bölgesi",
                description = "$value bpm (≈%${((value * 100f / maxHr).roundToInt())} maks).",
                recommendation = "Kısa süreli olabilir; uzun süre kalıcıysa yoğunluğu düşürün.",
                emoji = "🟡",
                sources = sources,
            )
            else -> result(
                HealthStatus.NORMAL,
                title = "Egzersiz nabzı: genellikle güvenli bant",
                description = "$value bpm.",
                recommendation = "Beta bloker vb. ilaçlar nabzı düşürür; kişisel hedef için kardiyoloji önerilir.",
                emoji = "💚",
                sources = sources,
            )
        }
    }

    private fun analyzeHeartRateSleep(
        value: Int,
        hasFever: Boolean,
        hasPalpitations: Boolean,
    ): AnalysisResult {
        val sources = listOf(ClinicalSourceRegistry.AHA_RESTING_HR)
        return when {
            value < 35 -> result(
                HealthStatus.CRITICAL,
                title = "Uyku nabzı çok düşük",
                description = "$value bpm.",
                recommendation = "Tekrar ölçüm; bayılma/bilinç bulanıklığı varsa 112.",
                emoji = "🚨",
                sources = sources,
            )
            value in 35..90 -> result(
                HealthStatus.NORMAL,
                title = "Uyku nabzı: sık görülen bant",
                description = "$value bpm. Uyku sırasında düşük nabız sık görülebilir.",
                recommendation = if (hasPalpitations || hasFever) "Belirti varsa uyku dışı ölçümle karşılaştırın; gerekiyorsa doktorunuza danışın."
                else "Genelde benign kabul edilebilir; şüphede doktorunuza danışın.",
                emoji = "😴",
                sources = sources,
            )
            value > 120 -> result(
                HealthStatus.ATTENTION,
                title = "Uyku nabzı yüksek",
                description = "$value bpm.",
                recommendation = "Ateş/ enfeksiyon/ uyku apnesi vb. nedenler düşünülebilir; tekrarlayan ise değerlendirme.",
                emoji = "🟡",
                sources = sources,
            )
            else -> result(
                HealthStatus.NORMAL,
                title = "Uyku nabzı: kabul edilebilir",
                description = "$value bpm.",
                recommendation = "Genel durumunuzu izleyin.",
                emoji = "✅",
                sources = sources,
            )
        }
    }

    private fun analyzeHeartRateRest(
        value: Int,
        hasFever: Boolean,
        hasPalpitations: Boolean,
        hasHeartFailure: Boolean,
    ): AnalysisResult {
        val sources = listOf(ClinicalSourceRegistry.AHA_RESTING_HR)
        return when {
            value < 40 -> result(
                HealthStatus.CRITICAL,
                title = "İstirahat nabzı çok düşük",
                description = "$value bpm.",
                recommendation = "Bayılma, göğüs ağrısı varsa 112. Aksi halde acil olmayan değerlendirme için doktorunuza danışın.",
                emoji = "🚨",
                sources = sources,
            )
            value in 40..59 -> result(
                HealthStatus.NORMAL,
                title = "İstirahat nabzı: düşük ama sık kabul edilebilir bant",
                description = "$value bpm. Sporcularda/ kardiyovasküler uyumda 40–60 bpm sık görülür.",
                recommendation = "Belirti yoksa genelde izlenebilir; çarpıntı/bayılma varsa değerlendirme.",
                emoji = "✅",
                sources = sources,
            )
            value in 60..100 -> result(
                HealthStatus.NORMAL,
                title = "İstirahat nabzı: yaygın normal bant",
                description = "$value bpm.",
                recommendation = when {
                    hasFever -> "Ateş nabzı yükseltebilir; hidrasyon ve ateş tedavisi."
                    hasPalpitations -> "Çarpıntı devam ederse değerlendirme önerilir."
                    hasHeartFailure -> "Kalp yetmezliği öyküsü: planlı kontrollerinizi sürdürün."
                    else -> "Düzenli aktivite ve uyku düzeni faydalıdır."
                },
                emoji = "💚",
                sources = sources,
            )
            value in 101..120 -> result(
                HealthStatus.ATTENTION,
                title = "İstirahat taşikardisi ile uyumlu olabilir",
                description = "$value bpm.",
                recommendation = when {
                    hasFever -> "Ateşe bağlı olabilir; ateş düzelince tekrar ölçün."
                    hasHeartFailure -> "Kontrol için sağlık profesyoneline bildirin."
                    else -> "Kafein/stres/ dehidratasyon; sakinleşip tekrar ölçün."
                },
                emoji = "🟡",
                sources = sources,
            )
            value in 121..150 -> result(
                HealthStatus.HIGH,
                title = "Belirgin istirahat taşikardisi riski",
                description = "$value bpm.",
                recommendation = "Devamlılık varsa kardiyoloji değerlendirmesi önerilir.",
                emoji = "🔴",
                sources = sources,
            )
            else -> result(
                HealthStatus.CRITICAL,
                title = "Çok hızlı istirahat nabzı",
                description = "$value bpm.",
                recommendation = "Göğüs ağrısı/ nefes darlığı/ bayılma varsa 112.",
                emoji = "🚨",
                sources = sources,
            )
        }
    }

    // --- SpO2 ---

    fun analyzeOxygen(
        value: Int,
        hasBreathingDifficulty: Boolean = false,
        hasCOPD: Boolean = false,
        altitude: Int? = null,
    ): AnalysisResult {
        val sources = mutableListOf(
            ClinicalSourceRegistry.BTS_OXYGEN_TARGETS,
            ClinicalSourceRegistry.FDA_PULSE_OX_LIMITATIONS,
            ClinicalSourceRegistry.MEDLINEPLUS_PULSE_OX,
        )
        val quality = buildList {
            add("Soğuk parmak, oje/ takma tırnak, hareket, zayıf perfüzyon ve cihaz kalitesi okumayı etkileyebilir (FDA).")
            if ((altitude ?: 0) > 1500) {
                add("Yükseklik SpO₂’yi düşürebilir; semptom varsa yüksekliğe güvenmeden değerlendirme alın.")
            }
        }

        if (hasCOPD) {
            return when {
                value < 88 -> result(
                    HealthStatus.CRITICAL,
                    title = "SpO₂ düşük (KOAH bağlamında ciddi olabilir)",
                    description = "%$value.",
                    recommendation = if (hasBreathingDifficulty) "112 veya acil değerlendirme düşünün." else "Kısa sürede sağlık profesyoneline ulaşın; tekrar ölçün.",
                    emoji = "🚨",
                    sources = sources + ClinicalSourceRegistry.WHO_OXYGEN_TRAINING,
                    quality = quality,
                )
                value in 88..92 -> result(
                    HealthStatus.NORMAL,
                    title = "KOAH için sık hedeflenen bant ile uyumlu olabilir",
                    description = "%$value. Birçok KOAH hastasında hedef bant kişisel olarak 88–92% aralığında olabilir (BTS çerçevesi).",
                    recommendation = "Kişisel hedefinizi doktorunuzla netleştirin; düşüş trendi varsa takip artırın.",
                    emoji = "💚",
                    sources = sources,
                    quality = quality,
                )
                value in 93..94 -> result(
                    HealthStatus.ATTENTION,
                    title = "SpO₂: sınır üstü",
                    description = "%$value.",
                    recommendation = "Tekrar ölçüm; nefes darlığı artarsa değerlendirme.",
                    emoji = "🟡",
                    sources = sources,
                    quality = quality,
                )
                else -> result(
                    HealthStatus.NORMAL,
                    title = "SpO₂: iyi",
                    description = "%$value.",
                    recommendation = "KOAH takibinizi sürdürün.",
                    emoji = "✅",
                    sources = sources,
                    quality = quality,
                )
            }
        }

        return when {
            value >= 95 -> result(
                HealthStatus.NORMAL,
                title = "SpO₂: genellikle normal bant",
                description = "%$value.",
                recommendation = "Semptomsuz ve stabil ise takip yeterli olabilir.",
                emoji = "💚",
                sources = sources,
                quality = quality,
            )
            value in 93..94 -> result(
                HealthStatus.ATTENTION,
                title = "SpO₂: sınır düşük",
                description = "%$value.",
                recommendation = "Parmak ısındıktan sonra tekrar ölçün; semptom varsa değerlendirme.",
                emoji = "🟡",
                sources = sources,
                quality = quality,
            )
            value in 90..92 -> result(
                HealthStatus.HIGH,
                title = "SpO₂ düşük — sağlık profesyoneli ile temas önerilir",
                description = "%$value.",
                recommendation = if (hasBreathingDifficulty) "Nefes darlığı ile birlikteyse 112 düşünün." else "Kısa sürede değerlendirme planlayın.",
                emoji = "🔴",
                sources = sources,
                quality = quality,
            )
            else -> result(
                HealthStatus.CRITICAL,
                title = "SpO₂ ciddi düşük",
                description = "%$value.",
                recommendation = "112 veya acil değerlendirme; oksijen ihtiyacı değerlendirilir.",
                emoji = "🚨",
                sources = sources,
                quality = quality,
            )
        }
    }

    // --- Geçmiş analizleri ---

    private fun calculateTrend(values: List<Float>): String {
        if (values.size < 2) return "Stabil"
        val firstHalf = values.take(values.size / 2).average()
        val secondHalf = values.takeLast(values.size / 2).average()
        val threshold = ((values.maxOrNull() ?: 0f) * 0.05f).coerceAtLeast(1f)
        val change = secondHalf - firstHalf
        return when {
            change > threshold -> "Artıyor"
            change < -threshold -> "Azalıyor"
            else -> "Stabil"
        }
    }

    fun analyzeBloodPressureHistory(records: List<BloodPressureRecord>, user: User): AggregateAnalysisResult? {
        if (records.isEmpty()) return null
        val sorted = records.sortedBy { it.timestamp }
        val avgSys = sorted.map { it.systolic.toFloat() }.average().toFloat()
        val avgDia = sorted.map { it.diastolic.toFloat() }.average().toFloat()

        val peakRecord = sorted.maxByOrNull { it.systolic + it.diastolic }!!
        val troughRecord = sorted.minByOrNull { it.systolic + it.diastolic }!!

        var okCount = 0
        sorted.forEach { r ->
            val res = analyzeBloodPressure(r.systolic, r.diastolic, user.hasHypertension, r.context)
            if (res.status == HealthStatus.NORMAL || res.status == HealthStatus.ATTENTION) okCount++
        }
        val compliance = okCount * 100f / sorted.size

        val trendSys = calculateTrend(sorted.map { it.systolic.toFloat() })
        val trendDia = calculateTrend(sorted.map { it.diastolic.toFloat() })
        val trendLabel = if (trendSys == trendDia) trendSys else "$trendSys (S) / $trendDia (D)"

        val allOffice = sorted.all { isOfficeContext(it.context) }
        val avgResult = analyzeBloodPressure(
            avgSys.roundToInt(),
            avgDia.roundToInt(),
            user.hasHypertension,
            context = if (allOffice) "Klinik/Ofis" else "İstirahat",
        )

        val recs = mutableListOf<String>()
        recs.add(
            "Ortalama (tüm kayıtlar): ${avgSys.roundToInt()}/${avgDia.roundToInt()} mmHg — tek başına tanı değildir." +
                if (!allOffice && sorted.any { isOfficeContext(it.context) }) " (Karışık ofis/ev kayıtları: yorum ev ölçümü varsayımına yakınsar; ofis ölçümleri ağırlıksız.)" else "",
        )
        if (compliance < 50f) recs.add("Kayıtların yarısından fazlası yüksek/ kritik bantlarda; ölçüm tekniği ve planlı kontrol önemlidir.")
        if (trendSys == "Artıyor" || trendDia == "Artıyor") recs.add("Dönem içinde yükseliş eğilimi gözleniyor.")
        if (user.hasHypertension) recs.add("Hipertansiyon öyküsü: hedefler kişiseldir; doktorunuzla görüşün.")

        return AggregateAnalysisResult(
            overallStatus = avgResult.status,
            personalizedMessage = "${sorted.size} KB kaydı; ortalama sistolik/diyastolik birlikte değerlendirildi.",
            complianceRate = compliance,
            peakValue = peakRecord.systolic.toFloat(),
            peakDate = peakRecord.timestamp,
            troughValue = troughRecord.systolic.toFloat(),
            troughDate = troughRecord.timestamp,
            averageValue = avgSys,
            averageSecondaryValue = avgDia,
            weeklyTrend = trendLabel,
            recommendations = recs,
            sourceRefs = avgResult.sourceRefs.distinct(),
        )
    }

    fun analyzeGlucoseHistory(records: List<GlucoseRecord>, user: User): AggregateAnalysisResult? {
        if (records.isEmpty()) return null
        val sorted = records.sortedBy { it.timestamp }

        fun bucket(ctx: String) = sorted.filter { it.context == ctx }
        val fasting = bucket("Açlık")
        val post = bucket("Tokluk")
        val ogtt = bucket("OGTT")
        val random = sorted.filter { it.context !in listOf("Açlık", "Tokluk", "OGTT") }

        data class Sub(val label: String, val list: List<GlucoseRecord>)
        val subs = listOf(
            Sub("Açlık", fasting),
            Sub("Tokluk", post),
            Sub("OGTT", ogtt),
            Sub("Rastgele/Diğer", random),
        ).filter { it.list.isNotEmpty() }

        val subMessages = subs.map { sub ->
            val avg = sub.list.map { it.value.toFloat() }.average().toFloat()
            val r = analyzeGlucose(avg.roundToInt(), context = when (sub.label) {
                "Açlık" -> "Açlık"
                "Tokluk" -> "Tokluk"
                "OGTT" -> "OGTT"
                else -> "Rastgele"
            }, hasDiabetes = user.hasDiabetes, measurementMethod = sub.list.last().method)
            "${sub.label}: ort=${avg.roundToInt()} mg/dL (n=${sub.list.size}) → ${r.title}"
        }

        val worst = subs.maxByOrNull { sub ->
            val avg = sub.list.map { it.value.toFloat() }.average().toFloat()
            analyzeGlucose(
                avg.roundToInt(),
                when (sub.label) {
                    "Açlık" -> "Açlık"
                    "Tokluk" -> "Tokluk"
                    "OGTT" -> "OGTT"
                    else -> "Rastgele"
                },
                user.hasDiabetes,
                sub.list.last().method,
            ).status.ordinal
        }!!

        val worstAvg = worst.list.map { it.value.toFloat() }.average().toFloat()
        val worstStatus = analyzeGlucose(
            worstAvg.roundToInt(),
            when (worst.label) {
                "Açlık" -> "Açlık"
                "Tokluk" -> "Tokluk"
                "OGTT" -> "OGTT"
                else -> "Rastgele"
            },
            user.hasDiabetes,
            worst.list.last().method,
        ).status

        val values = sorted.map { it.value.toFloat() }
        val peak = sorted.maxByOrNull { it.value }!!
        val trough = sorted.minByOrNull { it.value }!!
        var ok = 0
        sorted.forEach {
            val res = analyzeGlucose(it.value, it.context, user.hasDiabetes, it.method)
            if (res.status == HealthStatus.NORMAL) ok++
        }
        val compliance = ok * 100f / sorted.size
        val trend = calculateTrend(values)

        val recs = mutableListOf<String>()
        recs.addAll(subMessages)
        if (trough.value < 70) recs.add("Dönem içinde hipoglisemi düşüklüğü gözlenmiştir.")
        if (trend == "Artıyor") recs.add("Genel trend yükseliş yönündedir (bağlam karışımları dahil).")

        return AggregateAnalysisResult(
            overallStatus = worstStatus,
            personalizedMessage = "${sorted.size} glukoz kaydı; bağlamlar ayrık ortalamalarla özetlendi.",
            complianceRate = compliance,
            peakValue = peak.value.toFloat(),
            peakDate = peak.timestamp,
            troughValue = trough.value.toFloat(),
            troughDate = trough.timestamp,
            averageValue = values.average().toFloat(),
            averageSecondaryValue = null,
            weeklyTrend = trend,
            recommendations = recs,
            sourceRefs = listOf(ClinicalSourceRegistry.TEMD_2026_DIABETES, ClinicalSourceRegistry.ADA_2026_DIABETES),
        )
    }

    fun analyzeHeartRateHistory(records: List<HeartRateRecord>, user: User): AggregateAnalysisResult? {
        if (records.isEmpty()) return null
        val sorted = records.sortedBy { it.timestamp }
        fun bucket(ctx: String) = sorted.filter { it.context == ctx }
        val rest = bucket("İstirahat") + bucket("Stres")
        val sleep = bucket("Uyku")
        val ex = bucket("Egzersiz")

        fun avgOf(list: List<HeartRateRecord>) = if (list.isEmpty()) null else list.map { it.value.toFloat() }.average().toFloat()

        val msgs = mutableListOf<String>()
        avgOf(rest)?.let { a ->
            val r = analyzeHeartRate(a.roundToInt(), user.age, "İstirahat", false, false, user.hasHeartFailure)
            msgs.add("İstirahat/Stres ortalaması: ${a.roundToInt()} bpm — ${r.title}")
        }
        avgOf(sleep)?.let { a ->
            val r = analyzeHeartRate(a.roundToInt(), user.age, "Uyku", false, false, false)
            msgs.add("Uyku ortalaması: ${a.roundToInt()} bpm — ${r.title}")
        }
        avgOf(ex)?.let { a ->
            val r = analyzeHeartRate(a.roundToInt(), user.age, "Egzersiz", false, false, false)
            msgs.add("Egzersiz ortalaması: ${a.roundToInt()} bpm — ${r.title}")
        }

        val values = sorted.map { it.value.toFloat() }
        val peak = sorted.maxByOrNull { it.value }!!
        val trough = sorted.minByOrNull { it.value }!!

        var ok = 0
        sorted.forEach {
            val r = analyzeHeartRate(it.value, user.age, it.context, it.hasFever, it.hasPalpitations, user.hasHeartFailure)
            if (r.status == HealthStatus.NORMAL || r.status == HealthStatus.ATTENTION) ok++
        }
        val compliance = ok * 100f / sorted.size
        val trend = calculateTrend(values)

        val overall = sorted
            .map { analyzeHeartRate(it.value, user.age, it.context, it.hasFever, it.hasPalpitations, user.hasHeartFailure).status }
            .maxBy { it.ordinal }

        return AggregateAnalysisResult(
            overallStatus = overall,
            personalizedMessage = "${sorted.size} nabız kaydı; bağlam bazlı özet üretildi.",
            complianceRate = compliance,
            peakValue = peak.value.toFloat(),
            peakDate = peak.timestamp,
            troughValue = trough.value.toFloat(),
            troughDate = trough.timestamp,
            averageValue = values.average().toFloat(),
            averageSecondaryValue = null,
            weeklyTrend = trend,
            recommendations = msgs.ifEmpty { listOf("Bağlam bilgisi ile nabız kayıtlarınızı sürdürün.") },
            sourceRefs = listOf(ClinicalSourceRegistry.AHA_RESTING_HR, ClinicalSourceRegistry.AHA_EXERCISE_HR),
        )
    }

    fun analyzeOxygenHistory(records: List<OxygenRecord>, user: User): AggregateAnalysisResult? {
        if (records.isEmpty()) return null
        val sorted = records.sortedBy { it.timestamp }
        val values = sorted.map { it.value.toFloat() }
        val avg = values.average().toFloat()
        val minRec = sorted.minByOrNull { it.value }!!
        val maxRec = sorted.maxByOrNull { it.value }!!

        var ok = 0
        sorted.forEach {
            val r = analyzeOxygen(it.value, it.hasBreathingDifficulty, user.hasCOPD, it.altitude)
            if (r.status == HealthStatus.NORMAL || r.status == HealthStatus.ATTENTION) ok++
        }
        val compliance = ok * 100f / sorted.size
        val trend = calculateTrend(values)

        val overall = sorted
            .map { analyzeOxygen(it.value, it.hasBreathingDifficulty, user.hasCOPD, it.altitude).status }
            .maxBy { it.ordinal }

        val recs = mutableListOf<String>()
        recs.add(
            "Ortalama SpO₂: ${avg.roundToInt()}%; en düşük: ${minRec.value}% (${
            SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr", "TR")).format(Date(minRec.timestamp))
            }).",
        )
        if (sorted.any { it.hasBreathingDifficulty }) recs.add("Bazı kayıtlarda nefes darlığı işaretlendi; klinik değerlendirme önemlidir.")
        if (trend == "Azalıyor") recs.add("Dönem içinde düşüş eğilimi gözleniyor.")

        return AggregateAnalysisResult(
            overallStatus = overall,
            personalizedMessage = "${sorted.size} SpO₂ kaydı analiz edildi.",
            complianceRate = compliance,
            peakValue = maxRec.value.toFloat(),
            peakDate = maxRec.timestamp,
            troughValue = minRec.value.toFloat(),
            troughDate = minRec.timestamp,
            averageValue = avg,
            averageSecondaryValue = null,
            weeklyTrend = trend,
            recommendations = recs,
            sourceRefs = listOf(ClinicalSourceRegistry.BTS_OXYGEN_TARGETS, ClinicalSourceRegistry.FDA_PULSE_OX_LIMITATIONS),
        )
    }
}
