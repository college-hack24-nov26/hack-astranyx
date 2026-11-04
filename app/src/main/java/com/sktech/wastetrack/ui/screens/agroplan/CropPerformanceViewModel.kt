package com.sktech.wastetrack.ui.screens.agroplan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.agro.AgroEconomicsEngine
import com.sktech.wastetrack.domain.agro.DynamicCropAdvisoryEngine
import com.sktech.wastetrack.domain.agro.model.CropResiduePlan
import com.sktech.wastetrack.domain.agro.model.CropSuitabilitySummary
import com.sktech.wastetrack.domain.agro.model.IndicativeEconomicsSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CropPerformanceUiState(
    val farmId: String = "default_farm",
    val farmName: String = "Sinnar Agro Parcel 01",
    val selectedCrop: String = "Wheat (Lokwan / Sharbati / GW 322)",
    val availableCrops: List<String> = DynamicCropAdvisoryEngine.getCatalogCropNames(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val suitabilitySummary: CropSuitabilitySummary? = null,
    val economicsSummary: IndicativeEconomicsSummary? = null,
    val residuePlan: CropResiduePlan? = null,
    val isCustomCostDialogOpen: Boolean = false,
    val customSeedCost: Double? = null,
    val customFertilizerCost: Double? = null,
    val customLaborCost: Double? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CropPerformanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rawFarm = savedStateHandle.get<String>("farmId")
    private val farmIdParam: String = if (rawFarm.isNullOrBlank()) "default_farm" else rawFarm
    private val rawCrop = savedStateHandle.get<String>("crop") ?: "Wheat"
    private val initialCropParam: String = runCatching {
        java.net.URLDecoder.decode(rawCrop, "UTF-8")
    }.getOrDefault(rawCrop).ifBlank { "Wheat" }

    private val _state = MutableStateFlow(
        CropPerformanceUiState(
            farmId = farmIdParam,
            selectedCrop = initialCropParam
        )
    )
    val state: StateFlow<CropPerformanceUiState> = _state.asStateFlow()

    init {
        loadCropAssessment(farmIdParam, initialCropParam)
    }

    fun selectCrop(cropName: String) {
        if (cropName == _state.value.selectedCrop) return
        _state.update { it.copy(selectedCrop = cropName, searchQuery = "") }
        loadCropAssessment(_state.value.farmId, cropName)
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun toggleCustomCostDialog(open: Boolean) {
        _state.update { it.copy(isCustomCostDialogOpen = open) }
    }

    fun applyCustomCosts(seedCost: Double?, fertilizerCost: Double?, laborCost: Double?) {
        _state.update {
            it.copy(
                customSeedCost = seedCost,
                customFertilizerCost = fertilizerCost,
                customLaborCost = laborCost,
                isCustomCostDialogOpen = false
            )
        }
        recalculateEconomics()
    }

    private fun loadCropAssessment(farmId: String, cropName: String) {
        val summary = DynamicCropAdvisoryEngine.assessSpecificCrop(
            cropName = cropName,
            farm = null,
            soilProfile = null,
            weather = null,
            previousCrops = listOf("Cotton (Bt Hybrid)")
        )

        val modalPrice = when {
            cropName.contains("Wheat", ignoreCase = true) -> 2450.0
            cropName.contains("Chickpea", ignoreCase = true) || cropName.contains("Gram", ignoreCase = true) -> 5850.0
            cropName.contains("Mustard", ignoreCase = true) -> 5450.0
            cropName.contains("Onion", ignoreCase = true) -> 1850.0
            cropName.contains("Soybean", ignoreCase = true) -> 4890.0
            cropName.contains("Cotton", ignoreCase = true) -> 6920.0
            cropName.contains("Tomato", ignoreCase = true) -> 1600.0
            cropName.contains("Sugarcane", ignoreCase = true) -> 315.0
            cropName.contains("Maize", ignoreCase = true) -> 2200.0
            else -> 2500.0
        }

        val economics = AgroEconomicsEngine.computeIndicativeEconomics(
            cropName = cropName,
            acreage = 2.5,
            modalPricePerQuintal = modalPrice,
            customSeedCost = _state.value.customSeedCost,
            customFertilizerCost = _state.value.customFertilizerCost,
            customLaborCost = _state.value.customLaborCost
        )

        val residue = AgroEconomicsEngine.buildResiduePlan(cropName, 2.5)

        _state.update {
            it.copy(
                selectedCrop = summary.cropName,
                farmName = if (farmId == "default_farm") "Sinnar Agro Parcel 01" else "Farm Plot $farmId",
                suitabilitySummary = summary,
                economicsSummary = economics,
                residuePlan = residue,
                isLoading = false
            )
        }
    }

    private fun recalculateEconomics() {
        val cropName = _state.value.selectedCrop
        val modalPrice = _state.value.economicsSummary?.forecastModalRatePerQuintal ?: 2450.0
        val updated = AgroEconomicsEngine.computeIndicativeEconomics(
            cropName = cropName,
            acreage = 2.5,
            modalPricePerQuintal = modalPrice,
            customSeedCost = _state.value.customSeedCost,
            customFertilizerCost = _state.value.customFertilizerCost,
            customLaborCost = _state.value.customLaborCost
        )
        _state.update { it.copy(economicsSummary = updated) }
    }
}
