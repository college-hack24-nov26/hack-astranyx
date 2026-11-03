package com.sktech.wastetrack.ui.screens.transfer

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
import java.security.MessageDigest
import javax.inject.Inject

data class TransferUiState(
    val selectedFilter: String = "All",
    val transfers: List<TransferItem> = listOf(
        TransferItem(
            id = "tr_001",
            gatePassNumber = "GP-MH-2026-9142",
            residueTitle = "Baled Dry Cotton Stalks (45 MT)",
            cropName = "Cotton",
            quantityMt = 45.0,
            vehicleNumber = "MH-15-EG-4921",
            driverName = "Sachin Shinde",
            driverPhone = "+91 98220 14829",
            sourceLocation = "Sinnar Agro Hub, Nashik",
            destinationPlant = "Mahindra Bio-Energy Plant, Igatpuri",
            status = TransferStatus.IN_TRANSIT,
            weightAtSourceMt = 45.0,
            weighbridgeGrossMt = 62.4,
            weighbridgeTareMt = 17.6,
            weighbridgeNetMt = 44.8,
            dispatchTimestampMillis = System.currentTimeMillis() - 2L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-9142|TRUCK:MH15EG4921|CROP:COTTON|QTY:45MT|ORIGIN:SINNAR",
            digitalHashSignature = "0x8F9A4C71B2D0E6F3"
        ),
        TransferItem(
            id = "tr_002",
            gatePassNumber = "GP-MH-2026-8832",
            residueTitle = "High-Calorific Crushed Bagasse (80 MT)",
            cropName = "Sugarcane",
            quantityMt = 80.0,
            vehicleNumber = "MH-15-BD-8832",
            driverName = "Eknath Khairnar",
            driverPhone = "+91 98224 81920",
            sourceLocation = "Niphad Bio-Energy Cluster",
            destinationPlant = "Niphad Co-Gen Biomass Plant",
            status = TransferStatus.VERIFIED,
            weightAtSourceMt = 80.0,
            weighbridgeGrossMt = 104.2,
            weighbridgeTareMt = 24.3,
            weighbridgeNetMt = 79.9,
            dispatchTimestampMillis = System.currentTimeMillis() - 14L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-8832|TRUCK:MH15BD8832|CROP:SUGARCANE|QTY:80MT|ORIGIN:NIPHAD",
            digitalHashSignature = "0x4B3E91A8F702D9C5"
        ),
        TransferItem(
            id = "tr_003",
            gatePassNumber = "GP-MH-2026-7719",
            residueTitle = "Shredded Golden Wheat Straw (65 MT)",
            cropName = "Wheat",
            quantityMt = 65.0,
            vehicleNumber = "MH-15-CL-7719",
            driverName = "Ganesh Jadhav",
            driverPhone = "+91 94227 38192",
            sourceLocation = "Sinnar Agro Hub, Nashik",
            destinationPlant = "Sinnar Recycled Paper & Board Mill",
            status = TransferStatus.DELIVERED,
            weightAtSourceMt = 65.0,
            weighbridgeGrossMt = 86.8,
            weighbridgeTareMt = 21.9,
            weighbridgeNetMt = 64.9,
            dispatchTimestampMillis = System.currentTimeMillis() - 6L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-7719|TRUCK:MH15CL7719|CROP:WHEAT|QTY:65MT|ORIGIN:SINNAR",
            digitalHashSignature = "0x7D2A1C90B5E3F864"
        ),
        TransferItem(
            id = "tr_004",
            gatePassNumber = "GP-MH-2026-6105",
            residueTitle = "Threshed Soybean Husk & Straw (30 MT)",
            cropName = "Soybean",
            quantityMt = 30.0,
            vehicleNumber = "MH-15-AK-1204",
            driverName = "Dnyaneshwar Borse",
            driverPhone = "+91 97631 92834",
            sourceLocation = "Dindori Agri Cluster",
            destinationPlant = "Malegaon Green Fuel Pelletization",
            status = TransferStatus.INITIATED,
            weightAtSourceMt = 30.0,
            weighbridgeGrossMt = null,
            weighbridgeTareMt = null,
            weighbridgeNetMt = null,
            dispatchTimestampMillis = System.currentTimeMillis() - 40 * 60 * 1000,
            qrPayload = "GP:GP-MH-2026-6105|TRUCK:MH15AK1204|CROP:SOYBEAN|QTY:30MT|ORIGIN:DINDORI",
            digitalHashSignature = "0x1E8F6A902D4C3B77"
        )
    ),
    val selectedTransferForQR: TransferItem? = null,
    val activeQRTransferId: String = "tr_001",
    val activeQRPayload: String = "",
    val activeQRSignature: String = "",
    val qrGeneratedAtMillis: Long = System.currentTimeMillis()
)

@HiltViewModel
class TransferViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        prepareQRForTransfer("tr_001")
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun showGatepassQR(item: TransferItem) {
        _uiState.update { it.copy(selectedTransferForQR = item) }
    }

    fun dismissQR() {
        _uiState.update { it.copy(selectedTransferForQR = null) }
    }

    fun getTransferById(id: String): TransferItem? {
        return _uiState.value.transfers.find { it.id == id } ?: _uiState.value.transfers.firstOrNull()
    }

    fun prepareQRForTransfer(id: String) {
        val item = getTransferById(id) ?: return
        val now = System.currentTimeMillis()
        val token = MessageDigest.getInstance("SHA-256")
            .digest("${item.id}_${item.vehicleNumber}_$now".toByteArray())
            .take(8)
            .joinToString("") { "%02x".format(it) }
            .uppercase()
        val cleanVehicle = item.vehicleNumber.replace("-", "").uppercase()
        val cleanCrop = item.cropName.uppercase()
        val cleanOrigin = item.sourceLocation.split(",")[0].trim().uppercase()
        val payload = "GP:${item.gatePassNumber}|TRUCK:$cleanVehicle|CROP:$cleanCrop|QTY:${item.quantityMt.toInt()}MT|ORIGIN:$cleanOrigin"
        _uiState.update {
            it.copy(
                activeQRTransferId = item.id,
                activeQRPayload = payload,
                activeQRSignature = "0x$token",
                qrGeneratedAtMillis = now
            )
        }
    }

    fun confirmWeighbridgeReceipt(transferId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val updated = state.transfers.map { tr ->
                    if (tr.id == transferId) {
                        tr.copy(
                            status = TransferStatus.VERIFIED,
                            weighbridgeGrossMt = tr.weightAtSourceMt + 17.5,
                            weighbridgeTareMt = 17.5,
                            weighbridgeNetMt = tr.weightAtSourceMt - 0.2
                        )
                    } else tr
                }
                state.copy(transfers = updated)
            }
        }
    }
}
