package com.sktech.wastetrack.ui.screens.weather

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.weather.model.AgroClimaticBaseline
import com.sktech.wastetrack.domain.weather.model.CropSuitabilityEvaluation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CropSuitabilityUiState(
    val selectedHub: String = "Sinnar Agro Hub",
    val selectedSeason: String = "ALL",
    val showNpkRetentionMode: Boolean = false,
    val baseline: AgroClimaticBaseline = AgroClimaticBaseline(
        hubName = "Sinnar Agro Hub",
        latitude = 19.8512,
        longitude = 73.9982,
        soilType = "Black Cotton Clay Loam",
        soilPh = 7.4,
        organicCarbonPercent = 0.68,
        humidityPercent = 46,
        optimalBalingWindow = "11:00 AM to 4:30 PM (Dew moisture < 14% RH)"
    ),
    val evaluations: List<CropSuitabilityEvaluation> = emptyList(),
    val filteredEvaluations: List<CropSuitabilityEvaluation> = emptyList()
)

@HiltViewModel
class CropSuitabilityViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CropSuitabilityUiState())
    val uiState: StateFlow<CropSuitabilityUiState> = _uiState.asStateFlow()

    init {
        loadEvaluationsForHub("Sinnar Agro Hub")
    }

    fun selectHub(hubName: String) {
        val newBaseline = when (hubName) {
            "Niphad Bio-Energy Corridor" -> AgroClimaticBaseline(
                hubName = hubName,
                latitude = 20.0784,
                longitude = 74.1102,
                soilType = "Alluvial Deep River Basin",
                soilPh = 7.6,
                organicCarbonPercent = 0.74,
                humidityPercent = 52,
                optimalBalingWindow = "11:30 AM to 5:00 PM (Low morning mist window)"
            )
            "Igatpuri Agro-Climatic Belt" -> AgroClimaticBaseline(
                hubName = hubName,
                latitude = 19.6978,
                longitude = 73.5624,
                soilType = "Lateritic Clay Loam",
                soilPh = 6.8,
                organicCarbonPercent = 0.82,
                humidityPercent = 58,
                optimalBalingWindow = "12:00 PM to 4:00 PM (High relative humidity recovery)"
            )
            "Dindori Horticultural Zone" -> AgroClimaticBaseline(
                hubName = hubName,
                latitude = 20.2012,
                longitude = 73.8341,
                soilType = "Red Gravelly Sandy Loam",
                soilPh = 7.1,
                organicCarbonPercent = 0.62,
                humidityPercent = 44,
                optimalBalingWindow = "10:30 AM to 4:30 PM (Rapid solar radiation drying)"
            )
            else -> AgroClimaticBaseline(
                hubName = "Sinnar Agro Hub",
                latitude = 19.8512,
                longitude = 73.9982,
                soilType = "Black Cotton Clay Loam",
                soilPh = 7.4,
                organicCarbonPercent = 0.68,
                humidityPercent = 46,
                optimalBalingWindow = "11:00 AM to 4:30 PM (Dew moisture < 14% RH)"
            )
        }

        _uiState.update { it.copy(selectedHub = hubName, baseline = newBaseline) }
        loadEvaluationsForHub(hubName)
    }

    fun selectSeason(season: String) {
        _uiState.update { it.copy(selectedSeason = season) }
        applyFilters()
    }

    fun toggleNpkRetentionMode() {
        _uiState.update { it.copy(showNpkRetentionMode = !it.showNpkRetentionMode) }
    }

    private fun loadEvaluationsForHub(hubName: String) {
        val initialList = listOf(
            CropSuitabilityEvaluation(
                cropName = "Cotton (Bt Hybrid)",
                categoryName = "Fiber & Crop Stubble",
                suitabilityScore = 94,
                season = "Kharif",
                waterNeed = "Moderate",
                expectedYieldMtPerAcre = 1.4,
                estimatedResidueMtPerAcre = 3.2,
                estimatedResidueRevenueInr = 9120.0,
                soilNitrogenRetentionKg = 24.5,
                soilPhosphorusRetentionKg = 6.2,
                soilPotassiumRetentionKg = 32.8,
                soilPhRange = "6.5 - 8.0",
                rationale = "Deep taproot system thrives in black soil. Stalk residue delivers high lignin content ideal for bio-pelleting and co-firing tariffs.",
                dryingWindowHours = "4 to 5 hours direct sun"
            ),
            CropSuitabilityEvaluation(
                cropName = "Sugarcane (Co 86032)",
                categoryName = "Cash Crop Residue",
                suitabilityScore = 91,
                season = "Annual / Adsali",
                waterNeed = "High (Drip Irrigation)",
                expectedYieldMtPerAcre = 42.0,
                estimatedResidueMtPerAcre = 6.5,
                estimatedResidueRevenueInr = 14950.0,
                soilNitrogenRetentionKg = 35.0,
                soilPhosphorusRetentionKg = 12.4,
                soilPotassiumRetentionKg = 48.0,
                soilPhRange = "6.8 - 7.8",
                rationale = "High biomass dry matter output. Trash mulching prevents evaporation, while bagasse yields premium boiler feed contracts.",
                dryingWindowHours = "2 to 3 days field wilting"
            ),
            CropSuitabilityEvaluation(
                cropName = "Soybean (JS 335)",
                categoryName = "Legume Residue",
                suitabilityScore = 88,
                season = "Kharif",
                waterNeed = "Low to Moderate",
                expectedYieldMtPerAcre = 1.1,
                estimatedResidueMtPerAcre = 2.1,
                estimatedResidueRevenueInr = 5880.0,
                soilNitrogenRetentionKg = 38.2,
                soilPhosphorusRetentionKg = 8.1,
                soilPotassiumRetentionKg = 22.0,
                soilPhRange = "6.0 - 7.5",
                rationale = "Rhizobium root nodules fix atmospheric nitrogen. Fine pod chaff makes excellent microbial starter substrate.",
                dryingWindowHours = "3 to 4 hours post-combine"
            ),
            CropSuitabilityEvaluation(
                cropName = "Paddy Straw (Basmati / Indrayani)",
                categoryName = "Cereal Biomass",
                suitabilityScore = 85,
                season = "Kharif",
                waterNeed = "High",
                expectedYieldMtPerAcre = 2.2,
                estimatedResidueMtPerAcre = 3.8,
                estimatedResidueRevenueInr = 11400.0,
                soilNitrogenRetentionKg = 28.0,
                soilPhosphorusRetentionKg = 5.6,
                soilPotassiumRetentionKg = 42.0,
                soilPhRange = "6.5 - 7.5",
                rationale = "High silica content prevents soil compaction when pyrolyzed to biochar. 80 MT co-firing demand in Igatpuri plants.",
                dryingWindowHours = "6 to 8 hours (Wait for morning dew clearance)"
            ),
            CropSuitabilityEvaluation(
                cropName = "Wheat Stubble (Sharbati)",
                categoryName = "Rabi Cereal",
                suitabilityScore = 82,
                season = "Rabi",
                waterNeed = "Moderate",
                expectedYieldMtPerAcre = 1.8,
                estimatedResidueMtPerAcre = 2.8,
                estimatedResidueRevenueInr = 6720.0,
                soilNitrogenRetentionKg = 22.4,
                soilPhosphorusRetentionKg = 7.0,
                soilPotassiumRetentionKg = 35.5,
                soilPhRange = "6.5 - 8.2",
                rationale = "Optimum Rabi successor to Cotton/Soybean. Straw chopped with Happy Seeder restores organic matter without delay.",
                dryingWindowHours = "3 hours morning sunlight"
            ),
            CropSuitabilityEvaluation(
                cropName = "Mustard (Pusa Bold)",
                categoryName = "Oilseed Chaff",
                suitabilityScore = 79,
                season = "Rabi",
                waterNeed = "Low",
                expectedYieldMtPerAcre = 0.9,
                estimatedResidueMtPerAcre = 1.8,
                estimatedResidueRevenueInr = 6120.0,
                soilNitrogenRetentionKg = 18.0,
                soilPhosphorusRetentionKg = 4.2,
                soilPotassiumRetentionKg = 20.4,
                soilPhRange = "6.0 - 7.8",
                rationale = "High calorific value stalk briquettes. Sulphur-rich residue acts as a natural soil bio-fumigant when mulched.",
                dryingWindowHours = "2 to 3 hours dry afternoon"
            )
        )

        _uiState.update { current ->
            current.copy(evaluations = initialList)
        }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        val season = current.selectedSeason

        val filtered = current.evaluations.filter { evaluation ->
            season == "ALL" || evaluation.season.contains(season, ignoreCase = true)
        }.sortedByDescending { it.suitabilityScore }

        _uiState.update { it.copy(filteredEvaluations = filtered) }
    }
}

