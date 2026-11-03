package com.sktech.wastetrack.ui.screens.farm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditFarmUiState(
    val farmId: String? = null,
    val parcelName: String = "",
    val extractNumber712: String = "",
    val gutNumber: String = "",
    val surveyNumber: String = "",
    val plotAreaAcresText: String = "4.0",
    val selectedSoilType: String = "Black Cotton (Regur)",
    val selectedWaterSource: String = "Drip Irrigation",
    val village: String = "Sinnar Rural Hub",
    val taluka: String = "Sinnar",
    val district: String = "Nashik",
    val activeCropName: String = "Cotton",
    val projectedResidueMtText: String = "14.0",
    val hasSoilTest: Boolean = true,
    val soilPhText: String = "6.8",
    val nitrogenText: String = "240",
    val phosphorusText: String = "18",
    val potassiumText: String = "310",
    val organicCarbonText: String = "0.65",
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddEditFarmViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editFarmId: String? = savedStateHandle["farmId"]

    private val _uiState = MutableStateFlow(
        AddEditFarmUiState(
            farmId = if (editFarmId == "new") null else editFarmId,
            extractNumber712 = if (editFarmId == "new" || editFarmId == null) "7/12-NSK-${(1000..9999).random()}" else "7/12-NSK-4492"
        )
    )
    val uiState: StateFlow<AddEditFarmUiState> = _uiState.asStateFlow()

    fun onParcelNameChange(name: String) = _uiState.update { it.copy(parcelName = name, errorMessage = null) }
    fun onExtractNumberChange(num: String) = _uiState.update { it.copy(extractNumber712 = num) }
    fun onGutNumberChange(gut: String) = _uiState.update { it.copy(gutNumber = gut) }
    fun onSurveyNumberChange(survey: String) = _uiState.update { it.copy(surveyNumber = survey) }
    fun onAreaChange(area: String) {
        val acres = area.toDoubleOrNull() ?: 1.0
        val estResidue = acres * 3.5
        _uiState.update {
            it.copy(
                plotAreaAcresText = area,
                projectedResidueMtText = String.format(java.util.Locale.US, "%.1f", estResidue),
                errorMessage = null
            )
        }
    }
    fun onSoilTypeChange(soil: String) = _uiState.update { it.copy(selectedSoilType = soil) }
    fun onWaterSourceChange(water: String) = _uiState.update { it.copy(selectedWaterSource = water) }
    fun onVillageChange(v: String) = _uiState.update { it.copy(village = v) }
    fun onTalukaChange(t: String) = _uiState.update { it.copy(taluka = t) }
    fun onDistrictChange(d: String) = _uiState.update { it.copy(district = d) }
    fun onActiveCropChange(crop: String) = _uiState.update { it.copy(activeCropName = crop) }

    fun toggleSoilTest(enabled: Boolean) = _uiState.update { it.copy(hasSoilTest = enabled) }
    fun onSoilPhChange(ph: String) = _uiState.update { it.copy(soilPhText = ph) }
    fun onNitrogenChange(n: String) = _uiState.update { it.copy(nitrogenText = n) }
    fun onPhosphorusChange(p: String) = _uiState.update { it.copy(phosphorusText = p) }
    fun onPotassiumChange(k: String) = _uiState.update { it.copy(potassiumText = k) }
    fun onOrganicCarbonChange(oc: String) = _uiState.update { it.copy(organicCarbonText = oc) }

    fun saveParcel(onSuccess: (String) -> Unit) {
        val s = _uiState.value
        if (s.parcelName.trim().isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please enter a farm parcel name") }
            return
        }
        val acres = s.plotAreaAcresText.toDoubleOrNull()
        if (acres == null || acres <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter valid acreage") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            kotlinx.coroutines.delay(650)
            val parcelId = s.farmId ?: "plot_1"
            _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
            onSuccess(parcelId)
        }
    }
}

