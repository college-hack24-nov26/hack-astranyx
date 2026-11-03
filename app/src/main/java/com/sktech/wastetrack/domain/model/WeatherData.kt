package com.sktech.wastetrack.domain.model

data class WeatherData(
    val id: String = "weather_current",
    val locationName: String = "Nashik Agri Hub, Maharashtra",
    val temperatureCelsius: Double = 28.5,
    val humidityPercent: Int = 48,
    val windSpeedKmh: Double = 13.8,
    val windDirection: String = "NW",
    val pressureHpa: Double = 1012.0,
    val precipitationMm: Double = 0.0,
    val weatherDescription: String = "Sunny & Partially Clouded",
    val uvIndex: Double = 6.4,
    val cloudCoverPercent: Int = 20,
    val observedAtMillis: Long = System.currentTimeMillis()
)

data class DailyForecastItem(
    val dayLabel: String,
    val dateText: String,
    val tempMaxCelsius: Int,
    val tempMinCelsius: Int,
    val rainProbabilityPercent: Int,
    val conditionDescription: String,
    val dryingSuitability: String
)

data class BiomassDryingAdvisory(
    val dryingIndex: String = "Optimal (Fast Drying)",
    val moistureLossRatePerDayPercent: Double = 3.6,
    val fieldBurningRiskLevel: String = "HIGH (Burning Prohibited)",
    val residueBalingFeasibility: String = "Highly Suitable",
    val pesticideSprayWindow: String = "Favorable (Wind < 15 km/h)",
    val harvestReadinessWindow: String = "Open Dry Window (Next 5 Days)"
)
