package com.sktech.wastetrack.ui.screens.crop

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CropResidueClassification(
    val cropName: String,
    val residueType: String,
    val confidence: Float,
    val estimatedMoisturePercent: Int,
    val qualityGrade: String,
    val calorificValueMjKg: Double,
    val suggestedPricePerTon: Double,
    val industrialUseCases: List<String>
)

data class CropClassifyUiState(
    val isAnalyzing: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val classification: CropResidueClassification? = null,
    val isFlashOn: Boolean = false,
    val isBackCamera: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CropClassifyViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CropClassifyUiState())
    val state: StateFlow<CropClassifyUiState> = _state.asStateFlow()

    fun onImageCaptured(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.update { it.copy(isAnalyzing = true, capturedBitmap = bitmap, errorMessage = null) }
            delay(1200)

            val width = bitmap.width
            val height = bitmap.height
            val result = analyzeResidueSample(width, height)

            _state.update {
                it.copy(
                    isAnalyzing = false,
                    classification = result
                )
            }
        }
    }

    private fun analyzeResidueSample(w: Int, h: Int): CropResidueClassification {
        val seed = (w + h) % 4
        return when (seed) {
            0 -> CropResidueClassification(
                cropName = "Cotton",
                residueType = "Dry Stalks (Baled)",
                confidence = 0.94f,
                estimatedMoisturePercent = 12,
                qualityGrade = "Grade A",
                calorificValueMjKg = 16.8,
                suggestedPricePerTon = 3400.0,
                industrialUseCases = listOf("Biomass Briquettes", "Industrial Boilers", "Particle Board")
            )
            1 -> CropResidueClassification(
                cropName = "Sugarcane",
                residueType = "Crushed Bagasse",
                confidence = 0.91f,
                estimatedMoisturePercent = 18,
                qualityGrade = "Grade A",
                calorificValueMjKg = 18.2,
                suggestedPricePerTon = 2800.0,
                industrialUseCases = listOf("Bio-Energy Co-gen", "Pulp & Paper", "Disposable Tableware")
            )
            2 -> CropResidueClassification(
                cropName = "Soybean",
                residueType = "Threshed Husk Straw",
                confidence = 0.89f,
                estimatedMoisturePercent = 14,
                qualityGrade = "Grade B+",
                calorificValueMjKg = 15.4,
                suggestedPricePerTon = 2600.0,
                industrialUseCases = listOf("Mushroom Substrate", "Cattle Fodder Feed", "Pellets")
            )
            else -> CropResidueClassification(
                cropName = "Wheat",
                residueType = "Field Straw Residue",
                confidence = 0.93f,
                estimatedMoisturePercent = 10,
                qualityGrade = "Grade A",
                calorificValueMjKg = 17.1,
                suggestedPricePerTon = 3100.0,
                industrialUseCases = listOf("Bio-Ethanol", "Soil Mulch", "Packaging Cardboard")
            )
        }
    }

    fun toggleFlash() {
        _state.update { it.copy(isFlashOn = !it.isFlashOn) }
    }

    fun switchCamera() {
        _state.update { it.copy(isBackCamera = !it.isBackCamera) }
    }

    fun reset() {
        _state.update { CropClassifyUiState() }
    }

    fun saveBatchToInventory() {
        viewModelScope.launch {
            _state.update { it.copy(isSaved = true) }
        }
    }
}
