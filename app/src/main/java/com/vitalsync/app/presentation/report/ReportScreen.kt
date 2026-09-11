package com.vitalsync.app.presentation.report

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.domain.report.PdfReportGenerator
import com.vitalsync.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// === ViewModel ===

data class ReportState(
    val isGenerating: Boolean = false,
    val isGenerated: Boolean = false,
    val generatedFile: File? = null,
    val error: String? = null,
    val bpCount: Int = 0,
    val glucoseCount: Int = 0,
    val hrCount: Int = 0,
    val o2Count: Int = 0
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vitalsRepository: VitalsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private var userId: Long = -1

    fun setUserId(id: Long) {
        userId = id
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

            val bp = vitalsRepository.getBloodPressureByDateRange(userId, thirtyDaysAgo, now)
            val glucose = vitalsRepository.getGlucoseByDateRange(userId, thirtyDaysAgo, now)
            val hr = vitalsRepository.getHeartRateByDateRange(userId, thirtyDaysAgo, now)
            val o2 = vitalsRepository.getOxygenByDateRange(userId, thirtyDaysAgo, now)

            _state.update {
                it.copy(
                    bpCount = bp.size,
                    glucoseCount = glucose.size,
                    hrCount = hr.size,
                    o2Count = o2.size
                )
            }
        }
    }

    fun generateReport(context: android.content.Context) {
        _state.update { it.copy(isGenerating = true, error = null) }

        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(userId)
                    ?: throw Exception("Kullanıcı bulunamadı")

                val now = System.currentTimeMillis()
                val thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000)

                val reportData = PdfReportGenerator.ReportData(
                    user = user,
                    bpRecords = vitalsRepository.getBloodPressureByDateRange(userId, thirtyDaysAgo, now),
                    glucoseRecords = vitalsRepository.getGlucoseByDateRange(userId, thirtyDaysAgo, now),
                    hrRecords = vitalsRepository.getHeartRateByDateRange(userId, thirtyDaysAgo, now),
                    o2Records = vitalsRepository.getOxygenByDateRange(userId, thirtyDaysAgo, now)
                )

                val generator = PdfReportGenerator()
                val file = generator.generateReport(context, reportData)

                _state.update {
                    it.copy(
                        isGenerating = false,
                        isGenerated = true,
                        generatedFile = file
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        error = "Rapor oluşturulurken hata: ${e.message}"
                    )
                }
            }
        }
    }

    fun resetState() {
        _state.value = ReportState()
        loadStats()
    }
}

// === Screen ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    reportViewModel: ReportViewModel,
    onBack: () -> Unit
) {
    val state by reportViewModel.state.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
            }
            Text(
                "Sağlık Raporu",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        // Rapor ikonlu başlık kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📋", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "PDF Sağlık Raporu",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Son 30 günlük verilerinizi profesyonel bir PDF raporu olarak oluşturun",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Rapor içeriği özeti
        Text(
            "Rapor İçeriği",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(8.dp))

        ReportContentItem("🩺", "Kan Basıncı", "${state.bpCount} ölçüm")
        ReportContentItem("🩸", "Kan Şekeri", "${state.glucoseCount} ölçüm")
        ReportContentItem("❤️", "Nabız", "${state.hrCount} ölçüm")
        ReportContentItem("🫁", "Oksijen (SpO₂)", "${state.o2Count} ölçüm")

        Spacer(Modifier.height(20.dp))

        // Rapor özellikleri
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📄 Rapor şunları içerir:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(Modifier.height(8.dp))

                val features = listOf(
                    "✅ Kişisel ve fiziksel bilgileriniz",
                    "✅ Kronik hastalık geçmişi",
                    "✅ Son ölçüm değerleri ve klinik analizler",
                    "✅ Detaylı ölçüm geçmişi tabloları",
                    "✅ Uluslararası kılavuzlara dayalı öneriler",
                    "✅ Doktorunuzla paylaşıma uygun A4 format"
                )

                features.forEach { feature ->
                    Text(
                        feature,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Hata mesajı
        if (state.error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusHigh.copy(alpha = 0.1f))
            ) {
                Text(
                    "❌ ${state.error}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusHigh
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // Başarılı rapor
        if (state.isGenerated && state.generatedFile != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StatusNormal.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "✅ Rapor başarıyla oluşturuldu!",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = StatusNormal
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        state.generatedFile!!.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Paylaş butonu
            Button(
                onClick = {
                    shareFile(context, state.generatedFile!!)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("📤  Raporu Paylaş", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(8.dp))

            // Yeni rapor oluştur
            OutlinedButton(
                onClick = { reportViewModel.resetState() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🔄  Yeni Rapor Oluştur")
            }

        } else {
            // Oluştur butonu
            Button(
                onClick = { reportViewModel.generateReport(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !state.isGenerating
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Rapor Oluşturuluyor...")
                } else {
                    Text("📄  PDF Rapor Oluştur", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReportContentItem(emoji: String, title: String, count: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Text(
                count,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun shareFile(context: android.content.Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "VitalÖlçüm Sağlık Raporu")
            putExtra(Intent.EXTRA_TEXT, "VitalÖlçüm uygulaması tarafından oluşturulan sağlık raporumu paylaşıyorum.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Raporu Paylaş"))
    } catch (e: Exception) {
        Toast.makeText(context, "Paylaşma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
