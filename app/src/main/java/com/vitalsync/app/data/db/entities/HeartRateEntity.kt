package com.vitalsync.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "heart_rate",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class HeartRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val value: Int,            // atım/dk (bpm)
    val context: String = "İstirahat", // "İstirahat", "Egzersiz", "Uyku"
    val hasFever: Boolean = false,
    val hasPalpitations: Boolean = false,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
