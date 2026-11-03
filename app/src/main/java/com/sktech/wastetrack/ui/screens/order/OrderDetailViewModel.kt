package com.sktech.wastetrack.ui.screens.order

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.OrderStatus
import com.sktech.wastetrack.domain.model.marketplace.IndustrialOrderMilestone
import com.sktech.wastetrack.domain.model.marketplace.IndustrialOrderTrackingDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val orderId: String = "",
    val order: IndustrialOrderTrackingDetail? = null,
    val isLoading: Boolean = false,
    val isVerifyingWeighbridge: Boolean = false,
    val isReleasingEscrow: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = savedStateHandle["orderId"] ?: "ord_001"

    private val sampleOrders = listOf(
        IndustrialOrderTrackingDetail(
            orderId = "ord_001",
            orderNumber = "ORD-MH-2026-9041",
            lotId = "lot_1",
            contractHash = "0x7E3A9C82B1D4F0E6",
            cropName = "Cotton",
            residueTitle = "Baled Dry Cotton Stalks (High Calorific)",
            quantityMt = 45.0,
            agreedPricePerMt = 2400.0,
            cropSubtotal = 108000.0,
            mandiPlatformCess = 1620.0,
            freightDeposit = 2500.0,
            totalEscrowLocked = 112120.0,
            sellerFarmerName = "Dnyaneshwar Patil",
            sellerFpo = "Sahyadri Bio-Farmers Producer Co.",
            sellerUpiVpa = "patil.agri@okaxis",
            buyerCompanyName = "Mahindra Bio-Energy Plant",
            buyerContactPerson = "Rajiv Mehta (Procurement Head)",
            driverName = "Sachin Shinde",
            driverPhone = "+91 98231 44012",
            vehicleNumber = "MH-15-EG-4921",
            gatePassNumber = "GP-MH-2026-9142",
            originLocation = "Farm Gate, Niphad Taluka, Nashik",
            destinationPlant = "Mahindra Bio-Energy Plant, Igatpuri Industrial Area",
            currentMilestoneStage = 3,
            status = OrderStatus.IN_TRANSIT,
            grossTareVerifiedWeight = null,
            qualityMoistureResult = "Moisture: 12.4% (Grade A Approved)",
            milestones = listOf(
                IndustrialOrderMilestone(1, "Contract Confirmed & Escrow Deposited", "100% Funds Secured in Vault", 20, 21600.0, "08:30 AM, Today", isCompleted = true, isActive = false, proofReference = "ESCROW-TX-9901"),
                IndustrialOrderMilestone(2, "Gatepass Handshake & Farm Loading", "QR Verified at Farm Gate", 50, 54000.0, "10:15 AM, Today", isCompleted = true, isActive = false, proofReference = "GP-MH-2026-9142"),
                IndustrialOrderMilestone(3, "Gross & Tare Weighbridge Audit", "Awaiting Inbound Mill Scale Verification", 0, 0.0, "Pending Gate Arrival", isCompleted = false, isActive = true, proofReference = "WB-SLIP-PENDING"),
                IndustrialOrderMilestone(4, "Lab Moisture Quality & Final UPI Release", "Automatic UPI Settlement to Farmer", 30, 32400.0, "Pending Quality Sign-off", isCompleted = false, isActive = false, proofReference = "UPI-SETTLE-PENDING")
            ),
            createdAtMillis = System.currentTimeMillis() - 10800000L
        ),
        IndustrialOrderTrackingDetail(
            orderId = "ord_002",
            orderNumber = "ORD-MH-2026-8812",
            lotId = "lot_2",
            contractHash = "0x4C8F1E902D7A5B33",
            cropName = "Sugarcane",
            residueTitle = "Crushed Sugarcane Bagasse (Industrial Grade)",
            quantityMt = 80.0,
            agreedPricePerMt = 1850.0,
            cropSubtotal = 148000.0,
            mandiPlatformCess = 2220.0,
            freightDeposit = 3200.0,
            totalEscrowLocked = 153420.0,
            sellerFarmerName = "Sanjay Deshmukh",
            sellerFpo = "Godavari Agri-Cooperative Syndicate",
            sellerUpiVpa = "deshmukh.farms@oksbi",
            buyerCompanyName = "Niphad Co-Gen Biomass Plant",
            buyerContactPerson = "Anand Kulkarni",
            driverName = "Ganesh Jadhav",
            driverPhone = "+91 94220 88219",
            vehicleNumber = "MH-15-AB-7832",
            gatePassNumber = "GP-MH-2026-8790",
            originLocation = "Sugarcane Loading Bay, Niphad",
            destinationPlant = "Niphad Co-Gen Facility, Niphad",
            currentMilestoneStage = 3,
            status = OrderStatus.DELIVERED,
            grossTareVerifiedWeight = 79.4,
            qualityMoistureResult = "Moisture: 15.8% (Approved)",
            milestones = listOf(
                IndustrialOrderMilestone(1, "Contract Confirmed & Escrow Deposited", "100% Funds Secured in Vault", 20, 29600.0, "Yesterday, 04:00 PM", isCompleted = true, isActive = false, proofReference = "ESCROW-TX-8802"),
                IndustrialOrderMilestone(2, "Gatepass Handshake & Farm Loading", "QR Verified at Loading Bay", 50, 74000.0, "Yesterday, 06:30 PM", isCompleted = true, isActive = false, proofReference = "GP-MH-2026-8790"),
                IndustrialOrderMilestone(3, "Gross & Tare Weighbridge Audit", "Gross 98.4 MT - Tare 19.0 MT = Net 79.4 MT", 0, 0.0, "Today, 09:10 AM", isCompleted = true, isActive = false, proofReference = "WB-SLIP-8812-OK"),
                IndustrialOrderMilestone(4, "Lab Moisture Quality & Final UPI Release", "Awaiting Buyer Authorisation", 30, 44400.0, "Pending Final Release", isCompleted = false, isActive = true, proofReference = "UPI-SETTLE-PENDING")
            ),
            createdAtMillis = System.currentTimeMillis() - 86400000L
        ),
        IndustrialOrderTrackingDetail(
            orderId = "ord_003",
            orderNumber = "ORD-MH-2026-7643",
            lotId = "lot_3",
            contractHash = "0x9D1C7A4E3F802B65",
            cropName = "Soybean",
            residueTitle = "Fine Soybean Husk & Threshed Straw",
            quantityMt = 30.0,
            agreedPricePerMt = 2700.0,
            cropSubtotal = 81000.0,
            mandiPlatformCess = 1215.0,
            freightDeposit = 2000.0,
            totalEscrowLocked = 84215.0,
            sellerFarmerName = "Kailash Gavit",
            sellerFpo = "Dindori Tribal Farmers Collective",
            sellerUpiVpa = "gavit.agri@okaxis",
            buyerCompanyName = "GreenFuel Pelleting Unit",
            buyerContactPerson = "Sunil Patil",
            driverName = "Raju Wagh",
            driverPhone = "+91 97654 32110",
            vehicleNumber = "MH-15-CD-3102",
            gatePassNumber = "GP-MH-2026-7643",
            originLocation = "Dindori Farm Depot",
            destinationPlant = "GreenFuel Factory, Dindori",
            currentMilestoneStage = 1,
            status = OrderStatus.ESCROW_LOCKED,
            grossTareVerifiedWeight = null,
            qualityMoistureResult = null,
            milestones = listOf(
                IndustrialOrderMilestone(1, "Contract Confirmed & Escrow Deposited", "100% Funds Secured in Vault", 20, 16200.0, "Today, 11:00 AM", isCompleted = true, isActive = true, proofReference = "ESCROW-TX-7643"),
                IndustrialOrderMilestone(2, "Gatepass Handshake & Farm Loading", "Awaiting Truck Dispatch", 50, 40500.0, "Pending Truck Arrival", isCompleted = false, isActive = false, proofReference = "GP-MH-2026-7643"),
                IndustrialOrderMilestone(3, "Gross & Tare Weighbridge Audit", "Scale Weigh Verification", 0, 0.0, "Pending Dispatch", isCompleted = false, isActive = false, proofReference = "WB-SLIP-PENDING"),
                IndustrialOrderMilestone(4, "Lab Moisture Quality & Final UPI Release", "Direct UPI Release", 30, 24300.0, "Pending Gate Acceptance", isCompleted = false, isActive = false, proofReference = "UPI-SETTLE-PENDING")
            ),
            createdAtMillis = System.currentTimeMillis() - 7200000L
        ),
        IndustrialOrderTrackingDetail(
            orderId = "ord_004",
            orderNumber = "ORD-MH-2026-6129",
            lotId = "lot_4",
            contractHash = "0x2F78A1D4E90C3B5E",
            cropName = "Wheat",
            residueTitle = "Shredded Golden Wheat Straw",
            quantityMt = 65.0,
            agreedPricePerMt = 2100.0,
            cropSubtotal = 136500.0,
            mandiPlatformCess = 2047.5,
            freightDeposit = 2800.0,
            totalEscrowLocked = 141347.5,
            sellerFarmerName = "Ramesh Patil",
            sellerFpo = "Sinnar Progressive Agro Producers",
            sellerUpiVpa = "ramesh.patil@okhdfcbank",
            buyerCompanyName = "Jindal Bio-Power Plant",
            buyerContactPerson = "Vikram Singhania",
            driverName = "Amol Khairnar",
            driverPhone = "+91 98811 77234",
            vehicleNumber = "MH-15-XY-9081",
            gatePassNumber = "GP-MH-2026-6129",
            originLocation = "Sinnar Rural Hub",
            destinationPlant = "Jindal Bio-Power Plant, Sinnar MIDC",
            currentMilestoneStage = 4,
            status = OrderStatus.SETTLED,
            grossTareVerifiedWeight = 65.2,
            qualityMoistureResult = "Moisture: 9.8% (Grade A - Premium)",
            milestones = listOf(
                IndustrialOrderMilestone(1, "Contract Confirmed & Escrow Deposited", "100% Funds Secured in Vault", 20, 27300.0, "2 days ago", isCompleted = true, isActive = false, proofReference = "ESCROW-TX-6129"),
                IndustrialOrderMilestone(2, "Gatepass Handshake & Farm Loading", "QR Verified at Farm Gate", 50, 68250.0, "2 days ago", isCompleted = true, isActive = false, proofReference = "GP-MH-2026-6129"),
                IndustrialOrderMilestone(3, "Gross & Tare Weighbridge Audit", "Gross 84.6 MT - Tare 19.4 MT = Net 65.2 MT", 0, 0.0, "Yesterday, 02:00 PM", isCompleted = true, isActive = false, proofReference = "WB-SLIP-6129-OK"),
                IndustrialOrderMilestone(4, "Lab Moisture Quality & Final UPI Release", "Full Payout Cleared to ramesh.patil@okhdfcbank", 30, 40950.0, "Yesterday, 04:30 PM", isCompleted = true, isActive = false, proofReference = "UPI-REF-90218734")
            ),
            createdAtMillis = System.currentTimeMillis() - 172800000L
        )
    )

    private val _uiState = MutableStateFlow(OrderDetailUiState(orderId = orderId))
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrderDetail()
    }

    private fun loadOrderDetail() {
        val foundOrder = sampleOrders.find { it.orderId.equals(orderId, ignoreCase = true) } ?: sampleOrders.first()
        _uiState.update { it.copy(order = foundOrder) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    fun verifyWeighbridgeTare(verifiedNetWeight: Double) {
        val currentOrder = _uiState.value.order ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingWeighbridge = true, errorMessage = null) }
            kotlinx.coroutines.delay(650)
            val updatedMilestones = currentOrder.milestones.map { m ->
                if (m.stepIndex == 3) {
                    m.copy(
                        isCompleted = true,
                        isActive = false,
                        statusText = "Weighbridge Verified: ${String.format(java.util.Locale.US, "%.1f", verifiedNetWeight)} MT Net Payload",
                        proofReference = "WB-SLIP-${currentOrder.orderNumber.takeLast(4)}-AUDITED"
                    )
                } else if (m.stepIndex == 4) {
                    m.copy(isActive = true)
                } else {
                    m
                }
            }
            val updatedOrder = currentOrder.copy(
                status = OrderStatus.DELIVERED,
                currentMilestoneStage = 4,
                grossTareVerifiedWeight = verifiedNetWeight,
                milestones = updatedMilestones
            )
            _uiState.update {
                it.copy(
                    order = updatedOrder,
                    isVerifyingWeighbridge = false,
                    feedbackMessage = "Electronic weighbridge audit logged: ${String.format(java.util.Locale.US, "%.1f", verifiedNetWeight)} MT verified. Ready for escrow settlement."
                )
            }
        }
    }

    fun releaseEscrowSettlement() {
        val currentOrder = _uiState.value.order ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isReleasingEscrow = true, errorMessage = null) }
            kotlinx.coroutines.delay(700)
            val finalPayout = currentOrder.cropSubtotal
            val updatedMilestones = currentOrder.milestones.map { m ->
                if (m.stepIndex == 4) {
                    m.copy(
                        isCompleted = true,
                        isActive = false,
                        statusText = "₹${String.format(java.util.Locale.US, "%,.0f", finalPayout * 0.3)} direct UPI cleared to ${currentOrder.sellerUpiVpa}",
                        proofReference = "UPI-SETTLE-${System.currentTimeMillis().toString().takeLast(8)}"
                    )
                } else {
                    m.copy(isCompleted = true, isActive = false)
                }
            }
            val updatedOrder = currentOrder.copy(
                status = OrderStatus.SETTLED,
                currentMilestoneStage = 4,
                milestones = updatedMilestones
            )
            _uiState.update {
                it.copy(
                    order = updatedOrder,
                    isReleasingEscrow = false,
                    feedbackMessage = "Escrow release complete! ₹${String.format(java.util.Locale.US, "%,.0f", finalPayout)} cleared to seller UPI ${currentOrder.sellerUpiVpa}."
                )
            }
        }
    }
}

