package com.sktech.wastetrack.ui.screens.transfer

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.WeighbridgeSlip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class WeighbridgeUiState(
    val selectedFilter: String = "All",
    val isCreateDialogOpen: Boolean = false,
    val selectedSlipForPreview: WeighbridgeSlip? = null,
    val slips: List<WeighbridgeSlip> = listOf(
        WeighbridgeSlip(
            id = "wb_001",
            slipNumber = "WB-2026-9142",
            gatePassNumber = "GP-MH-2026-9142",
            vehicleNumber = "MH-15-EG-4921",
            driverName = "Sachin Shinde",
            commodityName = "Baled Cotton Stalks",
            scaleStationName = "Sinnar MIDC Scale Terminal #2",
            operatorId = "OP-SN-41",
            grossWeightKg = 62400.0,
            tareWeightKg = 17600.0,
            rawNetWeightKg = 44800.0,
            moisturePercent = 10.4,
            moistureDeductionKg = 0.0,
            netPayableBiomassKg = 44800.0,
            allowableMoistureThresholdPercent = 12.0,
            isMoistureAcceptable = true,
            inboundTimeMillis = System.currentTimeMillis() - 4L * 3600 * 1000,
            outboundTimeMillis = System.currentTimeMillis() - 2L * 3600 * 1000,
            digitalSealHash = "0x8F9A4C71B2D0E6F3"
        ),
        WeighbridgeSlip(
            id = "wb_002",
            slipNumber = "WB-2026-8832",
            gatePassNumber = "GP-MH-2026-8832",
            vehicleNumber = "MH-15-BD-8832",
            driverName = "Eknath Khairnar",
            commodityName = "High-Calorific Crushed Bagasse",
            scaleStationName = "Niphad Agro Weigh Scale #1",
            operatorId = "OP-NP-18",
            grossWeightKg = 104200.0,
            tareWeightKg = 24300.0,
            rawNetWeightKg = 79900.0,
            moisturePercent = 11.2,
            moistureDeductionKg = 0.0,
            netPayableBiomassKg = 79900.0,
            allowableMoistureThresholdPercent = 12.0,
            isMoistureAcceptable = true,
            inboundTimeMillis = System.currentTimeMillis() - 16L * 3600 * 1000,
            outboundTimeMillis = System.currentTimeMillis() - 14L * 3600 * 1000,
            digitalSealHash = "0x4B3E91A8F702D9C5"
        ),
        WeighbridgeSlip(
            id = "wb_003",
            slipNumber = "WB-2026-7719",
            gatePassNumber = "GP-MH-2026-7719",
            vehicleNumber = "MH-15-CL-7719",
            driverName = "Ganesh Jadhav",
            commodityName = "Shredded Wheat Straw",
            scaleStationName = "Igatpuri Biomass Terminal Scale",
            operatorId = "OP-IG-09",
            grossWeightKg = 86800.0,
            tareWeightKg = 21900.0,
            rawNetWeightKg = 64900.0,
            moisturePercent = 13.8,
            moistureDeductionKg = 1168.2,
            netPayableBiomassKg = 63731.8,
            allowableMoistureThresholdPercent = 12.0,
            isMoistureAcceptable = false,
            inboundTimeMillis = System.currentTimeMillis() - 8L * 3600 * 1000,
            outboundTimeMillis = System.currentTimeMillis() - 6L * 3600 * 1000,
            digitalSealHash = "0x7D2A1C90B5E3F864"
        )
    )
)

@HiltViewModel
class WeighbridgeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WeighbridgeUiState())
    val uiState: StateFlow<WeighbridgeUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openCreateDialog() {
        _uiState.update { it.copy(isCreateDialogOpen = true) }
    }

    fun closeCreateDialog() {
        _uiState.update { it.copy(isCreateDialogOpen = false) }
    }

    fun selectSlipForPreview(slip: WeighbridgeSlip?) {
        _uiState.update { it.copy(selectedSlipForPreview = slip) }
    }

    fun recordScaleTicket(
        gatePass: String,
        vehicleNo: String,
        driverName: String,
        commodity: String,
        stationName: String,
        operator: String,
        gross: Double,
        tare: Double,
        moisture: Double
    ) {
        val rawNet = (gross - tare).coerceAtLeast(0.0)
        val excessMoisture = (moisture - 12.0).coerceAtLeast(0.0)
        val deduction = if (excessMoisture > 0) (rawNet * (excessMoisture / 100.0)) else 0.0
        val payable = (rawNet - deduction).coerceAtLeast(0.0)

        val randomId = UUID.randomUUID().toString().take(6).uppercase()
        val slipNo = "WB-2026-$randomId"
        val sealHash = "0x" + UUID.randomUUID().toString().replace("-", "").take(16).uppercase()

        val newSlip = WeighbridgeSlip(
            id = "wb_$randomId",
            slipNumber = slipNo,
            gatePassNumber = gatePass.ifBlank { "GP-MH-2026-$randomId" },
            vehicleNumber = vehicleNo.ifBlank { "MH-15-EG-4921" },
            driverName = driverName.ifBlank { "Carrier Fleet Driver" },
            commodityName = commodity.ifBlank { "Mixed Agricultural Residue" },
            scaleStationName = stationName.ifBlank { "MIDC Central Weighbridge #1" },
            operatorId = operator.ifBlank { "OP-SCALE-01" },
            grossWeightKg = gross,
            tareWeightKg = tare,
            rawNetWeightKg = rawNet,
            moisturePercent = moisture,
            moistureDeductionKg = deduction,
            netPayableBiomassKg = payable,
            allowableMoistureThresholdPercent = 12.0,
            isMoistureAcceptable = moisture <= 12.0,
            inboundTimeMillis = System.currentTimeMillis() - 3600 * 1000,
            outboundTimeMillis = System.currentTimeMillis(),
            digitalSealHash = sealHash
        )

        _uiState.update { current ->
            current.copy(
                slips = listOf(newSlip) + current.slips,
                isCreateDialogOpen = false
            )
        }
    }
}
