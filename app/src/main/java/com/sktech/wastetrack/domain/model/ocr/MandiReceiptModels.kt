package com.sktech.wastetrack.domain.model.ocr

data class MandiReceiptSlip(
    val id: String,
    val rstNumber: String,
    val slipType: String,
    val facilityName: String,
    val vehicleNumber: String,
    val commodity: String,
    val grossWeightKg: Double,
    val tareWeightKg: Double,
    val netWeightKg: Double,
    val netWeightMt: Double,
    val ratePerMtInr: Double,
    val totalAmountInr: Double,
    val dateString: String,
    val timeString: String,
    val weigherOperator: String,
    val isVerified: Boolean = true,
    val rawExtractedLines: List<String> = emptyList()
)
