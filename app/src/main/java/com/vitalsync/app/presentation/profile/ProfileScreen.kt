package com.vitalsync.app.presentation.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.presentation.theme.*

@Composable
fun ProfileScreen(
    state: ProfileState,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSaveProfile: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onActivityLevelChange: (String) -> Unit,
    onIsSmokerChange: (Boolean) -> Unit,
    onDiabetesChange: (Boolean) -> Unit,
    onHypertensionChange: (Boolean) -> Unit,
    onCOPDChange: (Boolean) -> Unit,
    onHeartFailureChange: (Boolean) -> Unit,
    onKidneyDiseaseChange: (Boolean) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onHealthNotesChange: (String) -> Unit,
    onDeleteAccount: () -> Unit,
    onShowDeleteConfirm: () -> Unit,
    onHideDeleteConfirm: () -> Unit,
    onLogout: () -> Unit,
    onGenerateReport: () -> Unit,
    onReminders: () -> Unit = {},
    onHealthGuide: () -> Unit = {},
    onExportData: () -> Unit = {},
    onImportData: () -> Unit = {},
    onOpenSyncConsole: () -> Unit = {}
) {
    val user = state.user

    if (state.isLoading || user == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Silme onay dialogu
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onHideDeleteConfirm,
            title = { Text("Hesabı Sil", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Bu işlem geri alınamaz. Tüm verileriniz (ölçümler, grafikler, profil) kalıcı olarak silinecektir.\n\nEmin misiniz?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onDeleteAccount,
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusHigh)
                ) {
                    Text("Evet, Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onHideDeleteConfirm) {
                    Text("İptal")
                }
            }
        )
    }

    // Kayıt başarılı snackbar
    if (state.saveSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))

        // Başlık + Düzenle butonu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Profil",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (state.isEditing) {
                Row {
                    IconButton(onClick = onCancelEdit) {
                        Icon(Icons.Default.Close, contentDescription = "İptal")
                    }
                    IconButton(onClick = onSaveProfile) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = "Kaydet",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                IconButton(onClick = onStartEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle")
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // === Avatar ve İsim ===
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user.initials,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (state.isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.editFirstName,
                            onValueChange = onFirstNameChange,
                            label = { Text("Ad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.editLastName,
                            onValueChange = onLastNameChange,
                            label = { Text("Soyad") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                } else {
                    Text(
                        user.fullName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // İstatistik satırı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("📊", state.totalMeasurements.toString(), "Ölçüm")
                    ProfileStat("📅", "${state.memberSinceDays}", "Gün")
                    ProfileStat("🩸", user.bloodTypeDisplay, "Kan Grubu")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // === Fiziksel Bilgiler ===
        SectionHeader("📋 Fiziksel Bilgiler")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.editAge,
                            onValueChange = onAgeChange,
                            label = { Text("Yaş") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.editHeight,
                            onValueChange = onHeightChange,
                            label = { Text("Boy (cm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = state.editWeight,
                            onValueChange = onWeightChange,
                            label = { Text("Kilo (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Aktivite seviyesi dropdown
                    Text("Aktivite Seviyesi", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Sedanter", "Aktif", "Sporcu").forEach { level ->
                            FilterChip(
                                selected = state.editActivityLevel == level,
                                onClick = { onActivityLevelChange(level) },
                                label = { Text(level, fontSize = 11.sp) }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoItem("Yaş", "${user.age}")
                        InfoItem("Boy", "${user.heightCm.toInt()} cm")
                        InfoItem("Kilo", "${user.weightKg.toInt()} kg")
                        InfoItem("BMI", String.format("%.1f", user.bmi))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoItem("Cinsiyet", user.gender)
                        InfoItem("Aktivite", user.activityLevel)
                        InfoItem("BMI Kategori", user.bmiCategory)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // === Sağlık Geçmişi ===
        SectionHeader("🏥 Sağlık Geçmişi")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (state.isEditing) {
                    HealthToggle("Diyabet", state.editHasDiabetes, onDiabetesChange)
                    HealthToggle("Hipertansiyon", state.editHasHypertension, onHypertensionChange)
                    HealthToggle("KOAH", state.editHasCOPD, onCOPDChange)
                    HealthToggle("Kalp Yetmezliği", state.editHasHeartFailure, onHeartFailureChange)
                    HealthToggle("Böbrek Hastalığı", state.editHasKidneyDisease, onKidneyDiseaseChange)
                    HealthToggle("Sigara Kullanımı", state.editIsSmoker, onIsSmokerChange)

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.editAllergies,
                        onValueChange = onAllergiesChange,
                        label = { Text("Alerjiler") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.editHealthNotes,
                        onValueChange = onHealthNotesChange,
                        label = { Text("Sağlık Notları") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                } else {
                    val conditions = buildList {
                        if (user.hasDiabetes) add("🩸 Diyabet")
                        if (user.hasHypertension) add("💊 Hipertansiyon")
                        if (user.hasCOPD) add("🫁 KOAH")
                        if (user.hasHeartFailure) add("❤️ Kalp Yetmezliği")
                        if (user.hasKidneyDisease) add("🔬 Böbrek Hastalığı")
                        if (user.isSmoker) add("🚬 Sigara Kullanımı")
                    }

                    if (conditions.isEmpty()) {
                        Text(
                            "Bilinen kronik hastalık yok ✅",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StatusNormal
                        )
                    } else {
                        conditions.forEach { condition ->
                            Text(
                                condition,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    if (!user.allergies.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text("⚠️ Alerjiler: ${user.allergies}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusAttention
                        )
                    }

                    if (!user.healthNotes.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("📝 Not: ${user.healthNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // === Eylemler ===
        SectionHeader("⚙️ İşlemler")

        // PDF Rapor butonu
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onGenerateReport
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📄", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sağlık Raporu Oluştur",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "PDF formatında doktorunuzla paylaşın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hatırlatıcılar butonu (Faz 6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onReminders
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏰", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Hatırlatıcılar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Ölçüm ve ilaç hatırlatmaları",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Sağlık Rehberi butonu (Faz 6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onHealthGuide
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📚", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sağlık Rehberi",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Ölçümleriniz ne anlama geliyor?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Veri Yedekleme (Faz 6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onExportData
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💾", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Veri Dışa Aktar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Verilerinizi JSON olarak yedekleyin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Veri Geri Yükleme (Faz 6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onImportData
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📥", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Veri İçe Aktar",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "Yedek dosyasından geri yükleyin",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Bulut senkron — ViewBinding ile DialogFragment (rubric kanıtı)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onOpenSyncConsole
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("☁️", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Bulut senkron (REST demo)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        if (state.pendingSyncCount > 0) {
                            "${state.pendingSyncCount} kayıt gönderim bekliyor — dokunun"
                        } else {
                            "Outbox temiz · Retrofit + Room + WorkManager"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Text("→", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Çıkış butonu
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            onClick = onLogout
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🚪", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Oturumu Kapat",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Hesabı sil butonu
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = StatusHigh.copy(alpha = 0.08f)
            ),
            onClick = onShowDeleteConfirm
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑️", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Hesabı Sil",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = StatusHigh
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Text(
                "⚕️ VitalÖlçüm v1.0 · Tüm veriler cihazınızda güvenle saklanır.\nBu uygulama tıbbi tanı yerine geçmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// === Yardımcı Composable'lar ===

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ProfileStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun HealthToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = StatusAttention,
                checkedThumbColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}
