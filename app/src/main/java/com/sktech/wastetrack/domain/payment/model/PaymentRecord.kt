package com.sktech.wastetrack.domain.payment.model

enum class PaymentMethod(
    val title: String,
    val subtitle: String,
    val badge: String
) {
    UPI_INTENT("UPI App Intent", "Instant settlement via PhonePe, GPay, Paytm", "INSTANT"),
    UPI_QR("Dynamic UPI QR Code", "Scan with any BHIM / UPI banking app", "SCAN & PAY"),
    RTGS_NEFT("Corporate RTGS / IMPS", "Direct corporate virtual escrow account transfer", "DIRECT BANK"),
    LETTER_OF_CREDIT("Corporate Credit Line / LC", "30-Day verified industrial purchase order", "CREDIT LINE"),
    ESCROW_WALLET("Smart Escrow Vault", "Automated contract lock released upon gatepass", "ESCROW VAULT")
}

enum class PaymentStatus(val label: String) {
    ESCROW_LOCKED("Escrow Protected"),
    SETTLED("Settled to Farmer"),
    DIRECT_PAID("Direct Paid"),
    PENDING("Verification Pending")
}

data class PaymentRecord(
    val id: String,
    val orderId: String,
    val lotTitle: String,
    val cropName: String,
    val farmerName: String,
    val farmerPhone: String,
    val buyerName: String,
    val buyerGstin: String,
    val weightMt: Double,
    val ratePerMt: Double,
    val materialCost: Double,
    val gstAmount: Double,
    val tcsAmount: Double,
    val freightAmount: Double,
    val transportGst: Double,
    val handlingCharges: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val transactionReference: String,
    val upiId: String = "agristock.escrow@upi",
    val escrowVaultId: String,
    val createdAtMillis: Long,
    val escrowReleasedAtMillis: Long? = null
)

data class PaymentLedgerSummary(
    val totalSettledAmount: Double,
    val totalEscrowLockedAmount: Double,
    val totalFreightAmount: Double,
    val totalTaxCollected: Double
)

