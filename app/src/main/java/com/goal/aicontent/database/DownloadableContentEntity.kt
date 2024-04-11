package com.goal.aicontent.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DownloadableContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Auto-generated ID for the entity
    val taskId: String, // Unique identifier received from the backend
    val title: String,
    val downloadUrl: String?,
    val filePath: String?,
    val duration: Long,
    val status: String // Consider using an Enum for predefined status values
)