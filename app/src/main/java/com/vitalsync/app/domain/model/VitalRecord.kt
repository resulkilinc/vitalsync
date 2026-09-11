package com.vitalsync.app.domain.model

import com.vitalsync.app.data.db.entities.*
import java.text.SimpleDateFormat
import java.util.*

// === Domain Vital Record Modelleri ===

data class BloodPressureRecord(
    val id: Long,
    val userId: Long,
    val systolic: Int,
    val diastolic: Int,
    val pulse: Int?,
    val arm: String,
    val context: String,
    val notes: String?,
    val timestamp: Long
) {
    val displayValue: String get() = "$systolic/$diastolic"
    val displayUnit: String get() = "mmHg"
    val formattedDate: String get() = formatTimestamp(timestamp)
    val formattedTime: String get() = formatTime(timestamp)
}

data class GlucoseRecord(
    val id: Long,
    val userId: Long,
    val value: Int,
    val context: String,
    val method: String,
    val notes: String?,
    val timestamp: Long
) {
    val displayValue: String get() = "$value"
    val displayUnit: String get() = "mg/dL"
    val formattedDate: String get() = formatTimestamp(timestamp)
    val formattedTime: String get() = formatTime(timestamp)
}

data class HeartRateRecord(
    val id: Long,
    val userId: Long,
    val value: Int,
    val context: String,
    val hasFever: Boolean,
    val hasPalpitations: Boolean,
    val notes: String?,
    val timestamp: Long
) {
    val displayValue: String get() = "$value"
    val displayUnit: String get() = "bpm"
    val formattedDate: String get() = formatTimestamp(timestamp)
    val formattedTime: String get() = formatTime(timestamp)
}

data class OxygenRecord(
    val id: Long,
    val userId: Long,
    val value: Int,
    val hasBreathingDifficulty: Boolean,
    val hasPalpitations: Boolean,
    val altitude: Int?,
    val notes: String?,
    val timestamp: Long
) {
    val displayValue: String get() = "%$value"
    val displayUnit: String get() = "SpO₂"
    val formattedDate: String get() = formatTimestamp(timestamp)
    val formattedTime: String get() = formatTime(timestamp)
}

// === Entity → Domain Dönüşüm Extension'ları ===

fun BloodPressureEntity.toDomain() = BloodPressureRecord(
    id = id, userId = userId, systolic = systolic, diastolic = diastolic,
    pulse = pulse, arm = arm, context = context, notes = notes, timestamp = timestamp
)

fun GlucoseEntity.toDomain() = GlucoseRecord(
    id = id, userId = userId, value = value, context = context,
    method = method, notes = notes, timestamp = timestamp
)

fun HeartRateEntity.toDomain() = HeartRateRecord(
    id = id, userId = userId, value = value, context = context,
    hasFever = hasFever, hasPalpitations = hasPalpitations,
    notes = notes, timestamp = timestamp
)

fun OxygenEntity.toDomain() = OxygenRecord(
    id = id, userId = userId, value = value,
    hasBreathingDifficulty = hasBreathingDifficulty,
    hasPalpitations = hasPalpitations, altitude = altitude,
    notes = notes, timestamp = timestamp
)

// === Yardımcı Fonksiyonlar ===

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
    return sdf.format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("tr", "TR"))
    return sdf.format(Date(timestamp))
}
