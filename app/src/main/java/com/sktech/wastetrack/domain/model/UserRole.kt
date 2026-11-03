package com.sktech.wastetrack.domain.model

import com.sktech.wastetrack.R

enum class UserRole(
    val displayName: String,
    val fullName: String,
    val nameRes: Int,
    val descRes: Int
) {
    FARMER(
        displayName = "Farmer",
        fullName = "Farmer & Agricultural Seller",
        nameRes = R.string.role_farmer,
        descRes = R.string.role_farmer_desc
    ),
    BUYER(
        displayName = "Buyer / Dealer",
        fullName = "Agricultural Waste Buyer & Industry Partner",
        nameRes = R.string.role_buyer,
        descRes = R.string.role_buyer_desc
    ),
    DRIVER(
        displayName = "Transporter",
        fullName = "Logistics Driver & Transporter",
        nameRes = R.string.role_driver,
        descRes = R.string.role_driver_desc
    ),
    ADMIN(
        displayName = "Administrator",
        fullName = "Platform Administrator",
        nameRes = R.string.role_admin,
        descRes = R.string.role_admin_desc
    );

    companion object {

        @Deprecated("Use FARMER instead", replaceWith = ReplaceWith("FARMER"))
        @JvmField
        val SUPERVISOR: UserRole = FARMER

        @Deprecated("Use BUYER instead", replaceWith = ReplaceWith("BUYER"))
        @JvmField
        val RECYCLER: UserRole = BUYER

        fun fromString(role: String?): UserRole {
            if (role.isNullOrBlank()) return FARMER
            return when (role.uppercase().trim()) {
                "FARMER", "SUPERVISOR" -> FARMER
                "BUYER", "DEALER", "RECYCLER" -> BUYER
                "DRIVER", "TRANSPORTER", "LOGISTICS" -> DRIVER
                "ADMIN" -> ADMIN
                else -> FARMER
            }
        }
    }
}
