package com.vitalsync.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.repository.UserRepository
import com.vitalsync.app.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// === Registration Wizard State ===
data class RegistrationState(
    val currentStep: Int = 0,

    // Adım 1: Temel Bilgiler (ZORUNLU)
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val isUsernameTaken: Boolean = false,
    val isCheckingUsername: Boolean = false,

    // Adım 2: Fiziksel Bilgiler (ZORUNLU)
    val age: String = "",
    val gender: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val bloodType: String = "",
    val rhFactor: String = "",
    val calculatedBmi: Float = 0f,

    // Adım 3: Sağlık Bilgileri (OPSİYONEL)
    val activityLevel: String = "Belirtilmedi",
    val isSmoker: Boolean = false,
    val hasDiabetes: Boolean = false,
    val hasHypertension: Boolean = false,
    val hasCOPD: Boolean = false,
    val hasHeartFailure: Boolean = false,
    val hasKidneyDisease: Boolean = false,
    val allergies: String = "",
    val healthNotes: String = "",
    val isHealthInfoCompleted: Boolean = false,

    // Adım 4: PIN
    val pin: String = "",
    val pinConfirm: String = "",
    val pinError: String? = null,

    // Genel
    val isLoading: Boolean = false,
    val registrationComplete: Boolean = false,
    val createdUserId: Long = -1
) {
    val isStep1Valid: Boolean
        get() = firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                username.length >= 3 &&
                !isUsernameTaken

    val isStep2Valid: Boolean
        get() = age.isNotBlank() &&
                (age.toIntOrNull() ?: 0) in 1..150 &&
                gender.isNotBlank() &&
                heightCm.isNotBlank() &&
                (heightCm.toFloatOrNull() ?: 0f) > 0f &&
                weightKg.isNotBlank() &&
                (weightKg.toFloatOrNull() ?: 0f) > 0f &&
                bloodType.isNotBlank() &&
                rhFactor.isNotBlank()

    val isStep4Valid: Boolean
        get() = pin.length == 4 && pin == pinConfirm && pinError == null
}

// === PIN Giriş State ===
data class PinLoginState(
    val selectedUserId: Long = -1,
    val enteredPin: String = "",
    val isVerifying: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val attemptsLeft: Int = 5,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // Kullanıcı listesi
    val users: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Kayıt durumu
    private val _registrationState = MutableStateFlow(RegistrationState())
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    // PIN giriş durumu
    private val _pinLoginState = MutableStateFlow(PinLoginState())
    val pinLoginState: StateFlow<PinLoginState> = _pinLoginState.asStateFlow()

    // Kullanıcı var mı kontrolü
    private val _hasUsers = MutableStateFlow<Boolean?>(null)
    val hasUsers: StateFlow<Boolean?> = _hasUsers.asStateFlow()

    init {
        checkHasUsers()
    }

    private fun checkHasUsers() {
        viewModelScope.launch {
            val count = userRepository.getUserCount()
            _hasUsers.value = count > 0
        }
    }

    // =====================
    // REGISTRATION İŞLEMLERİ
    // =====================

    fun updateFirstName(value: String) {
        _registrationState.update { it.copy(firstName = value.trim()) }
    }

    fun updateLastName(value: String) {
        _registrationState.update { it.copy(lastName = value.trim()) }
    }

    fun updateUsername(value: String) {
        val cleaned = value.lowercase().trim().replace(" ", "")
        _registrationState.update { it.copy(username = cleaned, isCheckingUsername = true) }
        viewModelScope.launch {
            val taken = if (cleaned.length >= 3) userRepository.isUsernameTaken(cleaned) else false
            _registrationState.update { it.copy(isUsernameTaken = taken, isCheckingUsername = false) }
        }
    }

    fun updateAge(value: String) {
        _registrationState.update { it.copy(age = value.filter { c -> c.isDigit() }) }
    }

    fun updateGender(value: String) {
        _registrationState.update { it.copy(gender = value) }
    }

    fun updateHeight(value: String) {
        _registrationState.update { it.copy(heightCm = value) }
        recalculateBmi()
    }

    fun updateWeight(value: String) {
        _registrationState.update { it.copy(weightKg = value) }
        recalculateBmi()
    }

    fun updateBloodType(value: String) {
        _registrationState.update { it.copy(bloodType = value) }
    }

    fun updateRhFactor(value: String) {
        _registrationState.update { it.copy(rhFactor = value) }
    }

    fun updateActivityLevel(value: String) {
        _registrationState.update { it.copy(activityLevel = value) }
    }

    fun updateIsSmoker(value: Boolean) {
        _registrationState.update { it.copy(isSmoker = value) }
    }

    fun updateDiabetes(value: Boolean) {
        _registrationState.update { it.copy(hasDiabetes = value) }
    }

    fun updateHypertension(value: Boolean) {
        _registrationState.update { it.copy(hasHypertension = value) }
    }

    fun updateCOPD(value: Boolean) {
        _registrationState.update { it.copy(hasCOPD = value) }
    }

    fun updateHeartFailure(value: Boolean) {
        _registrationState.update { it.copy(hasHeartFailure = value) }
    }

    fun updateKidneyDisease(value: Boolean) {
        _registrationState.update { it.copy(hasKidneyDisease = value) }
    }

    fun updateAllergies(value: String) {
        _registrationState.update { it.copy(allergies = value) }
    }

    fun updateHealthNotes(value: String) {
        _registrationState.update { it.copy(healthNotes = value) }
    }

    fun markHealthInfoCompleted() {
        _registrationState.update { it.copy(isHealthInfoCompleted = true) }
    }

    fun skipHealthInfo() {
        _registrationState.update { it.copy(isHealthInfoCompleted = false) }
    }

    fun updatePin(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _registrationState.update { it.copy(pin = value, pinError = null) }
        }
    }

    fun updatePinConfirm(value: String) {
        if (value.length <= 4 && value.all { it.isDigit() }) {
            val error = if (value.length == 4 && value != _registrationState.value.pin) {
                "PIN'ler eşleşmiyor"
            } else null
            _registrationState.update { it.copy(pinConfirm = value, pinError = error) }
        }
    }

    fun nextStep() {
        _registrationState.update { it.copy(currentStep = it.currentStep + 1) }
    }

    fun previousStep() {
        _registrationState.update {
            it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(0))
        }
    }

    fun completeRegistration() {
        val state = _registrationState.value
        if (!state.isStep4Valid) return

        _registrationState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val userId = userRepository.createUser(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    username = state.username,
                    pin = state.pin,
                    age = state.age.toInt(),
                    gender = state.gender,
                    heightCm = state.heightCm.toFloat(),
                    weightKg = state.weightKg.toFloat(),
                    bloodType = state.bloodType,
                    rhFactor = state.rhFactor,
                    activityLevel = state.activityLevel,
                    isSmoker = state.isSmoker,
                    hasDiabetes = state.hasDiabetes,
                    hasHypertension = state.hasHypertension,
                    hasCOPD = state.hasCOPD,
                    hasHeartFailure = state.hasHeartFailure,
                    hasKidneyDisease = state.hasKidneyDisease,
                    allergies = state.allergies.ifBlank { null },
                    healthNotes = state.healthNotes.ifBlank { null },
                    isHealthInfoCompleted = state.isHealthInfoCompleted
                )
                _registrationState.update {
                    it.copy(isLoading = false, registrationComplete = true, createdUserId = userId)
                }
                _hasUsers.value = true
            } catch (e: Exception) {
                _registrationState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun resetRegistration() {
        _registrationState.value = RegistrationState()
    }

    private fun recalculateBmi() {
        val h = _registrationState.value.heightCm.toFloatOrNull() ?: 0f
        val w = _registrationState.value.weightKg.toFloatOrNull() ?: 0f
        val bmi = userRepository.calculateBmi(h, w)
        _registrationState.update { it.copy(calculatedBmi = bmi) }
    }

    // =====================
    // PIN GİRİŞ İŞLEMLERİ
    // =====================

    fun selectUserForLogin(userId: Long) {
        _pinLoginState.value = PinLoginState(selectedUserId = userId)
    }

    fun enterPinDigit(digit: Char) {
        val current = _pinLoginState.value
        if (current.enteredPin.length < 4) {
            val newPin = current.enteredPin + digit
            _pinLoginState.update { it.copy(enteredPin = newPin, isError = false, errorMessage = null) }

            if (newPin.length == 4) {
                verifyLoginPin(newPin)
            }
        }
    }

    fun deletePinDigit() {
        _pinLoginState.update {
            it.copy(
                enteredPin = it.enteredPin.dropLast(1),
                isError = false,
                errorMessage = null
            )
        }
    }

    fun clearLoginPin() {
        _pinLoginState.update { it.copy(enteredPin = "", isError = false, errorMessage = null) }
    }

    private fun verifyLoginPin(pin: String) {
        _pinLoginState.update { it.copy(isVerifying = true) }

        viewModelScope.launch {
            val userId = _pinLoginState.value.selectedUserId
            val isValid = userRepository.verifyPin(userId, pin)

            if (isValid) {
                _pinLoginState.update {
                    it.copy(isVerifying = false, isAuthenticated = true)
                }
            } else {
                val attemptsLeft = _pinLoginState.value.attemptsLeft - 1
                _pinLoginState.update {
                    it.copy(
                        isVerifying = false,
                        isError = true,
                        enteredPin = "",
                        errorMessage = if (attemptsLeft > 0) "Yanlış PIN. $attemptsLeft deneme hakkı kaldı." else "Çok fazla hatalı deneme.",
                        attemptsLeft = attemptsLeft
                    )
                }
            }
        }
    }

    fun resetPinLogin() {
        _pinLoginState.value = PinLoginState()
    }
}
