package com.sktech.wastetrack.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sktech.wastetrack.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import android.util.LruCache
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.*

data class AppLocation(
    val latitude: Double,
    val longitude: Double,
    val city: String? = null,
    val locality: String? = null,
    val formattedAddress: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class LocationFetchResult {
    data class Success(val location: AppLocation) : LocationFetchResult()
    object PermissionRequired : LocationFetchResult()
    object GpsDisabled : LocationFetchResult()
    data class Error(val message: String) : LocationFetchResult()
}

object LocationHelper {

    private val _currentLocation = MutableStateFlow<AppLocation?>(null)
    val currentLocation: StateFlow<AppLocation?> = _currentLocation.asStateFlow()

    fun init(context: Context) {
        if (_currentLocation.value == null) {
            try {
                val prefs = context.getSharedPreferences("agristock_location_prefs", Context.MODE_PRIVATE)
                val lat = prefs.getString("cached_lat", null)?.toDoubleOrNull()
                val lng = prefs.getString("cached_lng", null)?.toDoubleOrNull()
                if (lat != null && lng != null) {
                    _currentLocation.value = AppLocation(
                        latitude = lat,
                        longitude = lng,
                        city = prefs.getString("cached_city", null),
                        locality = prefs.getString("cached_locality", null),
                        formattedAddress = prefs.getString("cached_formatted", null),
                        timestamp = prefs.getLong("cached_time", 0L)
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun isGpsEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return false
        return lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
               lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
    }

    fun openGpsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    suspend fun fetchLocation(context: Context, forceRefresh: Boolean = false): LocationFetchResult {
        init(context)
        if (!hasLocationPermission(context)) {
            return LocationFetchResult.PermissionRequired
        }

        if (!isGpsEnabled(context)) {
            return LocationFetchResult.GpsDisabled
        }

        val rawLoc = getCurrentLocation(context) ?: return LocationFetchResult.Error("Unable to acquire GPS signal. Please ensure location services are enabled.")

        val (city, locality) = getCityAndLocality(context, rawLoc)
        val formatted = getFormattedLocationName(context, rawLoc.latitude, rawLoc.longitude)
            ?: listOfNotNull(locality, city, "Maharashtra").joinToString(", ")

        val appLoc = AppLocation(
            latitude = rawLoc.latitude,
            longitude = rawLoc.longitude,
            city = city,
            locality = locality,
            formattedAddress = formatted,
            timestamp = System.currentTimeMillis()
        )

        _currentLocation.value = appLoc

        try {
            val prefs = context.getSharedPreferences("agristock_location_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("cached_lat", appLoc.latitude.toString())
                .putString("cached_lng", appLoc.longitude.toString())
                .putString("cached_city", appLoc.city)
                .putString("cached_locality", appLoc.locality)
                .putString("cached_formatted", appLoc.formattedAddress)
                .putLong("cached_time", appLoc.timestamp)
                .apply()
        } catch (_: Exception) {}

        return LocationFetchResult.Success(appLoc)
    }

    fun updateLocationManually(context: Context, lat: Double, lng: Double, city: String?, address: String?) {
        val appLoc = AppLocation(
            latitude = lat,
            longitude = lng,
            city = city,
            formattedAddress = address,
            timestamp = System.currentTimeMillis()
        )
        _currentLocation.value = appLoc
        try {
            val prefs = context.getSharedPreferences("agristock_location_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("cached_lat", appLoc.latitude.toString())
                .putString("cached_lng", appLoc.longitude.toString())
                .putString("cached_city", appLoc.city)
                .putString("cached_formatted", appLoc.formattedAddress)
                .putLong("cached_time", appLoc.timestamp)
                .apply()
        } catch (_: Exception) {}
    }

    data class RoadRouteResult(
        val points: List<Pair<Double, Double>>,
        val distanceKm: Double,
        val durationMinutes: Int,
        val isRealRoad: Boolean = true
    )

    private val routeCache = LruCache<String, RoadRouteResult>(50)

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    fun hasFineLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean {
        return hasFineLocationPermission(context) ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    @android.annotation.SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val lastLoc = try {
                kotlinx.coroutines.withTimeoutOrNull(1500L) {
                    fusedLocationClient.lastLocation.await()
                }
            } catch (e: Exception) {
                null
            }
            if (lastLoc != null) return lastLoc

            val priority = if (hasFineLocationPermission(context)) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }

            val cts = com.google.android.gms.tasks.CancellationTokenSource()
            val freshLoc = try {
                kotlinx.coroutines.withTimeoutOrNull(4500L) {
                    fusedLocationClient.getCurrentLocation(priority, cts.token).await()
                }
            } catch (e: Exception) {
                null
            } finally {
                cts.cancel()
            }
            if (freshLoc != null) return freshLoc
        } catch (e: SecurityException) {
            android.util.Log.w("LocationHelper", "SecurityException in FusedLocationClient: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.w("LocationHelper", "Exception in FusedLocationClient: ${e.message}")
        }

        return getFallbackSystemLocation(context)
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun getFallbackSystemLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return null
            val providers = listOf(
                android.location.LocationManager.GPS_PROVIDER,
                android.location.LocationManager.NETWORK_PROVIDER,
                android.location.LocationManager.PASSIVE_PROVIDER
            )
            var bestLocation: Location? = null
            for (provider in providers) {
                try {
                    if (locationManager.isProviderEnabled(provider)) {
                        val loc = locationManager.getLastKnownLocation(provider)
                        if (loc != null) {
                            if (bestLocation == null || loc.time > bestLocation.time) {
                                bestLocation = loc
                            }
                        }
                    }
                } catch (_: SecurityException) {
                } catch (_: Exception) {
                }
            }
            bestLocation
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFormattedLocationName(context: Context, latitude: Double, longitude: Double): String? {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val city = address.locality ?: address.subAdminArea ?: address.subLocality
                    val state = address.adminArea
                    listOfNotNull(city, state).filter { it.isNotBlank() }.joinToString(", ")
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getCityAndLocality(context: Context, location: Location): Pair<String?, String?> {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address: Address = addresses[0]
                    val city = address.locality ?: address.subAdminArea ?: address.adminArea
                    val subLocality = address.subLocality ?: address.featureName
                    Pair(city, subLocality)
                } else {
                    Pair(null, null)
                }
            } catch (e: Exception) {
                Pair(null, null)
            }
        }
    }

    suspend fun geocodeAddress(context: Context, addressStr: String): Pair<Double, Double>? {
        val query = addressStr.trim()
        if (query.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    Pair(addr.latitude, addr.longitude)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if ((lat1 == 0.0 && lon1 == 0.0) || (lat2 == 0.0 && lon2 == 0.0)) return 48.0

        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun calculateRoadDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val direct = calculateDistanceKm(lat1, lon1, lat2, lon2)
        return (direct * 1.22).coerceAtLeast(4.5)
    }

    fun calculateTransitMinutes(distanceKm: Double, avgSpeedKmh: Double = 42.0): Int {
        return ((distanceKm / avgSpeedKmh) * 60).toInt().coerceAtLeast(15)
    }

    fun formatEta(distanceKm: Double): String {
        val mins = calculateTransitMinutes(distanceKm)
        val timeStr = if (mins >= 60) {
            val hrs = mins / 60
            val rem = mins % 60
            if (rem == 0) "$hrs hr" else "$hrs hr $rem mins"
        } else {
            "$mins mins"
        }
        return "ETA: ~$timeStr • ${String.format("%.1f", distanceKm)} km"
    }

    fun getIndustrialCoordinates(nameOrCity: String, isDestination: Boolean = false): Pair<Double, Double> {
        val query = nameOrCity.trim().lowercase()
        return when {
            query.contains("satpur") -> Pair(19.9980, 73.7420)
            query.contains("ambad") -> Pair(19.9576, 73.7436)
            query.contains("kopargaon") -> Pair(19.8828, 74.4789)
            query.contains("sinnar") -> Pair(19.8450, 74.0020)
            query.contains("shirdi") -> Pair(19.8940, 74.4760)
            query.contains("chakan") -> Pair(18.7562, 73.8580)
            query.contains("bhosari") || query.contains("pimpri") || query.contains("pune") -> Pair(18.6298, 73.8478)
            query.contains("talegaon") -> Pair(18.7300, 73.6800)
            query.contains("aurangabad") || query.contains("sambhaji") || query.contains("waluj") -> Pair(19.8350, 75.2450)
            query.contains("rabale") || query.contains("mahape") || query.contains("navi mumbai") || query.contains("thane") || query.contains("mumbai") -> Pair(19.1438, 73.0088)
            query.contains("tarapur") -> Pair(19.8150, 72.7050)
            query.contains("kurkumbh") -> Pair(18.3600, 74.5200)
            query.contains("nagpur") || query.contains("butibori") -> Pair(20.9300, 78.9800)
            query.contains("nashik") -> if (isDestination) Pair(19.9980, 73.7420) else Pair(19.9576, 73.7436)
            query.contains("rec") || query.contains("smelt") || query.contains("steel") || query.contains("polymer") || isDestination -> Pair(19.8828, 74.4789)
            else -> if (isDestination) Pair(19.8828, 74.4789) else Pair(19.9576, 73.7436)
        }
    }

    private fun buildCacheKey(lat1: Double, lon1: Double, lat2: Double, lon2: Double, wpLat: Double = 0.0, wpLon: Double = 0.0): String {
        return "${String.format(Locale.US, "%.4f,%.4f", lat1, lon1)}->${String.format(Locale.US, "%.4f,%.4f", lat2, lon2)}|${String.format(Locale.US, "%.4f,%.4f", wpLat, wpLon)}"
    }

    suspend fun fetchRealRoadRoute(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double,
        waypointLat: Double = 0.0,
        waypointLon: Double = 0.0
    ): RoadRouteResult = withContext(Dispatchers.IO) {
        val validOriginLat = if (originLat == 0.0) 19.9576 else originLat
        val validOriginLon = if (originLon == 0.0) 73.7436 else originLon
        var validDestLat = if (destLat == 0.0) 19.8828 else destLat
        var validDestLon = if (destLon == 0.0) 74.4789 else destLon

        if (abs(validOriginLat - validDestLat) < 0.001 && abs(validOriginLon - validDestLon) < 0.001) {
            validDestLat = 19.8828
            validDestLon = 74.4789
        }

        val key = buildCacheKey(validOriginLat, validOriginLon, validDestLat, validDestLon, waypointLat, waypointLon)
        routeCache.get(key)?.let { return@withContext it }

        val osrmResult = tryFetchOsrmRoute(validOriginLat, validOriginLon, validDestLat, validDestLon, waypointLat, waypointLon)
        if (osrmResult != null && osrmResult.points.size >= 2) {
            routeCache.put(key, osrmResult)
            return@withContext osrmResult
        }

        val googleResult = tryFetchGoogleDirections(validOriginLat, validOriginLon, validDestLat, validDestLon, waypointLat, waypointLon)
        if (googleResult != null && googleResult.points.size >= 2) {
            routeCache.put(key, googleResult)
            return@withContext googleResult
        }

        val fallbackResult = getOfflineCorridorRoute(validOriginLat, validOriginLon, validDestLat, validDestLon, waypointLat, waypointLon)
        routeCache.put(key, fallbackResult)
        return@withContext fallbackResult
    }

    fun getCachedOrCorridorRoute(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double,
        waypointLat: Double = 0.0,
        waypointLon: Double = 0.0
    ): RoadRouteResult {
        val validOriginLat = if (originLat == 0.0) 19.9576 else originLat
        val validOriginLon = if (originLon == 0.0) 73.7436 else originLon
        var validDestLat = if (destLat == 0.0) 19.8828 else destLat
        var validDestLon = if (destLon == 0.0) 74.4789 else destLon

        if (abs(validOriginLat - validDestLat) < 0.001 && abs(validOriginLon - validDestLon) < 0.001) {
            validDestLat = 19.8828
            validDestLon = 74.4789
        }

        val key = buildCacheKey(validOriginLat, validOriginLon, validDestLat, validDestLon, waypointLat, waypointLon)
        routeCache.get(key)?.let { return it }

        return getOfflineCorridorRoute(validOriginLat, validOriginLon, validDestLat, validDestLon, waypointLat, waypointLon)
    }

    private fun tryFetchOsrmRoute(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        wpLat: Double, wpLon: Double
    ): RoadRouteResult? {
        return try {
            val hasWp = wpLat != 0.0 && wpLon != 0.0
            val url = if (hasWp) {
                "https://router.project-osrm.org/route/v1/driving/$lon1,$lat1;$wpLon,$wpLat;$lon2,$lat2?overview=full&geometries=geojson"
            } else {
                "https://router.project-osrm.org/route/v1/driving/$lon1,$lat1;$lon2,$lat2?overview=full&geometries=geojson"
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WasteTrack-Enterprise/1.0 (Android; Transport-Logistics)")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                if (json.optString("code") != "Ok") return null

                val routes = json.optJSONArray("routes") ?: return null
                if (routes.length() == 0) return null

                val route = routes.getJSONObject(0)
                val distanceMeters = route.optDouble("distance", 0.0)
                val durationSec = route.optDouble("duration", 0.0)
                val geometry = route.optJSONObject("geometry") ?: return null
                val coordinates = geometry.optJSONArray("coordinates") ?: return null

                val points = ArrayList<Pair<Double, Double>>(coordinates.length())
                for (i in 0 until coordinates.length()) {
                    val coord = coordinates.getJSONArray(i)
                    val lon = coord.getDouble(0)
                    val lat = coord.getDouble(1)
                    points.add(Pair(lat, lon))
                }

                if (points.size >= 2) {
                    val distKm = if (distanceMeters > 0) distanceMeters / 1000.0 else calculateRoadDistanceKm(lat1, lon1, lat2, lon2)
                    val durMin = if (durationSec > 0) (durationSec / 60.0).toInt().coerceAtLeast(1) else calculateTransitMinutes(distKm)
                    RoadRouteResult(
                        points = points,
                        distanceKm = distKm,
                        durationMinutes = durMin,
                        isRealRoad = true
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun tryFetchGoogleDirections(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double,
        wpLat: Double, wpLon: Double
    ): RoadRouteResult? {
        val apiKey = BuildConfig.MAPS_API_KEY
        if (apiKey.isBlank()) return null

        return try {
            val hasWp = wpLat != 0.0 && wpLon != 0.0
            val wpParam = if (hasWp) "&waypoints=$wpLat,$wpLon" else ""
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=$lat1,$lon1&destination=$lat2,$lon2$wpParam&mode=driving&key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WasteTrack-Enterprise/1.0 (Android; Transport-Logistics)")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                if (json.optString("status") != "OK") return null

                val routes = json.optJSONArray("routes") ?: return null
                if (routes.length() == 0) return null

                val route = routes.getJSONObject(0)
                val polylineEncoded = route.optJSONObject("overview_polyline")?.optString("points") ?: return null
                val points = decodeGooglePolyline(polylineEncoded)

                val legs = route.optJSONArray("legs")
                var totalDistanceMeters = 0.0
                var totalDurationSec = 0.0
                if (legs != null) {
                    for (i in 0 until legs.length()) {
                        val leg = legs.getJSONObject(i)
                        totalDistanceMeters += leg.optJSONObject("distance")?.optDouble("value", 0.0) ?: 0.0
                        totalDurationSec += leg.optJSONObject("duration")?.optDouble("value", 0.0) ?: 0.0
                    }
                }

                if (points.size >= 2) {
                    val distKm = if (totalDistanceMeters > 0) totalDistanceMeters / 1000.0 else calculateRoadDistanceKm(lat1, lon1, lat2, lon2)
                    val durMin = if (totalDurationSec > 0) (totalDurationSec / 60.0).toInt().coerceAtLeast(1) else calculateTransitMinutes(distKm)
                    RoadRouteResult(
                        points = points,
                        distanceKm = distKm,
                        durationMinutes = durMin,
                        isRealRoad = true
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun decodeGooglePolyline(encoded: String): List<Pair<Double, Double>> {
        val poly = ArrayList<Pair<Double, Double>>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(Pair(lat.toDouble() / 1E5, lng.toDouble() / 1E5))
        }
        return poly
    }

    private fun getOfflineCorridorRoute(
        originLat: Double,
        originLon: Double,
        destLat: Double,
        destLon: Double,
        waypointLat: Double = 0.0,
        waypointLon: Double = 0.0
    ): RoadRouteResult {
        val keyNodes = mutableListOf<Pair<Double, Double>>()
        keyNodes.add(Pair(originLat, originLon))

        if (waypointLat != 0.0 && waypointLon != 0.0) {
            keyNodes.add(Pair(waypointLat, waypointLon))
        }

        val isNashikToKopargaon = (originLat in 19.9..20.1 && destLat in 19.7..20.0 && destLon > originLon) ||
                                  (destLat in 19.9..20.1 && originLat in 19.7..20.0 && originLon > destLon)
        val isNashikToPune = (originLat in 19.8..20.1 && destLat in 18.4..18.9) || (destLat in 19.8..20.1 && originLat in 18.4..18.9)
        val isNashikToMumbai = (originLat in 19.8..20.1 && destLon in 72.7..73.2) || (destLat in 19.8..20.1 && originLon in 72.7..73.2)

        val intermediateWaypoints = when {
            isNashikToKopargaon -> listOf(
                Pair(19.9880, 73.7850),
                Pair(19.9890, 73.8050),
                Pair(19.9550, 73.8350),
                Pair(19.8450, 74.0020),
                Pair(19.8250, 74.2250),
                Pair(19.8828, 74.4789)
            )
            isNashikToPune -> listOf(
                Pair(19.8450, 74.0020),
                Pair(19.5760, 74.2150),
                Pair(19.1830, 74.1150),
                Pair(19.1200, 73.9780),
                Pair(19.0050, 73.9450),
                Pair(18.8550, 73.9180),
                Pair(18.7562, 73.8580),
                Pair(18.6298, 73.8478)
            )
            isNashikToMumbai -> listOf(
                Pair(19.9200, 73.7100),
                Pair(19.7200, 73.6600),
                Pair(19.6950, 73.5600),
                Pair(19.6500, 73.4800),
                Pair(19.4500, 73.3300),
                Pair(19.2900, 73.0600),
                Pair(19.2183, 72.9781),
                Pair(19.1438, 73.0088)
            )
            else -> emptyList()
        }

        val allAnchors = mutableListOf<Pair<Double, Double>>()
        allAnchors.add(Pair(originLat, originLon))
        if (intermediateWaypoints.isNotEmpty()) {
            allAnchors.addAll(intermediateWaypoints)
        }
        if (waypointLat != 0.0 && waypointLon != 0.0) {
            allAnchors.add(Pair(waypointLat, waypointLon))
        }
        allAnchors.add(Pair(destLat, destLon))

        val points = mutableListOf<Pair<Double, Double>>()
        for (i in 0 until allAnchors.size - 1) {
            val a = allAnchors[i]
            val b = allAnchors[i + 1]
            val subSegments = 8
            for (step in 0 until subSegments) {
                val fraction = step.toDouble() / subSegments
                val lat = a.first + (b.first - a.first) * fraction
                val lon = a.second + (b.second - a.second) * fraction
                points.add(Pair(lat, lon))
            }
        }
        points.add(Pair(destLat, destLon))

        val roadDist = calculateRoadDistanceKm(originLat, originLon, destLat, destLon)
        val durationMins = calculateTransitMinutes(roadDist)

        return RoadRouteResult(
            points = points,
            distanceKm = roadDist,
            durationMinutes = durationMins,
            isRealRoad = false
        )
    }

    fun generateRoutePoints(originLat: Double, originLon: Double, destLat: Double, destLon: Double): List<Pair<Double, Double>> {
        return getCachedOrCorridorRoute(originLat, originLon, destLat, destLon).points
    }

    fun interpolatePositionAlongRoute(
        routePoints: List<Pair<Double, Double>>,
        progress: Float
    ): Pair<Double, Double> {
        if (routePoints.isEmpty()) return Pair(19.9576, 73.7436)
        if (routePoints.size == 1) return routePoints.first()

        val clamped = progress.coerceIn(0.0f, 1.0f)
        if (clamped <= 0.0f) return routePoints.first()
        if (clamped >= 1.0f) return routePoints.last()

        val segmentDistances = DoubleArray(routePoints.size - 1)
        var totalDist = 0.0
        for (i in 0 until routePoints.size - 1) {
            val p1 = routePoints[i]
            val p2 = routePoints[i + 1]
            val dist = calculateDistanceKm(p1.first, p1.second, p2.first, p2.second)
            segmentDistances[i] = dist
            totalDist += dist
        }

        if (totalDist <= 0.0) return routePoints.first()

        val targetDist = totalDist * clamped
        var accumulated = 0.0

        for (i in segmentDistances.indices) {
            val segDist = segmentDistances[i]
            if (accumulated + segDist >= targetDist || i == segmentDistances.size - 1) {
                val remaining = targetDist - accumulated
                val fraction = if (segDist > 0.0) (remaining / segDist).coerceIn(0.0, 1.0) else 0.0
                val p1 = routePoints[i]
                val p2 = routePoints[i + 1]
                val lat = p1.first + (p2.first - p1.first) * fraction
                val lon = p1.second + (p2.second - p1.second) * fraction
                return Pair(lat, lon)
            }
            accumulated += segDist
        }

        return routePoints.last()
    }

    fun interpolatePosition(
        routePoints: List<Pair<Double, Double>>,
        progress: Float
    ): Pair<Double, Double> {
        return interpolatePositionAlongRoute(routePoints, progress)
    }

    fun calculateBearing(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Float {
        val lat1 = Math.toRadians(p1.first)
        val lon1 = Math.toRadians(p1.second)
        val lat2 = Math.toRadians(p2.first)
        val lon2 = Math.toRadians(p2.second)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360.0) % 360.0
        return bearing.toFloat()
    }

    fun calculateBearingAlongRoute(routePoints: List<Pair<Double, Double>>, progress: Float): Float {
        if (routePoints.size < 2) return 0f
        val clamped = progress.coerceIn(0.0f, 1.0f)
        val segmentDistances = DoubleArray(routePoints.size - 1)
        var totalDist = 0.0
        for (i in 0 until routePoints.size - 1) {
            val p1 = routePoints[i]
            val p2 = routePoints[i + 1]
            val dist = calculateDistanceKm(p1.first, p1.second, p2.first, p2.second)
            segmentDistances[i] = dist
            totalDist += dist
        }
        if (totalDist <= 0.0) return calculateBearing(routePoints.first(), routePoints.last())

        val targetDist = totalDist * clamped
        var accumulated = 0.0

        for (i in segmentDistances.indices) {
            val segDist = segmentDistances[i]
            if (accumulated + segDist >= targetDist || i == segmentDistances.size - 1) {
                return calculateBearing(routePoints[i], routePoints[i + 1])
            }
            accumulated += segDist
        }
        return calculateBearing(routePoints[routePoints.size - 2], routePoints.last())
    }
}
