package com.vitalsync.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blood_pressure",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class BloodPressureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val systolic: Int,       // Büyük tansiyon (mmHg)
    val diastolic: Int,      // Küçük tansiyon (mmHg)
    val pulse: Int? = null,  // Nabız (opsiyonel, cihazdan gelen)
    val arm: String = "Sol", // "Sol" veya "Sağ"
    val context: String = "İstirahat", // "İstirahat", "Egzersiz Sonrası", "Stres"
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
