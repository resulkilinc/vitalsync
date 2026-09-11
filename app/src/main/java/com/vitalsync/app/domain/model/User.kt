package com.vitalsync.app.domain.model

import com.vitalsync.app.data.db.entities.UserEntity

/**
 * UI katmanının kullanacağı sade kullanıcı domain modeli.
 * Entity'deki hash/salt gibi güvenlik alanları burada bulunmaz.
 */
data class User(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val username: String,
    val age: Int,
    val gender: String,
    val heightCm: Float,
    val weightKg: Float,
    val bmi: Float,
    val bloodType: String,
    val rhFactor: String,
    val activityLevel: String,
    val isSmoker: Boolean,
    val hasDiabetes: Boolean,
    val hasHypertension: Boolean,
    val hasCOPD: Boolean,
    val hasHeartFailure: Boolean,
    val hasKidneyDisease: Boolean,
    val allergies: String?,
    val healthNotes: String?,
    val isHealthInfoCompleted: Boolean,
    val createdAt: Long
) {
    val fullName: String get() = "$firstName $lastName"

    val initials: String get() = buildString {
        if (firstName.isNotBlank()) append(firstName.first().uppercaseChar())
        if (lastName.isNotBlank()) append(lastName.first().uppercaseChar())
    }

    val bloodTypeDisplay: String get() = "$bloodType${rhFactor}"

    val bmiCategory: String get() = when {
        bmi < 18.5f -> "Zayıf"
        bmi < 25f -> "Normal"
        bmi < 30f -> "Fazla Kilolu"
        else -> "Obez"
    }
}

// === Extension Fonksiyonları: Entity ↔ Domain dönüşümleri ===

fun UserEntity.toDomain(): User = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    username = username,
    age = age,
    gender = gender,
    heightCm = heightCm,
    weightKg = weightKg,
    bmi = bmi,
    bloodType = bloodType,
    rhFactor = rhFactor,
    activityLevel = activityLevel,
    isSmoker = isSmoker,
    hasDiabetes = hasDiabetes,
    hasHypertension = hasHypertension,
    hasCOPD = hasCOPD,
    hasHeartFailure = hasHeartFailure,
    hasKidneyDisease = hasKidneyDisease,
    allergies = allergies,
    healthNotes = healthNotes,
    isHealthInfoCompleted = isHealthInfoCompleted,
    createdAt = createdAt
)
