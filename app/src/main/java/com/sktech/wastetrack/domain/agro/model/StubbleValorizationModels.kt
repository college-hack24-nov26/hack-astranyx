package com.sktech.wastetrack.domain.agro.model

data class ValorizationChannel(
    val id: String,
    val name: String,
    val categoryBadge: String,
    val netRoiPercent: Double,
    val netRevenuePerAcreInr: Double,
    val paybackDays: Int,
    val carbonAbatementTonsCo2: Double,
    val suitabilityScore: Int,
    val targetBuyers: String,
    val processingMethod: String,
    val statutoryIncentive: String,
    val isRecommendedBest: Boolean = false
)

data class StubbleValorizationSummary(
    val farmId: String,
    val farmName: String,
    val cropName: String,
    val totalAcreage: Double,
    val estimatedTonnage: Double,
    val moisturePercent: Double,
    val baseCalorificMjKg: Double,
    val totalGrossPotentialInr: Double,
    val channels: List<ValorizationChannel> = emptyList(),
    val carbonOffsetCertEligible: Boolean = true
)

