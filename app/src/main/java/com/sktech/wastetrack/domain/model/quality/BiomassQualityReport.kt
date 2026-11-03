package com.sktech.wastetrack.domain.model.quality

enum class QualityGrade(val label: String, val description: String) {
    GRADE_A_PREMIUM("Grade A (Premium)", "Moisture ≤ 12%, GCV ≥ 3800 kcal/kg. Optimal boiler fuel."),
    GRADE_B_STANDARD("Grade B (Standard)", "Moisture 12.1% - 14%, GCV ≥ 3400 kcal/kg. Minor moisture adjustment."),
    GRADE_C_PENALTY("Grade C (Moisture Penalty)", "Moisture 14.1% - 16%, 5% weight deduction applied to escrow."),
    REJECTED("Rejected", "Moisture > 16% or Ash > 8%. Unsuitable for thermal boilers.")
}

data class BiomassQualityReport(
    val id: String,
    val certificateNumber: String,
    val orderId: String,
    val lotNumber: String,
    val commodity: String,
    val supplierFpo: String,
    val vehicleNumber: String,
    val grossCalorificValueKcal: Double,
    val moisturePercentage: Double,
    val ashPercentage: Double,
    val volatileMatterPercentage: Double,
    val fixedCarbonPercentage: Double,
    val grade: QualityGrade,
    val moisturePenaltyDeductionInr: Double,
    val netEscrowReleaseAmount: Double,
    val testedByTechnician: String,
    val testingLab: String,
    val dateMillis: Long,
    val labStandardReference: String,
    val isEscrowApproved: Boolean,
    val digitalSignatureHash: String
)

