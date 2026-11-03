package com.sktech.wastetrack.domain.model.analytics

data class MandiRate(
    val cropName: String,
    val marketName: String,
    val minRateQuintal: Double,
    val modalRateQuintal: Double,
    val maxRateQuintal: Double,
    val stubbleResidueValuePerMt: Double,
    val trendPercent: Double,
    val isRising: Boolean,
    val lastArrivalsMt: Double
) {
    val ratePerMt: Double
        get() = modalRateQuintal * 10.0
}

data class PriceHistoryPoint(
    val dateLabel: String,
    val ratePerQuintal: Double
)

data class FarmExpenseItem(
    val id: String,
    val category: String,
    val description: String,
    val amountInr: Double,
    val dateText: String
)

data class FarmPnLLedger(
    val cropCycleName: String = "Kharif Wheat & Stubble Parcel #2",
    val acreage: Double = 5.0,
    val primaryHarvestRevenue: Double = 185000.0,
    val stubbleResidueRevenue: Double = 42000.0,
    val expenses: List<FarmExpenseItem> = listOf(
        FarmExpenseItem("exp_01", "Fertilizer & Soil NPK", "Urea (3 Bags) & DAP N-P-K Blend", 12400.0, "15 Oct 2026"),
        FarmExpenseItem("exp_02", "High-Yield Certified Seed", "Certified Lokwan Seed Stock (40 kg)", 6800.0, "18 Oct 2026"),
        FarmExpenseItem("exp_03", "Diesel Tractor Tillage", "Rotavator & Deep Ploughing (35 L Diesel)", 9200.0, "20 Oct 2026"),
        FarmExpenseItem("exp_04", "Labour & Super Seeder", "Combine Harvester Stubble Slicing", 14500.0, "28 Oct 2026"),
        FarmExpenseItem("exp_05", "Drip Irrigation Power", "Submersible Motor Electricity & Filter", 3800.0, "02 Nov 2026")
    )
) {
    val totalRevenue: Double
        get() = primaryHarvestRevenue + stubbleResidueRevenue

    val totalCost: Double
        get() = expenses.sumOf { it.amountInr }

    val netProfit: Double
        get() = totalRevenue - totalCost

    val profitMarginPercent: Double
        get() = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

    val stubbleRevenueSharePercent: Double
        get() = if (totalRevenue > 0) (stubbleResidueRevenue / totalRevenue) * 100 else 0.0
}

