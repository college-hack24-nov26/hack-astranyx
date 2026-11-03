package com.sktech.wastetrack.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.TransferItem
import com.sktech.wastetrack.domain.model.TransferStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DriverTrip(
    val id: String,
    val gatePassNumber: String,
    val cargoTitle: String,
    val cropName: String,
    val quantityMt: Double,
    val pickupLocation: String,
    val dropLocation: String,
    val distanceKm: Double,
    val status: TransferStatus,
    val scheduledPickupTime: Long,
    val qrPayload: String,
    val hashFingerprint: String
)

data class DriverDashboardUiState(
    val driverName: String = "Sachin Shinde",
    val vehicleNumber: String = "MH-15-EG-4921",
    val vehicleType: String = "10-Wheeler High-Sided Biomass Carrier",
    val isOnline: Boolean = true,
    val activeTrips: List<DriverTrip> = listOf(
        DriverTrip(
            id = "trip_1",
            gatePassNumber = "GP-MH-2026-9142",
            cargoTitle = "45 MT Dry Cotton Stalks (Baled)",
            cropName = "Cotton",
            quantityMt = 45.0,
            pickupLocation = "Sinnar Agro Hub, Nashik",
            dropLocation = "Mahindra Bio-Energy Plant, Igatpuri",
            distanceKm = 48.5,
            status = TransferStatus.IN_TRANSIT,
            scheduledPickupTime = System.currentTimeMillis() - 2L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-9142|TRUCK:MH15EG4921|CROP:COTTON|QTY:45MT|ORIGIN:SINNAR",
            hashFingerprint = "0x8F9A4C71B2D0E6F3"
        ),
        DriverTrip(
            id = "trip_2",
            gatePassNumber = "GP-MH-2026-6105",
            cargoTitle = "30 MT Threshed Soybean Husk & Straw",
            cropName = "Soybean",
            quantityMt = 30.0,
            pickupLocation = "Dindori Agri Cluster",
            dropLocation = "Malegaon Green Fuel Pelletization",
            distanceKm = 72.0,
            status = TransferStatus.INITIATED,
            scheduledPickupTime = System.currentTimeMillis() + 45 * 60 * 1000,
            qrPayload = "GP:GP-MH-2026-6105|TRUCK:MH15AK1204|CROP:SOYBEAN|QTY:30MT|ORIGIN:DINDORI",
            hashFingerprint = "0x1E8F6A902D4C3B77"
        )
    ),
    val completedTrips: List<DriverTrip> = listOf(
        DriverTrip(
            id = "trip_3",
            gatePassNumber = "GP-MH-2026-8832",
            cargoTitle = "80 MT High-Calorific Crushed Bagasse",
            cropName = "Sugarcane",
            quantityMt = 80.0,
            pickupLocation = "Niphad Bio-Energy Cluster",
            dropLocation = "Niphad Co-Gen Biomass Plant",
            distanceKm = 24.0,
            status = TransferStatus.VERIFIED,
            scheduledPickupTime = System.currentTimeMillis() - 18L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-8832|TRUCK:MH15BD8832|CROP:SUGARCANE|QTY:80MT|ORIGIN:NIPHAD",
            hashFingerprint = "0x4B3E91A8F702D9C5"
        ),
        DriverTrip(
            id = "trip_4",
            gatePassNumber = "GP-MH-2026-7719",
            cargoTitle = "65 MT Shredded Golden Wheat Straw",
            cropName = "Wheat",
            quantityMt = 65.0,
            pickupLocation = "Sinnar Agro Hub, Nashik",
            dropLocation = "Sinnar Recycled Paper & Board Mill",
            distanceKm = 18.0,
            status = TransferStatus.DELIVERED,
            scheduledPickupTime = System.currentTimeMillis() - 36L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-7719|TRUCK:MH15CL7719|CROP:WHEAT|QTY:65MT|ORIGIN:SINNAR",
            hashFingerprint = "0x7D2A1C90B5E3F864"
        )
    ),
    val totalTonnageHauledMt: Double = 220.0,
    val completedTripCount: Int = 14,
    val onTimeRatingPercent: Int = 98,
    val selectedTripForQR: DriverTrip? = null,
    val showIncidentDialog: Boolean = false,
    val selectedTripForIncident: DriverTrip? = null,
    val incidentReasonInput: String = "Traffic Delay"
)

@HiltViewModel
class DriverDashboardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DriverDashboardUiState())
    val uiState: StateFlow<DriverDashboardUiState> = _uiState.asStateFlow()

    fun toggleDutyStatus() {
        _uiState.update { it.copy(isOnline = !it.isOnline) }
    }

    fun showGatepassQR(trip: DriverTrip) {
        _uiState.update { it.copy(selectedTripForQR = trip) }
    }

    fun dismissQR() {
        _uiState.update { it.copy(selectedTripForQR = null) }
    }

    fun advanceTripStatus(tripId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updatedActive = state.activeTrips.map { trip ->
                    if (trip.id == tripId) {
                        val nextStatus = when (trip.status) {
                            TransferStatus.INITIATED -> TransferStatus.IN_TRANSIT
                            TransferStatus.IN_TRANSIT -> TransferStatus.DELIVERED
                            TransferStatus.DELIVERED -> TransferStatus.VERIFIED
                            else -> trip.status
                        }
                        trip.copy(status = nextStatus)
                    } else trip
                }
                state.copy(activeTrips = updatedActive)
            }
        }
    }

    fun openIncidentDialog(trip: DriverTrip) {
        _uiState.update { it.copy(showIncidentDialog = true, selectedTripForIncident = trip) }
    }

    fun closeIncidentDialog() {
        _uiState.update { it.copy(showIncidentDialog = false, selectedTripForIncident = null) }
    }

    fun updateIncidentReason(reason: String) {
        _uiState.update { it.copy(incidentReasonInput = reason) }
    }

    fun submitIncident() {
        _uiState.update { it.copy(showIncidentDialog = false, selectedTripForIncident = null) }
    }
}
