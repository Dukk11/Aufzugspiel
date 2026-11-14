package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.ElevatorGameState
import com.example.viewmodel.ElevatorSpeedMode
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class LandmarkFloor(val floor: Int, val label: String, val emoji: String)

/**
 * Ergonomic Material 3 Expressive bottom elevator console featuring a calibrated tactile rotary dial,
 * tabular floor telemetry, smooth spring CTA transitions, and landmark quick-jump rail.
 */
@Composable
fun ElevatorDialControl(
    state: ElevatorGameState,
    onFloorSelected: (Int) -> Unit,
    onAdjustFloor: (Int) -> Unit,
    onStartRide: () -> Unit,
    onStepIntoMall: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenDoors: () -> Unit = {},
    onCloseDoors: () -> Unit = {},
    onRingBell: () -> Unit = {},
    onOpenFloorMatrix: () -> Unit = {},
    onSpeedModeChange: (ElevatorSpeedMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val targetFloorData = SkyscraperFloorRegistry.getFloor(state.targetFloor)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current
    val lightingAccent = Color(state.lightingColor.primaryHex)

    var dialAngle by remember {
        mutableFloatStateOf((state.targetFloor.toFloat() / MallCatalog.TOTAL_FLOORS) * 360f)
    }

    val animatedProgressSweep by animateFloatAsState(
        targetValue = (state.targetFloor.toFloat() / MallCatalog.TOTAL_FLOORS) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dialSweep"
    )

    val landmarks = remember {
        listOf(
            LandmarkFloor(1, "Lobby", "🍬"),
            LandmarkFloor(10, "Schoko", "🍫"),
            LandmarkFloor(25, "Bausteine", "🧱"),
            LandmarkFloor(42, "Arcade", "🎮"),
            LandmarkFloor(50, "Dinos", "🦖"),
            LandmarkFloor(69, "Skater", "🛹"),
            LandmarkFloor(77, "Roboter", "🤖"),
            LandmarkFloor(100, "Jubiläum", "🎆"),
            LandmarkFloor(120, "Sky Deck", "🚀")
        )
    }

    Surface(
        color = studioColors.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, studioColors.hairlineBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Subtle architectural handle indicator
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 1. TARGET FLOOR TELEMETRY HEADER & UNIFIED EXPRESSIVE CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "ZIEL-ETAGE • ${targetFloorData.altitudeMeters} M",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Etage ${state.targetFloor}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = targetFloorData.emoji, fontSize = 15.sp)
                    }
                    Text(
                        text = targetFloorData.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (state.doorsOpen && !state.isMoving && state.targetFloor == state.currentFloor) {
                    Button(
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onStepIntoMall()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = studioColors.emerald,
                            contentColor = if (studioColors.isDark) Color(0xFF062314) else Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = studioColors.emerald
                            )
                            .testTag("enter_mall_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = "In die Mall",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                } else {
                    val isDifferentFloor = state.targetFloor != state.currentFloor
                    val ctaContainer by animateColorAsState(
                        targetValue = if (isDifferentFloor) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            studioColors.surfaceContainerHighest
                        },
                        label = "ctaBg"
                    )
                    Button(
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onStartRide()
                        },
                        enabled = !state.isMoving && isDifferentFloor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ctaContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = studioColors.surfaceContainerHigh,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("start_ride_button")
                    ) {
                        val icon = when {
                            state.isMoving -> Icons.Default.PlayArrow
                            state.targetFloor > state.currentFloor -> Icons.Default.ArrowUpward
                            else -> Icons.Default.ArrowDownward
                        }
                        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isMoving) "Fährt..." else "Fahrt starten",
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. PRECISION TACTILE ROTARY DIAL & STEPPERS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatorStepperButton(
                        label = "-10",
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onAdjustFloor(-10)
                        },
                        enabled = !state.isMoving && state.targetFloor > 1
                    )
                    ElevatorStepperButton(
                        label = "-1",
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onAdjustFloor(-1)
                        },
                        enabled = !state.isMoving && state.targetFloor > 1
                    )
                }

                // Center Tactile Rotary Dial (134dp)
                val dialTrackBg = studioColors.surfaceContainerLowest
                val dialHubBg = studioColors.surfaceContainerHigh
                val dialAccent = MaterialTheme.colorScheme.primary
                val tickMuted = MaterialTheme.colorScheme.outline

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(134.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(studioColors.surfaceContainerHighest, dialTrackBg)
                            )
                        )
                        .border(1.5.dp, dialAccent.copy(alpha = 0.55f), CircleShape)
                        .pointerInput(state.isMoving, state.hapticsEnabled) {
                            if (state.isMoving) return@pointerInput
                            detectDragGestures { change, _ ->
                                change.consume()
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchOffset = change.position - center
                                var angle = Math.toDegrees(atan2(touchOffset.y.toDouble(), touchOffset.x.toDouble())).toFloat()
                                angle = (angle + 90f + 360f) % 360f

                                dialAngle = angle
                                val mappedFloor = ((angle / 360f) * MallCatalog.TOTAL_FLOORS)
                                    .roundToInt()
                                    .coerceIn(1, MallCatalog.TOTAL_FLOORS)
                                if (mappedFloor != state.targetFloor) {
                                    if (state.hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onFloorSelected(mappedFloor)
                                }
                            }
                        }
                        .testTag("elevator_dial_wheel")
                ) {
                    Canvas(modifier = Modifier.size(134.dp)) {
                        val radius = size.minDimension / 2f
                        val center = size.center
                        val ringInset = 8.dp.toPx()

                        // Subtle background arc track
                        drawArc(
                            color = tickMuted.copy(alpha = 0.25f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset(ringInset, ringInset),
                            size = Size(size.width - ringInset * 2, size.height - ringInset * 2),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Luminous Expressive sweep arc from floor 1 to targetFloor
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(dialAccent, lightingAccent, dialAccent)
                            ),
                            startAngle = -90f,
                            sweepAngle = animatedProgressSweep.coerceAtLeast(4f),
                            useCenter = false,
                            topLeft = Offset(ringInset, ringInset),
                            size = Size(size.width - ringInset * 2, size.height - ringInset * 2),
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // 24 Architectural dial notches
                        val numTicks = 24
                        for (i in 0 until numTicks) {
                            val tickAngle = (i.toFloat() / numTicks) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
                            val isMajor = i % 6 == 0
                            val tickLen = if (isMajor) 8.dp.toPx() else 3.5.dp.toPx()
                            val tickColor = if (isMajor) dialAccent.copy(alpha = 0.9f) else tickMuted.copy(alpha = 0.55f)

                            val outerR = radius - 14.dp.toPx()
                            val innerR = outerR - tickLen
                            drawLine(
                                color = tickColor,
                                start = Offset(center.x + outerR * cos(tickAngle), center.y + outerR * sin(tickAngle)),
                                end = Offset(center.x + innerR * cos(tickAngle), center.y + innerR * sin(tickAngle)),
                                strokeWidth = if (isMajor) 2.2f else 1.2f,
                                cap = StrokeCap.Round
                            )
                        }

                        // Calibrated Needle Dot on the ring
                        val targetAngleRad = (animatedProgressSweep / 360f) * 2f * PI.toFloat() - (PI.toFloat() / 2f)
                        val dotRadius = radius - ringInset
                        val dotPos = Offset(
                            center.x + dotRadius * cos(targetAngleRad),
                            center.y + dotRadius * sin(targetAngleRad)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = dotPos
                        )
                        drawCircle(
                            color = dialAccent,
                            radius = 3.dp.toPx(),
                            center = dotPos
                        )
                    }

                    // Dial Center Hub
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(dialHubBg)
                            .border(1.dp, studioColors.hairlineBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${state.targetFloor}",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "DREHEN",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                                color = dialAccent,
                                maxLines = 1
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatorStepperButton(
                        label = "+1",
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onAdjustFloor(1)
                        },
                        enabled = !state.isMoving && state.targetFloor < MallCatalog.TOTAL_FLOORS
                    )
                    ElevatorStepperButton(
                        label = "+10",
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onAdjustFloor(10)
                        },
                        enabled = !state.isMoving && state.targetFloor < MallCatalog.TOTAL_FLOORS
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. EXPRESSIVE QUICK-JUMP RAIL (1–100 Matrix, Landmarks & Directory)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onOpenFloorMatrix()
                            }
                            .testTag("dial_open_matrix_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔢", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "1–100",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                items(landmarks) { landmark ->
                    val isSelected = state.targetFloor == landmark.floor
                    Surface(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            studioColors.surfaceContainerHigh
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else studioColors.hairlineBorder
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !state.isMoving) {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onFloorSelected(landmark.floor)
                                if (landmark.floor != state.currentFloor) {
                                    onStartRide()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = landmark.emoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${landmark.floor}",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                }

                item {
                    Surface(
                        color = studioColors.surfaceContainerHigh,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, studioColors.hairlineBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !state.isMoving) {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onOpenDirectory()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Alle 120",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ElevatorStepperButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val studioColors = StudioTheme.colors
    Surface(
        color = if (enabled) studioColors.surfaceContainerHigh else studioColors.surfaceContainerLow,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (enabled) studioColors.hairlineBorder else studioColors.hairlineBorder.copy(alpha = 0.4f)
        ),
        tonalElevation = if (enabled) 2.dp else 0.dp,
        modifier = Modifier
            .size(width = 56.dp, height = 40.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = TelemetryNumberStyle.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                color = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                }
            )
        }
    }
}
