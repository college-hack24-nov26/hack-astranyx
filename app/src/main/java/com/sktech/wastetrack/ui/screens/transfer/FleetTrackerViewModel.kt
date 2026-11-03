package com.sktech.wastetrack.ui.screens.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.TransferStatus
import com.sktech.wastetrack.domain.model.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FleetCarrier(
    val id: String,
    val vehicleNumber: String,
    val vehicleType: VehicleType,
    val driverName: String,
    val driverPhone: String,
    val currentCorridor: String,
    val destinationPlant: String,
    val status: TransferStatus,
    val speedKmph: Int,
    val fuelLevelPercent: Int,
    val cargoTitle: String,
    val quantityMt: Double,
    val gatePassNumber: String,
    val isAvailable: Boolean
)

data class FleetTrackerUiState(
    val selectedFilter: String = "All",
    val carriers: List<FleetCarrier> = listOf(
        FleetCarrier(
            id = "fl_001",
            vehicleNumber = "MH-15-EG-4921",
            vehicleType = VehicleType.BIG_TRUCK,
            driverName = "Sachin Shinde",
            driverPhone = "+91 98220 14829",
            currentCorridor = "Samruddhi Expressway (Ghoti Bypass)",
            destinationPlant = "Mahindra Bio-Energy Plant",
            status = TransferStatus.IN_TRANSIT,
            speedKmph = 48,
            fuelLevelPercent = 74,
            cargoTitle = "Dry Baled Cotton Stalks",
            quantityMt = 45.0,
            gatePassNumber = "GP-MH-2026-9142",
            isAvailable = false
        ),
        FleetCarrier(
            id = "fl_002",
            vehicleNumber = "MH-15-BD-8832",
            vehicleType = VehicleType.TIPPER_TRUCK,
            driverName = "Eknath Khairnar",
            driverPhone = "+91 98224 81920",
            currentCorridor = "Niphad Sugar Cluster Gate 2",
            destinationPlant = "Niphad Co-Gen Biomass Plant",
            status = TransferStatus.DELIVERED,
            speedKmph = 0,
            fuelLevelPercent = 88,
            cargoTitle = "Crushed Sugarcane Bagasse",
            quantityMt = 80.0,
            gatePassNumber = "GP-MH-2026-8832",
            isAvailable = false
        ),
        FleetCarrier(
            id = "fl_003",
            vehicleNumber = "MH-15-TC-5521",
            vehicleType = VehicleType.TRACTOR_TRAILER,
            driverName = "Ramesh Pawar",
            driverPhone = "+91 98229 33011",
            currentCorridor = "Dindori Rural Farm Corridor",
            destinationPlant = "Central Biomass Depot",
            status = TransferStatus.INITIATED,
            speedKmph = 22,
            fuelLevelPercent = 60,
            cargoTitle = "Soybean Biomass Silage",
            quantityMt = 35.0,
            gatePassNumber = "GP-MH-2026-7711",
            isAvailable = false
        ),
        FleetCarrier(
            id = "fl_004",
            vehicleNumber = "MH-15-AB-7744",
            vehicleType = VehicleType.MEDIUM_TRUCK,
            driverName = "Sunil More",
            driverPhone = "+91 98221 66490",
            currentCorridor = "Nashik Fleet Yard Hub",
            destinationPlant = "Ready for Inbound Dispatch",
            status = TransferStatus.VERIFIED,
            speedKmph = 0,
            fuelLevelPercent = 95,
            cargoTitle = "Idle / Awaiting Consignment",
            quantityMt = 0.0,
            gatePassNumber = "GP-STANDBY",
            isAvailable = true
        )
    ),
    val showDispatchModal: Boolean = false,
    val showDispatchedToast: Boolean = false,
    val activeCorridorCount: Int = 3,
    val totalTonnageInTransit: Double = 160.0
)

@HiltViewModel
class FleetTrackerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(FleetTrackerUiState())
    val uiState: StateFlow<FleetTrackerUiState> = _uiState.asStateFlow()

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openDispatchModal() {
        _uiState.update { it.copy(showDispatchModal = true) }
    }

    fun dismissDispatchModal() {
        _uiState.update { it.copy(showDispatchModal = false) }
    }

    fun confirmTruckDispatch(
        vehicleNo: String,
        driverName: String,
        driverPhone: String,
        vehicleType: VehicleType,
        destination: String,
        cargo: String,
        tonnage: Double
    ) {
        val newCarrier = FleetCarrier(
            id = "fl_${System.currentTimeMillis() % 10000}",
            vehicleNumber = vehicleNo.ifBlank { "MH-15-ZT-9901" },
            vehicleType = vehicleType,
            driverName = driverName.ifBlank { "Assigned Driver" },
            driverPhone = driverPhone.ifBlank { "+91 98220 00000" },
            currentCorridor = "Dispatched from Fleet Yard",
            destinationPlant = destination.ifBlank { "Regional Agro-Hub" },
            status = TransferStatus.INITIATED,
            speedKmph = 15,
            fuelLevelPercent = 100,
            cargoTitle = cargo.ifBlank { "High-Calorific Crop Residue" },
            quantityMt = if (tonnage > 0) tonnage else 25.0,
            gatePassNumber = "GP-MH-2026-${(1000..9999).random()}",
            isAvailable = false
        )

        _uiState.update { current ->
            current.copy(
                carriers = listOf(newCarrier) + current.carriers,
                showDispatchModal = false,
                showDispatchedToast = true,
                activeCorridorCount = current.activeCorridorCount + 1,
                totalTonnageInTransit = current.totalTonnageInTransit + newCarrier.quantityMt
            )
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(showDispatchedToast = false) }
    }
}
