package com.sktech.wastetrack.domain.model

data class WeighbridgeSlip(
    val id: String,
    val slipNumber: String,
    val gatePassNumber: String,
    val vehicleNumber: String,
    val driverName: String,
    val commodityName: String,
    val scaleStationName: String,
    val operatorId: String,
    val grossWeightKg: Double,
    val tareWeightKg: Double,
    val rawNetWeightKg: Double,
    val moisturePercent: Double,
    val moistureDeductionKg: Double,
    val netPayableBiomassKg: Double,
    val allowableMoistureThresholdPercent: Double = 12.0,
    val isMoistureAcceptable: Boolean = true,
    val inboundTimeMillis: Long,
    val outboundTimeMillis: Long,
    val digitalSealHash: String
)
