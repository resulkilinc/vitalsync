package com.vitalsync.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,           // "Tansiyon Ölçümü", "İlaç Hatırlatma"
    val description: String,     // "Sabah tansiyonunuzu ölçmeyi unutmayın"
    val type: String,            // "MEASUREMENT" veya "MEDICATION"
    val hour: Int,               // 0-23
    val minute: Int,             // 0-59
    val isEnabled: Boolean = true,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // Pazartesi=1 .. Pazar=7
    val createdAt: Long = System.currentTimeMillis()
)
