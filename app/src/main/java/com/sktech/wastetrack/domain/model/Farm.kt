package com.sktech.wastetrack.domain.model

data class Farm(
    val id: String,
    val ownerId: String = "current_farmer",
    val name: String,
    val surveyNumber: String,
    val plotAreaAcres: Double,
    val soilType: SoilType = SoilType.BLACK_COTTON,
    val waterSource: WaterSource = WaterSource.BOREWELL,
    val village: String = "Sinnar",
    val taluka: String = "Sinnar",
    val district: String = "Nashik",
    val latitude: Double = 19.8512,
    val longitude: Double = 73.9214,
    val activeCropName: String = "Cotton",
    val projectedResidueMt: Double = 14.5
)

enum class SoilType(val label: String) {
    BLACK_COTTON("Black Cotton (Regur)"),
    ALLUVIAL("Alluvial Loam"),
    RED_LOAMY("Red & Yellow"),
    CLAYEY("Clayey Loam"),
    SANDY_LOAM("Sandy Loam"),
    LATERITE("Laterite")
}

enum class WaterSource(val label: String) {
    BOREWELL("Well / Borewell"),
    CANAL_IRRIGATION("Canal Irrigation"),
    DRIP_IRRIGATION("Drip Irrigation"),
    RAIN_FED("Rain-fed Only"),
    RIVER_STREAM("River / Stream")
}

