package com.sktech.wastetrack.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Crops : Screen("crops")
    data object Market : Screen("market")
    data object Weather : Screen("weather")
    data object Settings : Screen("settings")
}
