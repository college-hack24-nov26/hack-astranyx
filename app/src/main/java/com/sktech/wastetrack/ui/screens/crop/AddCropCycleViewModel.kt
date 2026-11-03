package com.sktech.wastetrack.ui.screens.crop

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.CropCycle
import com.sktech.wastetrack.domain.model.CropCycleStatus
import com.sktech.wastetrack.domain.model.Farm
import com.sktech.wastetrack.domain.model.GrowthStage
import com.sktech.wastetrack.domain.model.SoilType
import com.sktech.wastetrack.domain.model.WaterSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddCropCycleUiState(
    val availableFarms: List<Farm> = listOf(
        Farm("plot_1", "current_farmer", "Patil Agro Bio-Parcel A", "Survey 44/2", 4.5, SoilType.BLACK_COTTON, WaterSource.DRIP_IRRIGATION, "Sinnar", "Sinnar", "Nashik", 19.8512, 73.9214, "Cotton", 15.5),
        Farm("plot_2", "current_farmer", "Godavari Sugarcane Plot", "Survey 56/1", 8.0, SoilType.ALLUVIAL, WaterSource.CANAL_IRRIGATION, "Niphad", "Niphad", "Nashik", 20.0782, 74.1124, "Sugarcane", 42.0),
        Farm("plot_3", "current_farmer", "Dindori Soybean Holding", "Survey 23/4", 3.2, SoilType.RED_LOAMY, WaterSource.RAIN_FED, "Dindori", "Dindori", "Nashik", 20.2104, 73.8341, "Soybean", 9.8)
    ),
    val selectedFarmId: String = "plot_1",
    val cropName: String = "Cotton",
    val variety: String = "Bt-II Gold Hybrid",
    val category: String = "Fibre Crops",
    val plotAreaAcresText: String = "4.5",
    val sowingDateMillis: Long = System.currentTimeMillis(),
    val expectedHarvestDateMillis: Long = System.currentTimeMillis() + 120L * 24 * 60 * 60 * 1000,
    val selectedDurationDays: Int = 120,
    val initialGrowthStage: GrowthStage = GrowthStage.SOWING,
    val notes: String = "Drip fertigation line verified. Ready for seed sowing.",
    val projectedResidueMtText: String = "15.5",
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddCropCycleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val navFarmId: String? = savedStateHandle["farmId"]

    private val _uiState = MutableStateFlow(
        AddCropCycleUiState(
            selectedFarmId = if (!navFarmId.isNullOrBlank() && navFarmId != "new") navFarmId else "plot_1"
        )
    )
    val uiState: StateFlow<AddCropCycleUiState> = _uiState.asStateFlow()

    init {
        val initialFarm = _uiState.value.availableFarms.find { it.id == _uiState.value.selectedFarmId }
        if (initialFarm != null) {
            _uiState.update {
                it.copy(
                    plotAreaAcresText = initialFarm.plotAreaAcres.toString(),
                    projectedResidueMtText = calculateResidue(initialFarm.plotAreaAcres, it.cropName)
                )
            }
        }
    }

    private fun calculateResidue(acres: Double, crop: String): String {
        val factor = when (crop.lowercase()) {
            "sugarcane" -> 5.2
            "cotton" -> 3.5
            "soybean" -> 3.0
            "wheat" -> 3.2
            "rice", "paddy" -> 3.4
            else -> 3.0
        }
        return String.format(java.util.Locale.US, "%.1f", acres * factor)
    }

    fun onFarmSelected(farmId: String) {
        val farm = _uiState.value.availableFarms.find { it.id == farmId }
        val acres = farm?.plotAreaAcres ?: 4.0
        _uiState.update {
            it.copy(
                selectedFarmId = farmId,
                plotAreaAcresText = acres.toString(),
                projectedResidueMtText = calculateResidue(acres, it.cropName),
                errorMessage = null
            )
        }
    }

    fun onCropNameChange(crop: String) {
        val defaultVariety = when (crop.lowercase()) {
            "cotton" -> "Bt-II Gold Hybrid"
            "sugarcane" -> "Co-86032 (Nira)"
            "soybean" -> "JS-335 Supreme"
            "wheat" -> "Lokwan Golden"
            "rice", "paddy" -> "Indrayani Scented"
            "tomato" -> "Abhinav F1"
            "onion" -> "Fursungi Red"
            else -> "High-Yield Hybrid"
        }
        val defaultCategory = when (crop.lowercase()) {
            "cotton" -> "Fibre Crops"
            "sugarcane" -> "Cash Crops"
            "soybean" -> "Oilseeds"
            "wheat", "rice", "paddy" -> "Cereal Crops"
            else -> "Vegetable / Horticulture"
        }
        val acres = _uiState.value.plotAreaAcresText.toDoubleOrNull() ?: 4.0
        _uiState.update {
            it.copy(
                cropName = crop,
                variety = defaultVariety,
                category = defaultCategory,
                projectedResidueMtText = calculateResidue(acres, crop),
                errorMessage = null
            )
        }
    }

    fun onVarietyChange(v: String) = _uiState.update { it.copy(variety = v) }

    fun onAreaChange(area: String) {
        val acres = area.toDoubleOrNull() ?: 1.0
        _uiState.update {
            it.copy(
                plotAreaAcresText = area,
                projectedResidueMtText = calculateResidue(acres, it.cropName),
                errorMessage = null
            )
        }
    }

    fun onDurationPresetChange(days: Int) {
        val harvestMillis = _uiState.value.sowingDateMillis + days.toLong() * 24 * 60 * 60 * 1000
        _uiState.update {
            it.copy(
                selectedDurationDays = days,
                expectedHarvestDateMillis = harvestMillis
            )
        }
    }

    fun onGrowthStageChange(stage: GrowthStage) = _uiState.update { it.copy(initialGrowthStage = stage) }

    fun onNotesChange(n: String) = _uiState.update { it.copy(notes = n) }

    fun saveCropCycle(onSuccess: (CropCycle) -> Unit) {
        val s = _uiState.value
        val acres = s.plotAreaAcresText.toDoubleOrNull()
        if (acres == null || acres <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter valid plot acreage") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            kotlinx.coroutines.delay(650)
            val selectedFarm = s.availableFarms.find { it.id == s.selectedFarmId }
            val newCrop = CropCycle(
                id = "crop_" + UUID.randomUUID().toString().take(6),
                cropName = s.cropName,
                variety = s.variety.ifBlank { "Standard Hybrid" },
                category = s.category,
                plotAreaAcres = acres,
                sowingDateMillis = s.sowingDateMillis,
                expectedHarvestMillis = s.expectedHarvestDateMillis,
                growthStage = s.initialGrowthStage,
                status = CropCycleStatus.ACTIVE,
                village = selectedFarm?.village ?: "Sinnar Rural",
                projectedResidueMt = s.projectedResidueMtText.toDoubleOrNull() ?: (acres * 3.5),
                healthScorePercent = 94,
                notes = s.notes
            )
            _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
            onSuccess(newCrop)
        }
    }
}

