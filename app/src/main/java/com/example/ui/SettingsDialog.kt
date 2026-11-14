package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ElevatorModel
import com.example.model.ElevatorMusicTrack
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.CabinLightingColor
import com.example.viewmodel.CabinTheme
import com.example.viewmodel.CabinWallMaterial
import com.example.viewmodel.ElevatorGameState
import com.example.viewmodel.ElevatorSpeedMode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun SettingsDialog(
    state: ElevatorGameState,
    onDismiss: () -> Unit,
    onMusicVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    onSpeedModeChange: (ElevatorSpeedMode) -> Unit,
    onCabinThemeChange: (CabinTheme) -> Unit,
    onAddPocketMoney: () -> Unit,
    onOpenLegal: () -> Unit,
    onOpenCoinShop: () -> Unit = {},
    onSelectTrack: (ElevatorMusicTrack) -> Unit = {},
    onOpenMusicCatalog: () -> Unit = {},
    onSelectModel: (ElevatorModel) -> Unit = {},
    onOpenModelDialog: () -> Unit = {},
    onHumVolumeChange: (Float) -> Unit = {},
    onChimeVolumeChange: (Float) -> Unit = {},
    onMallAmbienceVolumeChange: (Float) -> Unit = {},
    onWallMaterialChange: (CabinWallMaterial) -> Unit = {},
    onLightingColorChange: (CabinLightingColor) -> Unit = {},
    onToggleToddlerMode: () -> Unit = {},
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onToggleDynamicColor: (Boolean) -> Unit = {},
    onToggleHaptics: (Boolean) -> Unit = {},
    onSignOut: () -> Unit = {},
    onSignInWithGoogle: () -> Unit = {}
) {
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    fun tickHaptic(heavy: Boolean = false) {
        if (state.hapticsEnabled) {
            haptics.performHapticFeedback(
                if (heavy) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = studioColors.surfaceContainerLow,
            border = BorderStroke(1.dp, studioColors.hairlineBorder),
            tonalElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp)
                .padding(vertical = 12.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Studio-Einstellungen",
                            style = typography.headlineMedium,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = "Material 3 Expressive • Kabine, Sound & Haptik",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            tickHaptic()
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 0. DESIGN, DYNAMIC COLOR & HAPTIK (NEW GOOGLE MATERIAL 3 STUDIO CONTROLS)
                SectionHeader(
                    title = "DESIGN, DYNAMIC COLOR & HAPTIK",
                    accentColor = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.hairlineBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Erscheinungsbild",
                                style = typography.titleSmall,
                                color = colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppThemeMode.entries.forEach { mode ->
                                val isSelected = state.themeMode == mode
                                Surface(
                                    color = if (isSelected) colorScheme.primaryContainer else studioColors.surfaceContainerHigh,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) colorScheme.primary else studioColors.hairlineBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable {
                                            tickHaptic()
                                            onThemeModeChange(mode)
                                        }
                                        .testTag("settings_theme_mode_${mode.name.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = mode.emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = mode.label,
                                            style = typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dynamic Color Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = studioColors.sky,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Dynamic Color (Material You)",
                                        style = typography.titleSmall,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Passt Farben ab Android 12 an dein System an",
                                        style = typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = state.useDynamicColor,
                                onCheckedChange = {
                                    tickHaptic()
                                    onToggleDynamicColor(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = colorScheme.primary
                                ),
                                modifier = Modifier.testTag("settings_dynamic_color_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Haptics Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = studioColors.emerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Präzise Haptik & Drehrad-Rasten",
                                        style = typography.titleSmall,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Sanftes Feedback bei jeder Etage & jedem Klick",
                                        style = typography.bodySmall,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = state.hapticsEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onToggleHaptics(enabled)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colorScheme.onPrimary,
                                    checkedTrackColor = studioColors.emerald
                                ),
                                modifier = Modifier.testTag("settings_haptics_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 1. FAHRSTUHLMUSIK & SOUND
                SectionHeader(
                    title = "FAHRSTUHLMUSIK & AKUSTIK",
                    accentColor = studioColors.sky
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.hairlineBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        VolumeSliderRow(
                            label = "Fahrstuhlmusik während Fahrt",
                            value = state.musicVolume,
                            accent = studioColors.gold,
                            icon = {
                                Icon(
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = studioColors.gold,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onValueChange = onMusicVolumeChange
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        VolumeSliderRow(
                            label = "Gong & Knopf-Geräusche",
                            value = state.sfxVolume,
                            accent = studioColors.sky,
                            icon = {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = studioColors.sky,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onValueChange = onSfxVolumeChange
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        VolumeSliderRow(
                            label = "〰️ Kabinen-Summen",
                            value = state.humVolume,
                            accent = studioColors.sky,
                            onValueChange = onHumVolumeChange
                        )

                        VolumeSliderRow(
                            label = "🔔 Ankunftsgong",
                            value = state.chimeVolume,
                            accent = studioColors.gold,
                            onValueChange = onChimeVolumeChange
                        )

                        VolumeSliderRow(
                            label = "🌊 Mall-Atmosphäre",
                            value = state.mallAmbienceVolume,
                            accent = studioColors.emerald,
                            onValueChange = onMallAmbienceVolumeChange
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Titel: ${state.currentMusicTrack.emoji} ${state.currentMusicTrack.title}",
                                style = typography.titleSmall,
                                color = studioColors.gold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.extraSmall,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .clickable {
                                        tickHaptic()
                                        onOpenMusicCatalog()
                                    }
                            ) {
                                Text(
                                    text = "Jukebox 🎵",
                                    style = typography.labelMedium,
                                    color = colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ElevatorMusicTrack.entries.forEach { track ->
                                val isSelected = track == state.currentMusicTrack
                                Surface(
                                    color = if (isSelected) studioColors.gold else studioColors.surfaceContainerHigh,
                                    shape = MaterialTheme.shapes.extraSmall,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) studioColors.gold else studioColors.hairlineBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .clickable {
                                            tickHaptic()
                                            onSelectTrack(track)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = track.emoji, fontSize = 14.sp)
                                        Text(
                                            text = track.title.split(" ").first(),
                                            style = typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF1A1400) else colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. AUFZUG-MODELL WECHSELN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "AUFZUG-MODELL WÄHLEN",
                        accentColor = colorScheme.primary
                    )
                    Surface(
                        color = colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraSmall,
                        border = BorderStroke(1.dp, colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraSmall)
                            .clickable {
                                tickHaptic()
                                onOpenModelDialog()
                            }
                            .testTag("settings_open_model_dialog_button")
                    ) {
                        Text(
                            text = "Große Übersicht 🛗",
                            style = typography.labelMedium,
                            color = colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ElevatorModel.entries.forEach { model ->
                        val isSelected = state.currentModel == model
                        val primaryColor = Color(model.primaryAccentColor)
                        val secondaryColor = Color(model.secondaryAccentColor)

                        Surface(
                            color = if (isSelected) studioColors.surfaceContainerHigh else studioColors.surfaceContainer,
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) primaryColor else studioColors.hairlineBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    tickHaptic(heavy = true)
                                    onSelectModel(model)
                                }
                                .testTag("settings_model_${model.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = model.emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = model.modelName,
                                                style = typography.titleMedium,
                                                color = colorScheme.onSurface
                                            )
                                            Text(
                                                text = model.manufacturer,
                                                style = typography.labelSmall,
                                                color = secondaryColor
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            color = primaryColor.copy(alpha = 0.18f),
                                            shape = MaterialTheme.shapes.small,
                                            border = BorderStroke(1.dp, primaryColor)
                                        ) {
                                            Text(
                                                text = "AKTIV",
                                                style = typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = model.tagline,
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        color = studioColors.surfaceContainerLowest,
                                        shape = MaterialTheme.shapes.extraSmall,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⚡", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${model.topSpeedKmh} km/h",
                                                style = TelemetryNumberStyle.copy(
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Surface(
                                        color = studioColors.surfaceContainerLowest,
                                        shape = MaterialTheme.shapes.extraSmall,
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("👥", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${model.maxPassengers} Pers. (${model.maxPayloadKg}kg)",
                                                style = typography.labelSmall,
                                                color = colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Surface(
                                        color = studioColors.surfaceContainerLowest,
                                        shape = MaterialTheme.shapes.extraSmall,
                                        modifier = Modifier.weight(1.1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = model.styleCategory,
                                                style = typography.labelSmall,
                                                color = colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. FEINJUSTIERUNG GESCHWINDIGKEIT
                SectionHeader(
                    title = "FEINJUSTIERUNG GESCHWINDIGKEIT",
                    accentColor = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatorSpeedMode.entries.forEach { mode ->
                        val isSelected = state.speedMode == mode
                        Surface(
                            color = if (isSelected) colorScheme.primaryContainer else studioColors.surfaceContainer,
                            shape = MaterialTheme.shapes.small,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colorScheme.primary else studioColors.hairlineBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    tickHaptic()
                                    onSpeedModeChange(mode)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mode.label,
                                    style = typography.titleSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface
                                )
                                Text(
                                    text = "bis ${mode.topSpeedKmh} km/h",
                                    style = TelemetryNumberStyle.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 4. CABIN CUSTOMIZER (WÄNDE & LICHT)
                SectionHeader(
                    title = "CABIN CUSTOMIZER (WÄNDE & LICHT)",
                    accentColor = studioColors.sky
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.hairlineBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🏛️ Wandmaterial: ${state.wallMaterial.displayName}",
                            style = typography.titleSmall,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CabinWallMaterial.entries.forEach { material ->
                                val isSelected = state.wallMaterial == material
                                Surface(
                                    color = if (isSelected) Color(material.wallLight) else studioColors.surfaceContainerHigh,
                                    shape = MaterialTheme.shapes.extraSmall,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(state.lightingColor.primaryHex) else studioColors.hairlineBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .clickable {
                                            tickHaptic()
                                            onWallMaterialChange(material)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = material.emoji, fontSize = 14.sp)
                                        Text(
                                            text = material.shortName,
                                            style = typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "💡 Lichtfarbe: ${state.lightingColor.displayName}",
                            style = typography.titleSmall,
                            color = Color(state.lightingColor.primaryHex)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CabinLightingColor.entries.forEach { light ->
                                val isSelected = state.lightingColor == light
                                val lightColor = Color(light.primaryHex)
                                Surface(
                                    color = if (isSelected) lightColor.copy(alpha = 0.22f) else studioColors.surfaceContainerHigh,
                                    shape = MaterialTheme.shapes.extraSmall,
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) lightColor else studioColors.hairlineBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.extraSmall)
                                        .clickable {
                                            tickHaptic()
                                            onLightingColorChange(light)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = light.emoji, fontSize = 14.sp)
                                        Text(
                                            text = light.shortName,
                                            style = typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) lightColor else colorScheme.onSurface,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "🎨 Kabinen-Farbthema",
                            style = typography.titleSmall,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CabinTheme.entries.forEach { theme ->
                                val isSelected = state.cabinTheme == theme
                                val themeColor = Color(theme.primaryColor)
                                Surface(
                                    color = if (isSelected) themeColor.copy(alpha = 0.16f) else studioColors.surfaceContainerHigh,
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) themeColor else studioColors.hairlineBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable {
                                            tickHaptic()
                                            onCabinThemeChange(theme)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = theme.displayName,
                                            style = typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = colorScheme.onSurface
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(themeColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 5. KLEINKIND-MODUS (RUHIG & SICHER)
                SectionHeader(
                    title = "👶 KLEINKIND-MODUS (RUHIG & REIZARM)",
                    accentColor = studioColors.gold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.gold.copy(alpha = 0.65f))
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
                            Text(text = "🧸", fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ruhiger Kinder-Fahrstuhl",
                                    style = typography.titleSmall,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Kein Flimmern, reizarme Wände, gesprochene Zahlen & Eltern-Schloss.",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                tickHaptic(heavy = true)
                                onToggleToddlerMode()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isToddlerMode) studioColors.coral else studioColors.gold,
                                contentColor = if (state.isToddlerMode) Color.White else Color(0xFF1A1400)
                            ),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("toggle_toddler_mode_button")
                        ) {
                            Text(
                                text = if (state.isToddlerMode) "Aktiv 🔒" else "Starten ▶",
                                style = typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 6. GOOGLE PLAY COIN SHOP & TASCHENGELD
                SectionHeader(
                    title = "MÜNZ-BANK & TASCHENGELD",
                    accentColor = studioColors.emerald
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.emerald.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Münz-Pakete kaufen",
                                style = typography.titleSmall,
                                color = studioColors.emerald
                            )
                            Text(
                                text = "Offizielle Google Play In-App-Abrechnung",
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                tickHaptic()
                                onOpenCoinShop()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = studioColors.emerald,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("settings_open_coin_shop_button")
                        ) {
                            Text(
                                text = "Münz-Shop",
                                style = typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.gold.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Taschengeld für den Mall-Trip",
                                style = typography.titleSmall,
                                color = studioColors.gold
                            )
                            Text(
                                text = "Gratis +250 Münzen zum Shoppen",
                                style = typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                tickHaptic(heavy = true)
                                onAddPocketMoney()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = studioColors.gold,
                                contentColor = Color(0xFF1A1400)
                            ),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = "🪙 +250",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 7. RECHTLICHE HINWEISE & IMPRESSUM
                SectionHeader(
                    title = "RECHTLICHE HINWEISE & IMPRESSUM",
                    accentColor = studioColors.sky
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.sky.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            tickHaptic()
                            onOpenLegal()
                        }
                        .testTag("open_legal_button")
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
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = studioColors.sky,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Rechtliche Angaben",
                                    style = typography.titleSmall,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Datenschutz, Impressum & Kinder-Hinweise",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Öffnen ›",
                            style = typography.labelLarge,
                            color = studioColors.sky
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 8. KONTO & CLOUD-SPEICHER
                val currentUser = Firebase.auth.currentUser
                SectionHeader(
                    title = "KONTO & CLOUD-SPEICHER",
                    accentColor = studioColors.sky
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (currentUser != null) {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                        border = BorderStroke(1.dp, studioColors.sky.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = studioColors.sky,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = currentUser.email ?: (currentUser.displayName ?: "Verbunden"),
                                    style = typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    tickHaptic()
                                    onSignOut()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.errorContainer,
                                    contentColor = colorScheme.onErrorContainer
                                ),
                                shape = MaterialTheme.shapes.small,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Abmelden",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Abmelden", style = typography.labelMedium)
                            }
                        }
                    }
                } else {
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                        border = BorderStroke(1.dp, studioColors.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Gast-Modus (Lokales Spiel)",
                                    style = typography.titleSmall,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Kein Login nötig. Optional mit Google für Cloud-Sync verbinden.",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    tickHaptic()
                                    onSignInWithGoogle()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary,
                                    contentColor = colorScheme.onPrimary
                                ),
                                shape = MaterialTheme.shapes.small,
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("google_connect_button")
                            ) {
                                Text("Mit Google", style = typography.labelLarge)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 9. FREUNDE & TESTER EINLADEN
                SectionHeader(
                    title = "FREUNDE & TESTER EINLADEN",
                    accentColor = colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.hairlineBorder),
                    modifier = Modifier.fillMaxWidth()
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
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Noa.khasaev@gmail.com",
                                    style = typography.titleSmall,
                                    color = colorScheme.onSurface
                                )
                                Text(
                                    text = "Einladungslink per E-Mail senden",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                tickHaptic()
                                val inviteSubject = "Einladung: SkyMall Aufzug App"
                                val inviteBody =
                                    "Hallo Noa!\n\nHier ist deine Einladung zur SkyMall Aufzug App (120 Etagen im neuen Google Material-Design):\nhttps://ais-pre-ga46nj4bwhtksqokgthbty-26114007168.europe-west2.run.app\n\nViel Spaß beim Ausprobieren!"
                                try {
                                    val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:Noa.khasaev@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, inviteSubject)
                                        putExtra(Intent.EXTRA_TEXT, inviteBody)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(mailIntent, "Einladung an Noa senden")
                                    )
                                } catch (_: Exception) {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_EMAIL, arrayOf("Noa.khasaev@gmail.com"))
                                        putExtra(Intent.EXTRA_SUBJECT, inviteSubject)
                                        putExtra(Intent.EXTRA_TEXT, inviteBody)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Einladung teilen")
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.onPrimary
                            ),
                            shape = MaterialTheme.shapes.small,
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("send_invite_noa_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Senden",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Einladen", style = typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Close Button
                Button(
                    onClick = {
                        tickHaptic()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.onPrimaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Fertig & Zurück zum Spiel",
                        style = typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    accentColor: Color
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = accentColor,
        letterSpacing = 1.1.sp
    )
}

@Composable
private fun VolumeSliderRow(
    label: String,
    value: Float,
    accent: Color,
    icon: (@Composable () -> Unit)? = null,
    onValueChange: (Float) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val studioColors = StudioTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
            Text(
                text = "${(value * 100).toInt()}%",
                style = TelemetryNumberStyle.copy(
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = accent
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = studioColors.surfaceContainerHighest
            )
        )
    }
}
