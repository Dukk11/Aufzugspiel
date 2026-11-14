package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ElevatorMusicTrack
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle

@Composable
fun ElevatorMusicDialog(
    currentTrack: ElevatorMusicTrack,
    isMusicPlaying: Boolean,
    musicVolume: Float,
    onSelectTrack: (ElevatorMusicTrack) -> Unit,
    onTogglePlayPause: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "dialogEq")
    val eq1 by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(240, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val eq2 by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(190, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val eq3 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(270, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq3"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(MaterialTheme.shapes.extraLarge)
                .border(1.5.dp, studioColors.gold.copy(alpha = 0.7f), MaterialTheme.shapes.extraLarge)
                .testTag("elevator_music_dialog"),
            colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = studioColors.gold.copy(alpha = 0.18f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = studioColors.gold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Fahrstuhl-Jukebox",
                                style = typography.headlineMedium,
                                color = colorScheme.onSurface
                            )
                            Text(
                                text = "Wähle deine entspannte Begleitmusik",
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_music_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = studioColors.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, studioColors.hairlineBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = currentTrack.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "AKTUELLER TRACK",
                                        style = typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = studioColors.gold
                                    )
                                    if (isMusicPlaying) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.height(11.dp)
                                        ) {
                                            Box(modifier = Modifier.width(2.5.dp).height((11 * eq1).dp).background(studioColors.sky))
                                            Box(modifier = Modifier.width(2.5.dp).height((11 * eq2).dp).background(studioColors.gold))
                                            Box(modifier = Modifier.width(2.5.dp).height((11 * eq3).dp).background(studioColors.emerald))
                                        }
                                    }
                                }
                                Text(
                                    text = currentTrack.title,
                                    style = typography.titleMedium,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "${currentTrack.genre} • ${currentTrack.bpm} BPM",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            color = if (isMusicPlaying) studioColors.gold else studioColors.emerald,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onTogglePlayPause()
                                }
                                .testTag("dialog_music_toggle_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMusicPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isMusicPlaying) "Pausieren" else "Starten",
                                    tint = Color(0xFF1A1400),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = studioColors.gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lautstärke: ${(musicVolume * 100).toInt()}%",
                        style = TelemetryNumberStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = musicVolume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = studioColors.gold,
                            activeTrackColor = studioColors.gold,
                            inactiveTrackColor = studioColors.surfaceContainerHighest
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "VERFÜGBARE FAHRSTUHL-TRACKS",
                    style = typography.labelMedium,
                    color = colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ElevatorMusicTrack.entries) { track ->
                        val isSelected = track == currentTrack
                        val isTrackActive = isSelected && isMusicPlaying

                        Surface(
                            color = if (isSelected) studioColors.surfaceContainerHigh else studioColors.surfaceContainer,
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) studioColors.gold else studioColors.hairlineBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectTrack(track)
                                    if (!isMusicPlaying) {
                                        onTogglePlayPause()
                                    }
                                }
                                .testTag("track_item_${track.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) studioColors.gold.copy(alpha = 0.2f)
                                                else studioColors.surfaceContainerLowest
                                            )
                                            .border(
                                                1.dp,
                                                if (isSelected) studioColors.gold else studioColors.hairlineBorder,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = track.emoji, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = track.title,
                                                style = typography.titleSmall,
                                                color = if (isSelected) studioColors.gold else colorScheme.onSurface
                                            )
                                            if (isTrackActive) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = studioColors.emerald.copy(alpha = 0.18f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "SPIELT",
                                                        style = typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = studioColors.emerald,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = "${track.genre} • ${track.bpm} BPM",
                                            style = typography.labelSmall,
                                            color = studioColors.sky
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = track.description,
                                            style = typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectTrack(track)
                                        if (!isMusicPlaying) {
                                            onTogglePlayPause()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) studioColors.gold else colorScheme.secondaryContainer,
                                        contentColor = if (isSelected) Color(0xFF1A1400) else colorScheme.onSecondaryContainer
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.testTag("select_track_${track.id}")
                                ) {
                                    Text(
                                        text = if (isSelected) "Aktiv" else "Wählen",
                                        style = typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("done_music_dialog_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = studioColors.gold,
                        contentColor = Color(0xFF1A1400)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Fertig",
                        style = typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
