package com.sktech.wastetrack.domain.model

import com.sktech.wastetrack.R

enum class VehicleType(
    val code: String,
    val displayName: String,
    val categoryName: String,
    val typicalCapacityTons: Double,
    val description: String,
    val emoji: String,
    val nameRes: Int,
    val categoryNameRes: Int,
    val descRes: Int
) {
    BIG_TRUCK(
        code = "BIG_TRUCK",
        displayName = "Big Truck (10/12-Wheeler Heavy Haul)",
        categoryName = "Big Truck",
        typicalCapacityTons = 16.0,
        description = "10 - 25 MT · High-capacity bulk biomass transport across highways",
        emoji = "🚛",
        nameRes = R.string.vehicle_big_truck_name,
        categoryNameRes = R.string.vehicle_big_truck_cat,
        descRes = R.string.vehicle_big_truck_desc
    ),
    MEDIUM_TRUCK(
        code = "MEDIUM_TRUCK",
        displayName = "Medium Truck (6-Wheeler / Eicher)",
        categoryName = "Medium Truck",
        typicalCapacityTons = 5.0,
        description = "3 - 7 MT · Regional haulage for moderate farm consignments",
        emoji = "🚚",
        nameRes = R.string.vehicle_med_truck_name,
        categoryNameRes = R.string.vehicle_med_truck_cat,
        descRes = R.string.vehicle_med_truck_desc
    ),
    SMALL_VEHICLE(
        code = "SMALL_VEHICLE",
        displayName = "Small Vehicle (Pickup / Tata Ace)",
        categoryName = "Small Vehicle",
        typicalCapacityTons = 1.5,
        description = "Up to 2 MT · Ideal for narrow rural farm roads & small harvests",
        emoji = "🛻",
        nameRes = R.string.vehicle_small_veh_name,
        categoryNameRes = R.string.vehicle_small_veh_cat,
        descRes = R.string.vehicle_small_veh_desc
    ),
    TRACTOR_TRAILER(
        code = "TRACTOR_TRAILER",
        displayName = "Tractor Trolley / Trailer",
        categoryName = "Tractor Trailer",
        typicalCapacityTons = 4.0,
        description = "3 - 8 MT · Direct field-to-mill agricultural transport",
        emoji = "🚜",
        nameRes = R.string.vehicle_tractor_name,
        categoryNameRes = R.string.vehicle_tractor_cat,
        descRes = R.string.vehicle_tractor_desc
    ),
    TIPPER_TRUCK(
        code = "TIPPER_TRUCK",
        displayName = "Hydraulic Tipper / Dumper",
        categoryName = "Tipper Truck",
        typicalCapacityTons = 12.0,
        description = "8 - 18 MT · Fast unloading for loose husk, bagasse & silage",
        emoji = "🏗️",
        nameRes = R.string.vehicle_tipper_name,
        categoryNameRes = R.string.vehicle_tipper_cat,
        descRes = R.string.vehicle_tipper_desc
    );

    val capacityTons: Double get() = typicalCapacityTons

    companion object {
        val categories: List<VehicleType> get() = entries

        fun fromString(value: String?): VehicleType {
            if (value.isNullOrBlank()) return BIG_TRUCK
            return entries.firstOrNull {
                it.name.equals(value, ignoreCase = true) ||
                it.code.equals(value, ignoreCase = true) ||
                it.displayName.equals(value, ignoreCase = true) ||
                it.categoryName.equals(value, ignoreCase = true)
            } ?: if (value.contains("small", ignoreCase = true) || value.contains("pickup", ignoreCase = true) || value.contains("ace", ignoreCase = true)) {
                SMALL_VEHICLE
            } else if (value.contains("medium", ignoreCase = true) || value.contains("6-wheeler", ignoreCase = true) || value.contains("eicher", ignoreCase = true)) {
                MEDIUM_TRUCK
            } else if (value.contains("tractor", ignoreCase = true)) {
                TRACTOR_TRAILER
            } else if (value.contains("tipper", ignoreCase = true) || value.contains("dumper", ignoreCase = true)) {
                TIPPER_TRUCK
            } else {
                BIG_TRUCK
            }
        }

        fun getAllTypes(): List<VehicleType> = entries
    }
}
