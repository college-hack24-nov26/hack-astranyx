package com.sktech.wastetrack.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object CropClassify : Screen("crop_classify")
    data object Crops : Screen("crops")
    data object CropDetail : Screen("crop_detail/{cycleId}") {
        fun createRoute(cycleId: String) = "crop_detail/$cycleId"
    }
    data object CreateListing : Screen("create_listing")
    data object Market : Screen("market")
    data object Transfers : Screen("transfers")
    data object Weather : Screen("weather")
    data object Settings : Screen("settings")
}
