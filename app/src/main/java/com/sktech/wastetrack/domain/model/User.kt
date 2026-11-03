package com.sktech.wastetrack.domain.model

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val role: UserRole,
    val organizationName: String = "",
    val factoryId: String = "",
    val industrialArea: String = "",
    val registrationNumber: String = "",
    val isProfileComplete: Boolean = true,
    val languagePreference: String = "en",
    val city: String = "Nashik",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val vehicleNumber: String = "",
    val vehicleType: String = ""
) {

    val effectiveVehicleNumber: String
        get() = vehicleNumber.ifBlank {
            if (role == UserRole.DRIVER) registrationNumber else ""
        }

    val effectiveVehicleType: String
        get() = vehicleType.ifBlank {
            if (role == UserRole.DRIVER) "Big Truck (10/12-Wheeler Heavy Haul)" else ""
        }
}
