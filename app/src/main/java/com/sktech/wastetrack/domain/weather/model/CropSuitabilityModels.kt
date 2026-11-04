package com.sktech.wastetrack.domain.weather.model

data class CropSuitabilityEvaluation(
    val cropName: String,
    val categoryName: String,
    val suitabilityScore: Int,
    val season: String,
    val waterNeed: String,
    val expectedYieldMtPerAcre: Double,
    val estimatedResidueMtPerAcre: Double,
    val estimatedResidueRevenueInr: Double,
    val soilNitrogenRetentionKg: Double,
    val soilPhosphorusRetentionKg: Double,
    val soilPotassiumRetentionKg: Double,
    val soilPhRange: String,
    val rationale: String,
    val dryingWindowHours: String,
    val biocharNpkMultiplier: Double = 2.2
)

data class AgroClimaticBaseline(
    val hubName: String,
    val latitude: Double,
    val longitude: Double,
    val soilType: String,
    val soilPh: Double,
    val organicCarbonPercent: Double,
    val humidityPercent: Int,
    val optimalBalingWindow: String
)

