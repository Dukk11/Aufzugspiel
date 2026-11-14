package com.example.ui.cabin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.CabinAudioChannel
import com.example.viewmodel.ElevatorGameState
import com.example.viewmodel.ElevatorSpeedMode
import kotlin.math.roundToInt

/**
 * PERSPECTIVE 2: Looking directly at the Tactile Elevator Button Wall (Knöpfewand / COP).
 * Combines recessed tactile round floor buttons, precision telemetry typography,
 * calm speed selector, and compact hardware controls.
 */
@Composable
fun CabinButtonWallPerspective(
    state: ElevatorGameState,
    modelAccent: Color,
    modelWallDark: Color,
    modelWallLight: Color,
    onTravelToFloor: (Int) -> Unit,
    onSpeedModeChange: (ElevatorSpeedMode) -> Unit,
    onOpenDoors: () -> Unit,
    onCloseDoors: () -> Unit,
    onRingBell: () -> Unit,
    onToggleMusic: () -> Unit,
    onExpandMatrix: () -> Unit,
    onToggleAudioPanel: () -> Unit,
    onPreviewAudioChannel: (CabinAudioChannel) -> Unit,
    onToggleCabinCustomizer: () -> Unit
) {
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    var selectedDecadeIndex by remember {
        mutableIntStateOf(((state.currentFloor - 1) / 20).coerceIn(0, 5))
    }
    val decadeRanges = remember {
        listOf(
            "1–20" to (1..20),
            "21–40" to (21..40),
            "41–60" to (41..60),
            "61–80" to (61..80),
            "81–100" to (81..100),
            "101–120" to (101..120)
        )
    }
    val activeRange = decadeRanges[selectedDecadeIndex].second
    val floorsInBank = remember(selectedDecadeIndex) {
        SkyscraperFloorRegistry.getAllFloors().filter { it.floorNumber in activeRange }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    listOf(modelWallDark, modelWallLight, modelWallDark)
                )
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Speed Control Strip
        Surface(
            color = studioColors.surfaceContainer.copy(alpha = 0.94f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(0.8.dp, studioColors.hairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ FAHRSTUHL-GESCHWINDIGKEIT",
                        style = MaterialTheme.typography.labelSmall,
                        color = studioColors.sky
                    )
                    Text(
                        text = "Max. ${state.speedMode.topSpeedKmh} km/h",
                        style = TelemetryNumberStyle.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = studioColors.gold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    ElevatorSpeedMode.entries.forEach { mode ->
                        val isSelected = state.speedMode == mode
                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else studioColors.surfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                0.8.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else studioColors.hairlineBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (state.hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onSpeedModeChange(mode)
                                }
                                .testTag("speed_mode_btn_${mode.name.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.shortLabel,
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Floor Bank Selector Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(decadeRanges.size) { idx ->
                val (label, _) = decadeRanges[idx]
                val isSelected = selectedDecadeIndex == idx
                Surface(
                    color = if (isSelected) modelAccent else studioColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        0.8.dp,
                        if (isSelected) Color.White.copy(alpha = 0.7f) else studioColors.hairlineBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            selectedDecadeIndex = idx
                        }
                ) {
                    Text(
                        text = "Etagen $label",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color(0xFF090D16) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Tactile Recessed Circular Floor Buttons Grid
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(studioColors.surfaceContainerLowest.copy(alpha = 0.94f))
                .border(1.dp, studioColors.hairlineBorder, RoundedCornerShape(20.dp))
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(2.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(floorsInBank, key = { it.floorNumber }) { floor ->
                    val isCurrent = floor.floorNumber == state.currentFloor
                    val isTarget = floor.floorNumber == state.targetFloor
                    val isVisited = floor.floorNumber in state.visitedFloors

                    val ringColor = when {
                        isTarget -> modelAccent
                        isCurrent -> studioColors.emerald
                        floor.isLandmark -> studioColors.gold
                        isVisited -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        else -> studioColors.hairlineBorder
                    }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        when {
                                            isTarget -> MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)
                                            isCurrent -> studioColors.emerald.copy(alpha = 0.28f)
                                            else -> studioColors.surfaceContainerHigh
                                        },
                                        studioColors.surfaceContainerLowest
                                    )
                                )
                            )
                            .border(
                                width = if (isTarget || isCurrent) 2.dp else 1.dp,
                                color = ringColor,
                                shape = CircleShape
                            )
                            .clickable(enabled = !state.isMoving) {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onTravelToFloor(floor.floorNumber)
                            }
                            .testTag("cop_wall_floor_${floor.floorNumber}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${floor.floorNumber}",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = floor.emoji,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Compact Cabin Acoustic Strip
        Surface(
            color = studioColors.surfaceContainer.copy(alpha = 0.94f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.8.dp, studioColors.hairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CabinQuickAudioChip(
                        emoji = "〰️",
                        label = "Summen ${(state.humVolume * 100).roundToInt()}%",
                        active = state.humVolume > 0.02f,
                        onClick = { onPreviewAudioChannel(CabinAudioChannel.HUM) },
                        modifier = Modifier.weight(1f)
                    )
                    CabinQuickAudioChip(
                        emoji = "🔔",
                        label = "Gong ${(state.chimeVolume * 100).roundToInt()}%",
                        active = state.chimeVolume > 0.02f,
                        onClick = { onPreviewAudioChannel(CabinAudioChannel.CHIME) },
                        modifier = Modifier.weight(1f)
                    )
                    CabinQuickAudioChip(
                        emoji = "🌊",
                        label = "Mall ${(state.mallAmbienceVolume * 100).roundToInt()}%",
                        active = state.mallAmbienceVolume > 0.02f,
                        onClick = { onPreviewAudioChannel(CabinAudioChannel.MALL_AMBIENCE) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = if (state.showCabinAudioPanel) MaterialTheme.colorScheme.primary else studioColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, studioColors.sky.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onToggleAudioPanel()
                        }
                        .testTag("button_wall_audio_mixer_trigger")
                ) {
                    Text(
                        text = "🎚️ Regler",
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        color = if (state.showCabinAudioPanel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5. Operational Circular Hardware Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CabinRoundHardwareButton(
                symbol = "◄||►",
                label = "Tür auf",
                ringColor = if (state.doorsOpen) studioColors.emerald else MaterialTheme.colorScheme.outline,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenDoors()
                }
            )
            CabinRoundHardwareButton(
                symbol = "►||◄",
                label = "Tür zu",
                ringColor = if (!state.doorsOpen) studioColors.gold else MaterialTheme.colorScheme.outline,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCloseDoors()
                }
            )
            CabinRoundHardwareButton(
                symbol = "🎨",
                label = "Design",
                ringColor = if (state.showCabinCustomizer) Color.White else modelAccent,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleCabinCustomizer()
                }
            )
            CabinRoundHardwareButton(
                symbol = "🎚️",
                label = "Klang",
                ringColor = if (state.showCabinAudioPanel) MaterialTheme.colorScheme.primary else studioColors.sky,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleAudioPanel()
                }
            )
            CabinRoundHardwareButton(
                symbol = "🔔",
                label = "Notruf",
                ringColor = studioColors.gold,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRingBell()
                }
            )
            CabinRoundHardwareButton(
                symbol = "🎵",
                label = "Musik",
                ringColor = if (state.isMusicPlaying) studioColors.emerald else studioColors.sky,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleMusic()
                }
            )
            CabinRoundHardwareButton(
                symbol = "📋",
                label = "Alle 120",
                ringColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onExpandMatrix()
                }
            )
        }
    }
}

@Composable
private fun CabinQuickAudioChip(
    emoji: String,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studioColors = StudioTheme.colors
    Surface(
        color = if (active) studioColors.surfaceContainerHigh else studioColors.surfaceContainerLowest,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            0.8.dp,
            if (active) studioColors.sky.copy(alpha = 0.45f) else studioColors.hairlineBorder
        ),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 9.5.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                color = if (active) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CabinRoundHardwareButton(
    symbol: String,
    label: String,
    ringColor: Color,
    onClick: () -> Unit
) {
    val studioColors = StudioTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(studioColors.surfaceContainerLowest)
                .border(1.5.dp, ringColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
