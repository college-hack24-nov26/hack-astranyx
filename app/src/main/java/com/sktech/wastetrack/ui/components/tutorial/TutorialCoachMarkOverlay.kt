package com.sktech.wastetrack.ui.components.tutorial

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.R
import com.sktech.wastetrack.domain.agent.AgentLanguage
import com.sktech.wastetrack.ui.theme.*
import com.sktech.wastetrack.util.LocaleHelper
import com.sktech.wastetrack.util.SpeechManager
import kotlin.math.roundToInt

@Composable
fun TutorialCoachMarkOverlay(
    tutorialState: TutorialState,
    speechManager: SpeechManager? = null,
    listState: LazyListState? = null,
    modifier: Modifier = Modifier
) {
    if (!tutorialState.isOverlayVisible) return

    val currentStep = tutorialState.currentStep ?: return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }

    val currentLang by LocaleHelper.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by speechManager?.isSpeaking?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    val agentLang = remember(currentLang) {
        when (currentLang.lowercase()) {
            "mr" -> AgentLanguage.MARATHI
            "hi" -> AgentLanguage.HINDI
            else -> AgentLanguage.ENGLISH
        }
    }

    val audioPromptText = stringResource(currentStep.audioRes)


    DisposableEffect(currentStep.key) {
        onDispose {
            speechManager?.stopSpeaking()
        }
    }


    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_progress"
    )

    val targetPadding = with(density) { 8.dp.toPx() }
    val defaultCornerRadius = with(density) { 16.dp.toPx() }

    var overlayCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var tooltipHeightPx by remember { mutableFloatStateOf(with(density) { 220.dp.toPx() }) }


    val scrollOffset = listState?.firstVisibleItemScrollOffset ?: 0
    val scrollIndex = listState?.firstVisibleItemIndex ?: 0

    val currentTargetCoords = tutorialState.targetCoordinates[currentStep.key]

    val targetRect: Rect? = remember(currentTargetCoords, overlayCoordinates, currentStep.key, scrollOffset, scrollIndex) {
        if (currentTargetCoords != null && currentTargetCoords.isAttached) {
            if (overlayCoordinates != null && overlayCoordinates!!.isAttached) {
                overlayCoordinates!!.localBoundingBoxOf(currentTargetCoords, clipBounds = false)
            } else {
                currentTargetCoords.boundsInRoot()
            }
        } else {
            tutorialState.targetBounds[currentStep.key]
        }
    }

    val animLeft by animateFloatAsState(
        targetValue = targetRect?.left ?: (screenWidthPx * 0.1f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "spot_left"
    )
    val animTop by animateFloatAsState(
        targetValue = targetRect?.top ?: (screenHeightPx * 0.3f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "spot_top"
    )
    val animRight by animateFloatAsState(
        targetValue = targetRect?.right ?: (screenWidthPx * 0.9f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "spot_right"
    )
    val animBottom by animateFloatAsState(
        targetValue = targetRect?.bottom ?: (screenHeightPx * 0.5f),
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        label = "spot_bottom"
    )

    val isCircular = currentStep.key == TutorialTargetKey.AGENT

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayCoordinates = it }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {

            }
    ) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.99f)
        ) {

            drawRect(Color.Black.copy(alpha = 0.76f))

            if (animRight > animLeft && animBottom > animTop && targetRect != null) {
                val paddedRect = Rect(
                    left = (animLeft - targetPadding).coerceAtLeast(4f),
                    top = (animTop - targetPadding).coerceAtLeast(4f),
                    right = (animRight + targetPadding).coerceAtMost(size.width - 4f),
                    bottom = (animBottom + targetPadding).coerceAtMost(size.height - 4f)
                )

                val cr = if (isCircular) {
                    CornerRadius(paddedRect.width / 2f, paddedRect.height / 2f)
                } else {
                    CornerRadius(defaultCornerRadius, defaultCornerRadius)
                }


                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = paddedRect.topLeft,
                    size = paddedRect.size,
                    cornerRadius = cr,
                    blendMode = BlendMode.Clear
                )


                val strokeWidth = 2.5f + (pulseProgress * 2.5f)
                val strokeAlpha = 0.5f + (pulseProgress * 0.5f)
                drawRoundRect(
                    color = currentStep.accentColor.copy(alpha = strokeAlpha),
                    topLeft = paddedRect.topLeft,
                    size = paddedRect.size,
                    cornerRadius = cr,
                    style = Stroke(width = strokeWidth)
                )
            }
        }


        val currentCenterY = (animTop + animBottom) / 2f
        val isTargetInBottomHalf = if (targetRect != null) {
            currentCenterY > (screenHeightPx * 0.44f)
        } else {
            false
        }

        val topInset = with(density) { 48.dp.toPx() }
        val bottomInset = with(density) { 56.dp.toPx() }

        val cardYOffset = if (targetRect == null) {
            ((screenHeightPx - tooltipHeightPx) / 2f).roundToInt()
        } else if (isTargetInBottomHalf) {

            (animTop - targetPadding - tooltipHeightPx - with(density) { 14.dp.toPx() })
                .roundToInt()
                .coerceAtLeast(topInset.roundToInt())
        } else {

            (animBottom + targetPadding + with(density) { 14.dp.toPx() })
                .roundToInt()
                .coerceAtMost((screenHeightPx - tooltipHeightPx - bottomInset).roundToInt())
        }

        val animatedCardY by animateIntAsState(
            targetValue = cardYOffset,
            animationSpec = tween(350, easing = FastOutSlowInEasing),
            label = "card_y"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(x = 0, y = animatedCardY) }
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.94f, animationSpec = tween(220)))
                        .togetherWith(fadeOut(animationSpec = tween(150)))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .onGloballyPositioned { coords ->
                        tooltipHeightPx = coords.size.height.toFloat()
                    },
                label = "step_tooltip"
            ) { step ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 14.dp,
                    border = BorderStroke(1.2.dp, step.accentColor.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = step.accentColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.tutorial_step_counter,
                                        tutorialState.currentStepIndex + 1,
                                        tutorialState.steps.size
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = step.accentColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                IconButton(
                                    onClick = {
                                        if (isSpeaking) {
                                            speechManager?.stopSpeaking()
                                        } else {
                                            speechManager?.speak(audioPromptText, agentLang)
                                        }
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Outlined.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isSpeaking) "Mute narration" else "Listen narration",
                                        tint = if (isSpeaking) SafetyOrange else AgriPrimaryGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }


                                TextButton(
                                    onClick = {
                                        speechManager?.stopSpeaking()
                                        tutorialState.skipTour()
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.tutorial_skip),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(step.accentColor.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = null,
                                    tint = step.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = stringResource(step.titleRes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))


                        Text(
                            text = stringResource(step.descRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (tutorialState.hasPreviousStep) {
                                OutlinedButton(
                                    onClick = {
                                        speechManager?.stopSpeaking()
                                        tutorialState.prevStep()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.tutorial_previous),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = {
                                    speechManager?.stopSpeaking()
                                    tutorialState.nextStep()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = step.accentColor,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        if (tutorialState.isLastStep) R.string.tutorial_finish else R.string.tutorial_next
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (tutorialState.isLastStep) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
