package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.theme.StudioTheme
import com.example.viewmodel.AssistantAction
import com.example.viewmodel.ElevatorGameState
import kotlin.math.sin

/**
 * Material 3 Expressive Voice Intercom ("SKY-AI Gegensprechanlage").
 * Features a calm, soothing Gemini waveform orb, live spoken subtitle, German voice output,
 * push-to-talk microphone recognition, and direct elevator control actions.
 */
@Composable
fun ElevatorAssistantDialog(
    state: ElevatorGameState,
    onSendVoiceCommand: (String) -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onReplaySpeech: () -> Unit,
    onToggleMute: () -> Unit,
    onSimulateStuck: () -> Unit,
    onEmergencyRescue: () -> Unit,
    onTravelToFloor: (Int) -> Unit,
    onToggleMusic: () -> Unit,
    onOpenDoors: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current

    val currentFloorData = remember(state.currentFloor) {
        SkyscraperFloorRegistry.getFloor(state.currentFloor)
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartListening()
        } else {
            onSendVoiceCommand("Hilfe, ich möchte mit dem Bordcomputer über Etage ${state.currentFloor} sprechen!")
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "voiceOrbAnimation")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 680, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbPulse"
    )

    val quickIntercomButtons = remember(state.currentFloor) {
        listOf(
            "🚨 Ich stecke fest!" to "Hilfe, der Fahrstuhl steckt fest! Mach bitte einen Notfall-Check und befreie mich!",
            "🏢 Coolste Etagen" to "Welche Etagen im SkyMall-Gebäude sind besonders spektakulär?",
            "📍 Etage ${state.currentFloor} Info" to "Erzähl mir kurz, was es hier auf Etage ${state.currentFloor} gibt!",
            "🤖 Gebäude-Geheimnis" to "Verrate mir ein cooles Geheimnis über diesen Wolkenkratzer!",
            "🪙 Gratis-Münzen" to "Wo bekomme ich hier im Gebäude am schnellsten Gratis-Münzen?"
        )
    }

    val isActiveAudio = state.isIntercomSpeaking || state.isIntercomListening || state.isAssistantStreaming

    val accentColor = when {
        state.isEmergencyStuck -> studioColors.coral
        state.isIntercomListening -> studioColors.emerald
        state.isIntercomSpeaking -> colorScheme.primary
        else -> studioColors.sky
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = studioColors.surfaceContainerLow,
            border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.65f)),
            tonalElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("elevator_assistant_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                studioColors.surfaceContainer,
                                studioColors.surfaceContainerLow
                            )
                        )
                    )
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP STUDIO INTERCOM HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SKY-AI GEGENSPRECHANLAGE",
                                style = typography.labelLarge,
                                color = colorScheme.onSurface,
                                letterSpacing = 0.6.sp
                            )
                            Text(
                                text = "Etage ${state.currentFloor} (${currentFloorData.emoji} ${currentFloorData.name}) • ${state.intercomStatusBadge}",
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onReplaySpeech()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(studioColors.surfaceContainerHigh)
                                .testTag("intercom_replay_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Durchsage wiederholen",
                                tint = studioColors.sky,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleMute()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(studioColors.surfaceContainerHigh)
                                .testTag("intercom_mute_button")
                        ) {
                            Icon(
                                imageVector = if (state.isIntercomMuted) {
                                    Icons.AutoMirrored.Filled.VolumeOff
                                } else {
                                    Icons.AutoMirrored.Filled.VolumeUp
                                },
                                contentDescription = "Stimme stummschalten",
                                tint = if (state.isIntercomMuted) studioColors.coral else studioColors.emerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(studioColors.surfaceContainerHigh)
                                .testTag("close_assistant_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Schließen",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. EMERGENCY STATUS & TEST BAR
                Surface(
                    color = if (state.isEmergencyStuck) studioColors.coral.copy(alpha = 0.14f) else studioColors.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(
                        1.dp,
                        if (state.isEmergencyStuck) studioColors.coral else studioColors.hairlineBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (state.isEmergencyStuck) Icons.Default.Warning else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (state.isEmergencyStuck) studioColors.coral else studioColors.gold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (state.isEmergencyStuck) {
                                    "🚨 Notbremse verriegelt • Kabine wartet auf Freigabe"
                                } else {
                                    "Seilspannung 100% • O₂-Level 21% • Bordfunk klar"
                                },
                                style = typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (state.isEmergencyStuck) studioColors.coral else colorScheme.onSurface
                            )
                        }

                        if (state.isEmergencyStuck) {
                            Button(
                                onClick = {
                                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEmergencyRescue()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = studioColors.emerald,
                                    contentColor = Color(0xFF062114)
                                ),
                                shape = MaterialTheme.shapes.extraSmall,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("emergency_rescue_header_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Befreien", style = typography.labelMedium)
                            }
                        } else {
                            Surface(
                                color = studioColors.surfaceContainerLowest,
                                shape = MaterialTheme.shapes.extraSmall,
                                border = BorderStroke(1.dp, studioColors.gold.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .clickable {
                                        if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSimulateStuck()
                                    }
                                    .testTag("simulate_stuck_button")
                            ) {
                                Text(
                                    text = "🚨 Notfall-Test",
                                    style = typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = studioColors.gold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. PULSATING GOOGLE STUDIO INTERCOM WAVEFORM ORB
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(114.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = if (isActiveAudio) 0.25f else 0.14f),
                                    studioColors.surfaceContainerLowest
                                )
                            )
                        )
                        .border(1.dp, studioColors.hairlineBorder, MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(92.dp)) {
                        val barCount = 29
                        val spacing = size.width / (barCount + 4)
                        val centerY = size.height / 2f
                        val activeMultiplier = if (isActiveAudio) orbPulse else 0.26f

                        for (i in 0 until barCount) {
                            val x = spacing * (i + 2.5f)
                            val distFromCenter = 1f - (kotlin.math.abs(i - barCount / 2f) / (barCount / 2f))
                            val waveVal = kotlin.math.abs(sin(phase + i * 0.45f))
                            val barHeight = (10f + (size.height * 0.72f * distFromCenter * waveVal * activeMultiplier))
                                .coerceIn(8f, size.height * 0.88f)

                            val barColor = when {
                                state.isEmergencyStuck -> studioColors.coral
                                state.isIntercomListening -> studioColors.emerald
                                i % 2 == 0 -> colorScheme.primary
                                else -> studioColors.sky
                            }

                            drawLine(
                                color = barColor.copy(alpha = (0.45f + 0.55f * distFromCenter).coerceIn(0.3f, 1f)),
                                start = Offset(x, centerY - barHeight / 2f),
                                end = Offset(x, centerY + barHeight / 2f),
                                strokeWidth = 6f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Surface(
                        color = studioColors.surfaceContainerLowest.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                    ) {
                        val statusText = when {
                            state.isIntercomListening -> "🎙️ MIKROFON HÖRT ZU – SPRICH JETZT…"
                            state.isAssistantStreaming -> "⚡ SKY-AI CONCIERGE ANTWORTET…"
                            state.isIntercomSpeaking -> "🔊 SKY-AI SPRICHT ÜBER LAUTSPRECHER"
                            else -> "🟢 GEGENSPRECHANLAGE BEREIT"
                        }
                        Text(
                            text = statusText,
                            style = typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. LIVE SPOKEN SUBTITLE DISPLAY
                Surface(
                    color = studioColors.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, studioColors.hairlineBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!state.intercomLastUserSpeech.isNullOrBlank()) {
                            Text(
                                text = "🎙️ „${state.intercomLastUserSpeech}“",
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        AnimatedContent(
                            targetState = state.intercomSpokenText,
                            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(150)) },
                            label = "liveSubtitleAnimation"
                        ) { spokenSubtitle ->
                            Text(
                                text = spokenSubtitle,
                                style = typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("intercom_live_subtitle")
                            )
                        }
                    }
                }

                // 5. DIRECT SYSTEM ACTION BUTTONS FROM SKY-AI
                if (state.intercomActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        state.intercomActions.forEach { action ->
                            val (btnLabel, btnColor, btnIcon) = when (action) {
                                is AssistantAction.TravelToFloor -> Triple(
                                    "🚀 Direkt zu Etage ${action.floor} (${action.floorName}) fahren",
                                    colorScheme.primary,
                                    Icons.Default.PlayArrow
                                )
                                is AssistantAction.EmergencyRescue -> Triple(
                                    "🔓 Not-Entriegelung & Kabine jetzt befreien",
                                    studioColors.emerald,
                                    Icons.Default.LockOpen
                                )
                                is AssistantAction.ToggleMusic -> Triple(
                                    "🎵 Beruhigende Lounge-Musik umschalten",
                                    studioColors.gold,
                                    Icons.Default.MusicNote
                                )
                                is AssistantAction.OpenDoors -> Triple(
                                    "◄||► Kabinentüren sofort öffnen",
                                    studioColors.emerald,
                                    Icons.Default.LockOpen
                                )
                            }

                            Surface(
                                color = btnColor.copy(alpha = 0.16f),
                                shape = MaterialTheme.shapes.small,
                                border = BorderStroke(1.dp, btnColor.copy(alpha = 0.75f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable {
                                        if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        when (action) {
                                            is AssistantAction.TravelToFloor -> {
                                                onDismiss()
                                                onTravelToFloor(action.floor)
                                            }
                                            is AssistantAction.EmergencyRescue -> onEmergencyRescue()
                                            is AssistantAction.ToggleMusic -> onToggleMusic()
                                            is AssistantAction.OpenDoors -> onOpenDoors()
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = btnIcon,
                                        contentDescription = null,
                                        tint = btnColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = btnLabel,
                                        style = typography.labelLarge,
                                        color = colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. PRIMARY PUSH-TO-TALK MICROPHONE BUTTON
                Button(
                    onClick = {
                        if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (state.isIntercomListening) {
                            onStopListening()
                        } else {
                            val hasMicPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasMicPermission) {
                                onStartListening()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isIntercomListening) studioColors.coral else colorScheme.primary,
                        contentColor = if (state.isIntercomListening) Color.White else colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("intercom_mic_button")
                ) {
                    Icon(
                        imageVector = if (state.isIntercomListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Mit SKY-AI sprechen",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isIntercomListening) {
                            "Zuhören beenden…"
                        } else {
                            "🎙️ Antippen & mit SKY-AI sprechen"
                        },
                        style = typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7. QUICK INTERCOM VOICE CALL PILLS
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickIntercomButtons) { (label, spokenQuestion) ->
                        Surface(
                            color = studioColors.surfaceContainerHigh,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(1.dp, studioColors.hairlineBorder),
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .clickable(enabled = !state.isAssistantStreaming) {
                                    if (state.hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSendVoiceCommand(spokenQuestion)
                                }
                        ) {
                            Text(
                                text = label,
                                style = typography.labelMedium,
                                color = colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
