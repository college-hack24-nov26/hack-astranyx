package com.sktech.wastetrack.ui.screens.bid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.BidStatus
import com.sktech.wastetrack.domain.model.BuyerQuoteBid
import com.sktech.wastetrack.domain.model.ResidueAuctionLot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class BidMarketUiState(
    val selectedTab: String = "Open Auctions",
    val searchQuery: String = "",
    val selectedCrop: String = "All",
    val auctionLots: List<ResidueAuctionLot> = listOf(
        ResidueAuctionLot(
            id = "auc_101",
            title = "Baled High-Density Cotton Stalks (45 MT)",
            cropName = "Cotton",
            quantityMt = 45.0,
            farmerName = "Ramesh Patil",
            village = "Sinnar Agro Hub, Nashik",
            distanceKm = 14.5,
            reservePricePerTon = 3000.0,
            currentHighestBidPerTon = 3250.0,
            totalBidsCount = 5,
            endTimeMillis = System.currentTimeMillis() + 4L * 3600 * 1000 + 30 * 60 * 1000,
            status = BidStatus.OPEN
        ),
        ResidueAuctionLot(
            id = "auc_102",
            title = "Crushed Sugarcane Bagasse (80 MT)",
            cropName = "Sugarcane",
            quantityMt = 80.0,
            farmerName = "Vitthalrao Shinde",
            village = "Niphad Bio-Energy Zone",
            distanceKm = 28.0,
            reservePricePerTon = 2200.0,
            currentHighestBidPerTon = 2450.0,
            totalBidsCount = 8,
            endTimeMillis = System.currentTimeMillis() + 8L * 3600 * 1000,
            status = BidStatus.OPEN
        ),
        ResidueAuctionLot(
            id = "auc_103",
            title = "Threshed Soybean Husk & Straw (30 MT)",
            cropName = "Soybean",
            quantityMt = 30.0,
            farmerName = "Kailash Gavit",
            village = "Dindori Agri Cluster",
            distanceKm = 22.0,
            reservePricePerTon = 2500.0,
            currentHighestBidPerTon = 2700.0,
            totalBidsCount = 3,
            endTimeMillis = System.currentTimeMillis() + 12L * 3600 * 1000,
            status = BidStatus.OPEN
        ),
        ResidueAuctionLot(
            id = "auc_104",
            title = "Golden Shredded Wheat Straw (65 MT)",
            cropName = "Wheat",
            quantityMt = 65.0,
            farmerName = "Sanjay Deshmukh",
            village = "Sinnar Rural Hub",
            distanceKm = 38.0,
            reservePricePerTon = 2000.0,
            currentHighestBidPerTon = 2150.0,
            totalBidsCount = 4,
            endTimeMillis = System.currentTimeMillis() + 18L * 3600 * 1000,
            status = BidStatus.OPEN
        )
    ),
    val myQuotes: Map<String, BuyerQuoteBid> = mapOf(
        "auc_101" to BuyerQuoteBid("quote_1", "auc_101", "Mahindra Bio-Energy", 3250.0, 146250.0, 3, System.currentTimeMillis() - 3600 * 1000, true),
        "auc_102" to BuyerQuoteBid("quote_2", "auc_102", "Mahindra Bio-Energy", 2400.0, 192000.0, 2, System.currentTimeMillis() - 7200 * 1000, false)
    ),
    val selectedLotForBid: ResidueAuctionLot? = null,
    val bidPriceInput: String = "",
    val pickupDaysInput: String = "3",
    val isBidSubmitted: Boolean = false,
    val isSubmitting: Boolean = false
)

@HiltViewModel
class BidViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BidMarketUiState())
    val uiState: StateFlow<BidMarketUiState> = _uiState.asStateFlow()

    fun setFilterTab(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCrop(crop: String) {
        _uiState.update { it.copy(selectedCrop = crop) }
    }

    fun openBidSheet(lot: ResidueAuctionLot) {
        val minBid = lot.currentHighestBidPerTon + 50.0
        _uiState.update {
            it.copy(
                selectedLotForBid = lot,
                bidPriceInput = minBid.toInt().toString(),
                pickupDaysInput = "3",
                isBidSubmitted = false
            )
        }
    }

    fun closeBidSheet() {
        _uiState.update { it.copy(selectedLotForBid = null) }
    }

    fun updateBidPrice(price: String) {
        _uiState.update { it.copy(bidPriceInput = price) }
    }

    fun updatePickupDays(days: String) {
        _uiState.update { it.copy(pickupDaysInput = days) }
    }

    fun submitQuote() {
        val lot = _uiState.value.selectedLotForBid ?: return
        val price = _uiState.value.bidPriceInput.toDoubleOrNull() ?: return
        val days = _uiState.value.pickupDaysInput.toIntOrNull() ?: 3

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val newQuote = BuyerQuoteBid(
                id = "quote_" + UUID.randomUUID().toString().take(6),
                lotId = lot.id,
                buyerName = "My Industry Org",
                bidPricePerTon = price,
                totalBidAmount = price * lot.quantityMt,
                proposedPickupDays = days,
                timestampMillis = System.currentTimeMillis(),
                isWinning = price > lot.currentHighestBidPerTon
            )

            _uiState.update { state ->
                val updatedLots = state.auctionLots.map { l ->
                    if (l.id == lot.id) {
                        l.copy(
                            currentHighestBidPerTon = maxOf(l.currentHighestBidPerTon, price),
                            totalBidsCount = l.totalBidsCount + 1
                        )
                    } else l
                }
                val updatedQuotes = state.myQuotes.toMutableMap()
                updatedQuotes[lot.id] = newQuote

                state.copy(
                    auctionLots = updatedLots,
                    myQuotes = updatedQuotes,
                    isSubmitting = false,
                    isBidSubmitted = true
                )
            }
        }
    }
}
