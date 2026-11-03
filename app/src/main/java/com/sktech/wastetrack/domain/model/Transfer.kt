package com.sktech.wastetrack.domain.model

enum class TransferStatus(val displayName: String) {
    INITIATED("Dispatched for Pickup"),
    IN_TRANSIT("In Transit"),
    DELIVERED("Arrived at Plant"),
    VERIFIED("Weighbridge Verified")
}

data class TransferItem(
    val id: String,
    val gatePassNumber: String,
    val residueTitle: String,
    val cropName: String,
    val quantityMt: Double,
    val vehicleNumber: String,
    val driverName: String,
    val driverPhone: String,
    val sourceLocation: String,
    val destinationPlant: String,
    val status: TransferStatus,
    val weightAtSourceMt: Double,
    val weighbridgeGrossMt: Double? = null,
    val weighbridgeTareMt: Double? = null,
    val weighbridgeNetMt: Double? = null,
    val dispatchTimestampMillis: Long,
    val qrPayload: String,
    val digitalHashSignature: String
)
