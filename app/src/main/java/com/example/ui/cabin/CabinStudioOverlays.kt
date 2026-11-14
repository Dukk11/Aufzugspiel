package com.example.ui.cabin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.CabinAudioChannel
import com.example.viewmodel.CabinAudioPreset
import com.example.viewmodel.CabinLightingColor
import com.example.viewmodel.CabinWallMaterial
import com.example.viewmodel.ElevatorGameState
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * In-cabin Material 3 Expressive Studio Customizer Overlay for selecting architectural
 * wall materials and ambient ceiling LED lighting mood in real time.
 */
@Composable
fun CabinCustomizerOverlay(
    state: ElevatorGameState,
    onSelectWallMaterial: (CabinWallMaterial) -> Unit,
    onSelectLightingColor: (CabinLightingColor) -> Unit,
    onDismiss: () -> Unit
) {
    val activeAccent = Color(state.lightingColor.primaryHex)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    Surface(
        color = studioColors.surfaceContainer.copy(alpha = 0.98f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.2.dp, activeAccent.copy(alpha = 0.65f)),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cabin_customizer_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(activeAccent.copy(alpha = 0.2f))
                            .border(1.dp, activeAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎨", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KABINEN-STUDIO & LICHT",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Wandmaterialien & Lichtstimmung live wählen",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(studioColors.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Schließen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏛️ WANDMATERIAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = activeAccent
                )
                Text(
                    text = "${state.wallMaterial.emoji} ${state.wallMaterial.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CabinWallMaterial.entries.forEach { material ->
                    val isSelected = state.wallMaterial == material
                    Surface(
                        color = if (isSelected) Color(material.wallLight) else studioColors.surfaceContainerHigh,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.8.dp,
                            if (isSelected) activeAccent else studioColors.hairlineBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSelectWallMaterial(material)
                            }
                            .testTag("wall_material_${material.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(material.wallDark),
                                                Color(material.doorEnd),
                                                Color(material.wallLight)
                                            )
                                        )
                                    )
                                    .border(1.dp, Color(material.trimColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = material.emoji, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = material.shortName,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 AMBIENTE-BELEUCHTUNG",
                    style = MaterialTheme.typography.labelSmall,
                    color = activeAccent
                )
                Text(
                    text = "${state.lightingColor.emoji} ${state.lightingColor.displayName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = activeAccent
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CabinLightingColor.entries.forEach { light ->
                    val isSelected = state.lightingColor == light
                    val lightPrimary = Color(light.primaryHex)
                    Surface(
                        color = if (isSelected) lightPrimary.copy(alpha = 0.22f) else studioColors.surfaceContainerHigh,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 0.8.dp,
                            if (isSelected) lightPrimary else studioColors.hairlineBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onSelectLightingColor(light)
                            }
                            .testTag("lighting_color_${light.name.lowercase()}")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color(light.beamHex), lightPrimary)
                                        )
                                    )
                                    .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = light.emoji, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = light.shortName,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.5.sp),
                                color = if (isSelected) lightPrimary else MaterialTheme.colorScheme.onSurface,
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

/**
 * In-cabin Material 3 Expressive Audio Studio Mixer Overlay.
 */
@Composable
fun CabinAudioSettingsOverlay(
    state: ElevatorGameState,
    onHumVolumeChange: (Float, Boolean) -> Unit,
    onChimeVolumeChange: (Float, Boolean) -> Unit,
    onMallAmbienceVolumeChange: (Float, Boolean) -> Unit,
    onApplyAudioPreset: (CabinAudioPreset) -> Unit,
    onPreviewAudioChannel: (CabinAudioChannel) -> Unit,
    onDismiss: () -> Unit
) {
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    Surface(
        color = studioColors.surfaceContainer.copy(alpha = 0.98f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.2.dp, studioColors.sky.copy(alpha = 0.55f)),
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cabin_audio_settings_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, studioColors.sky, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎚️", fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KABINEN-AKUSTIK & KLANG-MIXER",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "Summen, Gong & Mall-Atmosphäre mit Hörprobe",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(studioColors.surfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Schließen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CabinAudioPreset.entries.forEach { preset ->
                    val isSelected = abs(state.humVolume - preset.hum) < 0.04f &&
                        abs(state.chimeVolume - preset.chime) < 0.04f &&
                        abs(state.mallAmbienceVolume - preset.mall) < 0.04f
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else studioColors.surfaceContainerHigh,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else studioColors.hairlineBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (state.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onApplyAudioPreset(preset)
                            }
                            .testTag("audio_preset_${preset.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = preset.emoji, fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CabinAudioChannelSliderRow(
                emoji = "〰️",
                title = "Kabinen-Summen",
                subtitle = "Magnetschwebe- & Lüftungsklang",
                volume = state.humVolume,
                accentColor = studioColors.sky,
                onVolumeChange = { onHumVolumeChange(it, false) },
                onVolumeFinished = { onHumVolumeChange(state.humVolume, true) },
                onToggleMute = {
                    val next = if (state.humVolume > 0.02f) 0f else 0.75f
                    onHumVolumeChange(next, next > 0f)
                },
                onPreview = { onPreviewAudioChannel(CabinAudioChannel.HUM) },
                testTag = "cabin_hum_slider"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CabinAudioChannelSliderRow(
                emoji = "🔔",
                title = "Ankunftsgong",
                subtitle = "Zweistufiger Studio-Etagengong",
                volume = state.chimeVolume,
                accentColor = studioColors.gold,
                onVolumeChange = { onChimeVolumeChange(it, false) },
                onVolumeFinished = { onChimeVolumeChange(state.chimeVolume, true) },
                onToggleMute = {
                    val next = if (state.chimeVolume > 0.02f) 0f else 0.85f
                    onChimeVolumeChange(next, next > 0f)
                },
                onPreview = { onPreviewAudioChannel(CabinAudioChannel.CHIME) },
                testTag = "cabin_chime_slider"
            )

            Spacer(modifier = Modifier.height(8.dp))

            CabinAudioChannelSliderRow(
                emoji = "🌊",
                title = "Mall-Atmosphäre",
                subtitle = "Atrium-Klang beim Türöffnen",
                volume = state.mallAmbienceVolume,
                accentColor = studioColors.emerald,
                onVolumeChange = { onMallAmbienceVolumeChange(it, false) },
                onVolumeFinished = { onMallAmbienceVolumeChange(state.mallAmbienceVolume, true) },
                onToggleMute = {
                    val next = if (state.mallAmbienceVolume > 0.02f) 0f else 0.70f
                    onMallAmbienceVolumeChange(next, next > 0f)
                },
                onPreview = { onPreviewAudioChannel(CabinAudioChannel.MALL_AMBIENCE) },
                testTag = "cabin_mall_ambience_slider"
            )
        }
    }
}

@Composable
private fun CabinAudioChannelSliderRow(
    emoji: String,
    title: String,
    subtitle: String,
    volume: Float,
    accentColor: Color,
    onVolumeChange: (Float) -> Unit,
    onVolumeFinished: () -> Unit,
    onToggleMute: () -> Unit,
    onPreview: () -> Unit,
    testTag: String
) {
    val isMuted = volume <= 0.02f
    val studioColors = StudioTheme.colors

    Surface(
        color = studioColors.surfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            0.8.dp,
            if (isMuted) studioColors.hairlineBorder else accentColor.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isMuted) studioColors.surfaceContainerLowest else accentColor.copy(alpha = 0.18f))
                            .border(1.dp, if (isMuted) studioColors.hairlineBorder else accentColor, CircleShape)
                            .clickable(onClick = onToggleMute),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = if (isMuted) "🔇" else emoji, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(volume * 100).roundToInt()}%",
                        style = TelemetryNumberStyle.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isMuted) MaterialTheme.colorScheme.onSurfaceVariant else accentColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = accentColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, accentColor),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onPreview)
                    ) {
                        Text(
                            text = "🔊 Test",
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                onValueChangeFinished = onVolumeFinished,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = studioColors.surfaceContainerLowest
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .testTag(testTag)
            )
        }
    }
}
