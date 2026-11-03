package com.sktech.wastetrack.domain.model.invoice

data class BiomassInvoice(
    val id: String,
    val invoiceNumber: String,
    val eWayBillNumber: String,
    val consignorName: String,
    val consignorGstin: String,
    val consigneeName: String,
    val consigneeGstin: String,
    val hsnCode: String,
    val commodity: String,
    val quantityMt: Double,
    val ratePerMt: Double,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val tcsAmount: Double,
    val totalAmount: Double,
    val vehicleNumber: String,
    val mpcbForm10Ref: String,
    val dateMillis: Long = System.currentTimeMillis(),
    val status: String = "PAID & SETTLED"
)
