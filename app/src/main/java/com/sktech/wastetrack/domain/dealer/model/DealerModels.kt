package com.sktech.wastetrack.domain.dealer.model

enum class BioEnergyPlantType(val title: String, val subtitle: String) {
    PELLET_PLANT("Bio-Pellet & Briquette Plant", "High-density fuel pellets for thermal co-firing"),
    CBG_SATAT_UNIT("CBG / Bio-CNG Plant", "SATAT scheme compressed biogas digester"),
    PAPER_BOARD_MILL("Paper & Pulp Mill", "Unbleached kraft paper and board manufacturing"),
    BIOCHAR_PYROLYSIS("Biochar Pyrolysis Hub", "Soil amendment carbon removal facility"),
    CO_GENERATION("Sugar Mill Co-Gen Unit", "Bagasse-fired 30MW turbine co-generation"),
    FPO_AGGREGATION_HUB("FPO Custom Aggregation Depot", "Village-level high-density baling cluster")
}

enum class VerificationBadge(val label: String, val colorHex: Long) {
    MPCB_CERTIFIED("MPCB Zero-Pollution", 0xFF2D6A4F),
    PROMPT_ESCROW_PAYOR("Prompt Escrow Payor", 0xFF0D9488),
    HIGH_ACCURACY_WEIGHBRIDGE("Verified Weighbridge", 0xFF4A90D9),
    CENTRAL_SATAT_UNIT("MNRE SATAT Unit", 0xFFD4A843)
}

data class BioEnergyDealer(
    val id: String,
    val name: String,
    val plantType: BioEnergyPlantType,
    val district: String,
    val address: String,
    val distanceKm: Double,
    val contactPhone: String,
    val contactEmail: String,
    val gstin: String,
    val acceptedCrops: List<String>,
    val minProcurementMt: Double,
    val offeredRatePerMt: Double,
    val badges: List<VerificationBadge>,
    val rating: Double,
    val reviewsCount: Int,
    val pickupAvailable: Boolean = true
)

data class DealerReview(
    val id: String,
    val dealerId: String,
    val farmerName: String,
    val village: String,
    val rating: Double,
    val cropSupplied: String,
    val quantityMt: Double,
    val paymentSpeedDays: Int,
    val comment: String,
    val dateText: String
)

