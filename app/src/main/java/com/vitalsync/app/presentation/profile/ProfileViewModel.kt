package com.vitalsync.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.data.repository.VitalsRepository
import com.vitalsync.app.domain.model.User
import com.vitalsync.app.sync.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val totalMeasurements: Int = 0,
    val memberSinceDays: Int = 0,

    // Düzenleme form alanları
    val editFirstName: String = "",
    val editLastName: String = "",
    val editAge: String = "",
    val editHeight: String = "",
    val editWeight: String = "",
    val editActivityLevel: String = "Belirtilmedi",
    val editIsSmoker: Boolean = false,
    val editHasDiabetes: Boolean = false,
    val editHasHypertension: Boolean = false,
    val editHasCOPD: Boolean = false,
    val editHasHeartFailure: Boolean = false,
    val editHasKidneyDisease: Boolean = false,
    val editAllergies: String = "",
    val editHealthNotes: String = "",

    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val showDeleteConfirm: Boolean = false,
    val pendingSyncCount: Int = 0,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val vitalsRepository: VitalsRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private var userId: Long = -1

    init {
        viewModelScope.launch {
            syncRepository.observePendingCount().collect { count ->
                _state.update { it.copy(pendingSyncCount = count) }
            }
        }
    }

    fun loadProfile(id: Long) {
        userId = id
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val user = userRepository.getUserById(id)

            // Toplam ölçüm sayısı — tüm verileri güncel dönem için çek
            val now = System.currentTimeMillis()
            val allTimeBp = vitalsRepository.getBloodPressureByDateRange(id, 0, now).size
            val allTimeGlucose = vitalsRepository.getGlucoseByDateRange(id, 0, now).size
            val allTimeHr = vitalsRepository.getHeartRateByDateRange(id, 0, now).size
            val allTimeO2 = vitalsRepository.getOxygenByDateRange(id, 0, now).size
            val totalMeasurements = allTimeBp + allTimeGlucose + allTimeHr + allTimeO2

            val memberDays = if (user != null) {
                ((now - user.createdAt) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
            } else 0

            _state.update {
                it.copy(
                    user = user,
                    isLoading = false,
                    totalMeasurements = totalMeasurements,
                    memberSinceDays = memberDays
                )
            }
        }
    }

    fun startEditing() {
        val user = _state.value.user ?: return
        _state.update {
            it.copy(
                isEditing = true,
                editFirstName = user.firstName,
                editLastName = user.lastName,
                editAge = user.age.toString(),
                editHeight = user.heightCm.toInt().toString(),
                editWeight = user.weightKg.toInt().toString(),
                editActivityLevel = user.activityLevel,
                editIsSmoker = user.isSmoker,
                editHasDiabetes = user.hasDiabetes,
                editHasHypertension = user.hasHypertension,
                editHasCOPD = user.hasCOPD,
                editHasHeartFailure = user.hasHeartFailure,
                editHasKidneyDisease = user.hasKidneyDisease,
                editAllergies = user.allergies ?: "",
                editHealthNotes = user.healthNotes ?: ""
            )
        }
    }

    fun cancelEditing() {
        _state.update { it.copy(isEditing = false) }
    }

    // === Form güncelleme fonksiyonları ===
    fun updateFirstName(v: String) { _state.update { it.copy(editFirstName = v) } }
    fun updateLastName(v: String) { _state.update { it.copy(editLastName = v) } }
    fun updateAge(v: String) { _state.update { it.copy(editAge = v.filter { c -> c.isDigit() }) } }
    fun updateHeight(v: String) { _state.update { it.copy(editHeight = v.filter { c -> c.isDigit() }) } }
    fun updateWeight(v: String) { _state.update { it.copy(editWeight = v.filter { c -> c.isDigit() }) } }
    fun updateActivityLevel(v: String) { _state.update { it.copy(editActivityLevel = v) } }
    fun updateIsSmoker(v: Boolean) { _state.update { it.copy(editIsSmoker = v) } }
    fun updateHasDiabetes(v: Boolean) { _state.update { it.copy(editHasDiabetes = v) } }
    fun updateHasHypertension(v: Boolean) { _state.update { it.copy(editHasHypertension = v) } }
    fun updateHasCOPD(v: Boolean) { _state.update { it.copy(editHasCOPD = v) } }
    fun updateHasHeartFailure(v: Boolean) { _state.update { it.copy(editHasHeartFailure = v) } }
    fun updateHasKidneyDisease(v: Boolean) { _state.update { it.copy(editHasKidneyDisease = v) } }
    fun updateAllergies(v: String) { _state.update { it.copy(editAllergies = v) } }
    fun updateHealthNotes(v: String) { _state.update { it.copy(editHealthNotes = v) } }

    fun saveProfile() {
        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val s = _state.value
            userRepository.updateUser(
                userId = userId,
                firstName = s.editFirstName,
                lastName = s.editLastName,
                age = s.editAge.toIntOrNull(),
                heightCm = s.editHeight.toFloatOrNull(),
                weightKg = s.editWeight.toFloatOrNull(),
                activityLevel = s.editActivityLevel,
                isSmoker = s.editIsSmoker,
                hasDiabetes = s.editHasDiabetes,
                hasHypertension = s.editHasHypertension,
                hasCOPD = s.editHasCOPD,
                hasHeartFailure = s.editHasHeartFailure,
                hasKidneyDisease = s.editHasKidneyDisease,
                allergies = s.editAllergies.ifBlank { null },
                healthNotes = s.editHealthNotes.ifBlank { null }
            )

            // Profili yeniden yükle
            loadProfile(userId)

            _state.update {
                it.copy(
                    isSaving = false,
                    isEditing = false,
                    saveSuccess = true
                )
            }
        }
    }

    fun showDeleteConfirmation() {
        _state.update { it.copy(showDeleteConfirm = true) }
    }

    fun hideDeleteConfirmation() {
        _state.update { it.copy(showDeleteConfirm = false) }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        viewModelScope.launch {
            userRepository.deleteUser(userId)
            onDeleted()
        }
    }

    fun dismissSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }
}
