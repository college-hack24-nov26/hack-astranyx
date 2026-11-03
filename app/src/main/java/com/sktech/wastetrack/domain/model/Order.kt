package com.sktech.wastetrack.domain.model

enum class OrderStatus(val displayName: String) {
    CONFIRMED("Contract Confirmed"),
    ESCROW_LOCKED("Escrow Protected"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Weighbridge Verified"),
    SETTLED("Escrow Released")
}

data class OrderItem(
    val id: String,
    val orderNumber: String,
    val residueTitle: String,
    val cropName: String,
    val sellerFarmerName: String,
    val buyerCompanyName: String,
    val quantityMt: Double,
    val pricePerMt: Double,
    val totalAmount: Double,
    val escrowLockedAmount: Double,
    val status: OrderStatus,
    val gatePassNumber: String,
    val vehicleNumber: String,
    val driverName: String,
    val contractHash: String,
    val createdAtMillis: Long
)
