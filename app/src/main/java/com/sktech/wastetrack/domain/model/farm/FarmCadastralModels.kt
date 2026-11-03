package com.sktech.wastetrack.domain.model.farm

data class SoilHealthProfile(
    val id: String,
    val farmId: String,
    val testDateFormatted: String,
    val ph: Double,
    val phCategory: String,
    val nitrogenKgHa: Double,
    val nitrogenStatus: String,
    val phosphorusKgHa: Double,
    val phosphorusStatus: String,
    val potassiumKgHa: Double,
    val potassiumStatus: String,
    val organicCarbonPct: Double,
    val electricalConductivityDsM: Double,
    val texture: String,
    val soilHealthCardNo: String,
    val recommendations: List<String> = emptyList()
)

data class CadastralParcelDetail(
    val id: String,
    val parcelName: String,
    val extractNumber712: String,
    val gutNumber: String,
    val surveyNumber: String,
    val village: String,
    val taluka: String,
    val district: String,
    val totalAreaAcres: Double,
    val totalAreaGunthas: Double,
    val latitude: Double,
    val longitude: Double,
    val boundariesDesc: String,
    val soilTypeLabel: String,
    val waterSourceLabel: String,
    val activeCropName: String,
    val projectedBiomassResidueMt: Double,
    val soilProfile: SoilHealthProfile
)

