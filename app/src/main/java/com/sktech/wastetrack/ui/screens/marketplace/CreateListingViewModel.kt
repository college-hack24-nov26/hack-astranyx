package com.sktech.wastetrack.ui.screens.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateListingUiState(
    val selectedCrop: String = "Cotton",
    val residueTitle: String = "Dry Cotton Stalk Residue (Baled)",
    val quantityMtText: String = "25.0",
    val moistureGrade: String = "Grade A (<14%)",
    val calorificValueMjKg: Double = 16.8,
    val askingPricePerTonText: String = "3200",
    val benchmarkPricePerTon: Double = 3200.0,
    val storageCondition: String = "Baled & Dry Warehouse",
    val villageAddress: String = "Sinnar Agro Hub, Nashik",
    val additionalNotes: String = "Field accessible by heavy commercial trucks. Ready for immediate loading.",
    val isPublishing: Boolean = false,
    val isPublishedSuccess: Boolean = false,
    val createdListingId: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class CreateListingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CreateListingUiState())
    val uiState: StateFlow<CreateListingUiState> = _uiState.asStateFlow()

    fun updateCrop(crop: String) {
        val (defaultTitle, defaultPrice, defaultCalorific) = when (crop) {
            "Cotton" -> Triple("Dry Cotton Stalk Residue (Baled)", 3200.0, 16.8)
            "Sugarcane" -> Triple("High-Calorific Crushed Bagasse", 2400.0, 18.2)
            "Soybean" -> Triple("Threshed Soybean Husk & Straw", 2700.0, 15.4)
            "Wheat" -> Triple("Shredded Golden Wheat Straw", 2100.0, 17.1)
            "Paddy" -> Triple("Combustible Rice Husk & Straw", 1950.0, 14.9)
            else -> Triple("Agricultural Biomass Residue", 2500.0, 16.0)
        }

        _uiState.update {
            it.copy(
                selectedCrop = crop,
                residueTitle = defaultTitle,
                askingPricePerTonText = defaultPrice.toInt().toString(),
                benchmarkPricePerTon = defaultPrice,
                calorificValueMjKg = defaultCalorific
            )
        }
    }

    fun updateResidueTitle(title: String) {
        _uiState.update { it.copy(residueTitle = title) }
    }

    fun updateQuantity(qty: String) {
        _uiState.update { it.copy(quantityMtText = qty) }
    }

    fun updateMoistureGrade(grade: String) {
        val calorific = when (grade) {
            "Grade A (<14%)" -> 17.5
            "Grade B (15-20%)" -> 15.2
            else -> 13.8
        }
        _uiState.update { it.copy(moistureGrade = grade, calorificValueMjKg = calorific) }
    }

    fun updateAskingPrice(price: String) {
        _uiState.update { it.copy(askingPricePerTonText = price) }
    }

    fun updateStorageCondition(condition: String) {
        _uiState.update { it.copy(storageCondition = condition) }
    }

    fun updateVillageAddress(address: String) {
        _uiState.update { it.copy(villageAddress = address) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(additionalNotes = notes) }
    }

    fun publishListing() {
        val qty = _uiState.value.quantityMtText.toDoubleOrNull()
        val price = _uiState.value.askingPricePerTonText.toDoubleOrNull()

        if (qty == null || qty <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid quantity in metric tons") }
            return
        }

        if (price == null || price <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid asking price per ton") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, errorMessage = null) }
            val newId = "lot_" + UUID.randomUUID().toString().take(8)
            _uiState.update {
                it.copy(
                    isPublishing = false,
                    isPublishedSuccess = true,
                    createdListingId = newId
                )
            }
        }
    }

    fun resetPublishState() {
        _uiState.update { it.copy(isPublishedSuccess = false, createdListingId = null) }
    }
}
