package com.sktech.wastetrack.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.gson.Gson
import com.sktech.wastetrack.data.local.db.dao.SyncQueueDao
import com.sktech.wastetrack.data.local.db.dao.UserDao
import com.sktech.wastetrack.data.local.db.entity.SyncQueueEntity
import com.sktech.wastetrack.service.SyncWorker
import com.sktech.wastetrack.util.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncQueueDao: SyncQueueDao,
    private val userDao: UserDao,
    private val networkMonitor: NetworkMonitor
) {
    companion object {
        private const val TAG = "CloudSyncEngine"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()
    private val workManager by lazy { WorkManager.getInstance(context) }

    init {
        scope.launch {
            networkMonitor.addOnNetworkRestoredListener {
                Log.d(TAG, "Network restored. Triggering immediate offline queue drain.")
                triggerImmediateSync()
            }
        }
    }

    suspend fun enqueueMutation(
        entityType: String,
        entityId: String,
        action: String,
        payloadObj: Any
    ) {
        val payloadJson = if (payloadObj is String) payloadObj else gson.toJson(payloadObj)
        val entity = SyncQueueEntity(
            entityType = entityType,
            entityId = entityId,
            action = action,
            payload = payloadJson
        )
        syncQueueDao.enqueueDeduplicated(entity)
        if (networkMonitor.isOnline.value) {
            triggerImmediateSync()
        }
    }

    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME + "_immediate",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun getPendingCount(): Flow<Int> = syncQueueDao.getCount()
}
