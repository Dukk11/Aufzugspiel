package com.example.ui.cabin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MallCatalog
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.CabinViewAngle
import com.example.viewmodel.ElevatorDirection
import com.example.viewmodel.ElevatorGameState

/**
 * PERSPECTIVE 1: Looking at the Elevator Doors & Open Mall Portal Entrance.
 * Features refined metallic/marble/wood door textures, subtle ambient light spill,
 * and expressive M3 glass-depth overlays.
 */
@Composable
fun CabinDoorsPerspective(
    state: ElevatorGameState,
    totalWidth: Dp,
    doorProgress: Float,
    zoneImageRes: Int,
    modelAccent: Color,
    modelWallDark: Color,
    modelWallLight: Color,
    modelDoorStart: Color,
    modelDoorEnd: Color,
    onRingBell: (() -> Unit)?,
    onToggleMusic: (() -> Unit)?,
    onSelectViewAngle: (CabinViewAngle) -> Unit,
    onStepIntoMall: () -> Unit,
    onToggleAudioPanel: () -> Unit,
    onToggleCabinCustomizer: () -> Unit
) {
    val currentZone = MallCatalog.getZoneForFloor(state.currentFloor)
    val currentFloorData = SkyscraperFloorRegistry.getFloor(state.currentFloor)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    val sideWallWidth = if (state.isMoving) 48.dp else 42.dp
    val doorOpeningWidth = (totalWidth - (sideWallWidth * 2)).coerceAtLeast(120.dp)
    val doorHalfWidth = doorOpeningWidth / 2
    val maxDoorTravel = (doorHalfWidth - 14.dp).coerceAtLeast(0.dp)
    val trimColor = Color(state.wallMaterial.trimColor)

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. OPEN MALL ENTRANCE PORTAL
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = sideWallWidth)
        ) {
            Image(
                painter = painterResource(id = zoneImageRes),
                contentDescription = currentZone.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle architectural vignette so portal typography stays legible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF060910).copy(alpha = 0.68f),
                                Color(0xFF060910).copy(alpha = 0.18f),
                                Color(0xFF060910).copy(alpha = 0.84f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Overhead Corridor Pill
                Surface(
                    color = Color(0xFF0B0E14).copy(alpha = 0.84f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(currentZone.secondaryColor).copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = currentZone.emoji, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ETAGE ${state.currentFloor} • ${currentZone.title.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Center Welcome Placard & Expressive CTA
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color(0xFF0D111A).copy(alpha = 0.84f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${currentFloorData.emoji} ${currentFloorData.name}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentFloorData.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD8DEE9),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.doorsOpen && !state.isMoving) {
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
                            modifier = Modifier.testTag("cabin_step_into_mall_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(
                                text = "Mall-Welt betreten",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 2. SLIDING ARCHITECTURAL DOORS WITH LIGHTING REFLECTION
        // Left Door
        Box(
            modifier = Modifier
                .width(doorHalfWidth)
                .fillMaxHeight()
                .align(Alignment.Center)
                .offset(x = -(doorHalfWidth / 2) - (maxDoorTravel * doorProgress))
                .background(
                    Brush.horizontalGradient(
                        listOf(modelWallDark, modelDoorStart, modelDoorEnd, modelDoorStart)
                    )
                )
                .border(0.5.dp, trimColor.copy(alpha = 0.25f))
        ) {
            // Subtle ceiling light reflection along door face
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                modelAccent.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    )
            )
            // Specular edge line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(0.8f)
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, modelAccent.copy(alpha = 0.38f), Color.Transparent)
                        )
                    )
            )
            // Rubber door seal
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
                    .background(Color(0xFF070A10))
            )
        }

        // Right Door
        Box(
            modifier = Modifier
                .width(doorHalfWidth)
                .fillMaxHeight()
                .align(Alignment.Center)
                .offset(x = (doorHalfWidth / 2) + (maxDoorTravel * doorProgress))
                .background(
                    Brush.horizontalGradient(
                        listOf(modelDoorStart, modelDoorEnd, modelDoorStart, modelWallDark)
                    )
                )
                .border(0.5.dp, trimColor.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                modelAccent.copy(alpha = 0.14f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.28f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .background(Color(0xFF070A10))
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight(0.8f)
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, modelAccent.copy(alpha = 0.38f), Color.Transparent)
                        )
                    )
            )
        }

        // Ride Status Card on Closed Doors
        if (doorProgress < 0.1f) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = sideWallWidth + 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color(0xFF0F131D).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    border = BorderStroke(1.dp, modelAccent.copy(alpha = 0.42f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                state.isMoving && state.direction == ElevatorDirection.UP -> "▲ AUFWÄRTS • ${state.speedMode.shortLabel}"
                                state.isMoving && state.direction == ElevatorDirection.DOWN -> "▼ ABWÄRTS • ${state.speedMode.shortLabel}"
                                else -> "KABINENTÜREN GESCHLOSSEN"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.direction == ElevatorDirection.UP) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                modelAccent
                            },
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.currentFloor}",
                            style = TelemetryNumberStyle.copy(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${currentFloorData.emoji} ${currentFloorData.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFC8D0DF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFF1D2434),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (state.hapticsEnabled) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onSelectViewAngle(CabinViewAngle.BUTTON_WALL)
                                    }
                            ) {
                                Text(
                                    text = "🔢 Knöpfewand",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFF1D2434),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(0.8.dp, studioColors.sky.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (state.hapticsEnabled) {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onSelectViewAngle(CabinViewAngle.GLASS_OUTSIDE)
                                    }
                            ) {
                                Text(
                                    text = "🌆 Glas-Ausblick",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = studioColors.sky,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. LEFT CABIN WALL (Panorama Window Shortcut)
        Box(
            modifier = Modifier
                .width(sideWallWidth)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(Brush.horizontalGradient(listOf(modelWallDark, modelWallLight, modelWallDark)))
                .border(0.5.dp, trimColor.copy(alpha = 0.2f))
                .clickable {
                    if (state.hapticsEnabled) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    onSelectViewAngle(CabinViewAngle.GLASS_OUTSIDE)
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🌆", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "GLAS",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = studioColors.sky,
                    maxLines = 1
                )
            }
        }

        // 4. RIGHT CABIN WALL (COP Quick Rail)
        Box(
            modifier = Modifier
                .width(sideWallWidth)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .background(Brush.horizontalGradient(listOf(modelWallDark, modelWallLight, modelWallDark)))
                .border(0.5.dp, trimColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .size(width = sideWallWidth - 8.dp, height = 34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onSelectViewAngle(CabinViewAngle.BUTTON_WALL)
                        }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "🔢", fontSize = 11.sp)
                        Text(
                            text = "KNÖPFE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 6.5.sp),
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }

                if (onToggleMusic != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isMusicPlaying) studioColors.emerald.copy(alpha = 0.28f)
                                else Color(0xFF161B26)
                            )
                            .border(
                                1.dp,
                                if (state.isMusicPlaying) studioColors.emerald else Color(0xFF475569),
                                CircleShape
                            )
                            .clickable {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onToggleMusic.invoke()
                            }
                            .testTag("cop_music_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎵", fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.showCabinCustomizer) modelAccent.copy(alpha = 0.35f)
                            else Color(0xFF161B26)
                        )
                        .border(
                            1.dp,
                            if (state.showCabinCustomizer) Color.White else modelAccent,
                            CircleShape
                        )
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onToggleCabinCustomizer()
                        }
                        .testTag("cop_customizer_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎨", fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (state.showCabinAudioPanel) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            else Color(0xFF161B26)
                        )
                        .border(
                            1.dp,
                            if (state.showCabinAudioPanel) MaterialTheme.colorScheme.primary else studioColors.sky,
                            CircleShape
                        )
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onToggleAudioPanel()
                        }
                        .testTag("cop_audio_panel_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎚️", fontSize = 11.sp)
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF161B26))
                        .border(1.dp, studioColors.gold, CircleShape)
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            onRingBell?.invoke()
                        }
                        .testTag("cop_bell_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notruf-Glocke",
                        tint = studioColors.gold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
