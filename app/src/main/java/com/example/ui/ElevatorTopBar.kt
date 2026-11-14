package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.viewmodel.ElevatorDirection
import com.example.viewmodel.ElevatorGameState

/**
 * Material 3 Expressive floating status & navigation capsule.
 * Uses tonal elevation layering, Dynamic Color tokens, and tabular telemetry typography.
 */
@Composable
fun ElevatorTopBar(
    state: ElevatorGameState,
    onOpenSettings: () -> Unit,
    onOpenBackpack: () -> Unit,
    onOpenDirectory: () -> Unit,
    onOpenCoinShop: () -> Unit = {},
    onOpenModelSelect: () -> Unit = {},
    onOpenFloorMatrix: () -> Unit = {},
    onToggleMusic: () -> Unit = {},
    onOpenMusicCatalog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentFloorData = SkyscraperFloorRegistry.getFloor(state.currentFloor)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "topBarCalm")
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowAlpha"
    )

    val musicContainerColor by animateColorAsState(
        targetValue = if (state.isMusicPlaying) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            studioColors.surfaceContainerHigh
        },
        animationSpec = tween(260),
        label = "musicContainerColor"
    )

    Surface(
        color = studioColors.surfaceContainer.copy(alpha = 0.96f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 4.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, studioColors.hairlineBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ─── LEFT: Settings ───
            Surface(
                color = studioColors.surfaceContainerHigh,
                shape = CircleShape,
                border = BorderStroke(0.5.dp, studioColors.hairlineBorder),
                modifier = Modifier
                    .size(40.dp)
                    .minimumInteractiveComponentSize()
                    .clip(CircleShape)
                    .clickable {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onOpenSettings()
                    }
                    .testTag("settings_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Einstellungen",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ─── CENTER: Dynamic Floor Pill ───
            Surface(
                color = studioColors.surfaceContainerHigh,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(0.5.dp, studioColors.hairlineBorder),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onOpenDirectory()
                    }
                    .testTag("floor_directory_trigger")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (state.direction) {
                        ElevatorDirection.UP -> {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = arrowAlpha),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        ElevatorDirection.DOWN -> {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = studioColors.gold.copy(alpha = arrowAlpha),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        ElevatorDirection.IDLE -> {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (state.doorsOpen) studioColors.emerald else studioColors.gold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Etage ${state.currentFloor}",
                        style = TelemetryNumberStyle.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = currentFloorData.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = currentFloorData.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Verzeichnis",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ─── RIGHT: Music, Coins, Backpack ───
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Music Capsule
                Surface(
                    color = musicContainerColor,
                    shape = CircleShape,
                    border = BorderStroke(
                        width = if (state.isMusicPlaying) 1.dp else 0.5.dp,
                        color = if (state.isMusicPlaying) studioColors.gold.copy(alpha = 0.7f) else studioColors.hairlineBorder
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onToggleMusic()
                        }
                        .testTag("music_toggle_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = if (state.isMusicPlaying) "Musik pausieren" else "Musik abspielen",
                            tint = if (state.isMusicPlaying) studioColors.gold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Coins Capsule
                Surface(
                    color = studioColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(0.5.dp, studioColors.hairlineBorder),
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onOpenCoinShop()
                        }
                        .testTag("open_coin_shop_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.coins}",
                            style = TelemetryNumberStyle.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = studioColors.gold,
                            maxLines = 1
                        )
                    }
                }

                // Backpack Button
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = CircleShape,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onOpenBackpack()
                        }
                        .testTag("backpack_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val totalItemsCount = state.inventory.values.sumOf { it.second }
                        BadgedBox(
                            badge = {
                                if (totalItemsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    ) {
                                        Text("$totalItemsCount", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Einkaufs-Rucksack",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
