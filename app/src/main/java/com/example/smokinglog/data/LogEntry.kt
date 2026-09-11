package com.example.smokinglog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EntryType { SMOKED, RESISTED }

@Entity(tableName = "log_entries")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val type: EntryType,
    val amount: Double = 0.0,
    val note: String = "",
    val urgeStrength: Int? = null,
)
