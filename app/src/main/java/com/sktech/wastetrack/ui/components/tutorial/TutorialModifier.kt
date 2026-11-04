package com.sktech.wastetrack.ui.components.tutorial

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned

fun Modifier.tutorialTarget(
    key: String,
    state: TutorialState
): Modifier = this.onGloballyPositioned { coordinates ->
    state.registerCoordinates(key, coordinates)
}
