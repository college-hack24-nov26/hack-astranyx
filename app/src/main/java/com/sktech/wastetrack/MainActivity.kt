package com.sktech.wastetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sktech.wastetrack.ui.navigation.BottomNavBar
import com.sktech.wastetrack.ui.navigation.NavGraph
import com.sktech.wastetrack.ui.navigation.Screen
import com.sktech.wastetrack.ui.theme.WastetrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WastetrackTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route
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
                        startDestination = Screen.Dashboard.route
                    )
                }
            }
        }
    }
}
