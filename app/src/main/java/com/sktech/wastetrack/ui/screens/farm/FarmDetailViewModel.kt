package com.sktech.wastetrack.ui.screens.farm

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.CropCycle
import com.sktech.wastetrack.domain.model.CropCycleStatus
import com.sktech.wastetrack.domain.model.GrowthStage
import com.sktech.wastetrack.domain.model.farm.CadastralParcelDetail
import com.sktech.wastetrack.domain.model.farm.SoilHealthProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FarmDetailUiState(
    val farmId: String = "",
    val parcel: CadastralParcelDetail? = null,
    val cropCycles: List<CropCycle> = emptyList(),
    val isLoading: Boolean = false,
    val isExportingPass: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FarmDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val farmId: String = savedStateHandle["farmId"] ?: "plot_1"

    private val sampleParcels = listOf(
        CadastralParcelDetail(
            id = "plot_1",
            parcelName = "Patil Agro Bio-Parcel A",
            extractNumber712 = "7/12-NSK-4492",
            gutNumber = "Gut No. 112/A",
            surveyNumber = "Survey 44/2",
            village = "Sinnar Rural Hub",
            taluka = "Sinnar",
            district = "Nashik",
            totalAreaAcres = 4.5,
            totalAreaGunthas = 180.0,
            latitude = 19.8512,
            longitude = 73.9214,
            boundariesDesc = "North: PWD Canal Road | South: Survey 44/3 | East: Deshmukh Mango Grove | West: Natural Nala Drain",
            soilTypeLabel = "Black Cotton (Regur)",
            waterSourceLabel = "Drip Irrigation & Borewell",
            activeCropName = "Cotton (BT-Hybrid)",
            projectedBiomassResidueMt = 15.5,
            soilProfile = SoilHealthProfile(
                id = "soil_101",
                farmId = "plot_1",
                testDateFormatted = "18 Oct 2026",
                ph = 6.8,
                phCategory = "Neutral (Optimal)",
                nitrogenKgHa = 240.0,
                nitrogenStatus = "Medium (240 kg/ha)",
                phosphorusKgHa = 18.5,
                phosphorusStatus = "High (18.5 kg/ha)",
                potassiumKgHa = 310.0,
                potassiumStatus = "Adequate (310 kg/ha)",
                organicCarbonPct = 0.68,
                electricalConductivityDsM = 0.38,
                texture = "Clayey Loam / Black Regur",
                soilHealthCardNo = "SHC-MH-2026-8812",
                recommendations = listOf(
                    "Apply bio-enricher mycorrhiza at root zone during vegetative flush",
                    "Maintain sub-surface drip fertigation to conserve 35% water",
                    "Mandatory post-harvest biomass mulching or baling per MPCB mandate"
                )
            )
        ),
        CadastralParcelDetail(
            id = "plot_2",
            parcelName = "Godavari Riverside Sugarcane Plot",
            extractNumber712 = "7/12-NSK-8901",
            gutNumber = "Gut No. 88/1B",
            surveyNumber = "Survey 56/1",
            village = "Niphad Bio-Energy Zone",
            taluka = "Niphad",
            district = "Nashik",
            totalAreaAcres = 8.0,
            totalAreaGunthas = 320.0,
            latitude = 20.0782,
            longitude = 74.1124,
            boundariesDesc = "North: Godavari Right Canal | South: Shinde Farmstead | East: Survey 56/2 | West: Paved Service Road",
            soilTypeLabel = "Alluvial Loam",
            waterSourceLabel = "Canal Irrigation & Lift Pump",
            activeCropName = "Sugarcane (Co-86032)",
            projectedBiomassResidueMt = 28.0,
            soilProfile = SoilHealthProfile(
                id = "soil_102",
                farmId = "plot_2",
                testDateFormatted = "24 Sep 2026",
                ph = 7.2,
                phCategory = "Slightly Alkaline (Good)",
                nitrogenKgHa = 280.0,
                nitrogenStatus = "High (280 kg/ha)",
                phosphorusKgHa = 22.0,
                phosphorusStatus = "High (22.0 kg/ha)",
                potassiumKgHa = 340.0,
                potassiumStatus = "Rich (340 kg/ha)",
                organicCarbonPct = 0.74,
                electricalConductivityDsM = 0.45,
                texture = "Deep Silt Alluvium",
                soilHealthCardNo = "SHC-MH-2026-7734",
                recommendations = listOf(
                    "High sugar carbon residue detected; excellent for industrial briquetting",
                    "Incorporate trash shredder after ratoon harvest",
                    "Apply zinc sulphate 25 kg/ha before next cropping cycle"
                )
            )
        ),
        CadastralParcelDetail(
            id = "plot_3",
            parcelName = "Dindori Hillside Soybean Holding",
            extractNumber712 = "7/12-NSK-3120",
            gutNumber = "Gut No. 45/C",
            surveyNumber = "Survey 23/4",
            village = "Dindori Agri Cluster",
            taluka = "Dindori",
            district = "Nashik",
            totalAreaAcres = 3.2,
            totalAreaGunthas = 128.0,
            latitude = 20.2104,
            longitude = 73.8341,
            boundariesDesc = "North: Forest Contour Trench | South: Village Cart Track | East: Gavit Agro Parcel | West: Survey 23/3",
            soilTypeLabel = "Red & Yellow Loam",
            waterSourceLabel = "Rain-fed with Farm Pond",
            activeCropName = "Soybean (JS-335)",
            projectedBiomassResidueMt = 9.8,
            soilProfile = SoilHealthProfile(
                id = "soil_103",
                farmId = "plot_3",
                testDateFormatted = "05 Oct 2026",
                ph = 6.4,
                phCategory = "Slightly Acidic",
                nitrogenKgHa = 195.0,
                nitrogenStatus = "Medium-Low (195 kg/ha)",
                phosphorusKgHa = 14.0,
                phosphorusStatus = "Medium (14.0 kg/ha)",
                potassiumKgHa = 260.0,
                potassiumStatus = "Adequate (260 kg/ha)",
                organicCarbonPct = 0.55,
                electricalConductivityDsM = 0.28,
                texture = "Gravelly Red Sandy Loam",
                soilHealthCardNo = "SHC-MH-2026-9041",
                recommendations = listOf(
                    "Apply agricultural lime @ 200 kg/ha to adjust soil pH",
                    "Inoculate soybean seed with Rhizobium culture",
                    "Store threshed husk in moisture-proof polyhouse before mill dispatch"
                )
            )
        )
    )

    private val sampleCrops = listOf(
        CropCycle(
            id = "crop_001",
            cropName = "Cotton (BT-Hybrid)",
            variety = "RCH-659 High Yield",
            category = "Fiber & Biomass",
            plotAreaAcres = 4.5,
            sowingDateMillis = System.currentTimeMillis() - 75L * 86400000L,
            expectedHarvestMillis = System.currentTimeMillis() + 35L * 86400000L,
            growthStage = GrowthStage.FRUITING,
            status = CropCycleStatus.ACTIVE,
            village = "Sinnar Rural Hub",
            projectedResidueMt = 15.5,
            healthScorePercent = 95,
            notes = "Drip fertigation active. Crop vigor optimal with zero pest threshold."
        ),
        CropCycle(
            id = "crop_002",
            cropName = "Wheat Stubble",
            variety = "Lokwan Golden",
            category = "Cereal",
            plotAreaAcres = 3.0,
            sowingDateMillis = System.currentTimeMillis() - 110L * 86400000L,
            expectedHarvestMillis = System.currentTimeMillis() - 10L * 86400000L,
            growthStage = GrowthStage.HARVESTED,
            status = CropCycleStatus.HARVESTED,
            village = "Sinnar Rural Hub",
            projectedResidueMt = 10.5,
            healthScorePercent = 92,
            notes = "Baled with Super Seeder. Ready for industrial procurement pickup."
        )
    )

    private val _uiState = MutableStateFlow(FarmDetailUiState(farmId = farmId))
    val uiState: StateFlow<FarmDetailUiState> = _uiState.asStateFlow()

    init {
        loadParcelDetail()
    }

    private fun loadParcelDetail() {
        val found = sampleParcels.find { it.id.equals(farmId, ignoreCase = true) } ?: sampleParcels.first()
        _uiState.update { it.copy(parcel = found, cropCycles = sampleCrops) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    fun exportCadastralPass() {
        val parcel = _uiState.value.parcel ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isExportingPass = true, errorMessage = null) }
            kotlinx.coroutines.delay(650)
            _uiState.update {
                it.copy(
                    isExportingPass = false,
                    feedbackMessage = "Digital 7/12 Land Pass for ${parcel.extractNumber712} exported to local storage."
                )
            }
        }
    }
}

