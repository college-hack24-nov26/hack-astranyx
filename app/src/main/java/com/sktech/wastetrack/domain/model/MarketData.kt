package com.sktech.wastetrack.domain.model

data class MarketData(
    val id: String,
    val cropType: String,
    val cropCategory: String = "GRAIN",
    val market: String,
    val pricePerUnit: Double,
    val unit: String = "quintal",
    val currency: String = "INR",
    val observedAt: Long = System.currentTimeMillis(),
    val source: String? = null,
    val trend: PriceTrend = PriceTrend.STABLE,
    val changePercent: Double? = null,
    val freshness: String = "LIVE"
)

enum class PriceTrend(val displayName: String, val icon: String) {
    RISING("Rising", "+"),
    FALLING("Falling", "-"),
    STABLE("Stable", "="),
    VOLATILE("Volatile", "~")
}

data class MandiComparisonItem(
    val mandiName: String,
    val district: String,
    val pricePerUnit: Double,
    val unit: String = "quintal",
    val trend: PriceTrend = PriceTrend.STABLE,
    val distanceKm: Double = 0.0,
    val state: String = "Maharashtra",
    val arrivalDate: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val variety: String? = null,
    val isLive: Boolean = true
)

data class MonthlyPricePoint(
    val monthLabel: String,
    val spotRate: Double,
    val mspRate: Double
)

data class MandiArrivalRadarItem(
    val yardName: String,
    val location: String,
    val dailyArrivalQuintals: Double,
    val modalRateQuintal: Double,
    val capacityUtilizationPercent: Int,
    val trendNote: String
)
