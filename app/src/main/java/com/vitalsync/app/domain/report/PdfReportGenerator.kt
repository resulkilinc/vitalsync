package com.vitalsync.app.domain.report

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.vitalsync.app.domain.engine.HealthAnalysisEngine
import com.vitalsync.app.domain.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android PdfDocument API kullanarak profesyonel sağlık raporu oluşturur.
 * Harici kütüphane bağımlılığı yoktur.
 */
class PdfReportGenerator {

    companion object {
        private const val PAGE_WIDTH = 595   // A4 genişlik (pt)
        private const val PAGE_HEIGHT = 842  // A4 yükseklik (pt)
        private const val MARGIN = 40f
        private const val LINE_HEIGHT = 18f
    }

    data class ReportData(
        val user: User,
        val bpRecords: List<BloodPressureRecord>,
        val glucoseRecords: List<GlucoseRecord>,
        val hrRecords: List<HeartRateRecord>,
        val o2Records: List<OxygenRecord>
    )

    /**
     * PDF dosyası oluşturur ve dosya yolunu döner.
     */
    fun generateReport(context: Context, reportData: ReportData): File {
        val document = PdfDocument()
        val sdf = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("tr", "TR"))
        val dateSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("tr", "TR"))
        val now = sdf.format(Date())

        // Sayfa 1: Başlık + Kişisel Bilgiler + Özet
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(pageInfo1)
        val canvas1 = page1.canvas
        var y = drawPage1(canvas1, reportData, now, dateSdf)
        document.finishPage(page1)

        // Sayfa 2: Detaylı Ölçüm Geçmişi
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = document.startPage(pageInfo2)
        drawPage2(page2.canvas, reportData, dateSdf)
        document.finishPage(page2)

        // Dosyayı kaydet
        val fileName = "VitalÖlçüm_Rapor_${System.currentTimeMillis()}.pdf"
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: context.filesDir
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return file
    }

    private fun drawPage1(
        canvas: Canvas,
        data: ReportData,
        dateStr: String,
        dateSdf: SimpleDateFormat
    ): Float {
        var y = MARGIN

        // === BAŞLIK ===
        val titlePaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("VitalÖlçüm Sağlık Raporu", MARGIN, y + 24f, titlePaint)
        y += 36f

        // Alt çizgi
        val linePaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            strokeWidth = 2f
        }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 16f

        // Tarih
        val smallPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }
        canvas.drawText("Oluşturulma: $dateStr", MARGIN, y, smallPaint)
        y += 10f
        canvas.drawText("⚕️ Bu rapor yalnızca bilgi amaçlıdır. Tıbbi tanı yerine geçmez.", MARGIN, y, smallPaint)
        y += 24f

        // === KİŞİSEL BİLGİLER ===
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 11f
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Hasta Bilgileri", MARGIN, y, sectionPaint)
        y += 6f
        val thinLine = Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 1f }
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, thinLine)
        y += LINE_HEIGHT

        val user = data.user
        val infoItems = listOf(
            "Ad Soyad" to user.fullName,
            "Yaş" to "${user.age}",
            "Cinsiyet" to user.gender,
            "Boy / Kilo" to "${user.heightCm.toInt()} cm / ${user.weightKg.toInt()} kg",
            "BMI" to String.format("%.1f (%s)", user.bmi, user.bmiCategory),
            "Kan Grubu" to user.bloodTypeDisplay,
            "Aktivite" to user.activityLevel
        )

        val colWidth = (PAGE_WIDTH - MARGIN * 2) / 2f
        infoItems.forEachIndexed { idx, (label, value) ->
            val x = if (idx % 2 == 0) MARGIN else MARGIN + colWidth
            canvas.drawText("$label:", x, y, boldBodyPaint)
            canvas.drawText(value, x + 90f, y, bodyPaint)
            if (idx % 2 != 0) y += LINE_HEIGHT
        }
        if (infoItems.size % 2 != 0) y += LINE_HEIGHT
        y += 8f

        // Kronik hastalıklar
        val conditions = buildList {
            if (user.hasDiabetes) add("Diyabet")
            if (user.hasHypertension) add("Hipertansiyon")
            if (user.hasCOPD) add("KOAH")
            if (user.hasHeartFailure) add("Kalp Yetmezliği")
            if (user.hasKidneyDisease) add("Böbrek Hastalığı")
            if (user.isSmoker) add("Sigara Kullanımı")
        }
        canvas.drawText("Kronik Hastalıklar:", MARGIN, y, boldBodyPaint)
        canvas.drawText(
            if (conditions.isEmpty()) "Bilinen hastalık yok" else conditions.joinToString(", "),
            MARGIN + 110f, y, bodyPaint
        )
        y += LINE_HEIGHT * 2

        // === SON ÖLÇÜM ÖZETİ ===
        canvas.drawText("Son Ölçüm Değerleri ve Analizler", MARGIN, y, sectionPaint)
        y += 6f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, thinLine)
        y += LINE_HEIGHT

        // Kan Basıncı
        val latestBp = data.bpRecords.lastOrNull()
        y = drawVitalSummary(canvas, y, "🩺 Kan Basıncı", latestBp?.let {
            val analysis = HealthAnalysisEngine.analyzeBloodPressure(
                it.systolic, it.diastolic, user.hasHypertension, it.context
            )
            Triple("${it.systolic}/${it.diastolic} mmHg", analysis.statusText, analysis.recommendation)
        }, dateSdf, latestBp?.timestamp, bodyPaint, boldBodyPaint)

        // Kan Şekeri
        val latestGlucose = data.glucoseRecords.lastOrNull()
        y = drawVitalSummary(canvas, y, "🩸 Kan Şekeri", latestGlucose?.let {
            val analysis = HealthAnalysisEngine.analyzeGlucose(
                it.value,
                it.context,
                user.hasDiabetes,
                measurementMethod = it.method,
            )
            Triple("${it.value} mg/dL (${it.context})", analysis.statusText, analysis.recommendation)
        }, dateSdf, latestGlucose?.timestamp, bodyPaint, boldBodyPaint)

        // Nabız
        val latestHr = data.hrRecords.lastOrNull()
        y = drawVitalSummary(canvas, y, "❤️ Nabız", latestHr?.let {
            val analysis = HealthAnalysisEngine.analyzeHeartRate(
                it.value, user.age, it.context, it.hasFever, it.hasPalpitations, user.hasHeartFailure
            )
            Triple("${it.value} bpm", analysis.statusText, analysis.recommendation)
        }, dateSdf, latestHr?.timestamp, bodyPaint, boldBodyPaint)

        // Oksijen
        val latestO2 = data.o2Records.lastOrNull()
        y = drawVitalSummary(canvas, y, "🫁 Oksijen (SpO₂)", latestO2?.let {
            val analysis = HealthAnalysisEngine.analyzeOxygen(
                it.value, it.hasBreathingDifficulty, user.hasCOPD, it.altitude
            )
            Triple("%${it.value}", analysis.statusText, analysis.recommendation)
        }, dateSdf, latestO2?.timestamp, bodyPaint, boldBodyPaint)

        return y
    }

    private fun drawVitalSummary(
        canvas: Canvas,
        startY: Float,
        title: String,
        data: Triple<String, String, String>?,
        dateSdf: SimpleDateFormat,
        timestamp: Long?,
        bodyPaint: Paint,
        boldPaint: Paint
    ): Float {
        var y = startY

        canvas.drawText(title, MARGIN, y, boldPaint)
        y += LINE_HEIGHT

        if (data != null && timestamp != null) {
            val (value, status, recommendation) = data
            canvas.drawText("  Değer: $value  |  Durum: $status", MARGIN + 8f, y, bodyPaint)
            y += LINE_HEIGHT
            canvas.drawText("  Tarih: ${dateSdf.format(Date(timestamp))}", MARGIN + 8f, y, bodyPaint)
            y += LINE_HEIGHT

            // Uzun önerileri satıra böl
            val recLines = wrapText("  Öneri: $recommendation", bodyPaint, PAGE_WIDTH - MARGIN * 2 - 16f)
            recLines.forEach { line ->
                canvas.drawText(line, MARGIN + 8f, y, bodyPaint)
                y += LINE_HEIGHT - 4f
            }
        } else {
            canvas.drawText("  Henüz ölçüm kaydedilmedi.", MARGIN + 8f, y, bodyPaint)
            y += LINE_HEIGHT
        }

        y += 8f
        return y
    }

    private fun drawPage2(
        canvas: Canvas,
        data: ReportData,
        dateSdf: SimpleDateFormat
    ) {
        var y = MARGIN

        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 10f
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val thinLine = Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 0.5f }

        canvas.drawText("Ölçüm Geçmişi (Son 30 Gün)", MARGIN, y + 16f, sectionPaint)
        y += 28f

        // BP Tablosu
        y = drawTable(
            canvas, y, "Kan Basıncı",
            listOf("Tarih", "Sistolik", "Diastolik", "Nabız", "Durum"),
            data.bpRecords.takeLast(10).map { rec ->
                val analysis = HealthAnalysisEngine.analyzeBloodPressure(
                    rec.systolic, rec.diastolic, data.user.hasHypertension, rec.context
                )
                listOf(
                    dateSdf.format(Date(rec.timestamp)),
                    "${rec.systolic}",
                    "${rec.diastolic}",
                    rec.pulse?.toString() ?: "-",
                    analysis.statusText
                )
            },
            sectionPaint, headerPaint, bodyPaint, thinLine
        )

        y += 12f

        // Glucose Tablosu
        y = drawTable(
            canvas, y, "Kan Şekeri",
            listOf("Tarih", "Değer (mg/dL)", "Bağlam", "Yöntem", "Durum"),
            data.glucoseRecords.takeLast(10).map { rec ->
                val analysis = HealthAnalysisEngine.analyzeGlucose(
                    rec.value,
                    rec.context,
                    data.user.hasDiabetes,
                    measurementMethod = rec.method,
                )
                listOf(
                    dateSdf.format(Date(rec.timestamp)),
                    "${rec.value}",
                    rec.context,
                    rec.method,
                    analysis.statusText
                )
            },
            sectionPaint, headerPaint, bodyPaint, thinLine
        )

        y += 12f

        // HR Tablosu
        y = drawTable(
            canvas, y, "Nabız",
            listOf("Tarih", "Değer (bpm)", "Bağlam", "Durum"),
            data.hrRecords.takeLast(8).map { rec ->
                val analysis = HealthAnalysisEngine.analyzeHeartRate(
                    rec.value, data.user.age, rec.context, rec.hasFever, rec.hasPalpitations, data.user.hasHeartFailure
                )
                listOf(
                    dateSdf.format(Date(rec.timestamp)),
                    "${rec.value}",
                    rec.context,
                    analysis.statusText
                )
            },
            sectionPaint, headerPaint, bodyPaint, thinLine
        )

        y += 12f

        // O2 Tablosu
        y = drawTable(
            canvas, y, "Oksijen (SpO₂)",
            listOf("Tarih", "Değer (%)", "Nefes Darlığı", "Durum"),
            data.o2Records.takeLast(8).map { rec ->
                val analysis = HealthAnalysisEngine.analyzeOxygen(
                    rec.value, rec.hasBreathingDifficulty, data.user.hasCOPD, rec.altitude
                )
                listOf(
                    dateSdf.format(Date(rec.timestamp)),
                    "${rec.value}",
                    if (rec.hasBreathingDifficulty) "Evet" else "Hayır",
                    analysis.statusText
                )
            },
            sectionPaint, headerPaint, bodyPaint, thinLine
        )

        // Alt bilgi
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText(
            "VitalÖlçüm Sağlık Raporu · Bu rapor otomatik oluşturulmuştur · Tıbbi tanı yerine geçmez",
            MARGIN, PAGE_HEIGHT - 20f, footerPaint
        )
    }

    private fun drawTable(
        canvas: Canvas,
        startY: Float,
        title: String,
        headers: List<String>,
        rows: List<List<String>>,
        sectionPaint: Paint,
        headerPaint: Paint,
        bodyPaint: Paint,
        linePaint: Paint
    ): Float {
        var y = startY

        // Başlık
        val subTitlePaint = Paint().apply {
            color = Color.parseColor("#2563EB")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(title, MARGIN, y, subTitlePaint)
        y += 4f

        if (rows.isEmpty()) {
            canvas.drawText("  Veri yok", MARGIN, y + LINE_HEIGHT - 4f, bodyPaint)
            return y + LINE_HEIGHT + 4f
        }

        val colCount = headers.size
        val colWidth = (PAGE_WIDTH - MARGIN * 2) / colCount

        // Header satırı
        y += LINE_HEIGHT - 2f
        headers.forEachIndexed { idx, header ->
            canvas.drawText(header, MARGIN + colWidth * idx, y, headerPaint)
        }
        y += 4f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += LINE_HEIGHT - 4f

        // Veri satırları
        rows.forEach { row ->
            if (y > PAGE_HEIGHT - 60f) return y // Sayfa taşması kontrolü
            row.forEachIndexed { idx, cell ->
                canvas.drawText(cell, MARGIN + colWidth * idx, y, bodyPaint)
            }
            y += LINE_HEIGHT - 4f
        }

        return y
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)

        return if (lines.isEmpty()) listOf(text) else lines
    }
}
