package com.vitalsync.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitalsync.app.presentation.components.VitalSyncButton
import com.vitalsync.app.presentation.components.VitalSyncTextField

@Composable
fun RegisterStepHealthInfo(
    activityLevel: String,
    isSmoker: Boolean,
    hasDiabetes: Boolean,
    hasHypertension: Boolean,
    hasCOPD: Boolean,
    hasHeartFailure: Boolean,
    hasKidneyDisease: Boolean,
    allergies: String,
    healthNotes: String,
    onActivityLevelChange: (String) -> Unit,
    onSmokerChange: (Boolean) -> Unit,
    onDiabetesChange: (Boolean) -> Unit,
    onHypertensionChange: (Boolean) -> Unit,
    onCOPDChange: (Boolean) -> Unit,
    onHeartFailureChange: (Boolean) -> Unit,
    onKidneyDiseaseChange: (Boolean) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onHealthNotesChange: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    var showSkipDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Sağlık Bilgileri",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bu adım opsiyoneldir ancak önerilir",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ÖNEMLİ bilgi kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kronik hastalık ve alerji bilgilerinizi paylaşmanız, " +
                            "sağlık analizlerinizin doğruluğunu önemli ölçüde artırır. " +
                            "Bu bilgiler yalnızca cihazınızda saklanır ve paylaşılmaz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kronik Hastalıklar
        Text(
            text = "Kronik Hastalıklar",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        HealthToggleItem("Diyabet (Şeker Hastalığı)", "🩸", hasDiabetes, onDiabetesChange)
        HealthToggleItem("Hipertansiyon (Yüksek Tansiyon)", "💊", hasHypertension, onHypertensionChange)
        HealthToggleItem("KOAH (Kronik Akciğer)", "🫁", hasCOPD, onCOPDChange)
        HealthToggleItem("Kalp Yetmezliği", "❤️", hasHeartFailure, onHeartFailureChange)
        HealthToggleItem("Böbrek Hastalığı", "🫘", hasKidneyDisease, onKidneyDiseaseChange)

        Spacer(modifier = Modifier.height(20.dp))

        // Yaşam Tarzı
        Text(
            text = "Yaşam Tarzı",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Aktivite Seviyesi
        Text(
            text = "Aktivite Seviyesi",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "Sedanter" to "🪑",
                "Aktif" to "🚶",
                "Sporcu" to "🏃"
            ).forEach { (level, icon) ->
                val isSelected = activityLevel == level
                FilterChip(
                    selected = isSelected,
                    onClick = { onActivityLevelChange(level) },
                    label = {
                        Text("$icon $level", style = MaterialTheme.typography.labelSmall)
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sigara
        HealthToggleItem("Sigara Kullanıyorum", "🚬", isSmoker, onSmokerChange)

        Spacer(modifier = Modifier.height(20.dp))

        // Alerjiler
        Text(
            text = "Alerjiler & Ek Notlar",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        VitalSyncTextField(
            value = allergies,
            onValueChange = onAllergiesChange,
            label = "Alerjiler",
            placeholder = "Örn: Penisilin, polen, fıstık...",
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(12.dp))

        VitalSyncTextField(
            value = healthNotes,
            onValueChange = onHealthNotesChange,
            label = "Ek Sağlık Notları",
            placeholder = "Doktorunuzla paylaşmak istediğiniz notlar...",
            singleLine = false,
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Kaydet & Devam Et
        VitalSyncButton(
            text = "Kaydet ve Devam Et",
            onClick = onNext
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Atla butonu
        VitalSyncButton(
            text = "Şimdilik Atla",
            onClick = { showSkipDialog = true },
            isOutline = true
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Atlama uyarı dialog'u
    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
            },
            title = {
                Text("Sağlık Bilgilerini Atla?")
            },
            text = {
                Text(
                    "Kronik hastalık ve alerji bilgileri olmadan sağlık analizleriniz " +
                            "daha az doğru olabilir. Bu bilgileri daha sonra profil " +
                            "ayarlarından ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSkipDialog = false
                    onSkip()
                }) {
                    Text("Yine de Atla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSkipDialog = false }) {
                    Text("Geri Dön", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun HealthToggleItem(
    title: String,
    icon: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, modifier = Modifier.padding(end = 12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    checkedThumbColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}
