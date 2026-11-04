package com.sktech.wastetrack.ui.components.tutorial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.sktech.wastetrack.R
import com.sktech.wastetrack.ui.theme.AgriDeepForest
import com.sktech.wastetrack.ui.theme.AgriEarthyGold
import com.sktech.wastetrack.ui.theme.AgriPrimaryGreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TutorialTargetKey {
    const val WEATHER = "target_weather"
    const val SCAN = "target_scan"
    const val CROPS = "target_crops"
    const val MANDI = "target_mandi"
    const val AGENT = "target_agent"
}

data class TutorialStep(
    val key: String,
    val titleRes: Int,
    val descRes: Int,
    val audioRes: Int,
    val icon: ImageVector,
    val accentColor: Color = AgriPrimaryGreen
)

val FARMER_TUTORIAL_STEPS = listOf(
    TutorialStep(
        key = TutorialTargetKey.WEATHER,
        titleRes = R.string.tutorial_step1_title,
        descRes = R.string.tutorial_step1_desc,
        audioRes = R.string.tutorial_step1_audio,
        icon = Icons.Filled.WbSunny,
        accentColor = Color(0xFF4A90D9)
    ),
    TutorialStep(
        key = TutorialTargetKey.SCAN,
        titleRes = R.string.tutorial_step2_title,
        descRes = R.string.tutorial_step2_desc,
        audioRes = R.string.tutorial_step2_audio,
        icon = Icons.Filled.DocumentScanner,
        accentColor = AgriPrimaryGreen
    ),
    TutorialStep(
        key = TutorialTargetKey.CROPS,
        titleRes = R.string.tutorial_step3_title,
        descRes = R.string.tutorial_step3_desc,
        audioRes = R.string.tutorial_step3_audio,
        icon = Icons.Filled.Agriculture,
        accentColor = AgriPrimaryGreen
    ),
    TutorialStep(
        key = TutorialTargetKey.MANDI,
        titleRes = R.string.tutorial_step4_title,
        descRes = R.string.tutorial_step4_desc,
        audioRes = R.string.tutorial_step4_audio,
        icon = Icons.Filled.TrendingUp,
        accentColor = AgriEarthyGold
    ),
    TutorialStep(
        key = TutorialTargetKey.AGENT,
        titleRes = R.string.tutorial_step5_title,
        descRes = R.string.tutorial_step5_desc,
        audioRes = R.string.tutorial_step5_audio,
        icon = Icons.Filled.AutoAwesome,
        accentColor = AgriEarthyGold
    )
)

object TutorialEventBus {
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    fun emit(action: String) {
        _events.tryEmit(action)
    }
}

@Stable
class TutorialState(
    val steps: List<TutorialStep> = FARMER_TUTORIAL_STEPS,
    private val onComplete: () -> Unit = {}
) {
    val targetBounds = mutableStateMapOf<String, Rect>()
    val targetCoordinates = mutableStateMapOf<String, LayoutCoordinates>()

    var currentStepIndex by mutableIntStateOf(0)
        private set

    var isWelcomeDialogVisible by mutableStateOf(false)
    var isOverlayVisible by mutableStateOf(false)

    val currentStep: TutorialStep?
        get() = steps.getOrNull(currentStepIndex)

    val currentTargetRect: Rect?
        get() = currentStep?.let { step ->
            val coords = targetCoordinates[step.key]
            if (coords != null && coords.isAttached) {
                coords.boundsInRoot()
            } else {
                targetBounds[step.key]
            }
        }

    val hasPreviousStep: Boolean
        get() = currentStepIndex > 0

    val isLastStep: Boolean
        get() = currentStepIndex >= steps.size - 1

    fun registerCoordinates(key: String, coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            targetCoordinates[key] = coordinates
            val position = coordinates.positionInRoot()
            val size = coordinates.size.toSize()
            targetBounds[key] = Rect(position, size)
        }
    }

    fun showWelcome() {
        isWelcomeDialogVisible = true
        isOverlayVisible = false
    }

    fun startTour() {
        isWelcomeDialogVisible = false
        currentStepIndex = 0
        isOverlayVisible = true
    }

    fun goToStep(index: Int) {
        if (index in steps.indices) {
            isWelcomeDialogVisible = false
            currentStepIndex = index
            isOverlayVisible = true
        }
    }

    fun nextStep() {
        if (isLastStep) {
            finishTour()
        } else {
            currentStepIndex++
        }
    }

    fun prevStep() {
        if (currentStepIndex > 0) {
            currentStepIndex--
        }
    }

    fun finishTour() {
        isOverlayVisible = false
        isWelcomeDialogVisible = false
        onComplete()
    }

    fun skipTour() {
        isOverlayVisible = false
        isWelcomeDialogVisible = false
        onComplete()
    }
}

@Composable
fun rememberTutorialState(
    steps: List<TutorialStep> = FARMER_TUTORIAL_STEPS,
    onComplete: () -> Unit = {}
): TutorialState {
    return remember(steps) {
        TutorialState(steps = steps, onComplete = onComplete)
    }
}
