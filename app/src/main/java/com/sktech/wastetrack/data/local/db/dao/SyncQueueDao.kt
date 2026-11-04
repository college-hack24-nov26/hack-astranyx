package com.sktech.wastetrack.data.local.db.dao

import androidx.room.*
import com.sktech.wastetrack.data.local.db.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId AND action = :action")
    suspend fun deleteExistingAction(entityType: String, entityId: String, action: String)

    @Transaction
    suspend fun enqueueDeduplicated(item: SyncQueueEntity) {
        deleteExistingAction(item.entityType, item.entityId, item.action)
        enqueue(item)
    }

    @Query("SELECT * FROM sync_queue WHERE status = 'QUEUED' AND retryCount < :maxRetries AND nextRetryAt <= :now ORDER BY createdAt ASC LIMIT :limit")
    suspend fun dequeueOldest(limit: Int = 50, maxRetries: Int = 5, now: Long = System.currentTimeMillis()): List<SyncQueueEntity>

    @Query("UPDATE sync_queue SET status = 'IN_PROGRESS', lastAttemptedAt = :now WHERE id IN (:ids)")
    suspend fun markInProgress(ids: List<Long>, now: Long = System.currentTimeMillis())

    @Query("UPDATE sync_queue SET status = 'QUEUED' WHERE id = :id")
    suspend fun resetToQueued(id: Long)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sync_queue WHERE entityId = :entityId AND entityType = :entityType")
    suspend fun deleteByEntity(entityId: String, entityType: String)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1, status = 'QUEUED', nextRetryAt = :nextRetryAt WHERE id = :id")
    suspend fun incrementRetry(id: Long, nextRetryAt: Long = System.currentTimeMillis() + 30000L)

    @Query("DELETE FROM sync_queue WHERE retryCount > :maxRetries")
    suspend fun clearFailed(maxRetries: Int = 5)

    @Query("UPDATE sync_queue SET retryCount = 0, nextRetryAt = 0, status = 'QUEUED' WHERE retryCount >= :maxRetries")
    suspend fun resetExhaustedRetries(maxRetries: Int = 5): Int

    @Query("UPDATE sync_queue SET status = 'QUEUED' WHERE status = 'IN_PROGRESS' AND (lastAttemptedAt < :staleThreshold OR (lastAttemptedAt = 0 AND createdAt < :staleThreshold))")
    suspend fun resetStaleItems(staleThreshold: Long = System.currentTimeMillis() - 300000L)

    @Query("SELECT COUNT(*) FROM sync_queue")
    fun getCount(): Flow<Int>
}
