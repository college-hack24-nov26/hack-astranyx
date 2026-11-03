package com.sktech.wastetrack.ui.screens.transfer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.TransferStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WaypointPoint(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lon: Double,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

data class WeighbridgeSlip(
    val gatePassNo: String,
    val vehicleNo: String,
    val cargoTitle: String,
    val grossWeightMt: Double,
    val tareWeightMt: Double,
    val netWeightMt: Double,
    val sourceWeightMt: Double,
    val varianceKg: Double,
    val variancePercent: Double,
    val timestampMillis: Long,
    val isVerified: Boolean
)

data class ShipmentTrackingUiState(
    val transferId: String = "tr_001",
    val gatePassNumber: String = "GP-MH-2026-9142",
    val cargoTitle: String = "45 MT Dry Cotton Stalks (Baled)",
    val cropName: String = "Cotton",
    val quantityMt: Double = 45.0,
    val vehicleNumber: String = "MH-15-EG-4921",
    val vehicleType: String = "10-Wheeler Biomass Hauler",
    val driverName: String = "Sachin Shinde",
    val driverPhone: String = "+91 98220 14829",
    val sourceLocation: String = "Sinnar Agro Hub, Nashik",
    val destinationPlant: String = "Mahindra Bio-Energy Plant, Igatpuri",
    val originLat: Double = 19.8490,
    val originLon: Double = 73.9984,
    val destLat: Double = 19.6970,
    val destLon: Double = 73.5593,
    val currentTruckLat: Double = 19.7650,
    val currentTruckLon: Double = 73.7420,
    val speedKmph: Int = 48,
    val etaMinutes: Int = 34,
    val distanceRemainingKm: Double = 26.8,
    val totalDistanceKm: Double = 52.4,
    val progressFraction: Float = 0.49f,
    val status: TransferStatus = TransferStatus.IN_TRANSIT,
    val isSatelliteView: Boolean = false,
    val weighbridgeSlip: WeighbridgeSlip? = WeighbridgeSlip(
        gatePassNo = "GP-MH-2026-9142",
        vehicleNo = "MH-15-EG-4921",
        cargoTitle = "Baled Dry Cotton Stalks",
        grossWeightMt = 62.4,
        tareWeightMt = 17.6,
        netWeightMt = 44.8,
        sourceWeightMt = 45.0,
        varianceKg = -200.0,
        variancePercent = -0.44,
        timestampMillis = System.currentTimeMillis() - 45L * 60 * 1000,
        isVerified = true
    ),
    val showWeighbridgeDialog: Boolean = false,
    val showIncidentDialog: Boolean = false,
    val showQrDialog: Boolean = false,
    val incidentReason: String = "",
    val qrPayload: String = "GP:GP-MH-2026-9142|TRUCK:MH15EG4921|CROP:COTTON|QTY:45MT|ORIGIN:SINNAR",
    val hashFingerprint: String = "0x8F9A4C71B2D0E6F3",
    val waypoints: List<WaypointPoint> = listOf(
        WaypointPoint("Sinnar Agro Hub", "Farm dispatch & origin weighment (45.0 MT)", 19.8490, 73.9984, true, false),
        WaypointPoint("Samruddhi Expressway Toll Plaza", "Fastag clearance & e-way bill verified", 19.7920, 73.8210, true, false),
        WaypointPoint("Ghoti Industrial Bypass", "Mid-transit telemetry ping (48 km/h)", 19.7650, 73.7420, false, true),
        WaypointPoint("Igatpuri Plant Security Gate", "Gatepass QR scan & bio-security checkpoint", 19.7120, 73.5780, false, false),
        WaypointPoint("Mahindra Bio-Energy Plant", "Destination dual-weighbridge gross/tare pit", 19.6970, 73.5593, false, false)
    )
)

@HiltViewModel
class ShipmentTrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navTransferId: String = savedStateHandle.get<String>("transferId") ?: "tr_001"

    private val _uiState = MutableStateFlow(
        ShipmentTrackingUiState(transferId = navTransferId)
    )
    val uiState: StateFlow<ShipmentTrackingUiState> = _uiState.asStateFlow()

    fun toggleSatelliteView() {
        _uiState.update { it.copy(isSatelliteView = !it.isSatelliteView) }
    }

    fun openWeighbridgeDialog() {
        _uiState.update { it.copy(showWeighbridgeDialog = true) }
    }

    fun dismissWeighbridgeDialog() {
        _uiState.update { it.copy(showWeighbridgeDialog = false) }
    }

    fun submitWeighbridgeEntry(grossMt: Double, tareMt: Double) {
        val net = grossMt - tareMt
        val source = _uiState.value.quantityMt
        val diffKg = (net - source) * 1000.0
        val diffPct = if (source > 0) ((net - source) / source) * 100.0 else 0.0

        val newSlip = WeighbridgeSlip(
            gatePassNo = _uiState.value.gatePassNumber,
            vehicleNo = _uiState.value.vehicleNumber,
            cargoTitle = _uiState.value.cargoTitle,
            grossWeightMt = grossMt,
            tareWeightMt = tareMt,
            netWeightMt = net,
            sourceWeightMt = source,
            varianceKg = diffKg,
            variancePercent = diffPct,
            timestampMillis = System.currentTimeMillis(),
            isVerified = true
        )

        _uiState.update {
            it.copy(
                weighbridgeSlip = newSlip,
                showWeighbridgeDialog = false,
                status = TransferStatus.VERIFIED
            )
        }
    }

    fun openIncidentDialog() {
        _uiState.update { it.copy(showIncidentDialog = true) }
    }

    fun dismissIncidentDialog() {
        _uiState.update { it.copy(showIncidentDialog = false) }
    }

    fun setIncidentReason(reason: String) {
        _uiState.update { it.copy(incidentReason = reason) }
    }

    fun submitIncident() {
        _uiState.update {
            it.copy(
                showIncidentDialog = false,
                incidentReason = ""
            )
        }
    }

    fun openQrDialog() {
        _uiState.update { it.copy(showQrDialog = true) }
    }

    fun dismissQrDialog() {
        _uiState.update { it.copy(showQrDialog = false) }
    }

    fun advanceTransitMilestone() {
        _uiState.update { current ->
            val nextStatus = when (current.status) {
                TransferStatus.INITIATED -> TransferStatus.IN_TRANSIT
                TransferStatus.IN_TRANSIT -> TransferStatus.DELIVERED
                TransferStatus.DELIVERED -> TransferStatus.VERIFIED
                TransferStatus.VERIFIED -> TransferStatus.VERIFIED
            }

            val updatedWaypoints = current.waypoints.mapIndexed { idx, point ->
                when (nextStatus) {
                    TransferStatus.DELIVERED -> point.copy(
                        isCompleted = idx <= 3,
                        isCurrent = idx == 3
                    )
                    TransferStatus.VERIFIED -> point.copy(
                        isCompleted = true,
                        isCurrent = false
                    )
                    else -> point
                }
            }

            val updatedProgress = when (nextStatus) {
                TransferStatus.IN_TRANSIT -> 0.49f
                TransferStatus.DELIVERED -> 0.88f
                TransferStatus.VERIFIED -> 1.0f
                else -> current.progressFraction
            }

            current.copy(
                status = nextStatus,
                waypoints = updatedWaypoints,
                progressFraction = updatedProgress
            )
        }
    }
}
