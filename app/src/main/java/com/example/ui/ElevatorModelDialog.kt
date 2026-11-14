package com.example.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ElevatorModel
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle

@Composable
fun ElevatorModelDialog(
    currentModel: ElevatorModel,
    onSelectModel: (ElevatorModel) -> Unit,
    onDismiss: () -> Unit
) {
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = studioColors.surfaceContainerLow,
            border = BorderStroke(1.dp, studioColors.hairlineBorder),
            tonalElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 740.dp)
                .padding(vertical = 12.dp)
                .testTag("elevator_model_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛗", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aufzug-Modell wählen",
                                style = typography.headlineMedium,
                                color = colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Unterschiedliche Geschwindigkeit, Kapazität & Kabinenarchitektur",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_model_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ElevatorModel.entries.forEach { model ->
                        val isSelected = model == currentModel
                        val primaryColor = Color(model.primaryAccentColor)
                        val secondaryColor = Color(model.secondaryAccentColor)

                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) studioColors.surfaceContainerHigh else studioColors.surfaceContainer
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) primaryColor else studioColors.hairlineBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSelectModel(model)
                                }
                                .testTag("select_model_${model.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            primaryColor.copy(alpha = 0.28f),
                                                            studioColors.surfaceContainerLowest
                                                        )
                                                    )
                                                )
                                                .border(
                                                    1.dp,
                                                    primaryColor.copy(alpha = 0.55f),
                                                    RoundedCornerShape(14.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = model.emoji, fontSize = 22.sp)
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

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
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(1.dp, primaryColor)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "AKTIV",
                                                    style = typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryColor
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = model.tagline,
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = studioColors.surfaceContainerLowest,
                                        shape = MaterialTheme.shapes.small,
                                        border = BorderStroke(1.dp, studioColors.hairlineBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(9.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Speed,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "TEMPO",
                                                    style = typography.labelSmall,
                                                    color = colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${model.topSpeedKmh} km/h",
                                                style = TelemetryNumberStyle.copy(
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }

                                    Surface(
                                        color = studioColors.surfaceContainerLowest,
                                        shape = MaterialTheme.shapes.small,
                                        border = BorderStroke(1.dp, studioColors.hairlineBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(9.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Group,
                                                    contentDescription = null,
                                                    tint = secondaryColor,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "KAPAZITÄT",
                                                    style = typography.labelSmall,
                                                    color = colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${model.maxPassengers} Pers.",
                                                style = TelemetryNumberStyle.copy(
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = colorScheme.onSurface
                                            )
                                            Text(
                                                text = "max. ${model.maxPayloadKg} kg",
                                                style = typography.labelSmall,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        color = studioColors.surfaceContainerLowest,
                                        shape = MaterialTheme.shapes.small,
                                        border = BorderStroke(1.dp, studioColors.hairlineBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(modifier = Modifier.padding(9.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Palette,
                                                    contentDescription = null,
                                                    tint = studioColors.gold,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "DESIGN",
                                                    style = typography.labelSmall,
                                                    color = colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = model.styleCategory,
                                                style = typography.labelMedium,
                                                color = colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 3.dp),
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(primaryColor))
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(secondaryColor))
                                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(model.wallColorLight)))
                                            }
                                        }
                                    }
                                }

                                if (!isSelected) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onSelectModel(model)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = primaryColor,
                                            contentColor = Color(0xFF0A0F1D)
                                        ),
                                        shape = MaterialTheme.shapes.small,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                    ) {
                                        Text(
                                            text = "Modell auswählen",
                                            style = typography.labelLarge,
                                            fontWeight = FontWeight.Bold
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
}
