package com.sktech.wastetrack.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.OrderItem
import com.sktech.wastetrack.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderListUiState(
    val selectedFilter: String = "All",
    val orders: List<OrderItem> = listOf(
        OrderItem(
            id = "ord_001",
            orderNumber = "ORD-MH-2026-9041",
            residueTitle = "Baled Dry Cotton Stalks (High Calorific)",
            cropName = "Cotton",
            sellerFarmerName = "Dnyaneshwar Patil",
            buyerCompanyName = "Mahindra Bio-Energy Plant",
            quantityMt = 45.0,
            pricePerMt = 2400.0,
            totalAmount = 108000.0,
            escrowLockedAmount = 108000.0,
            status = OrderStatus.IN_TRANSIT,
            gatePassNumber = "GP-MH-2026-9142",
            vehicleNumber = "MH-15-EG-4921",
            driverName = "Sachin Shinde",
            contractHash = "0x7E3A9C82B1D4F0E6",
            createdAtMillis = System.currentTimeMillis() - 3L * 3600 * 1000
        ),
        OrderItem(
            id = "ord_002",
            orderNumber = "ORD-MH-2026-8812",
            residueTitle = "Crushed Sugarcane Bagasse (Industrial Grade)",
            cropName = "Sugarcane",
            sellerFarmerName = "Sanjay Deshmukh",
            buyerCompanyName = "Niphad Co-Gen Biomass Plant",
            quantityMt = 80.0,
            pricePerMt = 1850.0,
            totalAmount = 148000.0,
            escrowLockedAmount = 148000.0,
            status = OrderStatus.DELIVERED,
            gatePassNumber = "GP-MH-2026-8832",
            vehicleNumber = "MH-15-BD-8832",
            driverName = "Eknath Khairnar",
            contractHash = "0x4A8F2C19E7B3D5A1",
            createdAtMillis = System.currentTimeMillis() - 28L * 3600 * 1000
        ),
        OrderItem(
            id = "ord_003",
            orderNumber = "ORD-MH-2026-7734",
            residueTitle = "Soybean Biomass Pellets",
            cropName = "Soybean",
            sellerFarmerName = "Ganesh Jadhav",
            buyerCompanyName = "GreenPower Pellets Ltd",
            quantityMt = 35.0,
            pricePerMt = 2600.0,
            totalAmount = 91000.0,
            escrowLockedAmount = 91000.0,
            status = OrderStatus.SETTLED,
            gatePassNumber = "GP-MH-2026-7711",
            vehicleNumber = "MH-15-TC-5521",
            driverName = "Ramesh Pawar",
            contractHash = "0x9C2E7B1A4F6D3802",
            createdAtMillis = System.currentTimeMillis() - 4L * 86400 * 1000
        ),
        OrderItem(
            id = "ord_004",
            orderNumber = "ORD-MH-2026-9205",
            residueTitle = "Golden Wheat Straw Bales",
            cropName = "Wheat",
            sellerFarmerName = "Sunil Gaikwad",
            buyerCompanyName = "BioMass Processing Unit 01",
            quantityMt = 50.0,
            pricePerMt = 2100.0,
            totalAmount = 105000.0,
            escrowLockedAmount = 105000.0,
            status = OrderStatus.ESCROW_LOCKED,
            gatePassNumber = "GP-MH-2026-9219",
            vehicleNumber = "MH-15-AB-7744",
            driverName = "Sunil More",
            contractHash = "0x1D8F4E2A7B9C3056",
            createdAtMillis = System.currentTimeMillis() - 45L * 60 * 1000
        )
    ),
    val selectedOrderForPayout: OrderItem? = null,
    val showPayoutSuccessDialog: Boolean = false,
    val totalEscrowLocked: Double = 452000.0,
    val totalProcuredMt: Double = 210.0
)

@HiltViewModel
class OrderListViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    fun selectFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openPayoutConfirmation(order: OrderItem) {
        _uiState.update { it.copy(selectedOrderForPayout = order) }
    }

    fun dismissPayoutConfirmation() {
        _uiState.update { it.copy(selectedOrderForPayout = null) }
    }

    fun confirmEscrowRelease() {
        val selected = _uiState.value.selectedOrderForPayout ?: return
        _uiState.update { current ->
            val updated = current.orders.map { ord ->
                if (ord.id == selected.id) {
                    ord.copy(status = OrderStatus.SETTLED)
                } else ord
            }
            current.copy(
                orders = updated,
                selectedOrderForPayout = null,
                showPayoutSuccessDialog = true
            )
        }
    }

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showPayoutSuccessDialog = false) }
    }
}
