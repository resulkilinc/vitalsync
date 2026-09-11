package com.vitalsync.app.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.domain.engine.HealthAnalysisEngine
import com.vitalsync.app.domain.model.AggregateAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalysisState(
    val isLoading: Boolean = false,
    val result: AggregateAnalysisResult? = null,
    val error: String? = null,
    val metricTitle: String = "",
    /** "BP", "Glucose", "HR", "O2" — özet kartında birim/çift değer gösterimi için. */
    val metricType: String = "",
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vitalsRepository: VitalsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalysisState())
    val state: StateFlow<AnalysisState> = _state.asStateFlow()

    private var currentUserId: Long = -1
    private var currentMetricType: String = ""

    fun initialize(userId: Long, metricType: String) {
        currentUserId = userId
        currentMetricType = metricType
        val title = when (metricType) {
            "BP" -> "Kan Basıncı Analizi"
            "Glucose" -> "Kan Şekeri Analizi"
            "HR" -> "Nabız Analizi"
            "O2" -> "Oksijen (SpO₂) Analizi"
            else -> "Sağlık Analizi"
        }
        _state.update { it.copy(metricTitle = title, metricType = metricType) }
        loadAnalysis(30) // Default 30 gün
    }

    fun loadAnalysis(days: Int) {
        if (currentUserId == -1L) return

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val user = userRepository.getUserById(currentUserId)
                    ?: throw Exception("Kullanıcı bulunamadı")

                val now = System.currentTimeMillis()
                val startDate = now - (days * 24L * 60 * 60 * 1000)

                val analysisResult = when (currentMetricType) {
                    "BP" -> {
                        val records = vitalsRepository.getBloodPressureByDateRange(currentUserId, startDate, now)
                        HealthAnalysisEngine.analyzeBloodPressureHistory(records, user)
                    }
                    "Glucose" -> {
                        val records = vitalsRepository.getGlucoseByDateRange(currentUserId, startDate, now)
                        HealthAnalysisEngine.analyzeGlucoseHistory(records, user)
                    }
                    "HR" -> {
                        val records = vitalsRepository.getHeartRateByDateRange(currentUserId, startDate, now)
                        HealthAnalysisEngine.analyzeHeartRateHistory(records, user)
                    }
                    "O2" -> {
                        val records = vitalsRepository.getOxygenByDateRange(currentUserId, startDate, now)
                        HealthAnalysisEngine.analyzeOxygenHistory(records, user)
                    }
                    else -> null
                }

                if (analysisResult == null) {
                    _state.update { it.copy(isLoading = false, error = "Seçili tarihler arasında yeterli veri bulunamadı.") }
                } else {
                    _state.update { it.copy(isLoading = false, result = analysisResult) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Bilinmeyen bir hata oluştu") }
            }
        }
    }
}
