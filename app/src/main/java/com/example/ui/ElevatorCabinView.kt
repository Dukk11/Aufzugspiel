package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MallCatalog
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.cabin.CabinAudioSettingsOverlay
import com.example.ui.cabin.CabinButtonWallPerspective
import com.example.ui.cabin.CabinCustomizerOverlay
import com.example.ui.cabin.CabinDoorsPerspective
import com.example.ui.cabin.CabinGlassOutsidePerspective
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.CabinAudioChannel
import com.example.viewmodel.CabinAudioPreset
import com.example.viewmodel.CabinLightingColor
import com.example.viewmodel.CabinViewAngle
import com.example.viewmodel.CabinWallMaterial
import com.example.viewmodel.ElevatorDirection
import com.example.viewmodel.ElevatorGameState
import com.example.viewmodel.ElevatorSpeedMode
import kotlin.math.abs
import kotlin.math.sin

/**
 * Interactive 360° Material 3 Expressive Elevator Cabin View.
 * Coordinates the architectural ceiling lantern, perspective switcher, spring-eased door animations,
 * ambient ceiling lighting wash, and modularized cabin perspectives.
 */
@Composable
fun ElevatorCabinView(
    state: ElevatorGameState,
    modifier: Modifier = Modifier,
    onRingBell: (() -> Unit)? = null,
    onFloorButtonClick: (() -> Unit)? = null,
    onToggleMusic: (() -> Unit)? = null,
    onSelectViewAngle: (CabinViewAngle) -> Unit = {},
    onCycleViewAngle: (Int) -> Unit = {},
    onTravelToFloor: (Int) -> Unit = {},
    onSpeedModeChange: (ElevatorSpeedMode) -> Unit = {},
    onStepIntoMall: () -> Unit = {},
    onOpenDoors: () -> Unit = {},
    onCloseDoors: () -> Unit = {},
    onToggleAudioPanel: () -> Unit = {},
    onHumVolumeChange: (Float, Boolean) -> Unit = { _, _ -> },
    onChimeVolumeChange: (Float, Boolean) -> Unit = { _, _ -> },
    onMallAmbienceVolumeChange: (Float, Boolean) -> Unit = { _, _ -> },
    onApplyAudioPreset: (CabinAudioPreset) -> Unit = {},
    onPreviewAudioChannel: (CabinAudioChannel) -> Unit = {},
    onToggleCabinCustomizer: () -> Unit = {},
    onSelectWallMaterial: (CabinWallMaterial) -> Unit = {},
    onSelectLightingColor: (CabinLightingColor) -> Unit = {}
) {
    val currentFloorData = SkyscraperFloorRegistry.getFloor(state.currentFloor)
    val zoneImageRes = MallCatalog.getZoneDrawableRes(state.currentFloor)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    val doorProgress by animateFloatAsState(
        targetValue = if (state.doorsOpen && !state.isMoving) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "doorAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "cabinMotion")

    val cabinVibrationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(tween(180, easing = LinearEasing)),
        label = "cabinVibration"
    )
    val cabinJitterY = if (state.isMoving) (sin(cabinVibrationTime) * 0.7f).dp else 0.dp

    val arrowBlink by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "arrowBlink"
    )

    val rawRideProgress = if (state.targetFloor != state.rideStartFloor) {
        (abs(state.currentFloor - state.rideStartFloor).toFloat() /
            abs(state.targetFloor - state.rideStartFloor)).coerceIn(0f, 1f)
    } else {
        1f
    }
    val animatedRideProgress by animateFloatAsState(
        targetValue = if (state.isMoving) rawRideProgress else 0f,
        animationSpec = tween(durationMillis = 260, easing = LinearEasing),
        label = "animatedRideProgress"
    )

    val modelAccent = Color(state.lightingColor.primaryHex)
    val modelWallDark = Color(state.wallMaterial.wallDark)
    val modelWallLight = Color(state.wallMaterial.wallLight)
    val modelDoorStart = Color(state.wallMaterial.doorStart)
    val modelDoorEnd = Color(state.wallMaterial.doorEnd)

    val cabinCornerRadius = if (state.isMoving) 0.dp else 26.dp
    val topHeaderHeight = if (state.isMoving) 98.dp else 90.dp

    var horizontalDragAccum by remember { mutableFloatStateOf(0f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = cabinJitterY)
            .clip(RoundedCornerShape(cabinCornerRadius))
            .background(studioColors.surfaceContainerLowest)
            .border(
                width = if (state.isMoving) 0.dp else 1.dp,
                color = if (state.isMoving) Color.Transparent else studioColors.hairlineBorder,
                shape = RoundedCornerShape(cabinCornerRadius)
            )
            .pointerInput(state.hapticsEnabled) {
                detectHorizontalDragGestures(
                    onDragStart = { horizontalDragAccum = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        horizontalDragAccum += dragAmount
                    },
                    onDragEnd = {
                        if (horizontalDragAccum > 75f) {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onCycleViewAngle(-1)
                        } else if (horizontalDragAccum < -75f) {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onCycleViewAngle(1)
                        }
                        horizontalDragAccum = 0f
                    }
                )
            }
            .testTag("elevator_cabin_view"),
        contentAlignment = Alignment.Center
    ) {
        val totalWidth = maxWidth

        // ══════════════════════════════════════════════════════════════
        // MAIN PERSPECTIVE STAGE
        // ══════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topHeaderHeight, bottom = 20.dp)
        ) {
            AnimatedContent(
                targetState = state.cabinViewAngle,
                transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(240)) },
                label = "cabinPerspectiveSwitch"
            ) { viewAngle ->
                when (viewAngle) {
                    CabinViewAngle.DOORS -> {
                        CabinDoorsPerspective(
                            state = state,
                            totalWidth = totalWidth,
                            doorProgress = doorProgress,
                            zoneImageRes = zoneImageRes,
                            modelAccent = modelAccent,
                            modelWallDark = modelWallDark,
                            modelWallLight = modelWallLight,
                            modelDoorStart = modelDoorStart,
                            modelDoorEnd = modelDoorEnd,
                            onRingBell = onRingBell,
                            onToggleMusic = onToggleMusic,
                            onSelectViewAngle = onSelectViewAngle,
                            onStepIntoMall = onStepIntoMall,
                            onToggleAudioPanel = onToggleAudioPanel,
                            onToggleCabinCustomizer = onToggleCabinCustomizer
                        )
                    }

                    CabinViewAngle.BUTTON_WALL -> {
                        CabinButtonWallPerspective(
                            state = state,
                            modelAccent = modelAccent,
                            modelWallDark = modelWallDark,
                            modelWallLight = modelWallLight,
                            onTravelToFloor = onTravelToFloor,
                            onSpeedModeChange = onSpeedModeChange,
                            onOpenDoors = onOpenDoors,
                            onCloseDoors = onCloseDoors,
                            onRingBell = { onRingBell?.invoke() },
                            onToggleMusic = { onToggleMusic?.invoke() },
                            onExpandMatrix = { onFloorButtonClick?.invoke() },
                            onToggleAudioPanel = onToggleAudioPanel,
                            onPreviewAudioChannel = onPreviewAudioChannel,
                            onToggleCabinCustomizer = onToggleCabinCustomizer
                        )
                    }

                    CabinViewAngle.GLASS_OUTSIDE -> {
                        CabinGlassOutsidePerspective(
                            state = state,
                            modelAccent = modelAccent,
                            onSpeedModeChange = onSpeedModeChange
                        )
                    }
                }
            }

            // Ambient subtle ceiling light beam wash matching chosen CabinLightingColor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                modelAccent.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Dedicated In-Cabin Audio Mixer Overlay
            AnimatedVisibility(
                visible = state.showCabinAudioPanel,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(tween(220)),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(tween(180)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                CabinAudioSettingsOverlay(
                    state = state,
                    onHumVolumeChange = onHumVolumeChange,
                    onChimeVolumeChange = onChimeVolumeChange,
                    onMallAmbienceVolumeChange = onMallAmbienceVolumeChange,
                    onApplyAudioPreset = onApplyAudioPreset,
                    onPreviewAudioChannel = onPreviewAudioChannel,
                    onDismiss = onToggleAudioPanel
                )
            }

            // Dedicated In-Cabin Studio Material & Lighting Overlay
            AnimatedVisibility(
                visible = state.showCabinCustomizer,
                enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(tween(220)),
                exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(tween(180)),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                CabinCustomizerOverlay(
                    state = state,
                    onSelectWallMaterial = onSelectWallMaterial,
                    onSelectLightingColor = onSelectLightingColor,
                    onDismiss = onToggleCabinCustomizer
                )
            }
        }

        // ══════════════════════════════════════════════════════════════
        // ARCHITECTURAL CEILING HEADER: LANTERN + 3-WAY M3 SWITCHER
        // ══════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(topHeaderHeight)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            studioColors.surfaceContainerLowest,
                            studioColors.surfaceContainerLow,
                            studioColors.surfaceContainer
                        )
                    )
                )
                .border(0.5.dp, studioColors.hairlineBorder)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Overhead Floor & Speed Digital Lantern Capsule
            Surface(
                color = studioColors.surfaceContainerLowest,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(0.6.dp, studioColors.hairlineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            when (state.direction) {
                                ElevatorDirection.UP -> {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = arrowBlink),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                ElevatorDirection.DOWN -> {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = studioColors.gold.copy(alpha = arrowBlink),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                ElevatorDirection.IDLE -> {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (state.doorsOpen) studioColors.emerald else studioColors.gold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = "E${state.currentFloor.toString().padStart(3, '0')}",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = studioColors.gold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${currentFloorData.emoji} ${currentFloorData.name}",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (state.isMoving) {
                                Text(
                                    text = "${state.speedKmh.toInt()} km/h",
                                    style = TelemetryNumberStyle.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = studioColors.sky
                                )
                            } else if (state.doorsOpen) {
                                Surface(
                                    color = studioColors.emerald.copy(alpha = 0.14f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, studioColors.emerald.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "OFFEN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                        color = studioColors.emerald,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = state.speedMode.shortLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(5.dp))

                            Surface(
                                color = if (state.showCabinCustomizer) modelAccent else studioColors.surfaceContainerHigh,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (state.showCabinCustomizer) Color.White else modelAccent.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleCabinCustomizer() }
                                    .testTag("lantern_cabin_customizer_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🎨", fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Design",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                        color = if (state.showCabinCustomizer) Color(0xFF090D16) else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Surface(
                                color = if (state.showCabinAudioPanel) MaterialTheme.colorScheme.primary else studioColors.surfaceContainerHigh,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (state.showCabinAudioPanel) MaterialTheme.colorScheme.primary else studioColors.hairlineBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleAudioPanel() }
                                    .testTag("lantern_audio_mixer_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "🎚️", fontSize = 9.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Klang",
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                                        color = if (state.showCabinAudioPanel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    if (state.isMoving) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("elevator_ride_progress_indicator")
                        ) {
                            Text(
                                text = "ZIEL E${state.targetFloor}",
                                style = TelemetryNumberStyle.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                                color = studioColors.sky
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(studioColors.surfaceContainerHigh)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = animatedRideProgress)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(studioColors.gold, MaterialTheme.colorScheme.primary)
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${(animatedRideProgress * 100).toInt()}%",
                                style = TelemetryNumberStyle.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                                color = studioColors.gold
                            )
                        }
                    }
                }
            }

            // 3-Way M3 Expressive Perspective Segmented Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(studioColors.surfaceContainerLowest)
                    .border(0.6.dp, studioColors.hairlineBorder, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CabinViewAngle.entries.forEach { angle ->
                    val isSelected = state.cabinViewAngle == angle
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(9.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .clickable {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSelectViewAngle(angle)
                            }
                            .testTag("perspective_tab_${angle.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = angle.emoji, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = angle.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════════
        // BOTTOM CABIN THRESHOLD SILL (Fahrkorbschwelle)
        // ══════════════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(11.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(state.wallMaterial.trimColor).copy(alpha = 0.6f),
                                Color(state.wallMaterial.wallDark)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = Color(0xFF04070D),
                        start = Offset(0f, size.height * 0.48f),
                        end = Offset(size.width, size.height * 0.48f),
                        strokeWidth = 2f
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .background(studioColors.surfaceContainerLowest)
            )
        }
    }
}
