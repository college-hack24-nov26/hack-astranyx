package com.sktech.wastetrack.ui.screens.agroplan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.agro.model.StubbleValorizationSummary
import com.sktech.wastetrack.domain.agro.model.ValorizationChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StubbleValorizationUiState(
    val farmId: String = "plot_1",
    val cropName: String = "Cotton",
    val summary: StubbleValorizationSummary? = null,
    val selectedChannelId: String = "chan_1",
    val customBalingCostPerTon: Double = 600.0,
    val customFreightKm: Double = 25.0,
    val isLoading: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class StubbleValorizationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val farmParam: String = savedStateHandle["farmId"] ?: "plot_1"
    private val cropParam: String = savedStateHandle["crop"] ?: "Cotton"

    private val sampleSummaries = mapOf(
        "cotton" to StubbleValorizationSummary(
            farmId = "plot_1",
            farmName = "Patil Agro Bio-Parcel A",
            cropName = "Cotton (BT-Hybrid)",
            totalAcreage = 4.5,
            estimatedTonnage = 15.5,
            moisturePercent = 12.4,
            baseCalorificMjKg = 16.8,
            totalGrossPotentialInr = 49600.0,
            channels = listOf(
                ValorizationChannel("chan_1", "Industrial Bio-Pelleting", "Thermal Co-Firing", 185.0, 14800.0, 14, 4.2, 96, "Mahindra Bio-Power, Forbes Marshall", "Shredding + Ring-Die Extrusion", "Eligible for 50% MPCB capital grant", isRecommendedBest = true),
                ValorizationChannel("chan_2", "Mushroom Substrate Cultivation", "Fungiculture & Compost", 240.0, 22500.0, 21, 3.8, 92, "Sahyadri Agro Farms, Local Growers", "Pasteurization + Mycelium Bagging", "National Horticulture Board 35% subsidy", isRecommendedBest = false),
                ValorizationChannel("chan_3", "Biochar Pyrolysis & Soil Carbon", "Regenerative Carbon", 145.0, 11200.0, 30, 5.6, 88, "Coromandel Agro, Carbon Exchangers", "Oxygen-deprived slow kiln pyrolysis", "Verra Verified Carbon Standard credits", isRecommendedBest = false),
                ValorizationChannel("chan_4", "Compressed Bio-Gas (CBG / SATAT)", "Bio-CNG Fuel", 210.0, 18000.0, 45, 4.8, 93, "IOCL Green Gas, Praj Industries", "Anaerobic deep digester slurry", "Central SATAT scheme ₹4/kg bonus", isRecommendedBest = false)
            )
        ),
        "sugarcane" to StubbleValorizationSummary(
            farmId = "plot_2",
            farmName = "Godavari Sugarcane Plot",
            cropName = "Sugarcane (Co-86032)",
            totalAcreage = 8.0,
            estimatedTonnage = 42.0,
            moisturePercent = 15.8,
            baseCalorificMjKg = 18.2,
            totalGrossPotentialInr = 100800.0,
            channels = listOf(
                ValorizationChannel("chan_1", "2G Bio-Ethanol Digestion", "Biofuel Matrix", 225.0, 26000.0, 18, 6.2, 98, "Praj Bio-Refinery, Niphad Distillery", "Hydrolysis + Cellulosic fermentation", "National Biofuel Policy 20% mandate", isRecommendedBest = true),
                ValorizationChannel("chan_2", "Industrial Boiler Briquettes", "Combustion Pellets", 175.0, 19200.0, 15, 5.1, 94, "Niphad Sugar Co-Gen Plant", "Crushing + Hydraulic Piston Press", "CERC tariff support ₹5.85/kWh", isRecommendedBest = false),
                ValorizationChannel("chan_3", "Microbial Biochar Compost", "Soil Regeneration", 160.0, 15800.0, 28, 4.9, 89, "Godavari Organic Soil Hub", "Trash mulching + Trichoderma spray", "Soil Health Card credit ₹1,200/Ac", isRecommendedBest = false)
            )
        ),
        "soybean" to StubbleValorizationSummary(
            farmId = "plot_3",
            farmName = "Dindori Soybean Holding",
            cropName = "Soybean (JS-335)",
            totalAcreage = 3.2,
            estimatedTonnage = 9.8,
            moisturePercent = 13.0,
            baseCalorificMjKg = 15.4,
            totalGrossPotentialInr = 26460.0,
            channels = listOf(
                ValorizationChannel("chan_1", "Cattle Fodder Pellet Matrix", "Livestock Nutrition", 205.0, 16500.0, 12, 3.4, 95, "Nashik District Dairy Federation", "Steam extrusion + Mineral enrichment", "State Dairy Board subsidized rate", isRecommendedBest = true),
                ValorizationChannel("chan_2", "Biochar Soil Conditioner", "Soil Pyrolysis", 150.0, 12000.0, 25, 4.1, 90, "Dindori Tribal Farmers Hub", "Farm trench pyrolysis kiln", "Zero residue burning compliance certificate", isRecommendedBest = false)
            )
        )
    )

    private val _uiState = MutableStateFlow(
        StubbleValorizationUiState(
            farmId = farmParam,
            cropName = cropParam
        )
    )
    val uiState: StateFlow<StubbleValorizationUiState> = _uiState.asStateFlow()

    init {
        loadSummary(cropParam)
    }

    private fun loadSummary(crop: String) {
        val key = crop.lowercase().let {
            if (it.contains("sugar")) "sugarcane"
            else if (it.contains("soy")) "soybean"
            else "cotton"
        }
        val found = sampleSummaries[key] ?: sampleSummaries["cotton"]!!
        _uiState.update { it.copy(summary = found, cropName = crop) }
    }

    fun selectCrop(crop: String) {
        _uiState.update { it.copy(cropName = crop) }
        loadSummary(crop)
    }

    fun selectChannel(channelId: String) {
        _uiState.update { it.copy(selectedChannelId = channelId) }
    }

    fun updateBalingCost(cost: Double) {
        _uiState.update { it.copy(customBalingCostPerTon = cost) }
    }

    fun updateFreightKm(km: Double) {
        _uiState.update { it.copy(customFreightKm = km) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    fun calculateSimulatedNetRevenue(baseRevenuePerAcre: Double): Double {
        val balingCostDeduction = _uiState.value.customBalingCostPerTon * 3.5
        val freightDeduction = _uiState.value.customFreightKm * 18.0
        return (baseRevenuePerAcre - balingCostDeduction - freightDeduction).coerceAtLeast(3500.0)
    }
}

