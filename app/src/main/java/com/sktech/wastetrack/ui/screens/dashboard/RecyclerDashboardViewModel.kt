package com.sktech.wastetrack.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.User
import com.sktech.wastetrack.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvailableBiomassLot(
    val id: String,
    val title: String,
    val category: String,
    val quantityMt: Double,
    val pricePerTonInr: Double,
    val location: String,
    val distanceKm: Double,
    val verified: Boolean = true
)

data class RecyclerDashboardUiState(
    val user: User? = null,
    val totalBiomassProcuredMt: Double = 1420.0,
    val activeBidsCount: Int = 4,
    val incomingDispatchesCount: Int = 3,
    val availableLotsCount: Int = 18,
    val activeLots: List<AvailableBiomassLot> = listOf(
        AvailableBiomassLot("lot_101", "Dry Cotton Stalk Residue (Grade A)", "Cotton", 42.0, 3200.0, "Nashik Rural Hub", 18.5),
        AvailableBiomassLot("lot_102", "High-Calorific Sugarcane Bagasse", "Bagasse", 85.0, 2400.0, "Niphad Bio-Energy Yard", 32.0),
        AvailableBiomassLot("lot_103", "Baled Soybean Husk Straw", "Soybean", 28.0, 2800.0, "Dindori Agri Cluster", 24.0),
        AvailableBiomassLot("lot_104", "Shredded Wheat Field Straw", "Wheat", 60.0, 2100.0, "Sinnar Agro Hub", 41.0)
    ),
    val selectedCategory: String = "All",
    val isLoading: Boolean = false
)

@HiltViewModel
class RecyclerDashboardViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecyclerDashboardUiState())
    val uiState: StateFlow<RecyclerDashboardUiState> = _uiState.asStateFlow()

    init {
        loadRecyclerData()
    }

    private fun loadRecyclerData() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.update { it.copy(user = user) }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
}
