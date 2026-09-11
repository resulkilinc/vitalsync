package com.vitalsync.app.presentation.reminder

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitalsync.app.data.db.entities.ReminderEntity
import com.vitalsync.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Silme onay dialogu
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("Hatırlatıcıyı Sil", fontWeight = FontWeight.Bold) },
            text = { Text("Bu hatırlatıcı kalıcı olarak silinecektir. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteReminder() },
                    colors = ButtonDefaults.textButtonColors(contentColor = StatusHigh)
                ) { Text("Sil", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) { Text("İptal") }
            }
        )
    }

    // Ekleme / Düzenleme dialogu
    if (state.showAddDialog || state.showEditDialog) {
        ReminderFormDialog(
            isEdit = state.showEditDialog,
            title = state.formTitle,
            description = state.formDescription,
            type = state.formType,
            hour = state.formHour,
            minute = state.formMinute,
            error = state.formError,
            onTitleChange = viewModel::updateFormTitle,
            onDescriptionChange = viewModel::updateFormDescription,
            onTypeChange = viewModel::updateFormType,
            onHourChange = viewModel::updateFormHour,
            onMinuteChange = viewModel::updateFormMinute,
            onDismiss = viewModel::dismissDialog,
            onSave = if (state.showEditDialog) viewModel::updateReminder else viewModel::saveReminder
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hatırlatıcılar",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Hatırlatıcı")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Bilgi kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⏰", fontSize = 36.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Düzenli ölçümlerinizi ve ilaçlarınızı takip etmek için hatırlatıcılar oluşturun",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            } else if (state.reminders.isEmpty()) {
                // Boş durum
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔔", fontSize = 64.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Henüz hatırlatıcı eklemediniz",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Sağ alttaki + butonuna tıklayarak ilk hatırlatıcınızı oluşturun",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onEdit = { viewModel.showEditDialog(reminder) },
                            onDelete = { viewModel.showDeleteConfirm(reminder.id) }
                        )
                    }

                    item {
                        Spacer(Modifier.height(80.dp)) // FAB için boşluk
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val alpha = if (reminder.isEnabled) 1f else 0.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon
            Icon(
                imageVector = if (reminder.isEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                contentDescription = null,
                tint = if (reminder.isEnabled) MaterialTheme.colorScheme.primary else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Bilgi
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    reminder.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Spacer(Modifier.height(2.dp))

                val typeLabel = when (reminder.type) {
                    "MEASUREMENT" -> "📊 Ölçüm"
                    "MEDICATION" -> "💊 İlaç"
                    else -> "🔔 Hatırlatma"
                }
                Text(
                    "$typeLabel · ${String.format("%02d:%02d", reminder.hour, reminder.minute)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f * alpha)
                )

                if (reminder.description.isNotBlank()) {
                    Text(
                        reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f * alpha),
                        maxLines = 1
                    )
                }
            }

            // Aksiyonlar
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit, "Düzenle",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete, "Sil",
                    tint = StatusHigh.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Switch(
                checked = reminder.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderFormDialog(
    isEdit: Boolean,
    title: String,
    description: String,
    type: String,
    hour: Int,
    minute: Int,
    error: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEdit) "Hatırlatıcıyı Düzenle" else "Yeni Hatırlatıcı",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                // Başlık
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Başlık") },
                    placeholder = { Text("Ör: Sabah Tansiyonu") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = error != null
                )

                if (error != null) {
                    Text(
                        error,
                        color = StatusHigh,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Açıklama
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Açıklama (opsiyonel)") },
                    placeholder = { Text("Ör: Aç karnına ölçüm") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Tür seçimi
                Text("Tür", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "MEASUREMENT",
                        onClick = { onTypeChange("MEASUREMENT") },
                        label = { Text("📊 Ölçüm") }
                    )
                    FilterChip(
                        selected = type == "MEDICATION",
                        onClick = { onTypeChange("MEDICATION") },
                        label = { Text("💊 İlaç") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Saat seçimi
                Text("Saat", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Saat
                    OutlinedTextField(
                        value = String.format("%02d", hour),
                        onValueChange = { v ->
                            v.filter { it.isDigit() }.take(2).toIntOrNull()?.let { onHourChange(it) }
                        },
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        " : ",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Dakika
                    OutlinedTextField(
                        value = String.format("%02d", minute),
                        onValueChange = { v ->
                            v.filter { it.isDigit() }.take(2).toIntOrNull()?.let { onMinuteChange(it) }
                        },
                        modifier = Modifier.width(72.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isEdit) "Güncelle" else "Kaydet", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
