package com.sktech.wastetrack.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    OTHER,
    NONE
}

sealed class NetworkStatus {
    data class Connected(
        val connectionType: ConnectionType,
        val isMetered: Boolean,
        val isValidated: Boolean
    ) : NetworkStatus()

    data class CaptivePortal(val loginUrl: String? = null) : NetworkStatus()

    data object Disconnected : NetworkStatus()

    val isOnline: Boolean
        get() = this is Connected && isValidated
}

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "NetworkMonitor"
        private const val WAN_PROBE_HOST = "8.8.8.8"
        private const val WAN_PROBE_PORT = 53
        private const val HTTP_PROBE_URL = "https://connectivitycheck.gstatic.com/generate_204"
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _networkStatus = MutableStateFlow<NetworkStatus>(resolveCurrentStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _isOnline = MutableStateFlow(resolveCurrentStatus().isOnline)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val networkRestoredListeners = CopyOnWriteArrayList<() -> Unit>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateStatusAsync(network)
        }

        override fun onLost(network: Network) {
            _networkStatus.value = NetworkStatus.Disconnected
            _isOnline.value = false
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val status = mapCapabilitiesToStatus(capabilities)
            val wasOffline = !_isOnline.value
            _networkStatus.value = status
            _isOnline.value = status.isOnline

            if (wasOffline && status.isOnline) {
                notifyNetworkRestored()
            }
        }
    }

    init {
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback: ${e.message}", e)
        }
    }

    private fun updateStatusAsync(network: Network) {
        scope.launch {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val status = if (capabilities != null) {
                mapCapabilitiesToStatus(capabilities)
            } else {
                NetworkStatus.Disconnected
            }
            val wasOffline = !_isOnline.value
            _networkStatus.value = status
            _isOnline.value = status.isOnline

            if (wasOffline && status.isOnline) {
                notifyNetworkRestored()
            }
        }
    }

    private fun mapCapabilitiesToStatus(capabilities: NetworkCapabilities): NetworkStatus {
        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return NetworkStatus.Disconnected

        val isCaptivePortal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
        } else false

        if (isCaptivePortal) {
            return NetworkStatus.CaptivePortal()
        }

        val isValidated = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            hasInternet
        }

        val connectionType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.OTHER
        }

        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        return NetworkStatus.Connected(
            connectionType = connectionType,
            isMetered = isMetered,
            isValidated = isValidated
        )
    }

    private fun resolveCurrentStatus(): NetworkStatus {
        val network = connectivityManager.activeNetwork ?: return NetworkStatus.Disconnected
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.Disconnected
        return mapCapabilitiesToStatus(capabilities)
    }

    suspend fun checkRealInternetReachability(timeoutMs: Long = 2500L): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(WAN_PROBE_HOST, WAN_PROBE_PORT), timeoutMs.toInt())
                return@withContext true
            }
        } catch (_: Exception) {
        }

        try {
            val connection = (URL(HTTP_PROBE_URL).openConnection() as HttpURLConnection).apply {
                connectTimeout = timeoutMs.toInt()
                readTimeout = timeoutMs.toInt()
                instanceFollowRedirects = false
                useCaches = false
            }
            try {
                connection.connect()
                val responseCode = connection.responseCode
                return@withContext responseCode == 204 || responseCode == 200
            } finally {
                connection.disconnect()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun addOnNetworkRestoredListener(listener: () -> Unit) {
        networkRestoredListeners.add(listener)
    }

    fun removeOnNetworkRestoredListener(listener: () -> Unit) {
        networkRestoredListeners.remove(listener)
    }

    private fun notifyNetworkRestored() {
        scope.launch {
            networkRestoredListeners.forEach { listener ->
                try {
                    listener()
                } catch (e: Exception) {
                    Log.w(TAG, "Error in network restored listener: ${e.message}")
                }
            }
        }
    }

    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val status = mapCapabilitiesToStatus(capabilities)
                trySend(status.isOnline)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register callback in observeConnectivity: ${e.message}")
        }
        trySend(_isOnline.value)
        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (_: Exception) { }
        }
    }
}
