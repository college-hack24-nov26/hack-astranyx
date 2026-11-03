package com.sktech.wastetrack.ui.screens.crop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.CropActivity
import com.sktech.wastetrack.domain.model.CropCycle
import com.sktech.wastetrack.domain.model.CropCycleStatus
import com.sktech.wastetrack.domain.model.GrowthStage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CropCycleUiState(
    val selectedFilter: String = "All",
    val crops: List<CropCycle> = listOf(
        CropCycle(
            id = "crop_1",
            cropName = "Cotton",
            variety = "Bt-II Gold Hybrid",
            category = "Fibre Crops",
            plotAreaAcres = 4.5,
            sowingDateMillis = System.currentTimeMillis() - 75L * 24 * 60 * 60 * 1000,
            expectedHarvestMillis = System.currentTimeMillis() + 45L * 24 * 60 * 60 * 1000,
            growthStage = GrowthStage.FLOWERING,
            status = CropCycleStatus.ACTIVE,
            village = "Sinnar Agro Hub",
            projectedResidueMt = 14.8,
            healthScorePercent = 92,
            notes = "Drip irrigation connected. High stalk volume expected."
        ),
        CropCycle(
            id = "crop_2",
            cropName = "Sugarcane",
            variety = "Co-86032 (Nira)",
            category = "Cash Crops",
            plotAreaAcres = 8.0,
            sowingDateMillis = System.currentTimeMillis() - 240L * 24 * 60 * 60 * 1000,
            expectedHarvestMillis = System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000,
            growthStage = GrowthStage.MATURATION,
            status = CropCycleStatus.ACTIVE,
            village = "Niphad Bio-Energy Zone",
            projectedResidueMt = 42.0,
            healthScorePercent = 95,
            notes = "Dense cane canopy. Bagasse contract pending with recycler."
        ),
        CropCycle(
            id = "crop_3",
            cropName = "Soybean",
            variety = "JS-335",
            category = "Oilseeds",
            plotAreaAcres = 3.2,
            sowingDateMillis = System.currentTimeMillis() - 95L * 24 * 60 * 60 * 1000,
            expectedHarvestMillis = System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000,
            growthStage = GrowthStage.HARVEST_READY,
            status = CropCycleStatus.ACTIVE,
            village = "Dindori Agri Cluster",
            projectedResidueMt = 9.6,
            healthScorePercent = 89,
            notes = "Pods dry and ready for threshing. Straw available for feed/biomass."
        ),
        CropCycle(
            id = "crop_4",
            cropName = "Wheat",
            variety = "HD-2967 Golden",
            category = "Cereals",
            plotAreaAcres = 5.0,
            sowingDateMillis = System.currentTimeMillis() - 35L * 24 * 60 * 60 * 1000,
            expectedHarvestMillis = System.currentTimeMillis() + 85L * 24 * 60 * 60 * 1000,
            growthStage = GrowthStage.VEGETATIVE,
            status = CropCycleStatus.ACTIVE,
            village = "Nashik Rural Hub",
            projectedResidueMt = 16.5,
            healthScorePercent = 96,
            notes = "First weeding completed. Second top-dress urea cycle scheduled."
        )
    ),
    val activities: Map<String, List<CropActivity>> = mapOf(
        "crop_1" to listOf(
            CropActivity(UUID.randomUUID().toString(), "crop_1", "Irrigation", "Drip fertigation cycle 4 hrs", "4 hrs", System.currentTimeMillis() - 2L * 24 * 3600 * 1000),
            CropActivity(UUID.randomUUID().toString(), "crop_1", "Fertilizer", "19:19:19 water soluble spray", "5 kg/acre", System.currentTimeMillis() - 8L * 24 * 3600 * 1000),
            CropActivity(UUID.randomUUID().toString(), "crop_1", "Pest Scouting", "Neem oil organic preventive spray", "2 ml/L", System.currentTimeMillis() - 15L * 24 * 3600 * 1000)
        ),
        "crop_2" to listOf(
            CropActivity(UUID.randomUUID().toString(), "crop_2", "Irrigation", "Canal flow watering", "6 hrs", System.currentTimeMillis() - 3L * 24 * 3600 * 1000),
            CropActivity(UUID.randomUUID().toString(), "crop_2", "De-trashing", "Removal of dry leaves", "8 acres", System.currentTimeMillis() - 20L * 24 * 3600 * 1000)
        ),
        "crop_3" to listOf(
            CropActivity(UUID.randomUUID().toString(), "crop_3", "Harvest Readiness", "Pod moisture test: 13%", "13% moisture", System.currentTimeMillis() - 1L * 24 * 3600 * 1000)
        ),
        "crop_4" to listOf(
            CropActivity(UUID.randomUUID().toString(), "crop_4", "Weeding", "Manual inter-row weeding", "5 acres", System.currentTimeMillis() - 5L * 24 * 3600 * 1000),
            CropActivity(UUID.randomUUID().toString(), "crop_4", "Irrigation", "Sprinkler watering", "3 hrs", System.currentTimeMillis() - 10L * 24 * 3600 * 1000)
        )
    ),
    val selectedCycleId: String? = null,
    val showLogSheet: Boolean = false,
    val activityTypeInput: String = "Irrigation",
    val activityDosageInput: String = "",
    val activityNotesInput: String = ""
)

@HiltViewModel
class CropCycleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CropCycleUiState())
    val uiState: StateFlow<CropCycleUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun selectCrop(cycleId: String) {
        _uiState.update { it.copy(selectedCycleId = cycleId) }
    }

    fun advanceGrowthStage(cycleId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                val stages = GrowthStage.entries.toTypedArray()
                val updatedCrops = state.crops.map { crop ->
                    if (crop.id == cycleId) {
                        val currentIndex = crop.growthStage.ordinal
                        val nextStage = if (currentIndex < stages.size - 1) stages[currentIndex + 1] else stages[currentIndex]
                        val nextStatus = if (nextStage == GrowthStage.HARVESTED) CropCycleStatus.HARVESTED else CropCycleStatus.ACTIVE
                        crop.copy(growthStage = nextStage, status = nextStatus)
                    } else {
                        crop
                    }
                }
                state.copy(crops = updatedCrops)
            }
        }
    }

    fun openLogSheet() {
        _uiState.update { it.copy(showLogSheet = true, activityDosageInput = "", activityNotesInput = "") }
    }

    fun closeLogSheet() {
        _uiState.update { it.copy(showLogSheet = false) }
    }

    fun updateActivityType(type: String) {
        _uiState.update { it.copy(activityTypeInput = type) }
    }

    fun updateActivityDosage(dosage: String) {
        _uiState.update { it.copy(activityDosageInput = dosage) }
    }

    fun updateActivityNotes(notes: String) {
        _uiState.update { it.copy(activityNotesInput = notes) }
    }

    fun submitActivity(cycleId: String) {
        val type = _uiState.value.activityTypeInput
        val dosage = _uiState.value.activityDosageInput.ifBlank { "Standard application" }
        val notes = _uiState.value.activityNotesInput.ifBlank { "Logged field activity" }

        val newActivity = CropActivity(
            id = UUID.randomUUID().toString(),
            cropCycleId = cycleId,
            activityType = type,
            description = notes,
            dosageAmount = dosage,
            timestampMillis = System.currentTimeMillis()
        )

        _uiState.update { state ->
            val existingList = state.activities[cycleId] ?: emptyList()
            val updatedMap = state.activities.toMutableMap()
            updatedMap[cycleId] = listOf(newActivity) + existingList
            state.copy(activities = updatedMap, showLogSheet = false)
        }
    }
}
