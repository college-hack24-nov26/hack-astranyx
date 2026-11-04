package com.sktech.wastetrack.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.data.local.TutorialPreferences
import com.sktech.wastetrack.domain.model.User
import com.sktech.wastetrack.domain.repository.IAuthRepository
import com.sktech.wastetrack.util.SpeechManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CropResidueItem(
    val id: String,
    val cropName: String,
    val residueType: String,
    val quantityKg: Double,
    val estimatedValueInr: Double,
    val moisturePercent: Int,
    val status: String
)

data class FarmerDashboardUiState(
    val user: User? = null,
    val activeCropCount: Int = 3,
    val totalPlotAcres: Double = 14.5,
    val totalResidueKg: Double = 8400.0,
    val pendingInquiriesCount: Int = 2,
    val currentTemperature: Double = 28.5,
    val weatherDescription: String = "Clear & Dry",
    val rainProbability: Int = 10,
    val humidityPercent: Int = 58,
    val advisoryHeadline: String = "Optimal Residue Collection Window",
    val advisoryTip: String = "Clear dry conditions across Nashik district. Safe for field harvest collection and residue baling.",
    val recentResidues: List<CropResidueItem> = listOf(
        CropResidueItem("res_1", "Cotton", "Dry Stalks (Baled)", 4200.0, 12600.0, 12, "Ready for Pickup"),
        CropResidueItem("res_2", "Soybean", "Threshed Husk Straw", 2800.0, 7000.0, 14, "Listed in Market"),
        CropResidueItem("res_3", "Wheat", "Field Straw Residue", 1400.0, 3500.0, 10, "Logged")
    ),
    val isLoading: Boolean = false,
    val isTutorialCompleted: Boolean = false
)

@HiltViewModel
class FarmerDashboardViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    val tutorialPreferences: TutorialPreferences,
    val speechManager: SpeechManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FarmerDashboardUiState())
    val uiState: StateFlow<FarmerDashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            val uid = user?.id ?: ""
            val completed = tutorialPreferences.isTutorialCompleted(uid)
            _uiState.update { it.copy(user = user, isTutorialCompleted = completed) }
        }
    }

    fun setTutorialCompleted(completed: Boolean) {
        val uid = _uiState.value.user?.id ?: ""
        tutorialPreferences.setTutorialCompleted(completed, uid)
        _uiState.update { it.copy(isTutorialCompleted = completed) }
    }

    fun restartTutorial() {
        setTutorialCompleted(false)
    }
}
