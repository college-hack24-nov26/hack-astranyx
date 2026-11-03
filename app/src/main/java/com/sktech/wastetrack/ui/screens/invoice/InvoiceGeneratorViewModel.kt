package com.sktech.wastetrack.ui.screens.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.invoice.AgriHsnCatalog
import com.sktech.wastetrack.domain.model.invoice.AgriHsnItem
import com.sktech.wastetrack.domain.model.invoice.BiomassInvoice
import com.sktech.wastetrack.domain.model.invoice.EWayBillUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class InvoiceTaxLedger(
    val taxableAmount: Double = 0.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val mandiCessAmount: Double = 0.0,
    val tcsAmount: Double = 0.0,
    val totalInvoiceAmount: Double = 0.0,
    val netFarmerPayout: Double = 0.0
) {
    companion object {
        fun compute(
            quantityMt: Double,
            ratePerMt: Double,
            isInterState: Boolean,
            applyTcs: Boolean,
            applyMandiCess: Boolean
        ): InvoiceTaxLedger {
            val taxable = quantityMt * ratePerMt
            val cgst = if (isInterState) 0.0 else taxable * 0.025
            val sgst = if (isInterState) 0.0 else taxable * 0.025
            val igst = if (isInterState) taxable * 0.05 else 0.0
            val mandiCess = if (applyMandiCess) taxable * 0.015 else 0.0
            val tcs = if (applyTcs) taxable * 0.01 else 0.0
            val total = taxable + cgst + sgst + igst + tcs
            val netFarmer = (taxable - mandiCess).coerceAtLeast(0.0)
            return InvoiceTaxLedger(
                taxableAmount = taxable,
                cgstAmount = cgst,
                sgstAmount = sgst,
                igstAmount = igst,
                mandiCessAmount = mandiCess,
                tcsAmount = tcs,
                totalInvoiceAmount = total,
                netFarmerPayout = netFarmer
            )
        }
    }
}

data class InvoiceGeneratorUiState(
    val sellerName: String = "Sinnar Agro FPO Producer Ltd",
    val sellerGstin: String = "27AABCU9603R1ZM",
    val sellerAddress: String = "Sinnar MIDC Agro Cluster, Nashik, MH - 422103",
    val buyerName: String = "Mahindra Bio-Energy Plant, Igatpuri",
    val buyerGstin: String = "27AAACM1234L1Z5",
    val buyerAddress: String = "Plot C-14, MIDC Industrial Area, Igatpuri - 422403",
    val transporterName: String = "Kisan Express Logistics",
    val transporterId: String = "27AAECK4819M1Z3",
    val vehicleNumber: String = "MH-15-EG-4921",
    val selectedHsnItem: AgriHsnItem = AgriHsnCatalog.items.first(),
    val quantityMtText: String = "35.0",
    val ratePerMtText: String = "2200.0",
    val approxDistanceKmText: String = "45",
    val isInterState: Boolean = false,
    val applyTcs: Boolean = true,
    val applyMandiCess: Boolean = true,
    val taxLedger: InvoiceTaxLedger = InvoiceTaxLedger.compute(35.0, 2200.0, false, true, true),
    val validityHours: Int = 24,
    val isGenerating: Boolean = false,
    val generatedInvoice: BiomassInvoice? = null,
    val message: String? = null,
    val error: String? = null,
    val historyFilter: String = "All",
    val invoices: List<BiomassInvoice> = listOf(
        BiomassInvoice(
            id = "inv_001",
            invoiceNumber = "INV/MH-26/0819",
            eWayBillNumber = "278819204123",
            consignorName = "Sinnar Agro FPO Producer Ltd",
            consignorGstin = "27AABCU9603R1ZM",
            consigneeName = "Mahindra Bio-Energy Plant, Igatpuri",
            consigneeGstin = "27AAACM1234L1Z5",
            hsnCode = "1404",
            commodity = "Baled Dry Cotton Stalk Biomass",
            quantityMt = 45.0,
            ratePerMt = 2800.0,
            taxableAmount = 126000.0,
            cgstAmount = 3150.0,
            sgstAmount = 3150.0,
            tcsAmount = 1260.0,
            totalAmount = 133560.0,
            vehicleNumber = "MH-15-EG-4921",
            mpcbForm10Ref = "MPCB-F10-NSK-2026-9142",
            dateMillis = System.currentTimeMillis() - 2L * 86400 * 1000,
            status = "PAID & SETTLED"
        ),
        BiomassInvoice(
            id = "inv_002",
            invoiceNumber = "INV/MH-26/0744",
            eWayBillNumber = "276105391288",
            consignorName = "Niphad Sugarcane Biomass Cooperative",
            consignorGstin = "27AABCN8812K1Z9",
            consigneeName = "Niphad Co-Gen Power Project",
            consigneeGstin = "27AAACN5542P1Z1",
            hsnCode = "2308",
            commodity = "High-Calorific Crushed Bagasse",
            quantityMt = 80.0,
            ratePerMt = 1900.0,
            taxableAmount = 152000.0,
            cgstAmount = 3800.0,
            sgstAmount = 3800.0,
            tcsAmount = 1520.0,
            totalAmount = 161120.0,
            vehicleNumber = "MH-15-BD-8832",
            mpcbForm10Ref = "MPCB-F10-NSK-2026-8832",
            dateMillis = System.currentTimeMillis() - 4L * 86400 * 1000,
            status = "PAID & SETTLED"
        ),
        BiomassInvoice(
            id = "inv_003",
            invoiceNumber = "INV/MH-26/0691",
            eWayBillNumber = "274412108961",
            consignorName = "Dindori Vineyard Residue Cluster",
            consignorGstin = "27AABCD4411Q1ZX",
            consigneeName = "Malegaon Green Fuel Pelletization",
            consigneeGstin = "27AAACM9921C1Z3",
            hsnCode = "4401",
            commodity = "Compacted Biomass Fuel Pellets",
            quantityMt = 30.0,
            ratePerMt = 4500.0,
            taxableAmount = 135000.0,
            cgstAmount = 3375.0,
            sgstAmount = 3375.0,
            tcsAmount = 1350.0,
            totalAmount = 143100.0,
            vehicleNumber = "MH-15-AK-1204",
            mpcbForm10Ref = "MPCB-F10-NSK-2026-6105",
            dateMillis = System.currentTimeMillis() - 7L * 86400 * 1000,
            status = "PENDING CLEARANCE"
        )
    )
)

@HiltViewModel
class InvoiceGeneratorViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceGeneratorUiState())
    val uiState: StateFlow<InvoiceGeneratorUiState> = _uiState.asStateFlow()

    fun updateSeller(name: String, gstin: String, address: String) {
        _uiState.update { it.copy(sellerName = name, sellerGstin = gstin, sellerAddress = address) }
    }

    fun updateBuyer(name: String, gstin: String, address: String) {
        _uiState.update { it.copy(buyerName = name, buyerGstin = gstin, buyerAddress = address) }
    }

    fun updateTransporter(name: String, id: String, vehicle: String) {
        _uiState.update { it.copy(transporterName = name, transporterId = id, vehicleNumber = vehicle) }
    }

    fun selectHsnItem(item: AgriHsnItem) {
        _uiState.update { current ->
            val updated = current.copy(
                selectedHsnItem = item,
                ratePerMtText = item.benchmarkRatePerMt.toString()
            )
            recalculate(updated)
        }
    }

    fun updateQuantity(qtyText: String) {
        _uiState.update { current ->
            recalculate(current.copy(quantityMtText = qtyText))
        }
    }

    fun updateRate(rateText: String) {
        _uiState.update { current ->
            recalculate(current.copy(ratePerMtText = rateText))
        }
    }

    fun updateDistance(distanceText: String) {
        val dist = distanceText.toIntOrNull() ?: 0
        val validity = EWayBillUtils.calculateValidityHours(dist)
        _uiState.update { it.copy(approxDistanceKmText = distanceText, validityHours = validity) }
    }

    fun toggleInterState(interState: Boolean) {
        _uiState.update { current ->
            recalculate(current.copy(isInterState = interState))
        }
    }

    fun toggleTcs(apply: Boolean) {
        _uiState.update { current ->
            recalculate(current.copy(applyTcs = apply))
        }
    }

    fun toggleMandiCess(apply: Boolean) {
        _uiState.update { current ->
            recalculate(current.copy(applyMandiCess = apply))
        }
    }

    fun setHistoryFilter(filter: String) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun clearGeneratedInvoice() {
        _uiState.update { it.copy(generatedInvoice = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    fun generateOfficialInvoice() {
        val current = _uiState.value
        val qty = current.quantityMtText.toDoubleOrNull() ?: 0.0
        val rate = current.ratePerMtText.toDoubleOrNull() ?: 0.0

        if (qty <= 0.0) {
            _uiState.update { it.copy(error = "Quantity must be greater than zero MT") }
            return
        }
        if (rate <= 0.0) {
            _uiState.update { it.copy(error = "Benchmark rate must be greater than zero") }
            return
        }
        if (current.vehicleNumber.isBlank()) {
            _uiState.update { it.copy(error = "Vehicle registration number is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }

            val invoiceNo = EWayBillUtils.generateInvoiceNumber()
            val ewbNo = EWayBillUtils.generateEWayBillNumber()
            val randomToken = UUID.randomUUID().toString().take(6).uppercase()
            val mpcbRef = "MPCB-F10-NSK-2026-$randomToken"

            val newInvoice = BiomassInvoice(
                id = "inv_$randomToken",
                invoiceNumber = invoiceNo,
                eWayBillNumber = ewbNo,
                consignorName = current.sellerName.ifBlank { "Sinnar Agro FPO Producer Ltd" },
                consignorGstin = current.sellerGstin.ifBlank { "27AABCU9603R1ZM" },
                consigneeName = current.buyerName.ifBlank { "Mahindra Bio-Energy Plant" },
                consigneeGstin = current.buyerGstin.ifBlank { "27AAACM1234L1Z5" },
                hsnCode = current.selectedHsnItem.hsnCode,
                commodity = current.selectedHsnItem.commodityName,
                quantityMt = qty,
                ratePerMt = rate,
                taxableAmount = current.taxLedger.taxableAmount,
                cgstAmount = current.taxLedger.cgstAmount,
                sgstAmount = current.taxLedger.sgstAmount,
                tcsAmount = current.taxLedger.tcsAmount,
                totalAmount = current.taxLedger.totalInvoiceAmount,
                vehicleNumber = current.vehicleNumber.uppercase(),
                mpcbForm10Ref = mpcbRef,
                dateMillis = System.currentTimeMillis(),
                status = "PAID & SETTLED"
            )

            _uiState.update { state ->
                state.copy(
                    isGenerating = false,
                    generatedInvoice = newInvoice,
                    invoices = listOf(newInvoice) + state.invoices,
                    message = "Form GST EWB-01 & Tax Invoice Generated Successfully"
                )
            }
        }
    }

    private fun recalculate(state: InvoiceGeneratorUiState): InvoiceGeneratorUiState {
        val qty = state.quantityMtText.toDoubleOrNull() ?: 0.0
        val rate = state.ratePerMtText.toDoubleOrNull() ?: 0.0
        val ledger = InvoiceTaxLedger.compute(
            quantityMt = qty,
            ratePerMt = rate,
            isInterState = state.isInterState,
            applyTcs = state.applyTcs,
            applyMandiCess = state.applyMandiCess
        )
        return state.copy(taxLedger = ledger)
    }
}

