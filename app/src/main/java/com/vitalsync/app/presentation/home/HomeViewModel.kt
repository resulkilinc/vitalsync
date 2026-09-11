package com.vitalsync.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.domain.engine.HealthAnalysisEngine
import com.vitalsync.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VitalSummary(
    val type: VitalType,
    val record: Any? = null, // BloodPressureRecord, GlucoseRecord, vb.
    val analysis: AnalysisResult? = null
)

enum class VitalType(val title: String, val emoji: String, val unit: String) {
    BLOOD_PRESSURE("Kan Basıncı", "🩺", "mmHg"),
    GLUCOSE("Kan Şekeri", "🩸", "mg/dL"),
    HEART_RATE("Nabız", "❤️", "bpm"),
    OXYGEN("Oksijen", "🫁", "SpO₂ %")
}

data class HomeState(
    val userName: String = "",
    val userId: Long = -1,
    val vitals: List<VitalSummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vitalsRepository: VitalsRepository
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeState())
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    fun loadDashboard(userId: Long) {
        _homeState.update { it.copy(userId = userId, isLoading = true) }

        viewModelScope.launch {
            // Kullanıcı bilgisi
            val user = userRepository.getUserById(userId)
            _homeState.update { it.copy(userName = user?.firstName ?: "") }

            // Son vital sign değerleri
            val bpRecord = vitalsRepository.getLatestBloodPressure(userId)
            val glucoseRecord = vitalsRepository.getLatestGlucose(userId)
            val hrRecord = vitalsRepository.getLatestHeartRate(userId)
            val o2Record = vitalsRepository.getLatestOxygen(userId)

            // Analizler
            val bpAnalysis = bpRecord?.let {
                HealthAnalysisEngine.analyzeBloodPressure(
                    it.systolic, it.diastolic,
                    hasHypertension = user?.hasHypertension ?: false,
                    context = it.context
                )
            }

            val glucoseAnalysis = glucoseRecord?.let {
                HealthAnalysisEngine.analyzeGlucose(
                    it.value,
                    it.context,
                    hasDiabetes = user?.hasDiabetes ?: false,
                    measurementMethod = it.method,
                )
            }

            val hrAnalysis = hrRecord?.let {
                HealthAnalysisEngine.analyzeHeartRate(
                    it.value, user?.age ?: 30,
                    context = it.context,
                    hasFever = it.hasFever,
                    hasPalpitations = it.hasPalpitations,
                    hasHeartFailure = user?.hasHeartFailure ?: false
                )
            }

            val o2Analysis = o2Record?.let {
                HealthAnalysisEngine.analyzeOxygen(
                    it.value,
                    hasBreathingDifficulty = it.hasBreathingDifficulty,
                    hasCOPD = user?.hasCOPD ?: false,
                    altitude = it.altitude
                )
            }

            val vitals = listOf(
                VitalSummary(VitalType.BLOOD_PRESSURE, bpRecord, bpAnalysis),
                VitalSummary(VitalType.GLUCOSE, glucoseRecord, glucoseAnalysis),
                VitalSummary(VitalType.HEART_RATE, hrRecord, hrAnalysis),
                VitalSummary(VitalType.OXYGEN, o2Record, o2Analysis)
            )

            _homeState.update {
                it.copy(vitals = vitals, isLoading = false)
            }
        }
    }

    fun refreshDashboard() {
        val userId = _homeState.value.userId
        if (userId > 0) loadDashboard(userId)
    }
}
