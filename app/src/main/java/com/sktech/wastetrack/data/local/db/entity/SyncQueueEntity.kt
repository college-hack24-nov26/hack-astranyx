package com.sktech.wastetrack.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["status", "retryCount", "nextRetryAt"]),
        Index(value = ["entityType", "entityId", "action"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val action: String,
    val payload: String,
    val status: String = "QUEUED",
    val retryCount: Int = 0,
    val nextRetryAt: Long = 0L,
    val lastAttemptedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
