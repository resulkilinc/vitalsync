package com.vitalsync.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // === Temel Bilgiler (ZORUNLU) ===
    val firstName: String,
    val lastName: String,
    val username: String,
    val pinHash: String,
    val salt: String,

    // === Fiziksel Bilgiler (ZORUNLU) ===
    val age: Int,
    val gender: String,            // "Erkek", "Kadın", "Belirtmek İstemiyorum"
    val heightCm: Float,
    val weightKg: Float,
    val bmi: Float,
    val bloodType: String,         // "A", "B", "AB", "0"
    val rhFactor: String,          // "+", "-"

    // === Sağlık Bilgileri (OPSİYONEL ama önerilen) ===
    val activityLevel: String = "Belirtilmedi",  // "Sedanter", "Aktif", "Sporcu", "Belirtilmedi"
    val isSmoker: Boolean = false,

    // Kronik hastalıklar
    val hasDiabetes: Boolean = false,
    val hasHypertension: Boolean = false,
    val hasCOPD: Boolean = false,
    val hasHeartFailure: Boolean = false,
    val hasKidneyDisease: Boolean = false,

    // Ek sağlık notları
    val allergies: String? = null,
    val healthNotes: String? = null,

    // Sağlık bilgilerinin doldurulup doldurulmadığı
    val isHealthInfoCompleted: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
