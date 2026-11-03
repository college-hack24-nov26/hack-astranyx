package com.sktech.wastetrack.ui.screens.analytics

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.model.analytics.MandiRate
import com.sktech.wastetrack.domain.model.analytics.PriceHistoryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class MandiPriceTrendUiState(
    val selectedCrop: String = "Wheat",
    val crops: List<String> = listOf("Wheat", "Cotton", "Sugarcane", "Soybean", "Maize"),
    val searchQuery: String = "",
    val allRates: List<MandiRate> = listOf(
        MandiRate("Wheat", "Lasalgaon APMC Market", 2280.0, 2450.0, 2620.0, 2200.0, 4.8, true, 185.0),
        MandiRate("Wheat", "Sinnar Agro Market Yard", 2200.0, 2380.0, 2510.0, 2150.0, 2.6, true, 140.0),
        MandiRate("Wheat", "Niphad Grain Depot", 2150.0, 2320.0, 2480.0, 2100.0, -1.2, false, 95.0),
        MandiRate("Wheat", "Yeola Mandi Hub", 2190.0, 2360.0, 2540.0, 2180.0, 1.9, true, 120.0),

        MandiRate("Cotton", "Malegaon Cotton Yard", 6800.0, 7250.0, 7600.0, 2800.0, 5.4, true, 260.0),
        MandiRate("Cotton", "Dindori Agro Terminal", 6650.0, 7100.0, 7450.0, 2750.0, 3.1, true, 175.0),
        MandiRate("Cotton", "Yeola Mandi Hub", 6700.0, 7180.0, 7520.0, 2780.0, 4.0, true, 130.0),

        MandiRate("Sugarcane", "Niphad Sugar Complex", 2950.0, 3180.0, 3350.0, 1900.0, 2.8, true, 580.0),
        MandiRate("Sugarcane", "Sinnar Agro Hub", 2900.0, 3120.0, 3280.0, 1850.0, 1.4, true, 340.0),

        MandiRate("Soybean", "Lasalgaon APMC Market", 4500.0, 4850.0, 5150.0, 2600.0, 6.2, true, 210.0),
        MandiRate("Soybean", "Dindori Mandi", 4400.0, 4780.0, 5050.0, 2550.0, 3.8, true, 160.0),

        MandiRate("Maize", "Yeola Grain Yard", 1850.0, 2020.0, 2180.0, 1650.0, -2.1, false, 190.0),
        MandiRate("Maize", "Sinnar Market Yard", 1880.0, 2060.0, 2210.0, 1700.0, 1.1, true, 145.0)
    ),
    val historyPoints: List<PriceHistoryPoint> = listOf(
        PriceHistoryPoint("01 Oct", 2180.0),
        PriceHistoryPoint("06 Oct", 2220.0),
        PriceHistoryPoint("12 Oct", 2280.0),
        PriceHistoryPoint("18 Oct", 2250.0),
        PriceHistoryPoint("24 Oct", 2340.0),
        PriceHistoryPoint("30 Oct", 2390.0),
        PriceHistoryPoint("04 Nov", 2450.0)
    ),
    val isRefreshing: Boolean = false
) {
    val filteredRates: List<MandiRate>
        get() = allRates.filter {
            it.cropName.equals(selectedCrop, ignoreCase = true) &&
                    (searchQuery.isBlank() || it.marketName.contains(searchQuery, ignoreCase = true))
        }

    val selectedModalRate: Double
        get() = filteredRates.firstOrNull()?.modalRateQuintal ?: 2400.0

    val selectedStubbleValue: Double
        get() = filteredRates.firstOrNull()?.stubbleResidueValuePerMt ?: 2200.0
}

@HiltViewModel
class MandiPriceTrendViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MandiPriceTrendUiState())
    val uiState: StateFlow<MandiPriceTrendUiState> = _uiState.asStateFlow()

    fun selectCrop(cropName: String) {
        val updatedHistory = when (cropName) {
            "Cotton" -> listOf(
                PriceHistoryPoint("01 Oct", 6600.0),
                PriceHistoryPoint("06 Oct", 6750.0),
                PriceHistoryPoint("12 Oct", 6900.0),
                PriceHistoryPoint("18 Oct", 7050.0),
                PriceHistoryPoint("24 Oct", 6980.0),
                PriceHistoryPoint("30 Oct", 7120.0),
                PriceHistoryPoint("04 Nov", 7250.0)
            )
            "Sugarcane" -> listOf(
                PriceHistoryPoint("01 Oct", 2980.0),
                PriceHistoryPoint("06 Oct", 3020.0),
                PriceHistoryPoint("12 Oct", 3080.0),
                PriceHistoryPoint("18 Oct", 3100.0),
                PriceHistoryPoint("24 Oct", 3120.0),
                PriceHistoryPoint("30 Oct", 3150.0),
                PriceHistoryPoint("04 Nov", 3180.0)
            )
            "Soybean" -> listOf(
                PriceHistoryPoint("01 Oct", 4250.0),
                PriceHistoryPoint("06 Oct", 4400.0),
                PriceHistoryPoint("12 Oct", 4550.0),
                PriceHistoryPoint("18 Oct", 4500.0),
                PriceHistoryPoint("24 Oct", 4680.0),
                PriceHistoryPoint("30 Oct", 4750.0),
                PriceHistoryPoint("04 Nov", 4850.0)
            )
            "Maize" -> listOf(
                PriceHistoryPoint("01 Oct", 2120.0),
                PriceHistoryPoint("06 Oct", 2080.0),
                PriceHistoryPoint("12 Oct", 2050.0),
                PriceHistoryPoint("18 Oct", 2020.0),
                PriceHistoryPoint("24 Oct", 1990.0),
                PriceHistoryPoint("30 Oct", 2010.0),
                PriceHistoryPoint("04 Nov", 2020.0)
            )
            else -> listOf(
                PriceHistoryPoint("01 Oct", 2180.0),
                PriceHistoryPoint("06 Oct", 2220.0),
                PriceHistoryPoint("12 Oct", 2280.0),
                PriceHistoryPoint("18 Oct", 2250.0),
                PriceHistoryPoint("24 Oct", 2340.0),
                PriceHistoryPoint("30 Oct", 2390.0),
                PriceHistoryPoint("04 Nov", 2450.0)
            )
        }
        _uiState.update { it.copy(selectedCrop = cropName, historyPoints = updatedHistory) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}

