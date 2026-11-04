package com.sktech.wastetrack.service

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sktech.wastetrack.data.local.db.dao.SyncQueueDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao
) : CoroutineWorker(context, params) {

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            syncQueueDao.resetStaleItems()

            val pendingItems = syncQueueDao.dequeueOldest(limit = 50)
            if (pendingItems.isEmpty()) {
                return@withContext Result.success()
            }

            syncQueueDao.markInProgress(pendingItems.map { it.id })

            Log.d(TAG, "SyncWorker started: processing ${pendingItems.size} pending items")

            for (item in pendingItems) {
                try {
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type
                    val data: Map<String, Any> = gson.fromJson(item.payload, mapType)

                    val collectionName = when (item.entityType.uppercase()) {
                        "USER" -> "users"
                        "FARM" -> "farms"
                        "CROP_CYCLE" -> "crop_cycles"
                        "CROP_ACTIVITY" -> "crop_activities"
                        "SOIL_PROFILE" -> "soil_profiles"
                        "WASTE_BATCH", "BIOMASS_BATCH" -> "waste_batches"
                        "TRANSFER", "SHIPMENT" -> "transfers"
                        "PAYMENT" -> "payments"
                        "LISTING" -> "listings"
                        "ORDER" -> "orders"
                        else -> item.entityType.lowercase() + "s"
                    }

                    firestore.collection(collectionName)
                        .document(item.entityId)
                        .set(data, SetOptions.merge())
                        .await()

                    syncQueueDao.deleteById(item.id)
                    Log.d(TAG, "Synced ${item.entityType}: ${item.entityId}")
                } catch (itemException: Exception) {
                    Log.w(TAG, "Failed to sync item ${item.id} (${item.entityType}): ${itemException.message}")
                    val backoffMs = 30_000L * (1L shl item.retryCount.coerceAtMost(4))
                    syncQueueDao.incrementRetry(item.id, System.currentTimeMillis() + backoffMs)
                }
            }

            syncQueueDao.clearFailed(maxRetries = 5)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker encountered an error: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        const val TAG = "WasteTrackSyncWorker"
        const val WORK_NAME = "wastetrack_cloud_sync"
    }
}
