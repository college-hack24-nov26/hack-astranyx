package com.sktech.wastetrack.ui.screens.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResidueMarketLot(
    val id: String,
    val cropName: String,
    val residueTitle: String,
    val category: String,
    val quantityMt: Double,
    val pricePerTonInr: Double,
    val moistureGrade: String,
    val calorificValueMjKg: Double,
    val farmerName: String,
    val village: String,
    val distanceKm: Double,
    val isVerified: Boolean = true
)

data class MarketBrowseUiState(
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val lots: List<ResidueMarketLot> = listOf(
        ResidueMarketLot("lot_1", "Cotton", "Dry Cotton Stalk Residue (Baled)", "Cotton", 45.0, 3200.0, "Grade A (<14%)", 16.8, "Ramesh Patil", "Nashik Rural Hub", 14.5),
        ResidueMarketLot("lot_2", "Sugarcane", "High-Calorific Crushed Bagasse", "Bagasse", 80.0, 2400.0, "Grade A (16%)", 18.2, "Vitthalrao Shinde", "Niphad Bio-Energy Zone", 28.0),
        ResidueMarketLot("lot_3", "Soybean", "Threshed Soybean Husk & Straw", "Soybean", 30.0, 2700.0, "Grade B+ (13%)", 15.4, "Kailash Gavit", "Dindori Agri Cluster", 22.0),
        ResidueMarketLot("lot_4", "Wheat", "Shredded Golden Wheat Straw", "Wheat", 65.0, 2100.0, "Grade A (10%)", 17.1, "Sanjay Deshmukh", "Sinnar Agro Hub", 38.5),
        ResidueMarketLot("lot_5", "Paddy", "Combustible Rice Husk & Straw", "Paddy", 50.0, 1950.0, "Grade B (15%)", 14.9, "Bhagwan Wagh", "Igatpuri Biomass Yard", 44.0)
    ),
    val selectedLotForBid: ResidueMarketLot? = null,
    val bidQuantityInput: String = "",
    val bidPriceInput: String = "",
    val isBidSubmitted: Boolean = false,
    val activeBidsCount: Int = 4,
    val isLoading: Boolean = false
)

@HiltViewModel
class MarketBrowseViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MarketBrowseUiState())
    val uiState: StateFlow<MarketBrowseUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(cat: String) {
        _uiState.update { it.copy(selectedCategory = cat) }
    }

    fun openBidSheet(lot: ResidueMarketLot) {
        _uiState.update {
            it.copy(
                selectedLotForBid = lot,
                bidQuantityInput = lot.quantityMt.toString(),
                bidPriceInput = lot.pricePerTonInr.toString(),
                isBidSubmitted = false
            )
        }
    }

    fun closeBidSheet() {
        _uiState.update { it.copy(selectedLotForBid = null) }
    }

    fun updateBidQuantity(qty: String) {
        _uiState.update { it.copy(bidQuantityInput = qty) }
    }

    fun updateBidPrice(price: String) {
        _uiState.update { it.copy(bidPriceInput = price) }
    }

    fun submitBid() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBidSubmitted = true,
                    activeBidsCount = it.activeBidsCount + 1
                )
            }
        }
    }
}
