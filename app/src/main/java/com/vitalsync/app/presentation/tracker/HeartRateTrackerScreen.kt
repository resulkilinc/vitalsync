package com.vitalsync.app.presentation.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vitalsync.app.presentation.components.VitalSyncButton
import com.vitalsync.app.presentation.components.VitalSyncTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateTrackerScreen(
    state: TrackerState,
    onValueChange: (String) -> Unit,
    onContextChange: (String) -> Unit,
    onFeverChange: (Boolean) -> Unit,
    onPalpitationsChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nabız Ekle", fontWeight = FontWeight.SemiBold) },
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
            Text("❤️", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nabzınızı Girin", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Dakikadaki atım sayısı (bpm)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(24.dp))

            VitalSyncTextField(
                value = state.hrValue,
                onValueChange = onValueChange,
                label = "Nabız (bpm)",
                placeholder = "72",
                isRequired = true,
                keyboardType = KeyboardType.Number,
                isValid = (state.hrValue.toIntOrNull() ?: 0) in 20..300,
                isError = state.hrValue.isNotBlank() && (state.hrValue.toIntOrNull() ?: 0) !in 20..300,
                errorMessage = if (state.hrValue.isNotBlank() && (state.hrValue.toIntOrNull() ?: 0) !in 20..300) "20-300 aralığında olmalı" else null
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bağlam
            Text("Ölçüm Bağlamı", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("İstirahat" to "🪑", "Egzersiz" to "🏃", "Uyku" to "😴").forEach { (ctx, icon) ->
                    FilterChip(
                        selected = state.hrContext == ctx,
                        onClick = { onContextChange(ctx) },
                        label = { Text("$icon $ctx", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Semptomlar
            Text("Semptomlar", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌡️", modifier = Modifier.padding(end = 12.dp))
                    Text("Ateşim Var", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = state.hrHasFever, onCheckedChange = onFeverChange)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💓", modifier = Modifier.padding(end = 12.dp))
                    Text("Çarpıntı Hissediyorum", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = state.hrHasPalpitations, onCheckedChange = onPalpitationsChange)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VitalSyncTextField(
                value = state.hrNotes,
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
