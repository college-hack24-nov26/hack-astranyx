package com.sktech.wastetrack.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["phone"], unique = true),
        Index(value = ["role"]),
        Index(value = ["role", "city"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val role: String = "SUPERVISOR",
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
    @get:androidx.room.Ignore
    val effectiveVehicleNumber: String
        get() = vehicleNumber.ifBlank { registrationNumber }

    @get:androidx.room.Ignore
    val effectiveVehicleType: String
        get() = vehicleType.ifBlank { "Big Truck (10/12-Wheeler Heavy Haul)" }
}
