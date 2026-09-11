package com.vitalsync.app.presentation.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vitalsync.app.presentation.components.VitalSyncButton
import com.vitalsync.app.presentation.components.VitalSyncTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodPressureTrackerScreen(
    state: TrackerState,
    onSystolicChange: (String) -> Unit,
    onDiastolicChange: (String) -> Unit,
    onPulseChange: (String) -> Unit,
    onArmChange: (String) -> Unit,
    onContextChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kan Basıncı Ekle", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Text("🩺", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Tansiyonunuzu girin",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Sistolik (büyük) ve diastolik (küçük) değerleri",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sistolik & Diastolik yan yana
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                VitalSyncTextField(
                    value = state.bpSystolic,
                    onValueChange = onSystolicChange,
                    label = "Sistolik",
                    placeholder = "120",
                    isRequired = true,
                    keyboardType = KeyboardType.Number,
                    isValid = (state.bpSystolic.toIntOrNull() ?: 0) in 50..300,
                    modifier = Modifier.weight(1f)
                )
                VitalSyncTextField(
                    value = state.bpDiastolic,
                    onValueChange = onDiastolicChange,
                    label = "Diastolik",
                    placeholder = "80",
                    isRequired = true,
                    keyboardType = KeyboardType.Number,
                    isValid = (state.bpDiastolic.toIntOrNull() ?: 0) in 20..200,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nabız (opsiyonel)
            VitalSyncTextField(
                value = state.bpPulse,
                onValueChange = onPulseChange,
                label = "Nabız (opsiyonel)",
                placeholder = "72",
                keyboardType = KeyboardType.Number,
                isValid = state.bpPulse.isBlank() || (state.bpPulse.toIntOrNull() ?: 0) in 30..250
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Kol seçimi
            Text("Ölçüm Kolu", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Sol", "Sağ").forEach { arm ->
                    FilterChip(
                        selected = state.bpArm == arm,
                        onClick = { onArmChange(arm) },
                        label = { Text(arm) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bağlam (Klinik/Ofis → ESC 2024 ofis eşikleri; diğerleri varsayılan ev/HBPM yaklaşımı)
            Text("Ölçüm Bağlamı", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("İstirahat", "Klinik/Ofis").forEach { ctx ->
                        FilterChip(
                            selected = state.bpContext == ctx,
                            onClick = { onContextChange(ctx) },
                            label = { Text(ctx, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Egzersiz Sonrası", "Stres").forEach { ctx ->
                        FilterChip(
                            selected = state.bpContext == ctx,
                            onClick = { onContextChange(ctx) },
                            label = { Text(ctx, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VitalSyncTextField(
                value = state.bpNotes,
                onValueChange = onNotesChange,
                label = "Notlar",
                placeholder = "Ek bilgi...",
                singleLine = false,
                maxLines = 3
            )

            Spacer(modifier = Modifier.weight(1f))

            VitalSyncButton(
                text = if (state.isSaving) "Kaydediliyor..." else "Kaydet ve Analiz Et",
                onClick = onSave,
                enabled = isSaveEnabled && !state.isSaving
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
