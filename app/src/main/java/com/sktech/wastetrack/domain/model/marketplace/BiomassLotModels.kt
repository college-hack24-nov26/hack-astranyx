package com.sktech.wastetrack.domain.model.marketplace

import com.sktech.wastetrack.domain.model.OrderStatus

data class BiomassLotBid(
    val id: String,
    val lotId: String,
    val bidderName: String,
    val companyName: String,
    val bidPricePerTon: Double,
    val totalAmount: Double,
    val pickupLeadDays: Int,
    val timestampMillis: Long,
    val isWinning: Boolean = false
)

data class BiomassLotDetail(
    val id: String,
    val cropName: String,
    val residueTitle: String,
    val category: String,
    val quantityMt: Double,
    val askingPricePerTon: Double,
    val moistureContentPct: Double,
    val moistureGrade: String,
    val calorificValueMjKg: Double,
    val calorificKcalKg: Double,
    val ashContentPct: Double,
    val bulkDensityKgM3: Double,
    val storageCondition: String,
    val harvestDate: String,
    val farmerName: String,
    val fpoName: String,
    val village: String,
    val taluka: String,
    val district: String,
    val pincode: String,
    val distanceKm: Double,
    val nearestCorridor: String,
    val sellerRating: Double,
    val sellerOrdersCount: Int,
    val isVerifiedFpo: Boolean = true,
    val description: String,
    val activeBids: List<BiomassLotBid> = emptyList(),
    val statusText: String = "Open for Industry Bidding"
)

data class IndustrialOrderMilestone(
    val stepIndex: Int,
    val title: String,
    val statusText: String,
    val payoutPct: Int,
    val payoutAmount: Double,
    val timestampFormatted: String,
    val isCompleted: Boolean,
    val isActive: Boolean,
    val proofReference: String
)

data class IndustrialOrderTrackingDetail(
    val orderId: String,
    val orderNumber: String,
    val lotId: String,
    val contractHash: String,
    val cropName: String,
    val residueTitle: String,
    val quantityMt: Double,
    val agreedPricePerMt: Double,
    val cropSubtotal: Double,
    val mandiPlatformCess: Double,
    val freightDeposit: Double,
    val totalEscrowLocked: Double,
    val sellerFarmerName: String,
    val sellerFpo: String,
    val sellerUpiVpa: String,
    val buyerCompanyName: String,
    val buyerContactPerson: String,
    val driverName: String,
    val driverPhone: String,
    val vehicleNumber: String,
    val gatePassNumber: String,
    val originLocation: String,
    val destinationPlant: String,
    val currentMilestoneStage: Int,
    val status: OrderStatus,
    val grossTareVerifiedWeight: Double? = null,
    val qualityMoistureResult: String? = null,
    val milestones: List<IndustrialOrderMilestone> = emptyList(),
    val createdAtMillis: Long
)

