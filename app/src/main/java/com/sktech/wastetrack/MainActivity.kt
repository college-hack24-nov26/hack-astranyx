package com.sktech.wastetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sktech.wastetrack.ui.screens.auth.LoginScreen
import com.sktech.wastetrack.ui.theme.WastetrackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WastetrackTheme {
                LoginScreen(
                    onLoginSuccess = {},
                    onNavigateToSignUp = {}
                )
            }
        }
    }
}
