package com.sktech.wastetrack.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryWithBackoffAndJitterInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRIES = 3
        private const val BASE_BACKOFF_MS = 800L
        private const val MAX_BACKOFF_MS = 6000L
    }

    private val random = Random()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var attempt = 0
        var lastException: IOException? = null

        while (attempt < MAX_RETRIES) {
            try {
                response = chain.proceed(request)

                if (response.isSuccessful || (response.code in 400..499 && response.code != 429)) {
                    return response
                }

                if (response.code == 429 || response.code in 500..599) {
                    attempt++
                    if (attempt >= MAX_RETRIES) {
                        return response
                    }

                    val retryAfterSeconds = response.header("Retry-After")?.toLongOrNull()
                    response.close()

                    val backoffMs = if (retryAfterSeconds != null && retryAfterSeconds in 1..60) {
                        retryAfterSeconds * 1000L
                    } else {
                        calculateBackoffWithJitter(attempt)
                    }

                    Log.w(
                        TAG,
                        "Transient HTTP ${response.code} on attempt $attempt for ${request.url.encodedPath}. Backing off ${backoffMs}ms before retry..."
                    )
                    sleep(backoffMs)
                } else {
                    return response
                }
            } catch (e: SocketTimeoutException) {
                attempt++
                lastException = e
                if (attempt >= MAX_RETRIES) {
                    throw e
                }
                val backoffMs = calculateBackoffWithJitter(attempt)
                Log.w(TAG, "Socket timeout on attempt $attempt for ${request.url.encodedPath}. Retrying in ${backoffMs}ms...")
                sleep(backoffMs)
            } catch (e: IOException) {
                throw e
            }
        }

        return response ?: throw (lastException ?: IOException("Request failed after $MAX_RETRIES attempts"))
    }

    private fun calculateBackoffWithJitter(attempt: Int): Long {
        val exponential = BASE_BACKOFF_MS * (1L shl (attempt - 1))
        val capped = Math.min(exponential, MAX_BACKOFF_MS)
        val jitter = random.nextInt(400)
        return capped + jitter
    }

    private fun sleep(millis: Long) {
        try {
            Thread.sleep(millis)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}
