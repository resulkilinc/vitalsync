package com.vitalsync.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "oxygen",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class OxygenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val value: Int,            // SpO2 yüzde (%)
    val hasBreathingDifficulty: Boolean = false,
    val hasPalpitations: Boolean = false,
    val altitude: Int? = null, // Rakım (metre, opsiyonel)
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
