package com.vitalsync.app.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContentTransitionScope

import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.vitalsync.app.presentation.analysis.*
import com.vitalsync.app.presentation.auth.*
import com.vitalsync.app.presentation.charts.*
import com.vitalsync.app.presentation.components.BottomNavBar
import com.vitalsync.app.presentation.guide.HealthGuideScreen
import com.vitalsync.app.presentation.home.*
import com.vitalsync.app.presentation.profile.*
import com.vitalsync.app.presentation.reminder.*
import com.vitalsync.app.presentation.report.*
import com.vitalsync.app.presentation.sync.SyncConsoleDialogFragment
import com.vitalsync.app.presentation.tracker.*
import com.vitalsync.app.presentation.theme.VitalSyncTheme
import com.vitalsync.app.util.DataBackupManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var backupManager: DataBackupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitalSyncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VitalSyncNavigation(
                        backupManager = backupManager,
                        fragmentManager = supportFragmentManager,
                    )
                }
            }
        }
    }
}

@Composable
fun VitalSyncNavigation(
    backupManager: DataBackupManager,
    fragmentManager: FragmentManager,
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Auth state
    val users by authViewModel.users.collectAsState()
    val hasUsers by authViewModel.hasUsers.collectAsState()
    val registrationState by authViewModel.registrationState.collectAsState()
    val pinLoginState by authViewModel.pinLoginState.collectAsState()

    // Session — giriş yapmış kullanıcı
    var loggedInUserId by remember { mutableStateOf(-1L) }

    // JSON Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && loggedInUserId > 0) {
            scope.launch {
                try {
                    val count = backupManager.importData(context, uri, loggedInUserId)
                    Toast.makeText(context, "✅ $count kayıt başarıyla içe aktarıldı!", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "❌ İçe aktarma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Giriş tamamlandığında
    LaunchedEffect(registrationState.registrationComplete) {
        if (registrationState.registrationComplete) {
            loggedInUserId = registrationState.createdUserId
            navController.navigate("main_app") { popUpTo(0) { inclusive = true } }
            authViewModel.resetRegistration()
        }
    }

    LaunchedEffect(pinLoginState.isAuthenticated) {
        if (pinLoginState.isAuthenticated) {
            loggedInUserId = pinLoginState.selectedUserId
            navController.navigate("main_app") { popUpTo(0) { inclusive = true } }
            authViewModel.resetPinLogin()
        }
    }

    // Navigasyondaki mevcut route
    val navBackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackEntry?.destination?.route

    // Ana uygulama ekranları (bottom nav var)
    val mainRoutes = listOf("home", "add_measurement", "charts", "profile")
    val showBottomNav = currentRoute in mainRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(
                bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else 0.dp
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = androidx.compose.animation.core.tween(300)
                ) + androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
            }
        ) {
            // === AUTH EKRANLARI ===
            composable("splash") {
                SplashScreen(
                    onNavigateToUserSelection = {
                        navController.navigate("user_selection") { popUpTo("splash") { inclusive = true } }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register") { popUpTo("splash") { inclusive = true } }
                    },
                    hasUsers = hasUsers
                )
            }

            composable("user_selection") {
                UserSelectionScreen(
                    users = users,
                    onUserSelected = { userId ->
                        authViewModel.selectUserForLogin(userId)
                        navController.navigate("pin_login")
                    },
                    onAddNewUser = {
                        authViewModel.resetRegistration()
                        navController.navigate("register")
                    }
                )
            }

            composable("pin_login") {
                val selectedUser = users.find { it.id == pinLoginState.selectedUserId }
                PinScreen(
                    title = "PIN Girin",
                    subtitle = selectedUser?.let { "Merhaba, ${it.firstName}" } ?: "",
                    enteredPin = pinLoginState.enteredPin,
                    isError = pinLoginState.isError,
                    errorMessage = pinLoginState.errorMessage,
                    isLoading = pinLoginState.isVerifying,
                    onDigitClick = { authViewModel.enterPinDigit(it) },
                    onDeleteClick = { authViewModel.deletePinDigit() },
                    onBackClick = {
                        authViewModel.resetPinLogin()
                        navController.popBackStack()
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    registrationState = registrationState,
                    onFirstNameChange = authViewModel::updateFirstName,
                    onLastNameChange = authViewModel::updateLastName,
                    onUsernameChange = authViewModel::updateUsername,
                    onAgeChange = authViewModel::updateAge,
                    onGenderChange = authViewModel::updateGender,
                    onHeightChange = authViewModel::updateHeight,
                    onWeightChange = authViewModel::updateWeight,
                    onBloodTypeChange = authViewModel::updateBloodType,
                    onRhFactorChange = authViewModel::updateRhFactor,
                    onActivityLevelChange = authViewModel::updateActivityLevel,
                    onSmokerChange = authViewModel::updateIsSmoker,
                    onDiabetesChange = authViewModel::updateDiabetes,
                    onHypertensionChange = authViewModel::updateHypertension,
                    onCOPDChange = authViewModel::updateCOPD,
                    onHeartFailureChange = authViewModel::updateHeartFailure,
                    onKidneyDiseaseChange = authViewModel::updateKidneyDisease,
                    onAllergiesChange = authViewModel::updateAllergies,
                    onHealthNotesChange = authViewModel::updateHealthNotes,
                    onPinChange = authViewModel::updatePin,
                    onPinConfirmChange = authViewModel::updatePinConfirm,
                    onNextStep = authViewModel::nextStep,
                    onPreviousStep = authViewModel::previousStep,
                    onSkipHealthInfo = authViewModel::skipHealthInfo,
                    onMarkHealthInfoCompleted = authViewModel::markHealthInfoCompleted,
                    onCompleteRegistration = authViewModel::completeRegistration,
                    onBackToSelection = {
                        authViewModel.resetRegistration()
                        if (users.isNotEmpty()) navController.popBackStack()
                    }
                )
            }

            // === ANA UYGULAMA — main_app → home'a yönlendir ===
            composable("main_app") {
                LaunchedEffect(Unit) {
                    navController.navigate("home") {
                        popUpTo("main_app") { inclusive = true }
                    }
                }
            }

            // === HOME ===
            composable("home") {
                val homeViewModel: HomeViewModel = hiltViewModel()
                val homeState by homeViewModel.homeState.collectAsState()

                LaunchedEffect(loggedInUserId) {
                    if (loggedInUserId > 0) homeViewModel.loadDashboard(loggedInUserId)
                }

                HomeScreen(
                    homeState = homeState,
                    onTrackerClick = { vitalType ->
                        val route = when (vitalType) {
                            VitalType.BLOOD_PRESSURE -> "tracker_bp"
                            VitalType.GLUCOSE -> "tracker_glucose"
                            VitalType.HEART_RATE -> "tracker_hr"
                            VitalType.OXYGEN -> "tracker_o2"
                        }
                        navController.navigate(route)
                    },
                    onAddMeasurement = { navController.navigate("add_measurement") }
                )
            }

            // === ÖLÇÜM EKLE SEÇİM ===
            composable("add_measurement") {
                AddMeasurementScreen(
                    onSelect = { route -> navController.navigate(route) }
                )
            }

            // === TRACKER EKRANLARI ===
            composable("tracker_bp") {
                val trackerVm: TrackerViewModel = hiltViewModel()
                val trackerState by trackerVm.state.collectAsState()

                LaunchedEffect(loggedInUserId) { trackerVm.setUserId(loggedInUserId) }

                BloodPressureTrackerScreen(
                    state = trackerState,
                    onSystolicChange = trackerVm::updateBpSystolic,
                    onDiastolicChange = trackerVm::updateBpDiastolic,
                    onPulseChange = trackerVm::updateBpPulse,
                    onArmChange = trackerVm::updateBpArm,
                    onContextChange = trackerVm::updateBpContext,
                    onNotesChange = trackerVm::updateBpNotes,
                    onSave = trackerVm::saveBloodPressure,
                    isSaveEnabled = trackerVm.isBpValid,
                    onBack = { navController.popBackStack() }
                )

                if (trackerState.showResult && trackerState.analysisResult != null) {
                    AnalysisResultSheet(
                        analysisResult = trackerState.analysisResult!!,
                        onDismiss = { trackerVm.resetForm() },
                        onGoHome = {
                            trackerVm.resetForm()
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("tracker_glucose") {
                val trackerVm: TrackerViewModel = hiltViewModel()
                val trackerState by trackerVm.state.collectAsState()

                LaunchedEffect(loggedInUserId) { trackerVm.setUserId(loggedInUserId) }

                GlucoseTrackerScreen(
                    state = trackerState,
                    onValueChange = trackerVm::updateGlucoseValue,
                    onContextChange = trackerVm::updateGlucoseContext,
                    onMethodChange = trackerVm::updateGlucoseMethod,
                    onNotesChange = trackerVm::updateGlucoseNotes,
                    onSave = trackerVm::saveGlucose,
                    isSaveEnabled = trackerVm.isGlucoseValid,
                    onBack = { navController.popBackStack() }
                )

                if (trackerState.showResult && trackerState.analysisResult != null) {
                    AnalysisResultSheet(
                        analysisResult = trackerState.analysisResult!!,
                        onDismiss = { trackerVm.resetForm() },
                        onGoHome = {
                            trackerVm.resetForm()
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        }
                    )
                }
            }

            composable("tracker_hr") {
                val trackerVm: TrackerViewModel = hiltViewModel()
                val trackerState by trackerVm.state.collectAsState()

                LaunchedEffect(loggedInUserId) { trackerVm.setUserId(loggedInUserId) }

                HeartRateTrackerScreen(
                    state = trackerState,
                    onValueChange = trackerVm::updateHrValue,
                    onContextChange = trackerVm::updateHrContext,
                    onFeverChange = trackerVm::updateHrFever,
                    onPalpitationsChange = trackerVm::updateHrPalpitations,
                    onNotesChange = trackerVm::updateHrNotes,
                    onSave = trackerVm::saveHeartRate,
                    isSaveEnabled = trackerVm.isHrValid,
                    onBack = { navController.popBackStack() }
                )

                if (trackerState.showResult && trackerState.analysisResult != null) {
                    AnalysisResultSheet(
                        analysisResult = trackerState.analysisResult!!,
                        onDismiss = { trackerVm.resetForm() },
                        onGoHome = {
                            trackerVm.resetForm()
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        }
                    )
                }
            }

            composable("tracker_o2") {
                val trackerVm: TrackerViewModel = hiltViewModel()
                val trackerState by trackerVm.state.collectAsState()

                LaunchedEffect(loggedInUserId) { trackerVm.setUserId(loggedInUserId) }

                OxygenTrackerScreen(
                    state = trackerState,
                    onValueChange = trackerVm::updateO2Value,
                    onBreathingDifficultyChange = trackerVm::updateO2BreathingDifficulty,
                    onPalpitationsChange = trackerVm::updateO2Palpitations,
                    onAltitudeChange = trackerVm::updateO2Altitude,
                    onNotesChange = trackerVm::updateO2Notes,
                    onSave = trackerVm::saveOxygen,
                    isSaveEnabled = trackerVm.isO2Valid,
                    onBack = { navController.popBackStack() }
                )

                if (trackerState.showResult && trackerState.analysisResult != null) {
                    AnalysisResultSheet(
                        analysisResult = trackerState.analysisResult!!,
                        onDismiss = { trackerVm.resetForm() },
                        onGoHome = {
                            trackerVm.resetForm()
                            navController.navigate("home") { popUpTo("home") { inclusive = true } }
                        }
                    )
                }
            }

            // === GRAFİKLER (Faz 4) ===
            composable("charts") {
                val chartsViewModel: ChartsViewModel = hiltViewModel()
                val chartsState by chartsViewModel.state.collectAsState()

                LaunchedEffect(loggedInUserId) {
                    if (loggedInUserId > 0) chartsViewModel.setUserId(loggedInUserId)
                }

                ChartsScreen(
                    state = chartsState,
                    onTabSelected = chartsViewModel::selectTab,
                    onPeriodSelected = chartsViewModel::selectPeriod,
                    onNavigateToAnalysis = { metricType ->
                        navController.navigate("analysis/$metricType")
                    }
                )
            }

            // === ANALİZ (Faz 5) ===
            composable(
                route = "analysis/{metricType}",
                arguments = listOf(navArgument("metricType") { type = NavType.StringType })
            ) { backStackEntry ->
                val metricType = backStackEntry.arguments?.getString("metricType") ?: "BP"
                val analysisViewModel: AnalysisViewModel = hiltViewModel()

                LaunchedEffect(loggedInUserId, metricType) {
                    if (loggedInUserId > 0) analysisViewModel.initialize(loggedInUserId, metricType)
                }

                AnalysisScreen(
                    viewModel = analysisViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // === PROFİL (Faz 4 + Faz 6) ===
            composable("profile") {
                val profileViewModel: ProfileViewModel = hiltViewModel()
                val profileState by profileViewModel.state.collectAsState()

                LaunchedEffect(loggedInUserId) {
                    if (loggedInUserId > 0) profileViewModel.loadProfile(loggedInUserId)
                }

                ProfileScreen(
                    state = profileState,
                    onStartEdit = profileViewModel::startEditing,
                    onCancelEdit = profileViewModel::cancelEditing,
                    onSaveProfile = profileViewModel::saveProfile,
                    onFirstNameChange = profileViewModel::updateFirstName,
                    onLastNameChange = profileViewModel::updateLastName,
                    onAgeChange = profileViewModel::updateAge,
                    onHeightChange = profileViewModel::updateHeight,
                    onWeightChange = profileViewModel::updateWeight,
                    onActivityLevelChange = profileViewModel::updateActivityLevel,
                    onIsSmokerChange = profileViewModel::updateIsSmoker,
                    onDiabetesChange = profileViewModel::updateHasDiabetes,
                    onHypertensionChange = profileViewModel::updateHasHypertension,
                    onCOPDChange = profileViewModel::updateHasCOPD,
                    onHeartFailureChange = profileViewModel::updateHasHeartFailure,
                    onKidneyDiseaseChange = profileViewModel::updateHasKidneyDisease,
                    onAllergiesChange = profileViewModel::updateAllergies,
                    onHealthNotesChange = profileViewModel::updateHealthNotes,
                    onDeleteAccount = {
                        profileViewModel.deleteAccount {
                            loggedInUserId = -1
                            navController.navigate("splash") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onShowDeleteConfirm = profileViewModel::showDeleteConfirmation,
                    onHideDeleteConfirm = profileViewModel::hideDeleteConfirmation,
                    onLogout = {
                        loggedInUserId = -1
                        navController.navigate("splash") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onGenerateReport = {
                        navController.navigate("report")
                    },
                    // === Faz 6 ===
                    onReminders = {
                        navController.navigate("reminders")
                    },
                    onHealthGuide = {
                        navController.navigate("health_guide")
                    },
                    onExportData = {
                        scope.launch {
                            try {
                                val file = backupManager.exportData(context, loggedInUserId)
                                // Dosyayı paylaş
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "VitalÖlçüm Veri Yedeği")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Yedeği Paylaş"))
                                Toast.makeText(context, "✅ Yedek dosyası oluşturuldu!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "❌ Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onImportData = {
                        importLauncher.launch(arrayOf("application/json"))
                    },
                    onOpenSyncConsole = {
                        SyncConsoleDialogFragment.newInstance(loggedInUserId)
                            .show(fragmentManager, "sync_console")
                    }
                )
            }

            // === RAPOR (Faz 4) ===
            composable("report") {
                val reportViewModel: ReportViewModel = hiltViewModel()

                LaunchedEffect(loggedInUserId) {
                    if (loggedInUserId > 0) reportViewModel.setUserId(loggedInUserId)
                }

                ReportScreen(
                    reportViewModel = reportViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // === HATIRLATICILAR (Faz 6) ===
            composable("reminders") {
                val reminderViewModel: ReminderViewModel = hiltViewModel()

                LaunchedEffect(loggedInUserId) {
                    if (loggedInUserId > 0) reminderViewModel.setUserId(loggedInUserId)
                }

                ReminderScreen(
                    viewModel = reminderViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // === SAĞLIK REHBERİ (Faz 6) ===
            composable("health_guide") {
                HealthGuideScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

// === Ölçüm Ekleme Seçim Ekranı ===
@Composable
private fun AddMeasurementScreen(onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            "Ölçüm Ekle",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Hangi sağlık verisini kaydetmek istiyorsunuz?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        val items = listOf(
            Triple("🩺", "Kan Basıncı", "tracker_bp"),
            Triple("🩸", "Kan Şekeri", "tracker_glucose"),
            Triple("❤️", "Nabız", "tracker_hr"),
            Triple("🫁", "Oksijen (SpO₂)", "tracker_o2")
        )

        items.forEach { (emoji, title, route) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                onClick = { onSelect(route) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text("→", style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
