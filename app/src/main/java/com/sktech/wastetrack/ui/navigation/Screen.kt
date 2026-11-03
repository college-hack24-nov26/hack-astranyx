package com.sktech.wastetrack.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object RecyclerDashboard : Screen("recycler_dashboard")
    data object CropClassify : Screen("crop_classify")
    data object Crops : Screen("crops")
    data object CropDetail : Screen("crop_detail/{cycleId}") {
        fun createRoute(cycleId: String) = "crop_detail/$cycleId"
    }
    data object CreateListing : Screen("create_listing")
    data object Market : Screen("market")
    data object Bids : Screen("bids")
    data object Orders : Screen("orders")
    data object Transfers : Screen("transfers")
    data object DriverDashboard : Screen("driver_dashboard")
    data object ShipmentTracking : Screen("shipment_tracking/{transferId}") {
        fun createRoute(transferId: String) = "shipment_tracking/$transferId"
    }
    data object Weather : Screen("weather")
    data object Analytics : Screen("analytics")
    data object Compliance : Screen("compliance")
    data object Settings : Screen("settings")
}
