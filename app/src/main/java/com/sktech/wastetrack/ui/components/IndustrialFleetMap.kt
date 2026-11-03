package com.sktech.wastetrack.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenContainer
import com.sktech.wastetrack.ui.theme.WarningOrange
import com.sktech.wastetrack.util.LocationHelper

@Composable
fun IndustrialFleetMap(
    vehicleNumber: String,
    driverName: String = "Assigned Driver",
    driverPhone: String = "",
    originName: String = "Sinnar Agro Hub",
    destinationName: String = "Igatpuri MIDC Plant",
    originLat: Double = 0.0,
    originLon: Double = 0.0,
    destLat: Double = 0.0,
    destLon: Double = 0.0,
    truckLat: Double = 0.0,
    truckLon: Double = 0.0,
    speedKmh: Int = 52,
    etaText: String? = null,
    progressFraction: Float = 0.55f,
    precomputedRoutePoints: List<Pair<Double, Double>>? = null,
    precomputedDistanceKm: Double? = null,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    showDriverCard: Boolean = true,
    isFullScreen: Boolean = false,
    onToggleFullScreen: (() -> Unit)? = null,
    onCallDriver: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val validOriginLat = if (originLat != 0.0) originLat else 19.8500
    val validOriginLon = if (originLon != 0.0) originLon else 73.9900
    val validDestLat = if (destLat != 0.0) destLat else 19.6936
    val validDestLon = if (destLon != 0.0) destLon else 73.5558

    val originPoint = remember(validOriginLat, validOriginLon) { LatLng(validOriginLat, validOriginLon) }
    val destPoint = remember(validDestLat, validDestLon) { LatLng(validDestLat, validDestLon) }

    var routeResult by remember(validOriginLat, validOriginLon, validDestLat, validDestLon, precomputedRoutePoints) {
        mutableStateOf(
            if (!precomputedRoutePoints.isNullOrEmpty()) {
                LocationHelper.RoadRouteResult(
                    points = precomputedRoutePoints,
                    distanceKm = precomputedDistanceKm ?: LocationHelper.calculateRoadDistanceKm(validOriginLat, validOriginLon, validDestLat, validDestLon),
                    durationMinutes = LocationHelper.calculateTransitMinutes(precomputedDistanceKm ?: 10.0),
                    isRealRoad = true
                )
            } else {
                LocationHelper.getCachedOrCorridorRoute(
                    originLat = validOriginLat,
                    originLon = validOriginLon,
                    destLat = validDestLat,
                    destLon = validDestLon
                )
            }
        )
    }

    LaunchedEffect(validOriginLat, validOriginLon, validDestLat, validDestLon, precomputedRoutePoints) {
        if (precomputedRoutePoints.isNullOrEmpty()) {
            val liveRealRoute = LocationHelper.fetchRealRoadRoute(
                originLat = validOriginLat,
                originLon = validOriginLon,
                destLat = validDestLat,
                destLon = validDestLon
            )
            routeResult = liveRealRoute
        }
    }

    val routePoints = remember(routeResult) {
        routeResult.points.map { LatLng(it.first, it.second) }
    }

    val roadDistanceKm = routeResult.distanceKm

    val displayEta = remember(etaText, roadDistanceKm) {
        etaText?.ifBlank { null } ?: LocationHelper.formatEta(roadDistanceKm)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0.02f, 0.98f),
        animationSpec = tween(1400, easing = LinearOutSlowInEasing),
        label = "truckMovement"
    )

    val truckPosition = remember(truckLat, truckLon, animatedProgress, routeResult) {
        if (truckLat != 0.0 && truckLon != 0.0) {
            LatLng(truckLat, truckLon)
        } else {
            val coords = LocationHelper.interpolatePositionAlongRoute(
                routeResult.points,
                animatedProgress
            )
            LatLng(coords.first, coords.second)
        }
    }

    val truckBearing = remember(routeResult, animatedProgress) {
        LocationHelper.calculateBearingAlongRoute(
            routeResult.points,
            animatedProgress
        )
    }

    val truckMarkerState = rememberMarkerState(position = truckPosition)
    LaunchedEffect(truckPosition) {
        truckMarkerState.position = truckPosition
    }

    val centerLat = (validOriginLat + validDestLat) / 2.0
    val centerLon = (validOriginLon + validDestLon) / 2.0
    val targetZoom = remember(roadDistanceKm) {
        when {
            roadDistanceKm > 200 -> 7.0f
            roadDistanceKm > 100 -> 8.5f
            roadDistanceKm > 40 -> 9.5f
            else -> 11.0f
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLon), targetZoom)
    }

    LaunchedEffect(originPoint, destPoint, routePoints) {
        if (routePoints.isNotEmpty()) {
            try {
                val boundsBuilder = LatLngBounds.builder()
                    .include(originPoint)
                    .include(destPoint)

                val sampleStep = (routePoints.size / 8).coerceAtLeast(1)
                for (i in routePoints.indices step sampleStep) {
                    boundsBuilder.include(routePoints[i])
                }

                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100),
                    800
                )
            } catch (e: Exception) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLon), targetZoom)
            }
        }
    }

    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    val uiSettings = remember(isInteractive, isFullScreen) {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = isFullScreen,
            scrollGesturesEnabled = isFullScreen,
            zoomGesturesEnabled = isFullScreen,
            rotationGesturesEnabled = isFullScreen,
            tiltGesturesEnabled = false
        )
    }

    val mapProperties = remember(mapType) {
        MapProperties(
            mapType = mapType,
            isTrafficEnabled = true
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isFullScreen && onToggleFullScreen != null) Modifier.clickable { onToggleFullScreen() } else Modifier),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = uiSettings,
                properties = mapProperties
            ) {
                DestinationRadarPulse(destPoint = destPoint)

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = HarvestGreen.copy(alpha = 0.3f),
                        width = 18f
                    )
                }

                if (routePoints.isNotEmpty()) {
                    Polyline(
                        points = routePoints,
                        color = HarvestGreen,
                        width = 10f
                    )
                }

                Marker(
                    state = rememberMarkerState(position = originPoint),
                    title = originName,
                    snippet = "Consignment Dispatch Gate",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )

                Marker(
                    state = rememberMarkerState(position = destPoint),
                    title = destinationName,
                    snippet = "Authorized Weighbridge & Processing Yard",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )

                Marker(
                    state = truckMarkerState,
                    title = "Carrier: $vehicleNumber ($driverName)",
                    snippet = "Speed: $speedKmh km/h • $displayEta",
                    rotation = truckBearing,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.4f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f, fill = false).padding(end = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreen,
                            modifier = Modifier.size(7.dp)
                        ) {}
                        Column {
                            Text(
                                text = "$speedKmh km/h • $displayEta",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = HarvestGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "LIVE CORRIDOR TELEMETRY",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (isInteractive) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    cameraPositionState.position = CameraPosition.fromLatLngZoom(truckPosition, 12.0f)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalShipping,
                                    contentDescription = "Center on Truck",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    try {
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLon), targetZoom)
                                    } catch (e: Exception) {
                                        cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLon), targetZoom)
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.NearMe,
                                    contentDescription = "Fit Route",
                                    tint = HarvestGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    mapType = when (mapType) {
                                        MapType.NORMAL -> MapType.SATELLITE
                                        MapType.SATELLITE -> MapType.HYBRID
                                        else -> MapType.NORMAL
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Layers,
                                    contentDescription = "Map Style",
                                    tint = HarvestGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (onToggleFullScreen != null) {
                                IconButton(
                                    onClick = onToggleFullScreen,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        if (isFullScreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                        contentDescription = if (isFullScreen) "Exit Full Screen" else "Full Screen",
                                        tint = HarvestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDriverCard) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = HarvestGreenContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.LocalShipping,
                                        contentDescription = null,
                                        tint = HarvestGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "$driverName • ${vehicleNumber.ifBlank { "MH-15-EG-4921" }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Emergency Driver Line: ${driverPhone.ifBlank { "+91 98224 81920" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                onCallDriver?.invoke() ?: run {
                                    val phoneToCall = driverPhone.ifBlank { "+919822481920" }
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${phoneToCall.filter { it.isDigit() || it == '+' }}")
                                    }
                                    runCatching { context.startActivity(intent) }
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(HarvestGreen, CircleShape)
                        ) {
                            Icon(
                                Icons.Filled.Call,
                                contentDescription = "Emergency Driver Call",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationRadarPulse(destPoint: LatLng) {
    val infiniteTransition = rememberInfiniteTransition(label = "destRadarTransition")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 180f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Circle(
        center = destPoint,
        radius = pulseRadius.toDouble(),
        fillColor = HarvestGreen.copy(alpha = pulseAlpha * 0.25f),
        strokeColor = HarvestGreen.copy(alpha = pulseAlpha * 0.8f),
        strokeWidth = 3f
    )

    Circle(
        center = destPoint,
        radius = (pulseRadius * 0.5f).toDouble(),
        fillColor = HarvestGreen.copy(alpha = pulseAlpha * 0.15f),
        strokeColor = HarvestGreen.copy(alpha = pulseAlpha * 0.5f),
        strokeWidth = 2f
    )
}

