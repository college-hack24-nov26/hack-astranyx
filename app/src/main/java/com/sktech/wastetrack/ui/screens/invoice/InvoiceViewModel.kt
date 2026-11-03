package com.sktech.wastetrack.ui.screens.invoice

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.invoice.AgriHsnCatalog
import com.sktech.wastetrack.domain.model.invoice.AgriHsnItem
import com.sktech.wastetrack.domain.model.invoice.BiomassInvoice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class InvoiceUiState(
    val selectedFilter: String = "All",
    val isCreateDialogOpen: Boolean = false,
    val selectedInvoiceForPreview: BiomassInvoice? = null,
    val invoices: List<BiomassInvoice> = listOf(
        BiomassInvoice(
            id = "inv_001",
            invoiceNumber = "INV-MH-2026-0819",
            eWayBillNumber = "EWB-7192-8819-2041",
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
            invoiceNumber = "INV-MH-2026-0744",
            eWayBillNumber = "EWB-8832-6105-3912",
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
            dateMillis = System.currentTimeMillis() - 5L * 86400 * 1000,
            status = "PAID & SETTLED"
        ),
        BiomassInvoice(
            id = "inv_003",
            invoiceNumber = "INV-MH-2026-0691",
            eWayBillNumber = "EWB-6105-4412-1089",
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
            dateMillis = System.currentTimeMillis() - 8L * 86400 * 1000,
            status = "ESCROW PENDING"
        )
    )
)

@HiltViewModel
class InvoiceViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(InvoiceUiState())
    val uiState: StateFlow<InvoiceUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(isCreateDialogOpen = true) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(isCreateDialogOpen = false) }
    }

    fun selectInvoiceForPreview(invoice: BiomassInvoice?) {
        _uiState.update { it.copy(selectedInvoiceForPreview = invoice) }
    }

    fun createInvoice(
        consignorName: String,
        consigneeName: String,
        hsnItem: AgriHsnItem,
        quantityMt: Double,
        ratePerMt: Double,
        vehicleNo: String,
        mpcbRef: String
    ) {
        val taxable = quantityMt * ratePerMt
        val cgst = taxable * 0.025
        val sgst = taxable * 0.025
        val tcs = taxable * 0.01
        val total = taxable + cgst + sgst + tcs

        val randomId = UUID.randomUUID().toString().take(6).uppercase()
        val invoiceNo = "INV-MH-2026-$randomId"
        val ewbNo = "EWB-9921-$randomId-2026"

        val newInvoice = BiomassInvoice(
            id = "inv_$randomId",
            invoiceNumber = invoiceNo,
            eWayBillNumber = ewbNo,
            consignorName = consignorName.ifBlank { "Sinnar Agro FPO Producer Ltd" },
            consignorGstin = "27AABCU9603R1ZM",
            consigneeName = consigneeName.ifBlank { "Industrial Bio-Power Unit" },
            consigneeGstin = "27AAACM1234L1Z5",
            hsnCode = hsnItem.hsnCode,
            commodity = hsnItem.commodityName,
            quantityMt = quantityMt,
            ratePerMt = ratePerMt,
            taxableAmount = taxable,
            cgstAmount = cgst,
            sgstAmount = sgst,
            tcsAmount = tcs,
            totalAmount = total,
            vehicleNumber = vehicleNo.ifBlank { "MH-15-EG-4921" },
            mpcbForm10Ref = mpcbRef.ifBlank { "MPCB-F10-NSK-2026-$randomId" },
            dateMillis = System.currentTimeMillis(),
            status = "PAID & SETTLED"
        )

        _uiState.update { current ->
            current.copy(
                invoices = listOf(newInvoice) + current.invoices,
                isCreateDialogOpen = false
            )
        }
    }
}
