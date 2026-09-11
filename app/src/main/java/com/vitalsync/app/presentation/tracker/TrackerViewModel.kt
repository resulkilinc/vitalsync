package com.vitalsync.app.presentation.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.domain.engine.HealthAnalysisEngine
import com.vitalsync.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// == Ortak Tracker State ==
data class TrackerState(
    // Kan Basıncı
    val bpSystolic: String = "",
    val bpDiastolic: String = "",
    val bpPulse: String = "",
    val bpArm: String = "Sol",
    val bpContext: String = "İstirahat",
    val bpNotes: String = "",

    // Kan Şekeri
    val glucoseValue: String = "",
    val glucoseContext: String = "Açlık",
    val glucoseMethod: String = "Parmak Ucu",
    val glucoseNotes: String = "",

    // Nabız
    val hrValue: String = "",
    val hrContext: String = "İstirahat",
    val hrHasFever: Boolean = false,
    val hrHasPalpitations: Boolean = false,
    val hrNotes: String = "",

    // Oksijen
    val o2Value: String = "",
    val o2HasBreathingDifficulty: Boolean = false,
    val o2HasPalpitations: Boolean = false,
    val o2Altitude: String = "",
    val o2Notes: String = "",

    // Genel
    val isSaving: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val showResult: Boolean = false
)

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val vitalsRepository: VitalsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerState())
    val state: StateFlow<TrackerState> = _state.asStateFlow()

    private var currentUserId: Long = -1
    private var userAge: Int = 30
    private var userHasHypertension: Boolean = false
    private var userHasDiabetes: Boolean = false
    private var userHasHeartFailure: Boolean = false
    private var userHasCOPD: Boolean = false

    fun setUserId(userId: Long) {
        currentUserId = userId
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            user?.let {
                userAge = it.age
                userHasHypertension = it.hasHypertension
                userHasDiabetes = it.hasDiabetes
                userHasHeartFailure = it.hasHeartFailure
                userHasCOPD = it.hasCOPD
            }
        }
    }

    // === KAN BASINCI ===
    fun updateBpSystolic(v: String) { _state.update { it.copy(bpSystolic = v.filter { c -> c.isDigit() }) } }
    fun updateBpDiastolic(v: String) { _state.update { it.copy(bpDiastolic = v.filter { c -> c.isDigit() }) } }
    fun updateBpPulse(v: String) { _state.update { it.copy(bpPulse = v.filter { c -> c.isDigit() }) } }
    fun updateBpArm(v: String) { _state.update { it.copy(bpArm = v) } }
    fun updateBpContext(v: String) { _state.update { it.copy(bpContext = v) } }
    fun updateBpNotes(v: String) { _state.update { it.copy(bpNotes = v) } }

    val isBpValid: Boolean get() {
        val s = _state.value.bpSystolic.toIntOrNull() ?: return false
        val d = _state.value.bpDiastolic.toIntOrNull() ?: return false
        return s in 50..300 && d in 20..200 && s > d
    }

    fun saveBloodPressure() {
        if (!isBpValid) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val s = _state.value
            vitalsRepository.insertBloodPressure(
                userId = currentUserId,
                systolic = s.bpSystolic.toInt(),
                diastolic = s.bpDiastolic.toInt(),
                pulse = s.bpPulse.toIntOrNull(),
                arm = s.bpArm,
                context = s.bpContext,
                notes = s.bpNotes.ifBlank { null }
            )

            val analysis = HealthAnalysisEngine.analyzeBloodPressure(
                s.bpSystolic.toInt(), s.bpDiastolic.toInt(),
                hasHypertension = userHasHypertension,
                context = s.bpContext
            )
            _state.update { it.copy(isSaving = false, analysisResult = analysis, showResult = true) }
        }
    }

    // === KAN ŞEKERİ ===
    fun updateGlucoseValue(v: String) { _state.update { it.copy(glucoseValue = v.filter { c -> c.isDigit() }) } }
    fun updateGlucoseContext(v: String) { _state.update { it.copy(glucoseContext = v) } }
    fun updateGlucoseMethod(v: String) { _state.update { it.copy(glucoseMethod = v) } }
    fun updateGlucoseNotes(v: String) { _state.update { it.copy(glucoseNotes = v) } }

    val isGlucoseValid: Boolean get() {
        val v = _state.value.glucoseValue.toIntOrNull() ?: return false
        return v in 20..600
    }

    fun saveGlucose() {
        if (!isGlucoseValid) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val s = _state.value
            vitalsRepository.insertGlucose(
                userId = currentUserId,
                value = s.glucoseValue.toInt(),
                context = s.glucoseContext,
                method = s.glucoseMethod,
                notes = s.glucoseNotes.ifBlank { null }
            )

            val analysis = HealthAnalysisEngine.analyzeGlucose(
                s.glucoseValue.toInt(),
                s.glucoseContext,
                hasDiabetes = userHasDiabetes,
                measurementMethod = s.glucoseMethod,
            )
            _state.update { it.copy(isSaving = false, analysisResult = analysis, showResult = true) }
        }
    }

    // === NABIZ ===
    fun updateHrValue(v: String) { _state.update { it.copy(hrValue = v.filter { c -> c.isDigit() }) } }
    fun updateHrContext(v: String) { _state.update { it.copy(hrContext = v) } }
    fun updateHrFever(v: Boolean) { _state.update { it.copy(hrHasFever = v) } }
    fun updateHrPalpitations(v: Boolean) { _state.update { it.copy(hrHasPalpitations = v) } }
    fun updateHrNotes(v: String) { _state.update { it.copy(hrNotes = v) } }

    val isHrValid: Boolean get() {
        val v = _state.value.hrValue.toIntOrNull() ?: return false
        return v in 20..300
    }

    fun saveHeartRate() {
        if (!isHrValid) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val s = _state.value
            vitalsRepository.insertHeartRate(
                userId = currentUserId,
                value = s.hrValue.toInt(),
                context = s.hrContext,
                hasFever = s.hrHasFever,
                hasPalpitations = s.hrHasPalpitations,
                notes = s.hrNotes.ifBlank { null }
            )

            val analysis = HealthAnalysisEngine.analyzeHeartRate(
                s.hrValue.toInt(), userAge,
                context = s.hrContext,
                hasFever = s.hrHasFever,
                hasPalpitations = s.hrHasPalpitations,
                hasHeartFailure = userHasHeartFailure
            )
            _state.update { it.copy(isSaving = false, analysisResult = analysis, showResult = true) }
        }
    }

    // === OKSİJEN ===
    fun updateO2Value(v: String) { _state.update { it.copy(o2Value = v.filter { c -> c.isDigit() }) } }
    fun updateO2BreathingDifficulty(v: Boolean) { _state.update { it.copy(o2HasBreathingDifficulty = v) } }
    fun updateO2Palpitations(v: Boolean) { _state.update { it.copy(o2HasPalpitations = v) } }
    fun updateO2Altitude(v: String) { _state.update { it.copy(o2Altitude = v.filter { c -> c.isDigit() }) } }
    fun updateO2Notes(v: String) { _state.update { it.copy(o2Notes = v) } }

    val isO2Valid: Boolean get() {
        val v = _state.value.o2Value.toIntOrNull() ?: return false
        return v in 50..100
    }

    fun saveOxygen() {
        if (!isO2Valid) return
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val s = _state.value
            vitalsRepository.insertOxygen(
                userId = currentUserId,
                value = s.o2Value.toInt(),
                hasBreathingDifficulty = s.o2HasBreathingDifficulty,
                hasPalpitations = s.o2HasPalpitations,
                altitude = s.o2Altitude.toIntOrNull(),
                notes = s.o2Notes.ifBlank { null }
            )

            val analysis = HealthAnalysisEngine.analyzeOxygen(
                s.o2Value.toInt(),
                hasBreathingDifficulty = s.o2HasBreathingDifficulty,
                hasCOPD = userHasCOPD,
                altitude = s.o2Altitude.toIntOrNull()
            )
            _state.update { it.copy(isSaving = false, analysisResult = analysis, showResult = true) }
        }
    }

    // === GENEL ===
    fun dismissResult() {
        _state.update { it.copy(showResult = false, analysisResult = null) }
    }

    fun resetForm() {
        _state.value = TrackerState()
    }
}
