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
fun GlucoseTrackerScreen(
    state: TrackerState,
    onValueChange: (String) -> Unit,
    onContextChange: (String) -> Unit,
    onMethodChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kan Şekeri Ekle", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
            Text("🩸", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Kan Şekerinizi Girin", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text("mg/dL cinsinden değeri girin", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(24.dp))

            VitalSyncTextField(
                value = state.glucoseValue,
                onValueChange = onValueChange,
                label = "Kan Şekeri (mg/dL)",
                placeholder = "100",
                isRequired = true,
                keyboardType = KeyboardType.Number,
                isValid = (state.glucoseValue.toIntOrNull() ?: 0) in 20..600,
                isError = state.glucoseValue.isNotBlank() && (state.glucoseValue.toIntOrNull() ?: 0) !in 20..600,
                errorMessage = if (state.glucoseValue.isNotBlank() && (state.glucoseValue.toIntOrNull() ?: 0) !in 20..600) "20-600 aralığında olmalı" else null
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Ölçüm zamanı
            Text("Ölçüm Zamanı", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Açlık" to "🌙", "Tokluk" to "🍽️", "Rastgele" to "🕐").forEach { (ctx, icon) ->
                    FilterChip(
                        selected = state.glucoseContext == ctx,
                        onClick = { onContextChange(ctx) },
                        label = { Text("$icon $ctx", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Yöntem
            Text("Ölçüm Yöntemi", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Parmak Ucu", "Laboratuvar").forEach { method ->
                    FilterChip(
                        selected = state.glucoseMethod == method,
                        onClick = { onMethodChange(method) },
                        label = { Text(method) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VitalSyncTextField(
                value = state.glucoseNotes,
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
