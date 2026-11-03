package com.sktech.wastetrack

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sktech.wastetrack.ui.navigation.BottomNavBar
import com.sktech.wastetrack.ui.navigation.NavGraph
import com.sktech.wastetrack.ui.navigation.Screen
import com.sktech.wastetrack.ui.theme.WastetrackTheme
import com.sktech.wastetrack.util.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WastetrackTheme {
                val context = LocalContext.current
                val startDestination = remember {
                    val prefs = context.getSharedPreferences("agristock_prefs", Context.MODE_PRIVATE)
                    val onboardingCompleted = prefs.getBoolean("onboarding_completed", false)
                    if (!onboardingCompleted) {
                        Screen.Onboarding.route
                    } else {
                        val authPrefs = context.getSharedPreferences("wastetrack_prefs", Context.MODE_PRIVATE)
                        val isLoggedIn = authPrefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
                        if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route
                    }
                }

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: startDestination
                val showBottomBar = currentRoute in listOf(
                    Screen.Dashboard.route,
                    Screen.RecyclerDashboard.route,
                    Screen.DriverDashboard.route,
                    Screen.Crops.route,
                    Screen.Market.route,
                    Screen.Orders.route,
                    Screen.Weather.route,
                    Screen.Analytics.route,
                    Screen.Compliance.route,
                    Screen.Settings.route
                )

                val activeRole = if (currentRoute in listOf(Screen.Orders.route, Screen.Compliance.route, Screen.RecyclerDashboard.route)) {
                    com.sktech.wastetrack.domain.model.UserRole.BUYER
                } else {
                    com.sktech.wastetrack.domain.model.UserRole.FARMER
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                role = activeRole,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        innerPadding = innerPadding,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
