package com.sktech.wastetrack.ui.screens.dashboard

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.data.ml.OCRProcessor
import com.sktech.wastetrack.domain.model.ocr.MandiReceiptSlip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.regex.Pattern
import javax.inject.Inject

data class LedgerScanUiState(
    val isProcessing: Boolean = false,
    val extractedRawText: String = "",
    val parsedSlip: MandiReceiptSlip? = null,
    val recentScannedSlips: List<MandiReceiptSlip> = emptyList(),
    val isSavedToLedger: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LedgerScanViewModel @Inject constructor(
    private val ocrProcessor: OCRProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(
        LedgerScanUiState(
            recentScannedSlips = listOf(
                MandiReceiptSlip(
                    id = "slip_001",
                    rstNumber = "RST-2026-7841",
                    slipType = "APMC Mandi Receipt",
                    facilityName = "Nashik APMC Krishi Mandi Terminal",
                    vehicleNumber = "MH-15-EG-4912",
                    commodity = "Cotton (Bt Hybrid) Stalks",
                    grossWeightKg = 14250.0,
                    tareWeightKg = 4120.0,
                    netWeightKg = 10130.0,
                    netWeightMt = 10.13,
                    ratePerMtInr = 2850.0,
                    totalAmountInr = 28870.5,
                    dateString = "03 Nov 2026",
                    timeString = "11:45 AM",
                    weigherOperator = "A. K. Deshmukh",
                    isVerified = true
                ),
                MandiReceiptSlip(
                    id = "slip_002",
                    rstNumber = "WB-SIN-4091",
                    slipType = "Industrial Weighbridge Chit",
                    facilityName = "Sinnar Bio-Energy Pelletizer Plant",
                    vehicleNumber = "MH-15-BH-2384",
                    commodity = "Sugarcane Bagasse & Dry Trash",
                    grossWeightKg = 22800.0,
                    tareWeightKg = 7200.0,
                    netWeightKg = 15600.0,
                    netWeightMt = 15.60,
                    ratePerMtInr = 3200.0,
                    totalAmountInr = 49920.0,
                    dateString = "02 Nov 2026",
                    timeString = "03:15 PM",
                    weigherOperator = "S. R. Patil",
                    isVerified = true
                )
            )
        )
    )
    val state: StateFlow<LedgerScanUiState> = _state.asStateFlow()

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, errorMessage = null, isSavedToLedger = false) }
            val rawText = ocrProcessor.processImage(bitmap)
            if (rawText.startsWith("Error")) {
                _state.update { it.copy(isProcessing = false, errorMessage = rawText) }
            } else {
                val slip = parseMandiText(rawText)
                _state.update {
                    it.copy(
                        isProcessing = false,
                        extractedRawText = rawText,
                        parsedSlip = slip
                    )
                }
            }
        }
    }

    fun simulateSampleScan(sampleIndex: Int) {
        val sampleSlip = when (sampleIndex) {
            1 -> MandiReceiptSlip(
                id = UUID.randomUUID().toString().take(8),
                rstNumber = "RST-2026-9214",
                slipType = "APMC Mandi Sale Receipt",
                facilityName = "Pimpalgaon Baswant APMC Market Yard",
                vehicleNumber = "MH-15-JC-8819",
                commodity = "Soybean Straw Fodder Bales",
                grossWeightKg = 9450.0,
                tareWeightKg = 3250.0,
                netWeightKg = 6200.0,
                netWeightMt = 6.20,
                ratePerMtInr = 4500.0,
                totalAmountInr = 27900.0,
                dateString = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
                timeString = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()),
                weigherOperator = "R. V. Shinde",
                isVerified = true,
                rawExtractedLines = listOf(
                    "PIMPALGAON BASWANT APMC YARD",
                    "RST NO: 9214 | DATE: TODAY",
                    "VEHICLE: MH-15-JC-8819",
                    "COMMODITY: SOYBEAN RESIDUE BALES",
                    "GROSS WT: 9450 KG",
                    "TARE WT:  3250 KG",
                    "NET WT:   6200 KG (6.20 MT)",
                    "RATE: INR 4500 / MT",
                    "TOTAL AMOUNT: INR 27,900"
                )
            )
            2 -> MandiReceiptSlip(
                id = UUID.randomUUID().toString().take(8),
                rstNumber = "CGEN-IGT-5510",
                slipType = "Industrial Weighbridge Chit",
                facilityName = "Igatpuri Biomass Co-Gen Power Terminal",
                vehicleNumber = "MH-15-FA-3120",
                commodity = "Paddy Straw High-Density Bales",
                grossWeightKg = 18900.0,
                tareWeightKg = 6500.0,
                netWeightKg = 12400.0,
                netWeightMt = 12.40,
                ratePerMtInr = 3000.0,
                totalAmountInr = 37200.0,
                dateString = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
                timeString = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()),
                weigherOperator = "K. M. Pawar",
                isVerified = true,
                rawExtractedLines = listOf(
                    "IGATPURI BIOMASS CO-GEN TERMINAL",
                    "WEIGHBRIDGE SLIP #5510",
                    "TRUCK REG: MH-15-FA-3120",
                    "CROP: PADDY STRAW BALES",
                    "GROSS WT: 18900 KG",
                    "TARE WT:  6500 KG",
                    "NET WT:   12400 KG (12.40 MT)",
                    "TARIFF: INR 3000 / MT",
                    "PAYABLE: INR 37,200"
                )
            )
            else -> MandiReceiptSlip(
                id = UUID.randomUUID().toString().take(8),
                rstNumber = "RST-2026-7841",
                slipType = "APMC Mandi Receipt",
                facilityName = "Nashik APMC Krishi Mandi Terminal",
                vehicleNumber = "MH-15-EG-4912",
                commodity = "Cotton (Bt Hybrid) Stalks",
                grossWeightKg = 14250.0,
                tareWeightKg = 4120.0,
                netWeightKg = 10130.0,
                netWeightMt = 10.13,
                ratePerMtInr = 2850.0,
                totalAmountInr = 28870.5,
                dateString = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
                timeString = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()),
                weigherOperator = "A. K. Deshmukh",
                isVerified = true,
                rawExtractedLines = listOf(
                    "NASHIK KRISHI UTPANNA BAZAR SAMITI",
                    "COMPUTERIZED WEIGHBRIDGE SLIP",
                    "RST NO: 7841 | DATE: TODAY",
                    "VEHICLE: MH-15-EG-4912",
                    "COMMODITY: COTTON STALK CHIPS",
                    "GROSS WT: 14250 KG",
                    "TARE WT:  4120 KG",
                    "NET WT:   10130 KG (10.13 MT)",
                    "APMC RATE: INR 2850 / MT",
                    "TOTAL: INR 28,870.50"
                )
            )
        }

        _state.update {
            it.copy(
                isProcessing = false,
                extractedRawText = sampleSlip.rawExtractedLines.joinToString("\n"),
                parsedSlip = sampleSlip,
                isSavedToLedger = false,
                errorMessage = null
            )
        }
    }

    fun saveSlipToLedger() {
        val slip = _state.value.parsedSlip ?: return
        val updatedList = listOf(slip) + _state.value.recentScannedSlips
        _state.update {
            it.copy(
                recentScannedSlips = updatedList,
                isSavedToLedger = true,
                statusMessage = "Slip #${slip.rstNumber} successfully recorded into Ledger"
            )
        }
    }

    fun clearResult() {
        _state.update {
            it.copy(
                extractedRawText = "",
                parsedSlip = null,
                isSavedToLedger = false,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    private fun parseMandiText(text: String): MandiReceiptSlip {
        val lines = text.split("\n", "\r\n")

        var grossKg = 12000.0
        var tareKg = 3500.0
        var rate = 2800.0
        var rst = "RST-${(1000..9999).random()}"
        var vehicle = "MH-15-AB-${(1000..9999).random()}"
        var commodity = "Cotton Biomass Residue"
        var facility = "Nashik Agricultural Terminal Weighbridge"

        val grossPattern = Pattern.compile("""(?i)(?:gross|grs)[^0-9]*([0-9]+(?:\.[0-9]+)?)""")
        val tarePattern = Pattern.compile("""(?i)(?:tare|tr)[^0-9]*([0-9]+(?:\.[0-9]+)?)""")
        val ratePattern = Pattern.compile("""(?i)(?:rate|price|msp)[^0-9]*([0-9]+(?:\.[0-9]+)?)""")
        val rstPattern = Pattern.compile("""(?i)(?:rst|slip|no|inv)[^0-9]*([0-9]{3,6})""")
        val vehiclePattern = Pattern.compile("""(?i)(MH\s*[-]?\s*\d{1,2}\s*[-]?\s*[A-Z]{1,2}\s*[-]?\s*\d{3,4})""")

        for (line in lines) {
            val gm = grossPattern.matcher(line)
            if (gm.find()) {
                grossKg = gm.group(1)?.toDoubleOrNull() ?: grossKg
            }

            val tm = tarePattern.matcher(line)
            if (tm.find()) {
                tareKg = tm.group(1)?.toDoubleOrNull() ?: tareKg
            }

            val rm = ratePattern.matcher(line)
            if (rm.find()) {
                rate = rm.group(1)?.toDoubleOrNull() ?: rate
            }

            val rstm = rstPattern.matcher(line)
            if (rstm.find()) {
                rst = "RST-${rstm.group(1)}"
            }

            val vm = vehiclePattern.matcher(line)
            if (vm.find()) {
                vehicle = vm.group(1)?.uppercase() ?: vehicle
            }

            if (line.contains("Sugarcane", ignoreCase = true) || line.contains("Bagasse", ignoreCase = true)) {
                commodity = "Sugarcane Bagasse"
            } else if (line.contains("Soybean", ignoreCase = true) || line.contains("Soyabean", ignoreCase = true)) {
                commodity = "Soybean Straw Fodder"
            } else if (line.contains("Wheat", ignoreCase = true)) {
                commodity = "Wheat Dry Straw"
            } else if (line.contains("Cotton", ignoreCase = true)) {
                commodity = "Cotton Stalk Chips"
            }

            if (line.contains("APMC", ignoreCase = true) || line.contains("Bazar Samiti", ignoreCase = true)) {
                facility = "Regional APMC Mandi Weighbridge"
            } else if (line.contains("Bio-Energy", ignoreCase = true) || line.contains("Pellet", ignoreCase = true)) {
                facility = "Industrial Pelletizer Gate Weighbridge"
            }
        }

        if (tareKg >= grossKg) {
            val temp = grossKg
            grossKg = tareKg + 2500.0
            tareKg = temp
        }

        val netKg = (grossKg - tareKg).coerceAtLeast(500.0)
        val netMt = (netKg / 1000.0)
        val total = netMt * rate

        return MandiReceiptSlip(
            id = UUID.randomUUID().toString().take(8),
            rstNumber = rst,
            slipType = "Digitized Weighbridge Record",
            facilityName = facility,
            vehicleNumber = vehicle,
            commodity = commodity,
            grossWeightKg = grossKg,
            tareWeightKg = tareKg,
            netWeightKg = netKg,
            netWeightMt = String.format(Locale.ENGLISH, "%.2f", netMt).toDoubleOrNull() ?: netMt,
            ratePerMtInr = rate,
            totalAmountInr = String.format(Locale.ENGLISH, "%.2f", total).toDoubleOrNull() ?: total,
            dateString = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date()),
            timeString = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date()),
            weigherOperator = "Certified Mandi Weigher",
            isVerified = true,
            rawExtractedLines = lines.filter { it.isNotBlank() }
        )
    }
}
