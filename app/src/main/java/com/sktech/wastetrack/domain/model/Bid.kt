package com.sktech.wastetrack.domain.model

data class ResidueAuctionLot(
    val id: String,
    val title: String,
    val cropName: String,
    val quantityMt: Double,
    val farmerName: String,
    val village: String,
    val distanceKm: Double,
    val reservePricePerTon: Double,
    val currentHighestBidPerTon: Double,
    val totalBidsCount: Int,
    val endTimeMillis: Long,
    val status: BidStatus = BidStatus.OPEN
)

data class BuyerQuoteBid(
    val id: String,
    val lotId: String,
    val buyerName: String,
    val bidPricePerTon: Double,
    val totalBidAmount: Double,
    val proposedPickupDays: Int,
    val timestampMillis: Long,
    val isWinning: Boolean = true
)
