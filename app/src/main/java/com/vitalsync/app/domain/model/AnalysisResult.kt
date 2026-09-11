package com.vitalsync.app.domain.model

import androidx.compose.ui.graphics.Color
import com.vitalsync.app.presentation.theme.*

enum class HealthStatus {
    NORMAL,     // Hedef aralıkta
    ATTENTION,  // Dikkat — sınırda
    HIGH,       // Yüksek
    LOW,        // Düşük
    CRITICAL    // Acil
}

data class AnalysisResult(
    val status: HealthStatus,
    val title: String,
    val description: String,
    val recommendation: String,
    val emoji: String,
    /** Kısa kaynak kodları (ör. ClinicalSourceRegistry.ESC_2024_BP). */
    val sourceRefs: List<String> = emptyList(),
    /** Ölçüm kalitesi / bağlam uyarıları (parmak ucu, rakım vb.). */
    val measurementQualityWarnings: List<String> = emptyList(),
) {
    val color: Color get() = when (status) {
        HealthStatus.NORMAL -> StatusNormal
        HealthStatus.ATTENTION -> StatusAttention
        HealthStatus.HIGH -> StatusHigh
        HealthStatus.LOW -> StatusLow
        HealthStatus.CRITICAL -> StatusCritical
    }

    val statusText: String get() = when (status) {
        HealthStatus.NORMAL -> "Normal"
        HealthStatus.ATTENTION -> "Dikkat"
        HealthStatus.HIGH -> "Yüksek"
        HealthStatus.LOW -> "Düşük"
        HealthStatus.CRITICAL -> "Kritik"
    }
}
