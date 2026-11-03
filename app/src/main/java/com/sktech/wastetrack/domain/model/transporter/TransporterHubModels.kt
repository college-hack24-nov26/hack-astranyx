package com.sktech.wastetrack.domain.model.transporter

data class StubbleLoad(
    val id: String,
    val fpoName: String,
    val originHub: String,
    val destinationPlant: String,
    val cropResidue: String,
    val tonnageMt: Double,
    val distanceKm: Int,
    val suggestedFreightRatePerMt: Double,
    val pickupWindow: String,
    val status: String = "AVAILABLE",
    val myBidRatePerMt: Double? = null
) {
    val totalFreightValue: Double
        get() = tonnageMt * (myBidRatePerMt ?: suggestedFreightRatePerMt)
}

data class FleetVehicle(
    val vehicleNumber: String,
    val modelName: String,
    val driverName: String,
    val driverPhone: String,
    val payloadCapacityMt: Double,
    val tareWeightKg: Int,
    val grossVehicleWeightKg: Int,
    val fastagBalance: Double,
    val mpcbPermitNumber: String,
    val mpcbPermitExpiry: String,
    val pucExpiry: String,
    val status: String = "AVAILABLE FOR PICKUP"
)

data class TripProfitLedger(
    val tripId: String,
    val vehicleNumber: String,
    val route: String,
    val cargoDescription: String,
    val tonnageMt: Double,
    val grossFreightRevenue: Double,
    val dieselLiters: Double,
    val dieselCostPerLiter: Double = 92.50,
    val fastagTollCost: Double,
    val driverBattaAllowance: Double,
    val unloadingFee: Double,
    val ePodToken: String,
    val isPodSigned: Boolean,
    val dateText: String
) {
    val dieselTotalCost: Double
        get() = dieselLiters * dieselCostPerLiter

    val totalExpenses: Double
        get() = dieselTotalCost + fastagTollCost + driverBattaAllowance + unloadingFee

    val netProfit: Double
        get() = grossFreightRevenue - totalExpenses

    val profitMarginPercent: Double
        get() = if (grossFreightRevenue > 0) (netProfit / grossFreightRevenue) * 100 else 0.0
}

