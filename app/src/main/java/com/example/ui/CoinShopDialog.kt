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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.billing.BillingStatus
import com.example.billing.CoinPackage
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle

@Composable
fun CoinShopDialog(
    currentCoins: Int,
    packages: List<CoinPackage>,
    billingStatus: BillingStatus,
    onBuyPackage: (CoinPackage) -> Unit,
    onDismiss: () -> Unit
) {
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val haptics = LocalHapticFeedback.current

    // Google Play Families Policy Parental Gate state
    var selectedPackageForPurchase by remember { mutableStateOf<CoinPackage?>(null) }
    var numA by remember { mutableStateOf((3..8).random()) }
    var numB by remember { mutableStateOf((3..9).random()) }
    var answerInput by remember { mutableStateOf("") }
    var gateError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(MaterialTheme.shapes.extraLarge)
                .border(1.dp, studioColors.hairlineBorder, MaterialTheme.shapes.extraLarge)
                .testTag("coin_shop_dialog"),
            color = studioColors.surfaceContainerLow,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Münz-Bank & In-App-Kauf",
                            style = typography.headlineMedium,
                            color = colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val statusColor = when (billingStatus) {
                                is BillingStatus.Connected -> studioColors.emerald
                                is BillingStatus.Connecting -> studioColors.gold
                                else -> colorScheme.onSurfaceVariant
                            }
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (billingStatus) {
                                    is BillingStatus.Connected -> "Google Play Abrechnung bereit"
                                    is BillingStatus.Connecting -> "Verbinde mit Google Play..."
                                    else -> "Google Play In-App-Abrechnung"
                                },
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
                            .testTag("close_coin_shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Balance Banner
                Surface(
                    color = studioColors.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, studioColors.gold.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Aktuelles Guthaben",
                                    style = typography.bodySmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currentCoins Münzen",
                                    style = TelemetryNumberStyle.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = studioColors.gold
                                )
                            }
                        }

                        Surface(
                            color = studioColors.surfaceContainerLowest,
                            shape = MaterialTheme.shapes.extraSmall,
                            border = BorderStroke(1.dp, studioColors.hairlineBorder)
                        ) {
                            Text(
                                text = "Spiel-Währung",
                                style = typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coin Packages List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(packages) { pack ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (pack.isPopular) studioColors.surfaceContainerHigh else studioColors.surfaceContainer
                            ),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(
                                if (pack.isPopular) 1.5.dp else 1.dp,
                                if (pack.isPopular) studioColors.gold else studioColors.hairlineBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = pack.iconEmoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = pack.title,
                                                style = typography.titleSmall,
                                                color = colorScheme.onSurface
                                            )
                                            if (pack.isPopular) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = studioColors.gold,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "TIPP",
                                                        style = typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1A1400),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+${pack.coins} Münzen",
                                            style = TelemetryNumberStyle.copy(
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = studioColors.gold
                                        )
                                        Text(
                                            text = pack.description,
                                            style = typography.bodySmall,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        numA = (3..8).random()
                                        numB = (3..9).random()
                                        answerInput = ""
                                        gateError = false
                                        selectedPackageForPurchase = pack
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (pack.isPopular) studioColors.gold else colorScheme.primary,
                                        contentColor = if (pack.isPopular) Color(0xFF1A1400) else colorScheme.onPrimary
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.testTag("buy_pack_${pack.productId}")
                                ) {
                                    Text(
                                        text = pack.formattedPrice,
                                        style = TelemetryNumberStyle.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Factual Security & Policy Notice
                Surface(
                    color = studioColors.surfaceContainer,
                    shape = MaterialTheme.shapes.small,
                    border = BorderStroke(1.dp, studioColors.hairlineBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = studioColors.emerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Abrechnung über Google Play In-App-Abrechnung (Google LLC). Kindersichere Eltern-Abfrage vor jedem Kauf. Münzen werden nach erfolgreicher Bestätigung direkt gutgeschrieben.",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Google Play Families Policy Parental Gate Dialog
    selectedPackageForPurchase?.let { pack ->
        Dialog(onDismissRequest = { selectedPackageForPurchase = null }) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = studioColors.surfaceContainerLow,
                border = BorderStroke(1.5.dp, colorScheme.primary),
                tonalElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("parental_purchase_gate_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Eltern-Sicherheitsabfrage",
                                style = typography.titleMedium,
                                color = colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { selectedPackageForPurchase = null },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Abbrechen",
                                tint = colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Um den Kauf von „${pack.title}“ (${pack.formattedPrice}) fortzusetzen, müssen Eltern die folgende Rechenaufgabe lösen:",
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = studioColors.surfaceContainerLowest,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, studioColors.hairlineBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Wie viel ist $numA × $numB ?",
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = studioColors.gold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (answerInput.isEmpty()) "Ergebnis eingeben" else answerInput,
                                style = TelemetryNumberStyle.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (answerInput.isEmpty()) colorScheme.onSurfaceVariant else colorScheme.onSurface
                            )
                        }
                    }

                    if (gateError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Falsches Ergebnis. Bitte erneut versuchen.",
                            style = typography.labelMedium,
                            color = studioColors.coral
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val digits = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "OK")
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        digits.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { key ->
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(studioColors.surfaceContainer)
                                            .border(1.dp, studioColors.hairlineBorder, CircleShape)
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                when (key) {
                                                    "C" -> {
                                                        answerInput = ""
                                                        gateError = false
                                                    }
                                                    "OK" -> {
                                                        val expected = (numA * numB).toString()
                                                        if (answerInput.trim() == expected) {
                                                            selectedPackageForPurchase = null
                                                            onBuyPackage(pack)
                                                        } else {
                                                            gateError = true
                                                            answerInput = ""
                                                        }
                                                    }
                                                    else -> {
                                                        if (answerInput.length < 3) {
                                                            gateError = false
                                                            answerInput += key
                                                            val expected = (numA * numB).toString()
                                                            if (answerInput == expected) {
                                                                selectedPackageForPurchase = null
                                                                onBuyPackage(pack)
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            style = TelemetryNumberStyle.copy(
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = when (key) {
                                                "OK" -> studioColors.emerald
                                                "C" -> studioColors.coral
                                                else -> colorScheme.onSurface
                                            }
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
