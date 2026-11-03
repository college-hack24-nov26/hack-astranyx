package com.sktech.wastetrack.domain.model

enum class BidStatus(val displayName: String) {
    OPEN("Open for Bids"),
    CLOSED("Auction Closed"),
    AWARDED("Contract Awarded"),
    EXPIRED("Expired")
}
