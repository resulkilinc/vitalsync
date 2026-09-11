package com.vitalsync.app.presentation.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitalsync.app.presentation.components.StepIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    registrationState: RegistrationState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onBloodTypeChange: (String) -> Unit,
    onRhFactorChange: (String) -> Unit,
    onActivityLevelChange: (String) -> Unit,
    onSmokerChange: (Boolean) -> Unit,
    onDiabetesChange: (Boolean) -> Unit,
    onHypertensionChange: (Boolean) -> Unit,
    onCOPDChange: (Boolean) -> Unit,
    onHeartFailureChange: (Boolean) -> Unit,
    onKidneyDiseaseChange: (Boolean) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onHealthNotesChange: (String) -> Unit,
    onPinChange: (String) -> Unit,
    onPinConfirmChange: (String) -> Unit,
    onNextStep: () -> Unit,
    onPreviousStep: () -> Unit,
    onSkipHealthInfo: () -> Unit,
    onMarkHealthInfoCompleted: () -> Unit,
    onCompleteRegistration: () -> Unit,
    onBackToSelection: () -> Unit
) {
    val stepTitles = listOf("Temel Bilgiler", "Fiziksel Bilgiler", "Sağlık Bilgileri", "PIN Oluştur")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        if (registrationState.currentStep > 0) {
                            onPreviousStep()
                        } else {
                            onBackToSelection()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Adım göstergesi
            StepIndicator(
                totalSteps = 4,
                currentStep = registrationState.currentStep,
                stepTitles = stepTitles
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Adım içerikleri
            AnimatedContent(
                targetState = registrationState.currentStep,
                transitionSpec = {
                    val direction = if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally { -it } + fadeOut(tween(300))
                    } else {
                        slideInHorizontally { -it } + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally { it } + fadeOut(tween(300))
                    }
                    direction
                },
                label = "stepAnimation"
            ) { step ->
                when (step) {
                    0 -> RegisterStepBasicInfo(
                        firstName = registrationState.firstName,
                        lastName = registrationState.lastName,
                        username = registrationState.username,
                        isUsernameTaken = registrationState.isUsernameTaken,
                        isValid = registrationState.isStep1Valid,
                        onFirstNameChange = onFirstNameChange,
                        onLastNameChange = onLastNameChange,
                        onUsernameChange = onUsernameChange,
                        onNext = onNextStep
                    )
                    1 -> RegisterStepPhysicalInfo(
                        age = registrationState.age,
                        gender = registrationState.gender,
                        heightCm = registrationState.heightCm,
                        weightKg = registrationState.weightKg,
                        bloodType = registrationState.bloodType,
                        rhFactor = registrationState.rhFactor,
                        calculatedBmi = registrationState.calculatedBmi,
                        isValid = registrationState.isStep2Valid,
                        onAgeChange = onAgeChange,
                        onGenderChange = onGenderChange,
                        onHeightChange = onHeightChange,
                        onWeightChange = onWeightChange,
                        onBloodTypeChange = onBloodTypeChange,
                        onRhFactorChange = onRhFactorChange,
                        onNext = onNextStep
                    )
                    2 -> RegisterStepHealthInfo(
                        activityLevel = registrationState.activityLevel,
                        isSmoker = registrationState.isSmoker,
                        hasDiabetes = registrationState.hasDiabetes,
                        hasHypertension = registrationState.hasHypertension,
                        hasCOPD = registrationState.hasCOPD,
                        hasHeartFailure = registrationState.hasHeartFailure,
                        hasKidneyDisease = registrationState.hasKidneyDisease,
                        allergies = registrationState.allergies,
                        healthNotes = registrationState.healthNotes,
                        onActivityLevelChange = onActivityLevelChange,
                        onSmokerChange = onSmokerChange,
                        onDiabetesChange = onDiabetesChange,
                        onHypertensionChange = onHypertensionChange,
                        onCOPDChange = onCOPDChange,
                        onHeartFailureChange = onHeartFailureChange,
                        onKidneyDiseaseChange = onKidneyDiseaseChange,
                        onAllergiesChange = onAllergiesChange,
                        onHealthNotesChange = onHealthNotesChange,
                        onNext = {
                            onMarkHealthInfoCompleted()
                            onNextStep()
                        },
                        onSkip = {
                            onSkipHealthInfo()
                            onNextStep()
                        }
                    )
                    3 -> RegisterStepPin(
                        pin = registrationState.pin,
                        pinConfirm = registrationState.pinConfirm,
                        pinError = registrationState.pinError,
                        isLoading = registrationState.isLoading,
                        isValid = registrationState.isStep4Valid,
                        onPinChange = onPinChange,
                        onPinConfirmChange = onPinConfirmChange,
                        onComplete = onCompleteRegistration
                    )
                }
            }
        }
    }
}
