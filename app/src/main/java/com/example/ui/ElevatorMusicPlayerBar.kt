package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ElevatorMusicTrack
import com.example.viewmodel.ElevatorGameState

/**
 * Compact, retro-modern in-cabin elevator music player.
 * Allows instant track skipping, play/pause toggling, and opening the full music catalog
 * during the elevator ride or while parked.
 */
@Composable
fun ElevatorMusicPlayerBar(
    state: ElevatorGameState,
    onPreviousTrack: () -> Unit,
    onNextTrack: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onOpenMusicCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = state.currentMusicTrack
    val isPlaying = state.isMusicPlaying

    val infiniteTransition = rememberInfiniteTransition(label = "musicEqualizer")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(210, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(tween(170, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(250, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq3"
    )
    val bar4 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(190, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "eq4"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.4f),
                        Color(0xFFFFB703).copy(alpha = 0.6f),
                        Color(0xFFFF758F).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("elevator_music_player_bar"),
        color = Color(0xFF0D1322).copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Track Info & Equalizer (clickable to open full music catalog)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenMusicCatalog)
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker / Vinyl Icon Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, Color(0xFFFFB703).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = track.emoji,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "FAHRSTUHL-SOUND",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFB703),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Live animated equalizer bars when playing
                        if (isPlaying) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                                modifier = Modifier.height(10.dp)
                            ) {
                                Box(modifier = Modifier.width(2.dp).height((10 * bar1).dp).background(Color(0xFF38BDF8)))
                                Box(modifier = Modifier.width(2.dp).height((10 * bar2).dp).background(Color(0xFFFFB703)))
                                Box(modifier = Modifier.width(2.dp).height((10 * bar3).dp).background(Color(0xFF10B981)))
                                Box(modifier = Modifier.width(2.dp).height((10 * bar4).dp).background(Color(0xFFFF758F)))
                            }
                        } else {
                            Text(
                                text = "PAUSIERT",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = track.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${track.genre} • ${track.bpm} BPM",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Playback Controls: Previous, Play/Pause, Next, Jukebox Catalog
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Previous Track Button
                IconButton(
                    onClick = onPreviousTrack,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("music_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Vorheriger Titel",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Toggle Button
                Surface(
                    color = if (isPlaying) Color(0xFFFFB703) else Color(0xFF38BDF8),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onTogglePlayPause)
                        .testTag("music_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausieren" else "Abspielen",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Next Track Button
                IconButton(
                    onClick = onNextTrack,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("music_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Nächster Titel",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Track Catalog / Jukebox Button
                IconButton(
                    onClick = onOpenMusicCatalog,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("music_catalog_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Musik-Auswahl öffnen",
                        tint = Color(0xFFFFB703),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
