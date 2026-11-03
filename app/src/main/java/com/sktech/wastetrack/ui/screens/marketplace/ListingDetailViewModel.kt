package com.sktech.wastetrack.ui.screens.marketplace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.marketplace.BiomassLotBid
import com.sktech.wastetrack.domain.model.marketplace.BiomassLotDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListingDetailUiState(
    val listingId: String = "",
    val lot: BiomassLotDetail? = null,
    val isLoading: Boolean = false,
    val bidPricePerTonText: String = "",
    val isSubmittingBid: Boolean = false,
    val isProcuringLot: Boolean = false,
    val feedbackMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val listingId: String = savedStateHandle["listingId"] ?: "lot_1"

    private val sampleLots = listOf(
        BiomassLotDetail(
            id = "lot_1",
            cropName = "Cotton",
            residueTitle = "Dry Cotton Stalk Residue (Baled)",
            category = "Cotton",
            quantityMt = 45.0,
            askingPricePerTon = 3200.0,
            moistureContentPct = 12.4,
            moistureGrade = "Grade A (<14% moisture)",
            calorificValueMjKg = 16.8,
            calorificKcalKg = 4015.0,
            ashContentPct = 4.2,
            bulkDensityKgM3 = 145.0,
            storageCondition = "Covered Shed, Baled & Strapped",
            harvestDate = "Oct 2026 Harvest",
            farmerName = "Ramesh Patil",
            fpoName = "Sahyadri Bio-Farmers Producer Co.",
            village = "Nashik Rural Hub",
            taluka = "Niphad",
            district = "Nashik",
            pincode = "422303",
            distanceKm = 14.5,
            nearestCorridor = "NH-60 Mumbai-Nashik Freight Highway",
            sellerRating = 4.8,
            sellerOrdersCount = 34,
            isVerifiedFpo = true,
            description = "High-density baled cotton stalk residue with zero soil contamination. Moisture verified with calibrated thermal sensor. Immediate loading available with tractor crane at farm gate.",
            activeBids = listOf(
                BiomassLotBid("bid_101", "lot_1", "Mahindra Bio-Power", "Mahindra Bio-Energy Ltd", 3250.0, 146250.0, 2, System.currentTimeMillis() - 7200000L, true),
                BiomassLotBid("bid_102", "lot_1", "Adani Green Energy", "Adani Biomass Pellet Unit", 3180.0, 143100.0, 3, System.currentTimeMillis() - 14400000L, false)
            ),
            statusText = "Open for Industry Bidding"
        ),
        BiomassLotDetail(
            id = "lot_2",
            cropName = "Sugarcane",
            residueTitle = "High-Calorific Crushed Bagasse",
            category = "Bagasse",
            quantityMt = 80.0,
            askingPricePerTon = 2400.0,
            moistureContentPct = 15.8,
            moistureGrade = "Grade A (16% moisture)",
            calorificValueMjKg = 18.2,
            calorificKcalKg = 4350.0,
            ashContentPct = 3.6,
            bulkDensityKgM3 = 180.0,
            storageCondition = "Paved Yard Sun-Dried",
            harvestDate = "Nov 2026 Crushing",
            farmerName = "Vitthalrao Shinde",
            fpoName = "Godavari Agri-Cooperative Syndicate",
            village = "Niphad Bio-Energy Zone",
            taluka = "Niphad",
            district = "Nashik",
            pincode = "422306",
            distanceKm = 28.0,
            nearestCorridor = "State Highway 30 Agro Logistics Lane",
            sellerRating = 4.9,
            sellerOrdersCount = 52,
            isVerifiedFpo = true,
            description = "Sun-dried crushed sugarcane bagasse with high sugar carbon residue suitable for 2G bio-ethanol digestors and thermal boiler co-firing.",
            activeBids = listOf(
                BiomassLotBid("bid_201", "lot_2", "Praj Bio-Refinery", "Praj Industries Matrix", 2450.0, 196000.0, 1, System.currentTimeMillis() - 5400000L, true)
            ),
            statusText = "Open for Industry Bidding"
        ),
        BiomassLotDetail(
            id = "lot_3",
            cropName = "Soybean",
            residueTitle = "Threshed Soybean Husk & Straw",
            category = "Soybean",
            quantityMt = 30.0,
            askingPricePerTon = 2700.0,
            moistureContentPct = 13.0,
            moistureGrade = "Grade B+ (13% moisture)",
            calorificValueMjKg = 15.4,
            calorificKcalKg = 3680.0,
            ashContentPct = 5.1,
            bulkDensityKgM3 = 120.0,
            storageCondition = "Gunny Bag Stacked",
            harvestDate = "Sep 2026 Threshing",
            farmerName = "Kailash Gavit",
            fpoName = "Dindori Tribal Farmers Collective",
            village = "Dindori Agri Cluster",
            taluka = "Dindori",
            district = "Nashik",
            pincode = "422202",
            distanceKm = 22.0,
            nearestCorridor = "Nashik-Kalwan Highway Corridor",
            sellerRating = 4.7,
            sellerOrdersCount = 18,
            isVerifiedFpo = true,
            description = "Fine threshed soybean husk with high nitrogen organic residue. Suitable for industrial briquette manufacturing and compost substrate.",
            activeBids = emptyList(),
            statusText = "Open for Industry Bidding"
        ),
        BiomassLotDetail(
            id = "lot_4",
            cropName = "Wheat",
            residueTitle = "Shredded Golden Wheat Straw",
            category = "Wheat",
            quantityMt = 65.0,
            askingPricePerTon = 2100.0,
            moistureContentPct = 9.8,
            moistureGrade = "Grade A (10% moisture)",
            calorificValueMjKg = 17.1,
            calorificKcalKg = 4085.0,
            ashContentPct = 4.8,
            bulkDensityKgM3 = 130.0,
            storageCondition = "Baled in Covered Poly-Warehouse",
            harvestDate = "Oct 2026 Harvest",
            farmerName = "Sanjay Deshmukh",
            fpoName = "Sinnar Progressive Agro Producers",
            village = "Sinnar Agro Hub",
            taluka = "Sinnar",
            district = "Nashik",
            pincode = "422103",
            distanceKm = 38.5,
            nearestCorridor = "Samruddhi Mahamarg Interchange Sinnar",
            sellerRating = 4.9,
            sellerOrdersCount = 41,
            isVerifiedFpo = true,
            description = "Super Seeder shredded dry wheat stubble. Extremely low moisture and high energy combustion rate for thermal power plants.",
            activeBids = listOf(
                BiomassLotBid("bid_401", "lot_4", "NTPC Biomass Cell", "NTPC Green Power", 2150.0, 139750.0, 2, System.currentTimeMillis() - 10800000L, true)
            ),
            statusText = "Open for Industry Bidding"
        ),
        BiomassLotDetail(
            id = "lot_5",
            cropName = "Paddy",
            residueTitle = "Combustible Rice Husk & Straw",
            category = "Paddy",
            quantityMt = 50.0,
            askingPricePerTon = 1950.0,
            moistureContentPct = 14.8,
            moistureGrade = "Grade B (15% moisture)",
            calorificValueMjKg = 14.9,
            calorificKcalKg = 3560.0,
            ashContentPct = 16.5,
            bulkDensityKgM3 = 160.0,
            storageCondition = "Dry Silo Warehouse",
            harvestDate = "Oct 2026 Threshing",
            farmerName = "Bhagwan Wagh",
            fpoName = "Igatpuri Bio-Farming Union",
            village = "Igatpuri Biomass Yard",
            taluka = "Igatpuri",
            district = "Nashik",
            pincode = "422403",
            distanceKm = 44.0,
            nearestCorridor = "Mumbai-Nashik Kasara Corridor",
            sellerRating = 4.6,
            sellerOrdersCount = 26,
            isVerifiedFpo = true,
            description = "High silica content rice husk residue ideal for industrial steam boilers, brick kilns, and cement manufacturing co-processing.",
            activeBids = emptyList(),
            statusText = "Open for Industry Bidding"
        )
    )

    private val _uiState = MutableStateFlow(ListingDetailUiState(listingId = listingId))
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    init {
        loadLotDetail()
    }

    private fun loadLotDetail() {
        val foundLot = sampleLots.find { it.id.equals(listingId, ignoreCase = true) } ?: sampleLots.first()
        _uiState.update { current ->
            current.copy(
                lot = foundLot,
                bidPricePerTonText = String.format(java.util.Locale.US, "%.0f", foundLot.askingPricePerTon)
            )
        }
    }

    fun onBidPriceChange(priceText: String) {
        _uiState.update { it.copy(bidPricePerTonText = priceText, errorMessage = null) }
    }

    fun onQuickIncrement(increment: Double) {
        val currentPrice = _uiState.value.bidPricePerTonText.toDoubleOrNull() ?: _uiState.value.lot?.askingPricePerTon ?: 0.0
        val newPrice = currentPrice + increment
        _uiState.update { it.copy(bidPricePerTonText = String.format(java.util.Locale.US, "%.0f", newPrice)) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(feedbackMessage = null, errorMessage = null) }
    }

    fun submitBid() {
        val state = _uiState.value
        val lot = state.lot ?: return
        val price = state.bidPricePerTonText.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid bid price per Ton") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingBid = true, errorMessage = null) }
            kotlinx.coroutines.delay(600)
            val totalAmount = price * lot.quantityMt
            val newBid = BiomassLotBid(
                id = "bid_" + System.currentTimeMillis().toString().takeLast(5),
                lotId = lot.id,
                bidderName = "Kasturi Industrial Buyer Desk",
                companyName = "AstraNyx Procurement Core",
                bidPricePerTon = price,
                totalAmount = totalAmount,
                pickupLeadDays = 2,
                timestampMillis = System.currentTimeMillis(),
                isWinning = true
            )
            val updatedBids = listOf(newBid) + lot.activeBids.map { it.copy(isWinning = false) }
            val updatedLot = lot.copy(activeBids = updatedBids)
            _uiState.update {
                it.copy(
                    lot = updatedLot,
                    isSubmittingBid = false,
                    feedbackMessage = "Industrial bid of ₹${String.format(java.util.Locale.US, "%,.0f", price)}/Ton placed and locked in escrow contract."
                )
            }
        }
    }

    fun instantProcureLot(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val lot = state.lot ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isProcuringLot = true, errorMessage = null) }
            kotlinx.coroutines.delay(700)
            val generatedOrderId = when (lot.id) {
                "lot_1" -> "ord_001"
                "lot_2" -> "ord_002"
                "lot_3" -> "ord_003"
                "lot_4" -> "ord_004"
                else -> "ord_001"
            }
            _uiState.update { it.copy(isProcuringLot = false) }
            onSuccess(generatedOrderId)
        }
    }
}

