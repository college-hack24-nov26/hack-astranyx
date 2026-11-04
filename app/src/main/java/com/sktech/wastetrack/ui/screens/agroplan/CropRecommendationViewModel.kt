package com.sktech.wastetrack.ui.screens.agroplan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.agro.DynamicCropAdvisoryEngine
import com.sktech.wastetrack.domain.agro.model.CropSuitabilitySummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class CropRecommendationUiState(
    val farmId: String = "default_farm",
    val farmName: String = "Sinnar Agro Parcel 01",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val recommendations: List<CropSuitabilitySummary> = emptyList(),
    val selectedCropsForComparison: Set<String> = emptySet(),
    val isComparisonSheetOpen: Boolean = false,
    val comparisonList: List<CropSuitabilitySummary> = emptyList(),
    val dataFreshnessNotice: String = "Live Agmarknet & IMD Weather Radar",
    val errorMessage: String? = null
)

@HiltViewModel
class CropRecommendationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val farmIdParam: String = savedStateHandle.get<String>("farmId")?.ifBlank { "default_farm" } ?: "default_farm"

    private val _state = MutableStateFlow(CropRecommendationUiState(farmId = farmIdParam))
    val state: StateFlow<CropRecommendationUiState> = _state.asStateFlow()

    init {
        loadData(farmIdParam)
    }

    fun loadData(farmId: String) {
        val recs = DynamicCropAdvisoryEngine.evaluateCompositeSuitability(
            farm = null,
            soilProfile = null,
            weather = null,
            previousCrops = listOf("Cotton (Bt Hybrid)")
        )
        _state.update {
            it.copy(
                farmId = farmId,
                farmName = if (farmId == "default_farm") "Sinnar Agro Parcel 01" else "Farm Plot $farmId",
                isLoading = false,
                isRefreshing = false,
                recommendations = recs
            )
        }
    }

    fun refresh() {
        _state.update { it.copy(isRefreshing = true) }
        loadData(_state.value.farmId)
    }

    fun toggleCropComparison(cropName: String) {
        val currentSelected = _state.value.selectedCropsForComparison.toMutableSet()
        if (currentSelected.contains(cropName)) {
            currentSelected.remove(cropName)
        } else {
            if (currentSelected.size < 4) {
                currentSelected.add(cropName)
            }
        }
        val comparisonList = _state.value.recommendations.filter { currentSelected.contains(it.cropName) }
        _state.update {
            it.copy(
                selectedCropsForComparison = currentSelected,
                comparisonList = comparisonList
            )
        }
    }

    fun openComparisonSheet() {
        if (_state.value.selectedCropsForComparison.isNotEmpty()) {
            _state.update { it.copy(isComparisonSheetOpen = true) }
        }
    }

    fun closeComparisonSheet() {
        _state.update { it.copy(isComparisonSheetOpen = false) }
    }
}
