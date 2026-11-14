package com.example.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.StudioTheme

enum class LegalTab(val title: String, val icon: ImageVector) {
    PRIVACY("Datenschutz", Icons.Default.Security),
    IMPRINT("Impressum", Icons.Default.Info),
    CHILD_SAFETY("Jugendschutz", Icons.Default.VerifiedUser),
    LICENSES("Lizenzen", Icons.Default.Gavel)
}

@Composable
fun LegalDialog(
    onDismiss: () -> Unit,
    onResetData: () -> Unit
) {
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = studioColors.surfaceContainerLow,
            border = BorderStroke(1.dp, studioColors.hairlineBorder),
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp)
                .testTag("legal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rechtliche Angaben",
                            style = typography.headlineSmall,
                            color = colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_legal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = studioColors.surfaceContainer,
                    contentColor = colorScheme.primary,
                    edgePadding = 4.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                ) {
                    LegalTab.entries.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    style = typography.labelMedium,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedTabIndex == index) colorScheme.primary else colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (LegalTab.entries[selectedTabIndex]) {
                        LegalTab.PRIVACY -> PrivacyPolicySection(onRequestDataReset = { showDeleteConfirmDialog = true })
                        LegalTab.IMPRINT -> ImprintSection()
                        LegalTab.CHILD_SAFETY -> ChildSafetySection()
                        LegalTab.LICENSES -> LicensesSection()
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Schließen", style = typography.labelLarge)
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Spieldaten zurücksetzen",
                    style = typography.titleMedium,
                    color = colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Möchten Sie alle lokal gespeicherten Spieldaten (Münzen, Rucksack, Etagen) zurücksetzen?",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onResetData()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = studioColors.coral,
                        contentColor = Color.White
                    )
                ) {
                    Text("Zurücksetzen", style = typography.labelLarge)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Abbrechen", style = typography.labelLarge)
                }
            },
            containerColor = studioColors.surfaceContainer,
            shape = MaterialTheme.shapes.large
        )
    }
}

@Composable
private fun PrivacyPolicySection(onRequestDataReset: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
        LegalCard(title = "Datenschutzerklärung") {
            Text(
                text = "Verantwortlicher:\n" +
                    "D. Khasaev\n" +
                    "E-Mail: D.Khasaev@gmail.com\n\n" +
                    "Erhebung und Speicherung von Daten:\n" +
                    "Diese Anwendung erhebt keine personenbezogenen Daten. Es werden weder Namen, Adressen noch Kontaktdaten erfasst. Es existieren keine Benutzerkonten und keine Registrierung.\n\n" +
                    "Tracking & Werbung:\n" +
                    "Die App nutzt keine Werbedienste und keine Analysedienste zur Nachverfolgung des Nutzerverhaltens.\n\n" +
                    "Berechtigungen:\n" +
                    "Die Berechtigung VIBRATE wird ausschließlich für kurzes Vibrationsfeedback bei Tastenberührungen verwendet.\n\n" +
                    "Lokale Speicherung:\n" +
                    "Spielstände und Einstellungen werden ausschließlich lokal auf dem Endgerät gespeichert.",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRequestDataReset,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.errorContainer,
                    contentColor = colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Lokale Spieldaten zurücksetzen", style = typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ImprintSection() {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
        LegalCard(title = "Impressum gem. § 5 DDG") {
            Text(
                text = "D. Khasaev\n" +
                    "Kontakt: D.Khasaev@gmail.com\n\n" +
                    "Inhaltlich Verantwortlicher:\n" +
                    "D. Khasaev\n\n" +
                    "Online-Streitbeilegung:\n" +
                    "Plattform der EU-Kommission: https://ec.europa.eu/consumers/odr/\n" +
                    "Zur Teilnahme an einem Streitbeilegungsverfahren vor einer Verbraucherschlichtungsstelle besteht keine Verpflichtung.",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LegalCard(title = "Haftungsausschluss") {
            Text(
                text = "Die Inhalte dieser Anwendung dienen ausschließlich Unterhaltungszwecken. Es wird keine Gewähr für die ständige Verfügbarkeit übernommen.",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChildSafetySection() {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
        LegalCard(title = "Jugendschutz & Google Play Familienprogramm") {
            Text(
                text = "• Für Kinder & Familien geeignet: Die App enthält keinerlei Gewalt, Schimpfwörter oder jugendgefährdende Inhalte.\n" +
                    "• Kleinkind-Modus („Ruhiger Kinder-Fahrstuhl“): Bietet eine reizarme, beruhigte Ansicht ohne Flackern mit gesprochenen Etagen-Zahlen und 3-Sekunden-Eltern-Sicherung.\n" +
                    "• Kindersichere Eltern-Sperre (Parental Gate): Vor jedem optionalen In-App-Kauf von Spielmünzen muss eine Eltern-Sicherheitsabfrage (Rechenaufgabe) gelöst werden. Versehentliche Käufe durch Kinder sind ausgeschlossen.\n" +
                    "• Keine Fremdwerbung: Die Anwendung ist komplett werbefrei (keine Werbebanner, keine Pop-ups, keine Tracking-Dienste Dritter).\n" +
                    "• Datenschutz für Kinder: Es werden keinerlei personenbezogene Daten von Kindern oder Familien erhoben, gespeichert oder weitergegeben.",
                style = typography.bodySmall,
                lineHeight = 18.sp,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicensesSection() {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    Column(modifier = Modifier.padding(horizontal = 2.dp)) {
        LegalCard(title = "Marken & Bezeichnungen") {
            Text(
                text = "Alle in der Anwendung genannten Etagen- und Geschäftsnamen sind fiktiv. Etwaige Marken und Warenzeichen stehen ihren jeweiligen Inhabern zu.",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LegalCard(title = "Software-Lizenzen") {
            Text(
                text = "Bestandteile dieser Anwendung basieren auf Open-Source-Komponenten (Android Jetpack, Compose, Kotlin), lizenziert unter der Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0).",
                style = typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LegalCard(
    title: String,
    content: @Composable () -> Unit
) {
    val studioColors = StudioTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
        border = BorderStroke(1.dp, studioColors.hairlineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = studioColors.hairlineBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
