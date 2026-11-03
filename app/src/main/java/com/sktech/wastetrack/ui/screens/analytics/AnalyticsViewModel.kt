package com.sktech.wastetrack.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonthlyRevenue(
    val monthName: String,
    val revenueAmount: Double,
    val biomassTonnage: Double
)

data class MandiPriceBenchmark(
    val cropName: String,
    val ratePerMt: Double,
    val changePercent: Double,
    val mandiLocation: String,
    val isPositive: Boolean
)

data class AnalyticsUiState(
    val totalBiomassDivertedMt: Double = 185.0,
    val totalRevenueEarned: Double = 398500.0,
    val co2AvoidedMt: Double = 142.8,
    val cleanEnergyGeneratedKwh: Double = 84500.0,
    val treesEquivalent: Int = 6480,
    val selectedTimeRange: String = "6 Months",
    val monthlyData: List<MonthlyRevenue> = listOf(
        MonthlyRevenue("Jun", 18500.0, 8.5),
        MonthlyRevenue("Jul", 32000.0, 14.0),
        MonthlyRevenue("Aug", 48500.0, 22.0),
        MonthlyRevenue("Sep", 72000.0, 34.5),
        MonthlyRevenue("Oct", 95500.0, 46.0),
        MonthlyRevenue("Nov", 132000.0, 60.0)
    ),
    val categoryShares: Map<String, Float> = mapOf(
        "Cotton Stalks" to 0.42f,
        "Sugarcane Bagasse" to 0.31f,
        "Soybean Husk" to 0.18f,
        "Wheat Straw" to 0.09f
    ),
    val mandiBenchmarks: List<MandiPriceBenchmark> = listOf(
        MandiPriceBenchmark("Cotton Stalks", 2400.0, 4.2, "Nashik APMC", true),
        MandiPriceBenchmark("Sugarcane Bagasse", 1850.0, 1.8, "Niphad Co-Gen", true),
        MandiPriceBenchmark("Soybean Residue", 2600.0, 5.0, "Dindori Biomass Hub", true),
        MandiPriceBenchmark("Wheat Straw", 2100.0, -0.8, "Yeola Mandi", false)
    ),
    val showCertificateDialog: Boolean = false,
    val certificateHash: String = "0x9F4A8C2B7E1D3065"
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun setTimeRange(range: String) {
        _uiState.update { it.copy(selectedTimeRange = range) }
    }

    fun openCertificateDialog() {
        _uiState.update { it.copy(showCertificateDialog = true) }
    }

    fun dismissCertificateDialog() {
        _uiState.update { it.copy(showCertificateDialog = false) }
    }
}
