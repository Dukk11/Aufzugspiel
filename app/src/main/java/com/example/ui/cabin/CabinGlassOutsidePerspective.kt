package com.example.ui.cabin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MallCatalog
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.ElevatorDirection
import com.example.viewmodel.ElevatorGameState
import com.example.viewmodel.ElevatorSpeedMode

/**
 * PERSPECTIVE 3: Floor-to-Ceiling Panoramic Glass Horizon View (Glas-Ausblick).
 * Renders atmospheric altitude sky zones, multi-layer parallax skyscrapers, soft cloud layers,
 * stratosphere twilight stars, glass reflection sheen, and architectural handrail bar.
 */
@Composable
fun CabinGlassOutsidePerspective(
    state: ElevatorGameState,
    modelAccent: Color,
    onSpeedModeChange: (ElevatorSpeedMode) -> Unit
) {
    val currentFloorData = SkyscraperFloorRegistry.getFloor(state.currentFloor)
    val heightRatio = (state.currentFloor.toFloat() / MallCatalog.TOTAL_FLOORS).coerceIn(0f, 1f)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "glassParallax")
    val animDuration = when (state.speedMode) {
        ElevatorSpeedMode.SLOW_PANORAMA -> 3000
        ElevatorSpeedMode.NORMAL -> 1800
        ElevatorSpeedMode.TURBO -> 900
        ElevatorSpeedMode.HYPER_WARP -> 420
    }
    val parallaxPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "parallaxPhase"
    )

    val skyColors = when {
        heightRatio > 0.82f -> listOf(Color(0xFF03050D), Color(0xFF0B1130), Color(0xFF231F5C))
        heightRatio > 0.55f -> listOf(Color(0xFF0E172B), Color(0xFF1E3A8A), Color(0xFFE07A3A))
        heightRatio > 0.25f -> listOf(Color(0xFF0A4D8C), Color(0xFF38BDF8), Color(0xFFD6F1FF))
        else -> listOf(Color(0xFF0D1422), Color(0xFF19273E), Color(0xFF31445E))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(skyColors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val motionShift = if (state.isMoving) {
                if (state.direction == ElevatorDirection.UP) parallaxPhase * h else -parallaxPhase * h
            } else {
                (state.currentFloor * 18f) % h
            }

            // Atmospheric horizon glow band
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, modelAccent.copy(alpha = 0.08f), Color.Transparent),
                    startY = h * 0.45f,
                    endY = h * 0.85f
                ),
                size = Size(w, h)
            )

            // Stars in upper atmosphere (Floors 60..120)
            if (heightRatio > 0.48f) {
                val starAlpha = ((heightRatio - 0.48f) * 1.9f).coerceIn(0.12f, 0.92f)
                for (i in 0..32) {
                    val sx = ((i * 73) % 100) / 100f * w
                    val sy = (((i * 41) % 100) / 100f * h + motionShift * 0.2f).mod(h)
                    drawCircle(
                        color = Color.White.copy(alpha = starAlpha),
                        radius = if (i % 4 == 0) 3.2f else 1.8f,
                        center = Offset(sx, sy)
                    )
                }
            }

            // Soft layered clouds
            for (i in 0..6) {
                val cy = ((i * (h / 6f)) + motionShift * 0.55f).mod(h)
                val cx = ((i * 137) % 80 + 10) / 100f * w
                drawRoundRect(
                    color = Color.White.copy(alpha = if (heightRatio > 0.8f) 0.10f else 0.24f),
                    topLeft = Offset(cx - 80f, cy),
                    size = Size(180f, 30f),
                    cornerRadius = CornerRadius(18f, 18f)
                )
            }

            // Distant skyscraper silhouettes & warm lit windows (Floors 1..78)
            if (heightRatio < 0.78f) {
                val buildingWidths = listOf(w * 0.21f, w * 0.17f, w * 0.23f)
                val buildingX = listOf(w * 0.05f, w * 0.39f, w * 0.72f)
                for (b in 0..2) {
                    val bx = buildingX[b]
                    val bw = buildingWidths[b]
                    drawRect(
                        color = Color(0xFF080C15).copy(alpha = 0.68f),
                        topLeft = Offset(bx, 0f),
                        size = Size(bw, h)
                    )
                    for (row in 0..14) {
                        val wy = ((row * (h / 14f)) + motionShift).mod(h)
                        drawRect(
                            color = if ((row + b) % 3 == 0) Color(0xFFFFD670).copy(alpha = 0.55f)
                            else Color(0xFF7AD7F0).copy(alpha = 0.38f),
                            topLeft = Offset(bx + 12f, wy),
                            size = Size(bw - 24f, 5f)
                        )
                    }
                }
            }

            // Subtle diagonal glass pane reflection sheen
            drawLine(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.08f), Color.Transparent)
                ),
                start = Offset(w * 0.10f, 0f),
                end = Offset(w * 0.48f, h),
                strokeWidth = 42f
            )

            // Minimal structural mullions
            drawLine(
                color = Color.White.copy(alpha = 0.16f),
                start = Offset(w * 0.22f, 0f),
                end = Offset(w * 0.22f, h),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.16f),
                start = Offset(w * 0.78f, 0f),
                end = Offset(w * 0.78f, h),
                strokeWidth = 2.5f
            )

            // Brushed architectural handrail
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFE2E8F0).copy(alpha = 0.72f), Color(0xFF475569).copy(alpha = 0.82f))
                ),
                topLeft = Offset(0f, h * 0.74f),
                size = Size(w, 11f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Altitude Horizon Telemetry Pill
            Surface(
                color = Color(0xFF0A0E18).copy(alpha = 0.84f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, studioColors.sky.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🌆 PANORAMA-GLASWAND • ${currentFloorData.altitudeMeters} METER HÖHE",
                        style = TelemetryNumberStyle.copy(
                            fontSize = 10.sp
                        ),
                        color = studioColors.sky,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = when {
                            heightRatio > 0.82f -> "🌌 Stratosphären-Blick: Erdkrümmung & Satelliten-Orbit"
                            heightRatio > 0.55f -> "☁️ Über den Wolken: Abendgold & ruhiger Horizont"
                            heightRatio > 0.25f -> "🏙️ Skyline-Panorama: Glasbrücken & Stadtleben"
                            else -> "🌃 Plaza-Ebene: Hauptstraße & Mall-Eingangsportal"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Bottom Panorama Speed Selector
            Surface(
                color = Color(0xFF0A0E18).copy(alpha = 0.86f),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.18f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fahrttempo beim Ausblick",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFD6DCE8)
                        )
                        Text(
                            text = "Wischen zum Drehen ↔",
                            style = MaterialTheme.typography.labelSmall,
                            color = studioColors.sky
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1A2233),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFF334155)
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
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = mode.shortLabel,
                                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
