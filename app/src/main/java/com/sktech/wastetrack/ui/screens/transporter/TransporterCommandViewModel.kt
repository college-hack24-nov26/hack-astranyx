package com.sktech.wastetrack.ui.screens.transporter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.transporter.FleetVehicle
import com.sktech.wastetrack.domain.model.transporter.StubbleLoad
import com.sktech.wastetrack.domain.model.transporter.TripProfitLedger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransporterHubUiState(
    val selectedTab: Int = 0,
    val loads: List<StubbleLoad> = listOf(
        StubbleLoad(
            id = "load_001",
            fpoName = "Sinnar Taluka Agro Producer Ltd",
            originHub = "Sinnar Agro Hub, Gate #4",
            destinationPlant = "Mahindra Bio-Energy Plant, Igatpuri",
            cropResidue = "Wheat Straw Compressed Bales",
            tonnageMt = 35.0,
            distanceKm = 45,
            suggestedFreightRatePerMt = 420.0,
            pickupWindow = "Today, 4:00 PM - 7:00 PM",
            status = "AVAILABLE"
        ),
        StubbleLoad(
            id = "load_002",
            fpoName = "Niphad Sugarcane Bio-Cooperative",
            originHub = "Pimpalgaon Biomass Depot",
            destinationPlant = "Niphad Co-Gen Power Project",
            cropResidue = "Crushed Sugarcane Bagasse & Trash",
            tonnageMt = 50.0,
            distanceKm = 28,
            suggestedFreightRatePerMt = 380.0,
            pickupWindow = "Tomorrow, 8:00 AM - 11:00 AM",
            status = "AVAILABLE"
        ),
        StubbleLoad(
            id = "load_003",
            fpoName = "Dindori Vineyard Residue Cluster",
            originHub = "Vani Phata Bio-Stockyard",
            destinationPlant = "Malegaon Green Fuel Pelletization",
            cropResidue = "Pruned Vineyard Woodchips & Stalks",
            tonnageMt = 25.0,
            distanceKm = 62,
            suggestedFreightRatePerMt = 510.0,
            pickupWindow = "Tomorrow, 1:00 PM - 4:00 PM",
            status = "AVAILABLE"
        )
    ),
    val vehicles: List<FleetVehicle> = listOf(
        FleetVehicle(
            vehicleNumber = "MH-15-EG-4921",
            modelName = "Tata Signa 2823.K Multi-Axle",
            driverName = "Ramesh Tukaram Patil",
            driverPhone = "+91 98224 81920",
            payloadCapacityMt = 24.0,
            tareWeightKg = 8400,
            grossVehicleWeightKg = 28000,
            fastagBalance = 3450.0,
            mpcbPermitNumber = "MPCB/WT/NSK/2026/819",
            mpcbPermitExpiry = "Valid (Exp: 31 Dec 2026)",
            pucExpiry = "Valid (Exp: 18 Nov 2026)",
            status = "IN TRANSIT"
        ),
        FleetVehicle(
            vehicleNumber = "MH-15-BD-8832",
            modelName = "Ashok Leyland Ecomet 1615",
            driverName = "Ganesh Bhikaji Jadhav",
            driverPhone = "+91 94220 55142",
            payloadCapacityMt = 16.0,
            tareWeightKg = 6100,
            grossVehicleWeightKg = 16200,
            fastagBalance = 1820.0,
            mpcbPermitNumber = "MPCB/WT/NSK/2026/744",
            mpcbPermitExpiry = "Valid (Exp: 15 Oct 2026)",
            pucExpiry = "Valid (Exp: 05 Jan 2027)",
            status = "AVAILABLE FOR PICKUP"
        ),
        FleetVehicle(
            vehicleNumber = "MH-15-AK-1204",
            modelName = "Eicher Pro 3019 Heavy Tipper",
            driverName = "Suresh Kashinath Shinde",
            driverPhone = "+91 97632 11094",
            payloadCapacityMt = 20.0,
            tareWeightKg = 7200,
            grossVehicleWeightKg = 19500,
            fastagBalance = 2100.0,
            mpcbPermitNumber = "MPCB/WT/NSK/2026/610",
            mpcbPermitExpiry = "Valid (Exp: 20 Feb 2027)",
            pucExpiry = "Renewal Due (Exp: 08 Nov 2026)",
            status = "AT WEIGHBRIDGE"
        )
    ),
    val tripLedgers: List<TripProfitLedger> = listOf(
        TripProfitLedger(
            tripId = "TRIP-MH-9102",
            vehicleNumber = "MH-15-EG-4921",
            route = "Sinnar Agro Hub -> Igatpuri Bio-Energy (45 km)",
            cargoDescription = "35 MT Wheat Straw Bales",
            tonnageMt = 35.0,
            grossFreightRevenue = 14700.0,
            dieselLiters = 18.0,
            fastagTollCost = 280.0,
            driverBattaAllowance = 800.0,
            unloadingFee = 500.0,
            ePodToken = "EPOD-9102-OK",
            isPodSigned = true,
            dateText = "Today, 11:30 AM"
        ),
        TripProfitLedger(
            tripId = "TRIP-MH-8841",
            vehicleNumber = "MH-15-BD-8832",
            route = "Pimpalgaon Depot -> Niphad Co-Gen (28 km)",
            cargoDescription = "50 MT Crushed Sugarcane Bagasse",
            tonnageMt = 50.0,
            grossFreightRevenue = 19000.0,
            dieselLiters = 14.0,
            fastagTollCost = 150.0,
            driverBattaAllowance = 700.0,
            unloadingFee = 600.0,
            ePodToken = "EPOD-8841-PENDING",
            isPodSigned = false,
            dateText = "Yesterday, 04:15 PM"
        )
    ),
    val biddingLoad: StubbleLoad? = null,
    val bidRateInput: String = "",
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class TransporterCommandViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TransporterHubUiState())
    val uiState: StateFlow<TransporterHubUiState> = _uiState.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun openBidding(load: StubbleLoad) {
        _uiState.update {
            it.copy(
                biddingLoad = load,
                bidRateInput = load.suggestedFreightRatePerMt.toString()
            )
        }
    }

    fun closeBidding() {
        _uiState.update { it.copy(biddingLoad = null, bidRateInput = "") }
    }

    fun updateBidRate(rateText: String) {
        _uiState.update { it.copy(bidRateInput = rateText) }
    }

    fun submitBid(loadId: String) {
        val rate = _uiState.value.bidRateInput.toDoubleOrNull() ?: 0.0
        if (rate <= 0.0) {
            _uiState.update { it.copy(error = "Enter a valid freight quote rate per MT") }
            return
        }

        _uiState.update { state ->
            val updatedLoads = state.loads.map { load ->
                if (load.id == loadId) {
                    load.copy(
                        status = "BID SUBMITTED",
                        myBidRatePerMt = rate
                    )
                } else load
            }
            state.copy(
                loads = updatedLoads,
                biddingLoad = null,
                bidRateInput = "",
                message = "Transport quote submitted to FPO successfully"
            )
        }
    }

    fun instantAcceptLoad(loadId: String) {
        _uiState.update { state ->
            val targetLoad = state.loads.find { it.id == loadId }
            val updatedLoads = state.loads.map { load ->
                if (load.id == loadId) {
                    load.copy(status = "DISPATCHED & LOCKED")
                } else load
            }

            val newLedger = targetLoad?.let { load ->
                TripProfitLedger(
                    tripId = "TRIP-MH-${(1000..9999).random()}",
                    vehicleNumber = "MH-15-EG-4921",
                    route = "${load.originHub} -> ${load.destinationPlant} (${load.distanceKm} km)",
                    cargoDescription = "${load.tonnageMt} MT ${load.cropResidue}",
                    tonnageMt = load.tonnageMt,
                    grossFreightRevenue = load.totalFreightValue,
                    dieselLiters = (load.distanceKm * 0.4).coerceAtLeast(10.0),
                    fastagTollCost = 280.0,
                    driverBattaAllowance = 800.0,
                    unloadingFee = 500.0,
                    ePodToken = "EPOD-${(1000..9999).random()}-GATE",
                    isPodSigned = false,
                    dateText = "Just now"
                )
            }

            state.copy(
                loads = updatedLoads,
                tripLedgers = if (newLedger != null) listOf(newLedger) + state.tripLedgers else state.tripLedgers,
                message = "Freight contract locked and vehicle allocated for pickup"
            )
        }
    }

    fun updateVehicleStatus(vehicleNumber: String, newStatus: String) {
        _uiState.update { state ->
            val updatedVehicles = state.vehicles.map { vehicle ->
                if (vehicle.vehicleNumber == vehicleNumber) {
                    vehicle.copy(status = newStatus)
                } else vehicle
            }
            state.copy(
                vehicles = updatedVehicles,
                message = "Vehicle $vehicleNumber status updated to $newStatus"
            )
        }
    }

    fun signPod(tripId: String) {
        _uiState.update { state ->
            val updatedLedgers = state.tripLedgers.map { trip ->
                if (trip.tripId == tripId) {
                    trip.copy(isPodSigned = true)
                } else trip
            }
            state.copy(
                tripLedgers = updatedLedgers,
                message = "Consignee gate e-POD verified and signed"
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }
}

