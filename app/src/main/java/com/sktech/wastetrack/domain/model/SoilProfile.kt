package com.sktech.wastetrack.domain.model

data class SoilProfile(
    val id: String = "soil_profile_default",
    val farmId: String = "default_farm",
    val testDate: Long = System.currentTimeMillis(),
    val ph: Double? = 7.2,
    val nitrogen: Double? = 220.0,
    val phosphorus: Double? = 18.0,
    val potassium: Double? = 150.0,
    val organicCarbon: Double? = 0.65,
    val texture: String? = "Medium Clay Loam",
    val notes: String? = null
)
