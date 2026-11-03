package com.sktech.wastetrack.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sktech.wastetrack.ui.screens.auth.LoginScreen
import com.sktech.wastetrack.ui.screens.auth.SignUpScreen
import com.sktech.wastetrack.ui.screens.crop.CropClassifyScreen
import com.sktech.wastetrack.ui.screens.dashboard.FarmerDashboardScreen
import com.sktech.wastetrack.ui.screens.marketplace.MarketBrowseScreen
import com.sktech.wastetrack.ui.screens.onboarding.OnboardingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onProfileCompleted = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            FarmerDashboardScreen(
                onNavigateToCrops = {
                    navController.navigate(Screen.CropClassify.route)
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                },
                onNavigateToWeather = {}
            )
        }
        composable(Screen.CropClassify.route) {
            CropClassifyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToMarket = {
                    navController.navigate(Screen.Market.route)
                }
            )
        }
        composable(Screen.Market.route) {
            MarketBrowseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
