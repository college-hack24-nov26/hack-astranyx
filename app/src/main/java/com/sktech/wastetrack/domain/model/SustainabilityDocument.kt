package com.sktech.wastetrack.domain.model

data class SustainabilityDocument(
    val documentId: String,
    val orderId: String,
    val certificateNumber: String,
    val cropType: String,
    val cropCategory: String,
    val quantityKg: Double,
    val co2AvoidedKg: Double,
    val pm25AvoidedKg: Double,
    val dieselReplacedLiters: Double,
    val cleanEnergyMwh: Double = 0.0,
    val farmerName: String,
    val buyerName: String,
    val farmLocation: String,
    val destinationFacility: String,
    val diversionPurpose: String,
    val methodology: String = "ISO 14064-2 & GHG Protocol Agricultural Residue Baseline",
    val generatedAt: Long = System.currentTimeMillis(),
    val status: String = "VERIFIED",
    val digitalHash: String = "0x8E2A7B1C4D9F0E31",
    val issuingAuthority: String = "Maharashtra Pollution Control Board (MPCB)",
    val form10ManifestNumber: String = "FORM10-MH-2026-8812"
) {
    val co2AvoidedTonnes: Double get() = co2AvoidedKg / 1000.0
    val quantityTonnes: Double get() = quantityKg / 1000.0

    companion object {
        const val CO2_EMISSION_FACTOR = 1.46
        const val PM25_EMISSION_FACTOR = 0.003
        const val DIESEL_EQUIVALENT_FACTOR = 0.55
        const val ENERGY_MWH_FACTOR = 0.0034
    }
}
