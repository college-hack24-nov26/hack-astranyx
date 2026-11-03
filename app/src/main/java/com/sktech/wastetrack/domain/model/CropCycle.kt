package com.sktech.wastetrack.domain.model

data class CropCycle(
    val id: String,
    val cropName: String,
    val variety: String,
    val category: String,
    val plotAreaAcres: Double,
    val sowingDateMillis: Long,
    val expectedHarvestMillis: Long,
    val growthStage: GrowthStage,
    val status: CropCycleStatus = CropCycleStatus.ACTIVE,
    val village: String = "Nashik Rural",
    val projectedResidueMt: Double,
    val healthScorePercent: Int = 94,
    val notes: String = ""
)

enum class GrowthStage(val displayName: String, val progress: Float, val stageOrder: Int) {
    PLANNING("Planning", 0.10f, 0),
    SOWING("Sowing", 0.25f, 1),
    GERMINATION("Germination", 0.40f, 2),
    VEGETATIVE("Vegetative", 0.55f, 3),
    FLOWERING("Flowering", 0.70f, 4),
    FRUITING("Fruiting", 0.85f, 5),
    MATURATION("Maturation", 0.95f, 6),
    HARVEST_READY("Harvest Ready", 1.0f, 7),
    HARVESTED("Harvested", 1.0f, 8)
}

enum class CropCycleStatus(val displayName: String) {
    ACTIVE("Active"),
    HARVESTED("Harvested"),
    DAMAGED("Damaged / Lost")
}

data class CropActivity(
    val id: String,
    val cropCycleId: String,
    val activityType: String,
    val description: String,
    val dosageAmount: String,
    val timestampMillis: Long,
    val recordedBy: String = "Self (Farmer)"
)
