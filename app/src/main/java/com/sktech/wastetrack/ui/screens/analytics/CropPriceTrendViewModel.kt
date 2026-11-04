package com.sktech.wastetrack.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.agro.MandiPriceEngine
import com.sktech.wastetrack.domain.model.MandiArrivalRadarItem
import com.sktech.wastetrack.domain.model.MandiComparisonItem
import com.sktech.wastetrack.domain.model.MarketData
import com.sktech.wastetrack.domain.model.MonthlyPricePoint
import com.sktech.wastetrack.domain.model.PriceTrend
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CropPriceTrendUiState(
    val selectedCrop: String = "Wheat",
    val selectedState: String = "Maharashtra",
    val availableCrops: List<String> = MandiPriceEngine.getCropNames(),
    val availableStates: List<String> = listOf("Maharashtra", "Madhya Pradesh", "Gujarat", "Punjab", "Karnataka"),
    val currentPrice: MarketData? = null,
    val priceHistory: List<MarketData> = emptyList(),
    val monthlyHistoryPoints: List<MonthlyPricePoint> = emptyList(),
    val marketComparison: List<MandiComparisonItem> = emptyList(),
    val arrivalRadarItems: List<MandiArrivalRadarItem> = emptyList(),
    val ema7Day: Double = 0.0,
    val ema30Day: Double = 0.0,
    val mspBenchmarkPrice: Double = 0.0,
    val volatilityPercent: Double = 0.0,
    val breakoutSignal: String? = null,
    val isBullish: Boolean = true,
    val modalRate: Double? = null,
    val minRate: Double? = null,
    val peakRate: Double? = null,
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val activeMarketsCount: Int = 18,
    val lastSyncedText: String = "Live Agmarknet / APMC Telemetry"
)

@HiltViewModel
class CropPriceTrendViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CropPriceTrendUiState())
    val state: StateFlow<CropPriceTrendUiState> = _state.asStateFlow()

    init {
        loadDataForCrop(_state.value.selectedCrop, _state.value.selectedState)
    }

    fun selectCrop(cropName: String) {
        _state.update { it.copy(selectedCrop = cropName) }
        loadDataForCrop(cropName, _state.value.selectedState)
    }

    fun selectState(stateName: String) {
        _state.update { it.copy(selectedState = stateName) }
        loadDataForCrop(_state.value.selectedCrop, stateName)
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            kotlinx.coroutines.delay(350)
            loadDataForCrop(_state.value.selectedCrop, _state.value.selectedState)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadDataForCrop(cropName: String, stateName: String) {
        val history = MandiPriceEngine.generateRealTimeMandiHistory(cropName)
        val monthlyPoints = MandiPriceEngine.get12MonthPriceTrajectory(cropName)
        val (ema7, ema30, vol) = MandiPriceEngine.getMovingAverageAndVolatility(cropName)
        val (signal, isBullish) = MandiPriceEngine.getBreakoutAlert(cropName)
        val radar = MandiPriceEngine.getMandiArrivalRadar(cropName)
        val comparisons = MandiPriceEngine.getRegionalComparisons(cropName, stateName)
        val msp = MandiPriceEngine.getMspBenchmark(cropName)
        val latest = history.lastOrNull()
        val modal = latest?.pricePerUnit ?: msp

        _state.update {
            it.copy(
                currentPrice = latest,
                priceHistory = history,
                monthlyHistoryPoints = monthlyPoints,
                marketComparison = comparisons,
                arrivalRadarItems = radar,
                ema7Day = ema7,
                ema30Day = ema30,
                volatilityPercent = vol,
                breakoutSignal = signal,
                isBullish = isBullish,
                mspBenchmarkPrice = msp,
                modalRate = modal,
                minRate = (modal * 0.93 * 10.0).toInt() / 10.0,
                peakRate = (modal * 1.07 * 10.0).toInt() / 10.0,
                activeMarketsCount = comparisons.size + radar.size
            )
        }
    }
}
