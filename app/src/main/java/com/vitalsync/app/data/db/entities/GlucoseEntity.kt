package com.vitalsync.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "glucose",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class GlucoseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val value: Int,            // mg/dL
    val context: String,       // "Açlık", "Tokluk", "Rastgele", "OGTT"
    val method: String = "Parmak Ucu", // "Parmak Ucu", "Laboratuvar"
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
