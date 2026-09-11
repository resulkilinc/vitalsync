package com.vitalsync.app.presentation.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitalsync.app.data.db.entities.ReminderEntity
import com.vitalsync.app.data.repository.ReminderRepository
import com.vitalsync.app.util.ReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderScreenState(
    val reminders: List<ReminderEntity> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingReminder: ReminderEntity? = null,

    // Form alanları
    val formTitle: String = "",
    val formDescription: String = "",
    val formType: String = "MEASUREMENT", // "MEASUREMENT" veya "MEDICATION"
    val formHour: Int = 8,
    val formMinute: Int = 0,
    val formError: String? = null,
    val showDeleteConfirm: Boolean = false,
    val deletingReminderId: Long? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ReminderScreenState())
    val state: StateFlow<ReminderScreenState> = _state.asStateFlow()

    private var userId: Long = -1

    fun setUserId(id: Long) {
        userId = id
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            reminderRepository.getAllReminders(userId).collect { list ->
                _state.update { it.copy(reminders = list, isLoading = false) }
            }
        }
    }

    // === Dialog kontrolleri ===
    fun showAddDialog() {
        _state.update {
            it.copy(
                showAddDialog = true,
                formTitle = "",
                formDescription = "",
                formType = "MEASUREMENT",
                formHour = 8,
                formMinute = 0,
                formError = null
            )
        }
    }

    fun showEditDialog(reminder: ReminderEntity) {
        _state.update {
            it.copy(
                showEditDialog = true,
                editingReminder = reminder,
                formTitle = reminder.title,
                formDescription = reminder.description,
                formType = reminder.type,
                formHour = reminder.hour,
                formMinute = reminder.minute,
                formError = null
            )
        }
    }

    fun dismissDialog() {
        _state.update {
            it.copy(
                showAddDialog = false,
                showEditDialog = false,
                editingReminder = null,
                formError = null
            )
        }
    }

    // === Form güncelleme ===
    fun updateFormTitle(v: String) { _state.update { it.copy(formTitle = v) } }
    fun updateFormDescription(v: String) { _state.update { it.copy(formDescription = v) } }
    fun updateFormType(v: String) { _state.update { it.copy(formType = v) } }
    fun updateFormHour(v: Int) { _state.update { it.copy(formHour = v.coerceIn(0, 23)) } }
    fun updateFormMinute(v: Int) { _state.update { it.copy(formMinute = v.coerceIn(0, 59)) } }

    // === CRUD işlemleri ===
    fun saveReminder() {
        val s = _state.value
        if (s.formTitle.isBlank()) {
            _state.update { it.copy(formError = "Başlık boş bırakılamaz") }
            return
        }

        viewModelScope.launch {
            val id = reminderRepository.insertReminder(
                userId = userId,
                title = s.formTitle,
                description = s.formDescription.ifBlank { getDefaultDescription(s.formType) },
                type = s.formType,
                hour = s.formHour,
                minute = s.formMinute
            )

            // WorkManager ile planla
            ReminderWorker.scheduleReminder(
                context = appContext,
                reminderId = id,
                title = s.formTitle,
                description = s.formDescription.ifBlank { getDefaultDescription(s.formType) },
                hour = s.formHour,
                minute = s.formMinute
            )

            dismissDialog()
        }
    }

    fun updateReminder() {
        val s = _state.value
        val existing = s.editingReminder ?: return

        if (s.formTitle.isBlank()) {
            _state.update { it.copy(formError = "Başlık boş bırakılamaz") }
            return
        }

        viewModelScope.launch {
            val updated = existing.copy(
                title = s.formTitle,
                description = s.formDescription.ifBlank { getDefaultDescription(s.formType) },
                type = s.formType,
                hour = s.formHour,
                minute = s.formMinute
            )
            reminderRepository.updateReminder(updated)

            // Takvimi güncelle
            ReminderWorker.cancelReminder(appContext, existing.id)
            if (updated.isEnabled) {
                ReminderWorker.scheduleReminder(
                    context = appContext,
                    reminderId = updated.id,
                    title = updated.title,
                    description = updated.description,
                    hour = updated.hour,
                    minute = updated.minute
                )
            }

            dismissDialog()
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            val newEnabled = !reminder.isEnabled
            reminderRepository.toggleReminder(reminder.id, newEnabled)

            if (newEnabled) {
                ReminderWorker.scheduleReminder(
                    context = appContext,
                    reminderId = reminder.id,
                    title = reminder.title,
                    description = reminder.description,
                    hour = reminder.hour,
                    minute = reminder.minute
                )
            } else {
                ReminderWorker.cancelReminder(appContext, reminder.id)
            }
        }
    }

    fun showDeleteConfirm(reminderId: Long) {
        _state.update { it.copy(showDeleteConfirm = true, deletingReminderId = reminderId) }
    }

    fun hideDeleteConfirm() {
        _state.update { it.copy(showDeleteConfirm = false, deletingReminderId = null) }
    }

    fun deleteReminder() {
        val reminderId = _state.value.deletingReminderId ?: return

        viewModelScope.launch {
            val reminder = reminderRepository.getReminderById(reminderId) ?: return@launch
            ReminderWorker.cancelReminder(appContext, reminderId)
            reminderRepository.deleteReminder(reminder)
            hideDeleteConfirm()
        }
    }

    private fun getDefaultDescription(type: String): String = when (type) {
        "MEASUREMENT" -> "Sağlık ölçümünüzü yapmayı unutmayın!"
        "MEDICATION" -> "İlacınızı almayı unutmayın!"
        else -> "Hatırlatma zamanı geldi!"
    }
}
