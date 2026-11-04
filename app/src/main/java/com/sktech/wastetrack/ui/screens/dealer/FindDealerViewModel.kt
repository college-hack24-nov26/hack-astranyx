package com.sktech.wastetrack.ui.screens.dealer

import androidx.lifecycle.ViewModel
import com.sktech.wastetrack.domain.dealer.model.BioEnergyDealer
import com.sktech.wastetrack.domain.dealer.model.BioEnergyPlantType
import com.sktech.wastetrack.domain.dealer.model.DealerReview
import com.sktech.wastetrack.domain.dealer.model.VerificationBadge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class FindDealerUiState(
    val dealers: List<BioEnergyDealer> = emptyList(),
    val filteredDealers: List<BioEnergyDealer> = emptyList(),
    val reviews: Map<String, List<DealerReview>> = emptyMap(),
    val searchQuery: String = "",
    val selectedCrop: String = "ALL",
    val selectedPlantType: BioEnergyPlantType? = null,
    val maxDistanceKm: Double = 60.0,
    val activeHubName: String = "Sinnar-Nashik Agri Corridor",
    val feedbackMessage: String? = null
)

@HiltViewModel
class FindDealerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FindDealerUiState())
    val uiState: StateFlow<FindDealerUiState> = _uiState.asStateFlow()

    init {
        loadInitialDealers()
    }

    private fun loadInitialDealers() {
        val initialDealers = listOf(
            BioEnergyDealer(
                id = "DLR-MH-101",
                name = "Igatpuri Bio-Energy Private Limited",
                plantType = BioEnergyPlantType.PELLET_PLANT,
                district = "Nashik",
                address = "Plot 44, MIDC Industrial Area, Igatpuri",
                distanceKm = 18.5,
                contactPhone = "+91 98221 44512",
                contactEmail = "procurement@igatpuribio.in",
                gstin = "27AABCI4819M1Z8",
                acceptedCrops = listOf("Cotton", "Paddy Straw", "Soybean"),
                minProcurementMt = 10.0,
                offeredRatePerMt = 2850.0,
                badges = listOf(
                    VerificationBadge.MPCB_CERTIFIED,
                    VerificationBadge.PROMPT_ESCROW_PAYOR,
                    VerificationBadge.HIGH_ACCURACY_WEIGHBRIDGE
                ),
                rating = 4.9,
                reviewsCount = 48,
                pickupAvailable = true
            ),
            BioEnergyDealer(
                id = "DLR-MH-102",
                name = "Sinnar Clean Biogas SATAT Plant",
                plantType = BioEnergyPlantType.CBG_SATAT_UNIT,
                district = "Nashik",
                address = "Survey 112, Malegaon Road, Sinnar",
                distanceKm = 14.0,
                contactPhone = "+91 94223 90112",
                contactEmail = "feedstock@sinnarcbg.org",
                gstin = "27AAACS1102Q1ZF",
                acceptedCrops = listOf("Sugarcane", "Mustard", "Cotton"),
                minProcurementMt = 5.0,
                offeredRatePerMt = 3100.0,
                badges = listOf(
                    VerificationBadge.CENTRAL_SATAT_UNIT,
                    VerificationBadge.MPCB_CERTIFIED,
                    VerificationBadge.PROMPT_ESCROW_PAYOR
                ),
                rating = 4.8,
                reviewsCount = 36,
                pickupAvailable = true
            ),
            BioEnergyDealer(
                id = "DLR-MH-103",
                name = "Niphad Paper & Kraft Board Mills Ltd",
                plantType = BioEnergyPlantType.PAPER_BOARD_MILL,
                district = "Nashik",
                address = "State Highway 17, Niphad Bypass",
                distanceKm = 28.0,
                contactPhone = "+91 97654 33210",
                contactEmail = "rawmaterials@niphadpaper.com",
                gstin = "27AAACN5120K1ZX",
                acceptedCrops = listOf("Sugarcane", "Wheat", "Paddy Straw"),
                minProcurementMt = 15.0,
                offeredRatePerMt = 2400.0,
                badges = listOf(
                    VerificationBadge.MPCB_CERTIFIED,
                    VerificationBadge.HIGH_ACCURACY_WEIGHBRIDGE
                ),
                rating = 4.6,
                reviewsCount = 29,
                pickupAvailable = true
            ),
            BioEnergyDealer(
                id = "DLR-MH-104",
                name = "Dindori Agri-Biochar Pyrolysis Hub",
                plantType = BioEnergyPlantType.BIOCHAR_PYROLYSIS,
                district = "Nashik",
                address = "Vani Road, Dindori Rural Industrial Cluster",
                distanceKm = 34.2,
                contactPhone = "+91 98901 22345",
                contactEmail = "biochar@dindorifpo.org",
                gstin = "27AABCD3311L1ZK",
                acceptedCrops = listOf("Cotton", "Mustard", "Soybean"),
                minProcurementMt = 8.0,
                offeredRatePerMt = 2700.0,
                badges = listOf(
                    VerificationBadge.MPCB_CERTIFIED,
                    VerificationBadge.PROMPT_ESCROW_PAYOR
                ),
                rating = 4.9,
                reviewsCount = 22,
                pickupAvailable = false
            ),
            BioEnergyDealer(
                id = "DLR-MH-105",
                name = "Nashik Sahakari Sakhar Karkhana Co-Gen Unit",
                plantType = BioEnergyPlantType.CO_GENERATION,
                district = "Nashik",
                address = "Palse-Shinde Road, Nashik",
                distanceKm = 22.0,
                contactPhone = "+91 98230 77112",
                contactEmail = "cogen@nashiksugar.coop",
                gstin = "27AAACN1922Q1ZP",
                acceptedCrops = listOf("Sugarcane", "Wheat"),
                minProcurementMt = 20.0,
                offeredRatePerMt = 2250.0,
                badges = listOf(
                    VerificationBadge.MPCB_CERTIFIED,
                    VerificationBadge.HIGH_ACCURACY_WEIGHBRIDGE
                ),
                rating = 4.5,
                reviewsCount = 54,
                pickupAvailable = true
            ),
            BioEnergyDealer(
                id = "DLR-MH-106",
                name = "Sinnar Taluka FPO Custom Aggregation Depot",
                plantType = BioEnergyPlantType.FPO_AGGREGATION_HUB,
                district = "Nashik",
                address = "Near APMC Sub-Market Yard, Sinnar",
                distanceKm = 9.5,
                contactPhone = "+91 98224 81099",
                contactEmail = "balinghub@sinnarfpo.org",
                gstin = "27AABCS9011E1ZF",
                acceptedCrops = listOf("Cotton", "Sugarcane", "Paddy Straw", "Wheat", "Mustard", "Soybean"),
                minProcurementMt = 2.0,
                offeredRatePerMt = 2650.0,
                badges = listOf(
                    VerificationBadge.PROMPT_ESCROW_PAYOR,
                    VerificationBadge.HIGH_ACCURACY_WEIGHBRIDGE,
                    VerificationBadge.CENTRAL_SATAT_UNIT
                ),
                rating = 5.0,
                reviewsCount = 72,
                pickupAvailable = true
            )
        )

        val initialReviews = mapOf(
            "DLR-MH-101" to listOf(
                DealerReview(
                    id = "REV-01",
                    dealerId = "DLR-MH-101",
                    farmerName = "Vitthal Patil",
                    village = "Wadivarhe",
                    rating = 5.0,
                    cropSupplied = "Cotton Stalks",
                    quantityMt = 32.0,
                    paymentSpeedDays = 1,
                    comment = "Prompt weighbridge slip and instant escrow clearance. Truck arrived with hydraulic baler.",
                    dateText = "2 days ago"
                ),
                DealerReview(
                    id = "REV-02",
                    dealerId = "DLR-MH-101",
                    farmerName = "Sharad Kadam",
                    village = "Ghoti",
                    rating = 4.8,
                    cropSupplied = "Paddy Straw",
                    quantityMt = 18.0,
                    paymentSpeedDays = 1,
                    comment = "Clean deductions and fair moisture allowance under 14% limit.",
                    dateText = "1 week ago"
                )
            ),
            "DLR-MH-102" to listOf(
                DealerReview(
                    id = "REV-03",
                    dealerId = "DLR-MH-102",
                    farmerName = "Dnyaneshwar Sanap",
                    village = "Musalgaon",
                    rating = 5.0,
                    cropSupplied = "Sugarcane Trash",
                    quantityMt = 45.0,
                    paymentSpeedDays = 1,
                    comment = "Government SATAT certified rate. Dedicated payment voucher via direct bank transfer.",
                    dateText = "3 days ago"
                )
            )
        )

        _uiState.update { current ->
            current.copy(
                dealers = initialDealers,
                reviews = initialReviews
            )
        }
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun setMaxDistance(distanceKm: Double) {
        _uiState.update { it.copy(maxDistanceKm = distanceKm) }
        applyFilters()
    }

    fun setSelectedCrop(crop: String) {
        _uiState.update { it.copy(selectedCrop = crop) }
        applyFilters()
    }

    fun setSelectedPlantType(type: BioEnergyPlantType?) {
        _uiState.update { it.copy(selectedPlantType = type) }
        applyFilters()
    }

    fun sendInquiry(
        dealerId: String,
        crop: String,
        quantityMt: Double,
        preferredDate: String
    ) {
        val dealer = _uiState.value.dealers.find { it.id == dealerId }
        val msg = if (dealer != null) {
            "Consignment inquiry for %.0f MT of %s dispatched to %s".format(quantityMt, crop, dealer.name)
        } else {
            "Inquiry submitted successfully"
        }
        _uiState.update { it.copy(feedbackMessage = msg) }
    }

    fun addReview(
        dealerId: String,
        farmerName: String,
        village: String,
        rating: Double,
        crop: String,
        quantity: Double,
        comment: String
    ) {
        val newReview = DealerReview(
            id = "REV-" + (1000 + (Math.random() * 9000).toInt()),
            dealerId = dealerId,
            farmerName = farmerName,
            village = village,
            rating = rating,
            cropSupplied = crop,
            quantityMt = quantity,
            paymentSpeedDays = 1,
            comment = comment,
            dateText = "Just now"
        )

        val currentMap = _uiState.value.reviews.toMutableMap()
        val existing = currentMap[dealerId]?.toMutableList() ?: mutableListOf()
        existing.add(0, newReview)
        currentMap[dealerId] = existing

        _uiState.update { current ->
            current.copy(
                reviews = currentMap,
                feedbackMessage = "Review submitted for verified buyer community"
            )
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    private fun applyFilters() {
        val current = _uiState.value
        val query = current.searchQuery.trim().lowercase()
        val crop = current.selectedCrop
        val plantType = current.selectedPlantType
        val maxDist = current.maxDistanceKm

        val filtered = current.dealers.filter { dealer ->
            val matchesDistance = dealer.distanceKm <= maxDist
            val matchesCrop = (crop == "ALL" || dealer.acceptedCrops.any { it.equals(crop, ignoreCase = true) })
            val matchesPlantType = (plantType == null || dealer.plantType == plantType)
            val matchesQuery = query.isBlank() ||
                    dealer.name.lowercase().contains(query) ||
                    dealer.address.lowercase().contains(query) ||
                    dealer.district.lowercase().contains(query) ||
                    dealer.plantType.title.lowercase().contains(query) ||
                    dealer.acceptedCrops.any { it.lowercase().contains(query) }

            matchesDistance && matchesCrop && matchesPlantType && matchesQuery
        }.sortedBy { it.distanceKm }

        _uiState.update { it.copy(filteredDealers = filtered) }
    }
}

