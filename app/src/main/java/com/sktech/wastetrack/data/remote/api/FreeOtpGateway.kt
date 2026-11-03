package com.sktech.wastetrack.data.remote.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.sktech.wastetrack.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreeOtpGateway @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "FreeOtpGateway"
        private const val CHANNEL_ID = "otp_notifications_channel"
    }

    private data class OtpSession(
        val sessionId: String?,
        val localOtp: String?,
        val expiryTime: Long
    )

    private val activeSessions = java.util.concurrent.ConcurrentHashMap<String, OtpSession>()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OTP Verification Codes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Delivers login OTP verification codes"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    enum class DeliveryMode {
        BANNER_ONLY,
        VOICE_CALL,
        BOTH
    }

    suspend fun sendOtp(
        phone: String,
        mode: DeliveryMode = DeliveryMode.BANNER_ONLY,
        apiKey: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanDigits = phone.filter { it.isDigit() }
            val tenDigitPhone = if (cleanDigits.length >= 10) cleanDigits.takeLast(10) else cleanDigits

            if (tenDigitPhone.length != 10) {
                return@withContext Result.failure(IllegalArgumentException("Invalid 10-digit mobile number"))
            }

            val activeKey = apiKey.ifBlank { com.sktech.wastetrack.BuildConfig.SMS_API_KEY }.trim()
            var sessionId: String? = null
            val random = SecureRandom()
            val localOtp = String.format("%06d", random.nextInt(900000) + 100000)
            val expiry = System.currentTimeMillis() + (5 * 60 * 1000)

            try {
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("WasteTrack Verification Code")
                    .setContentText("Your login OTP is: $localOtp (Valid for 5 mins)")
                    .setStyle(NotificationCompat.BigTextStyle().bigText("Your WasteTrack login OTP is: $localOtp.\nValid for 5 minutes."))
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .build()

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.notify(1001, notification)
                Log.d(TAG, "📲 System notification bar posted for OTP: $localOtp")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to post system notification: ${e.message}")
            }

            if (mode == DeliveryMode.VOICE_CALL) {
                if (activeKey.isNotBlank()) {
                    try {
                        val voiceUrl = "https://2factor.in/API/V1/$activeKey/VOICE/$tenDigitPhone/$localOtp"
                        val request = Request.Builder().url(voiceUrl).get().build()
                        val response = okHttpClient.newCall(request).execute()
                        val responseBody = response.body?.string() ?: ""
                        Log.d(TAG, "2Factor Voice Response: $responseBody")

                        if (response.isSuccessful) {
                            val json = JSONObject(responseBody)
                            if (json.optString("Status").equals("Success", ignoreCase = true)) {
                                sessionId = json.optString("Details")
                                Log.d(TAG, "✅ 2Factor Voice Call initiated to +91 $tenDigitPhone. Session: $sessionId")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "2Factor Voice error: ${e.message}")
                    }
                }
            }

            activeSessions[tenDigitPhone] = OtpSession(
                sessionId = sessionId,
                localOtp = localOtp,
                expiryTime = expiry
            )

            Result.success(localOtp ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Failed sending OTP: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendSmsOtp(
        phone: String,
        apiKey: String = ""
    ): Result<String> = sendOtp(phone, DeliveryMode.BOTH, apiKey)

    suspend fun verifySmsOtp(
        phone: String,
        enteredCode: String,
        apiKey: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanDigits = phone.filter { it.isDigit() }
        val tenDigitPhone = if (cleanDigits.length >= 10) cleanDigits.takeLast(10) else cleanDigits
        val session = activeSessions[tenDigitPhone] ?: return@withContext false

        val now = System.currentTimeMillis()
        if (now > session.expiryTime) {
            activeSessions.remove(tenDigitPhone)
            return@withContext false
        }

        val entered = enteredCode.trim()

        if (session.localOtp != null && (entered == session.localOtp || (session.localOtp.startsWith(entered) && entered.length >= 4))) {
            activeSessions.remove(tenDigitPhone)
            return@withContext true
        }

        val activeKey = apiKey.ifBlank { com.sktech.wastetrack.BuildConfig.SMS_API_KEY }.trim()
        val sessionId = session.sessionId

        if (!sessionId.isNullOrBlank() && activeKey.isNotBlank()) {
            try {
                val verifyUrl = "https://2factor.in/API/V1/$activeKey/SMS/VERIFY/$sessionId/$entered"
                val request = Request.Builder().url(verifyUrl).get().build()
                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "2Factor Verify Response: $responseBody")

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val status = json.optString("Status")
                    val details = json.optString("Details")
                    if (status.equals("Success", ignoreCase = true) || details.contains("Matched", ignoreCase = true)) {
                        activeSessions.remove(tenDigitPhone)
                        return@withContext true
                    }
                }

                val voiceVerifyUrl = "https://2factor.in/API/V1/$activeKey/VOICE/VERIFY/$sessionId/$entered"
                val vReq = Request.Builder().url(voiceVerifyUrl).get().build()
                val vResp = okHttpClient.newCall(vReq).execute()
                val vBody = vResp.body?.string() ?: ""
                if (vResp.isSuccessful) {
                    val json = JSONObject(vBody)
                    val status = json.optString("Status")
                    val details = json.optString("Details")
                    if (status.equals("Success", ignoreCase = true) || details.contains("Matched", ignoreCase = true)) {
                        activeSessions.remove(tenDigitPhone)
                        return@withContext true
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "2Factor verification request error: ${e.message}")
            }
        }

        return@withContext false
    }
}
