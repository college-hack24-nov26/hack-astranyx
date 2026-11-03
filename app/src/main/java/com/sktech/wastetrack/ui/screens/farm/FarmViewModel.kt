package com.sktech.wastetrack.ui.screens.farm

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.Farm
import com.sktech.wastetrack.domain.model.SoilType
import com.sktech.wastetrack.domain.model.WaterSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

data class FarmUiState(
    val selectedFilter: String = "All",
    val isAddDialogOpen: Boolean = false,
    val farms: List<Farm> = listOf(
        Farm(
            id = "farm_1",
            name = "Sinnar Shivar Plot A",
            surveyNumber = "Gat No. 142/2A",
            plotAreaAcres = 6.5,
            soilType = SoilType.BLACK_COTTON,
            waterSource = WaterSource.DRIP_IRRIGATION,
            village = "Sinnar Agro Hub",
            taluka = "Sinnar",
            district = "Nashik",
            latitude = 19.8512,
            longitude = 73.9214,
            activeCropName = "Cotton (Bt-II Gold)",
            projectedResidueMt = 22.4
        ),
        Farm(
            id = "farm_2",
            name = "Dindori Vineyard & Mixed Orchard",
            surveyNumber = "Survey 89/1B",
            plotAreaAcres = 4.2,
            soilType = SoilType.RED_LOAMY,
            waterSource = WaterSource.CANAL_IRRIGATION,
            village = "Dindori Valley",
            taluka = "Dindori",
            district = "Nashik",
            latitude = 20.2014,
            longitude = 73.8341,
            activeCropName = "Table Grapes (Thompson)",
            projectedResidueMt = 8.5
        ),
        Farm(
            id = "farm_3",
            name = "Niphad Sugarcane Estate",
            surveyNumber = "Gat No. 312",
            plotAreaAcres = 12.0,
            soilType = SoilType.ALLUVIAL,
            waterSource = WaterSource.BOREWELL,
            village = "Niphad Basin",
            taluka = "Niphad",
            district = "Nashik",
            latitude = 20.0821,
            longitude = 74.1082,
            activeCropName = "Sugarcane (Co-86032)",
            projectedResidueMt = 48.0
        ),
        Farm(
            id = "farm_4",
            name = "Malegaon Pearl Millet Patch",
            surveyNumber = "Gat No. 74/4",
            plotAreaAcres = 5.0,
            soilType = SoilType.SANDY_LOAM,
            waterSource = WaterSource.RAIN_FED,
            village = "Sayane",
            taluka = "Malegaon",
            district = "Nashik",
            latitude = 20.5511,
            longitude = 74.5298,
            activeCropName = "Bajra / Pearl Millet",
            projectedResidueMt = 11.2
        )
    )
)

@HiltViewModel
class FarmViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FarmUiState())
    val uiState: StateFlow<FarmUiState> = _uiState.asStateFlow()

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true) }
    }

    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false) }
    }

    fun addFarm(
        name: String,
        surveyNumber: String,
        acres: Double,
        soilType: SoilType,
        waterSource: WaterSource,
        village: String,
        activeCropName: String,
        projectedResidueMt: Double
    ) {
        val newFarm = Farm(
            id = "farm_${UUID.randomUUID().toString().take(8)}",
            name = name.ifBlank { "Farmland Plot" },
            surveyNumber = surveyNumber.ifBlank { "Gat 00/0" },
            plotAreaAcres = if (acres > 0) acres else 1.0,
            soilType = soilType,
            waterSource = waterSource,
            village = village.ifBlank { "Nashik Region" },
            activeCropName = activeCropName.ifBlank { "Mixed Crop" },
            projectedResidueMt = projectedResidueMt
        )
        _uiState.update { current ->
            current.copy(
                farms = listOf(newFarm) + current.farms,
                isAddDialogOpen = false
            )
        }
    }

    fun deleteFarm(farmId: String) {
        _uiState.update { current ->
            current.copy(
                farms = current.farms.filterNot { it.id == farmId }
            )
        }
    }
}

