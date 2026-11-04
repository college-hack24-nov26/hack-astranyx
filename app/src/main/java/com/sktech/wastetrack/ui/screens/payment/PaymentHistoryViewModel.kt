package com.sktech.wastetrack.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.payment.model.PaymentLedgerSummary
import com.sktech.wastetrack.domain.payment.model.PaymentMethod
import com.sktech.wastetrack.domain.payment.model.PaymentRecord
import com.sktech.wastetrack.domain.payment.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class PaymentHistoryUiState(
    val payments: List<PaymentRecord> = emptyList(),
    val filteredPayments: List<PaymentRecord> = emptyList(),
    val selectedFilter: String = "ALL",
    val searchQuery: String = "",
    val summary: PaymentLedgerSummary = PaymentLedgerSummary(0.0, 0.0, 0.0, 0.0),
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null
)

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentHistoryUiState())
    val uiState: StateFlow<PaymentHistoryUiState> = _uiState.asStateFlow()

    init {
        loadInitialPayments()
    }

    private fun loadInitialPayments() {
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L

        val initialList = listOf(
            PaymentRecord(
                id = "PAY-2026-9041",
                orderId = "ORD-MH-4421",
                lotTitle = "Shredded Cotton Stalks (45 MT)",
                cropName = "Cotton",
                farmerName = "Rameshwar Patil",
                farmerPhone = "+91 98221 44512",
                buyerName = "Igatpuri Bio-Energy Private Limited",
                buyerGstin = "27AABCI4819M1Z8",
                weightMt = 45.0,
                ratePerMt = 2800.0,
                materialCost = 126000.0,
                gstAmount = 6300.0,
                tcsAmount = 1260.0,
                freightAmount = 7500.0,
                transportGst = 375.0,
                handlingCharges = 1200.0,
                totalAmount = 142635.0,
                paymentMethod = PaymentMethod.ESCROW_WALLET,
                paymentStatus = PaymentStatus.ESCROW_LOCKED,
                transactionReference = "UTR-SBI-9918237190",
                escrowVaultId = "VAULT-ESC-4401",
                createdAtMillis = now - (dayMillis * 1)
            ),
            PaymentRecord(
                id = "PAY-2026-8890",
                orderId = "ORD-MH-4390",
                lotTitle = "Dry Sugarcane Bagasse Bales (80 MT)",
                cropName = "Sugarcane",
                farmerName = "Vitthal Shinde",
                farmerPhone = "+91 94223 90112",
                buyerName = "Niphad Paper & Board Mills Ltd",
                buyerGstin = "27AAACN5120K1ZX",
                weightMt = 80.0,
                ratePerMt = 2200.0,
                materialCost = 176000.0,
                gstAmount = 8800.0,
                tcsAmount = 1760.0,
                freightAmount = 12000.0,
                transportGst = 600.0,
                handlingCharges = 2000.0,
                totalAmount = 201160.0,
                paymentMethod = PaymentMethod.RTGS_NEFT,
                paymentStatus = PaymentStatus.SETTLED,
                transactionReference = "UTR-HDFC-8821901234",
                escrowVaultId = "VAULT-ESC-4389",
                createdAtMillis = now - (dayMillis * 3),
                escrowReleasedAtMillis = now - (dayMillis * 1)
            ),
            PaymentRecord(
                id = "PAY-2026-8715",
                orderId = "ORD-MH-4355",
                lotTitle = "High-Density Paddy Straw Bales (30 MT)",
                cropName = "Paddy Straw",
                farmerName = "Dnyaneshwar Jadhav",
                farmerPhone = "+91 98901 22345",
                buyerName = "Sinnar Green Pelletization Unit",
                buyerGstin = "27AABCS9011E1ZF",
                weightMt = 30.0,
                ratePerMt = 3100.0,
                materialCost = 93000.0,
                gstAmount = 4650.0,
                tcsAmount = 930.0,
                freightAmount = 5500.0,
                transportGst = 275.0,
                handlingCharges = 900.0,
                totalAmount = 105255.0,
                paymentMethod = PaymentMethod.UPI_INTENT,
                paymentStatus = PaymentStatus.ESCROW_LOCKED,
                transactionReference = "UPI-YESB-4412093812",
                escrowVaultId = "VAULT-ESC-4310",
                createdAtMillis = now - (dayMillis * 2)
            ),
            PaymentRecord(
                id = "PAY-2026-8560",
                orderId = "ORD-MH-4301",
                lotTitle = "Fine Chopped Wheat Stubble (110 MT)",
                cropName = "Wheat",
                farmerName = "Kailas Gaikwad",
                farmerPhone = "+91 97654 33210",
                buyerName = "Nashik Co-Generation Thermal Plant",
                buyerGstin = "27AAACN1922Q1ZP",
                weightMt = 110.0,
                ratePerMt = 2400.0,
                materialCost = 264000.0,
                gstAmount = 13200.0,
                tcsAmount = 2640.0,
                freightAmount = 16500.0,
                transportGst = 825.0,
                handlingCharges = 2500.0,
                totalAmount = 299665.0,
                paymentMethod = PaymentMethod.RTGS_NEFT,
                paymentStatus = PaymentStatus.SETTLED,
                transactionReference = "UTR-BARB-7721839042",
                escrowVaultId = "VAULT-ESC-4255",
                createdAtMillis = now - (dayMillis * 5),
                escrowReleasedAtMillis = now - (dayMillis * 2)
            ),
            PaymentRecord(
                id = "PAY-2026-8412",
                orderId = "ORD-MH-4288",
                lotTitle = "Mustard Husk Briquettes (25 MT)",
                cropName = "Mustard",
                farmerName = "Balasaheb Thorat",
                farmerPhone = "+91 98230 77112",
                buyerName = "Dindori Agri Distillers Hub",
                buyerGstin = "27AABCD3311L1ZK",
                weightMt = 25.0,
                ratePerMt = 3400.0,
                materialCost = 85000.0,
                gstAmount = 4250.0,
                tcsAmount = 850.0,
                freightAmount = 4200.0,
                transportGst = 210.0,
                handlingCharges = 750.0,
                totalAmount = 95260.0,
                paymentMethod = PaymentMethod.UPI_QR,
                paymentStatus = PaymentStatus.DIRECT_PAID,
                transactionReference = "UPI-AXIS-9921830491",
                escrowVaultId = "VAULT-ESC-4190",
                createdAtMillis = now - (dayMillis * 6),
                escrowReleasedAtMillis = now - (dayMillis * 6)
            )
        )

        updatePayments(initialList)
    }

    private fun updatePayments(list: List<PaymentRecord>) {
        val settled = list.filter { it.paymentStatus == PaymentStatus.SETTLED || it.paymentStatus == PaymentStatus.DIRECT_PAID }
            .sumOf { it.totalAmount }
        val escrow = list.filter { it.paymentStatus == PaymentStatus.ESCROW_LOCKED }
            .sumOf { it.totalAmount }
        val freight = list.sumOf { it.freightAmount + it.transportGst }
        val taxes = list.sumOf { it.gstAmount + it.tcsAmount }

        val summary = PaymentLedgerSummary(
            totalSettledAmount = settled,
            totalEscrowLockedAmount = escrow,
            totalFreightAmount = freight,
            totalTaxCollected = taxes
        )

        _uiState.update { current ->
            current.copy(
                payments = list,
                summary = summary
            )
        }
        applyFilters()
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun releaseEscrow(paymentId: String) {
        val updated = _uiState.value.payments.map { payment ->
            if (payment.id == paymentId && payment.paymentStatus == PaymentStatus.ESCROW_LOCKED) {
                payment.copy(
                    paymentStatus = PaymentStatus.SETTLED,
                    escrowReleasedAtMillis = System.currentTimeMillis()
                )
            } else {
                payment
            }
        }
        updatePayments(updated)
        _uiState.update {
            it.copy(feedbackMessage = "Escrow funds released to farmer bank account successfully")
        }
    }

    fun addPaymentRecord(
        cropName: String,
        lotTitle: String,
        farmerName: String,
        farmerPhone: String,
        buyerName: String,
        buyerGstin: String,
        weightMt: Double,
        ratePerMt: Double,
        freightAmount: Double,
        handlingCharges: Double,
        paymentMethod: PaymentMethod,
        transactionRef: String
    ) {
        val materialCost = weightMt * ratePerMt
        val gst = materialCost * 0.05
        val tcs = materialCost * 0.01
        val transportGst = freightAmount * 0.05
        val grandTotal = materialCost + gst + tcs + freightAmount + transportGst + handlingCharges

        val record = PaymentRecord(
            id = "PAY-2026-" + (1000 + (Math.random() * 9000).toInt()),
            orderId = "ORD-MH-" + (4000 + (Math.random() * 1000).toInt()),
            lotTitle = lotTitle,
            cropName = cropName,
            farmerName = farmerName,
            farmerPhone = farmerPhone,
            buyerName = buyerName,
            buyerGstin = buyerGstin,
            weightMt = weightMt,
            ratePerMt = ratePerMt,
            materialCost = materialCost,
            gstAmount = gst,
            tcsAmount = tcs,
            freightAmount = freightAmount,
            transportGst = transportGst,
            handlingCharges = handlingCharges,
            totalAmount = grandTotal,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == PaymentMethod.UPI_INTENT || paymentMethod == PaymentMethod.UPI_QR) PaymentStatus.DIRECT_PAID else PaymentStatus.ESCROW_LOCKED,
            transactionReference = transactionRef,
            escrowVaultId = "VAULT-ESC-" + (4000 + (Math.random() * 1000).toInt()),
            createdAtMillis = System.currentTimeMillis(),
            escrowReleasedAtMillis = if (paymentMethod == PaymentMethod.UPI_INTENT || paymentMethod == PaymentMethod.UPI_QR) System.currentTimeMillis() else null
        )

        val updated = listOf(record) + _uiState.value.payments
        updatePayments(updated)
        _uiState.update {
            it.copy(feedbackMessage = "Transaction ${record.id} settled and recorded in Escrow Ledger")
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    private fun applyFilters() {
        val current = _uiState.value
        val query = current.searchQuery.trim().lowercase()
        val filter = current.selectedFilter

        val filtered = current.payments.filter { payment ->
            val matchesFilter = when (filter) {
                "ESCROW_LOCKED" -> payment.paymentStatus == PaymentStatus.ESCROW_LOCKED
                "SETTLED" -> payment.paymentStatus == PaymentStatus.SETTLED
                "DIRECT_PAID" -> payment.paymentStatus == PaymentStatus.DIRECT_PAID
                else -> true
            }

            val matchesQuery = query.isBlank() ||
                    payment.id.lowercase().contains(query) ||
                    payment.orderId.lowercase().contains(query) ||
                    payment.lotTitle.lowercase().contains(query) ||
                    payment.cropName.lowercase().contains(query) ||
                    payment.farmerName.lowercase().contains(query) ||
                    payment.buyerName.lowercase().contains(query) ||
                    payment.transactionReference.lowercase().contains(query)

            matchesFilter && matchesQuery
        }

        _uiState.update { it.copy(filteredPayments = filtered) }
    }
}

