package com.sktech.wastetrack.domain.agro.model

data class CustomAgroPlan(
    val id: String,
    val farmId: String,
    val farmName: String,
    val cropName: String,
    val cropCategory: String,
    val variety: String? = null,
    val targetSeason: String,
    val planVersion: Int = 1,
    val isLatest: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val suitabilitySummary: CropSuitabilitySummary,
    val economicsSnapshot: IndicativeEconomicsSummary?,
    val irrigationGuidance: IrrigationPlanGuidance,
    val fertilizerGuidance: FertilizerPlanGuidance,
    val careSchedule: List<GrowthStageScheduleStep>,
    val residuePlan: CropResiduePlan,
    val activeAdaptationAlerts: List<WeatherAdaptationNotice> = emptyList(),
    val statutoryDisclaimer: String = STATUTORY_DISCLAIMER_TEXT
) {
    companion object {
        const val STATUTORY_DISCLAIMER_TEXT =
            "Recommendations, suitability ratings, yield predictions, and profit projections are algorithmic estimates and do not guarantee crop success, realized yield, market price, or financial profit. Decisions remain solely with the farmer."
    }
}

data class CropSuitabilitySummary(
    val cropName: String,
    val categoryName: String,
    val compositeScore: Int,
    val weatherScore: Int,
    val climateScore: Int,
    val soilScore: Int,
    val rotationScore: Int,
    val marketScore: Int,
    val primaryRecommendationRationale: String,
    val agronomicAdvantages: List<String>,
    val riskCaveats: List<String>,
    val waterNeed: String,
    val expectedYieldRange: String
)

data class IrrigationPlanGuidance(
    val baseMethod: String,
    val frequencyHours: String,
    val preferredTimeOfDay: String,
    val criticalWaterNeedStages: List<String>,
    val waterSavingTips: List<String>,
    val rainHaltActive: Boolean = false,
    val rainAdjustmentNote: String? = null
)

data class FertilizerPlanGuidance(
    val soilSourceType: String,
    val soilSummaryText: String,
    val basalApplicationDose: String,
    val vegetativeTopDressDose: String,
    val floweringFertigationDose: String,
    val micronutrientPrescription: String?,
    val organicCompostDose: String,
    val previousCropCarryOverCredit: String?,
    val soilTestRecommendationNotice: String?
)

data class GrowthStageScheduleStep(
    val stageOrder: Int,
    val stageName: String,
    val windowDescription: String,
    val approximateDurationDays: Int,
    val keyObjectives: String,
    val actions: List<ScheduledCareAction>,
    val stageWarnings: List<String>
)

data class ScheduledCareAction(
    val actionId: String,
    val title: String,
    val category: String,
    val instruction: String,
    val isCritical: Boolean = false,
    var isCompleted: Boolean = false
)

data class IndicativeEconomicsSummary(
    val currency: String = "INR",
    val acreage: Double = 1.0,
    val expectedYieldQuintals: Double,
    val forecastModalRatePerQuintal: Double,
    val grossCropRevenue: Double,
    val residueBiomassTons: Double,
    val residueValuePerTon: Double,
    val grossResidueRevenue: Double,
    val totalGrossRevenue: Double,
    val seedCost: Double,
    val fertilizerNutrientCost: Double,
    val cropProtectionCost: Double,
    val irrigationElectricityCost: Double,
    val laborHarvestCost: Double,
    val logisticsTransportCost: Double,
    val totalProductionCost: Double,
    val conservativeNetProfit: Double,
    val expectedNetProfit: Double,
    val optimisticNetProfit: Double,
    val costSourceNote: String
)

data class CropResiduePlan(
    val residueName: String,
    val estimatedBiomassTonsPerAcre: Double,
    val targetMoisturePercentMax: Double = 14.0,
    val suggestedIndustrialApplication: String,
    val estimatedResidueValuePerTonInr: Double,
    val inSituManagementAlternative: String
)

data class WeatherAdaptationNotice(
    val noticeId: String,
    val urgencyLevel: String = "HIGH",
    val title: String = "Weather Adaptation Notice",
    val headline: String = "",
    val recommendedFarmerAction: String = "",
    val triggerCondition: String = headline,
    val suggestedAction: String = recommendedFarmerAction,
    val agronomicRationale: String = headline,
    val reviewTime: String = "Review in 48 hours",
    val detectedAt: Long = System.currentTimeMillis()
) {
    val id: String get() = noticeId
}

data class AgroPlanRevisionRecord(
    val revisionId: String,
    val planVersion: Int = 1,
    val revisionTrigger: String = "MANUAL_ADJUSTMENT",
    val changeSummary: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val agroplanId: String = ""
) {
    val versionNumber: Int get() = planVersion
    val createdAt: Long get() = timestamp
}

data class DailyRainForecastItem(
    val dayLabel: String,
    val rainProbabilityPercent: Int,
    val precipitationMm: Double,
    val weatherDescription: String,
    val tempCelsius: Double
)

