package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.MallCatalog
import com.example.model.SkyscraperFloor
import com.example.model.SkyscraperFloorRegistry
import kotlinx.coroutines.launch

/**
 * Decade range filter for floor matrix browsing.
 */
enum class MatrixRangeFilter(val label: String, val range: IntRange) {
    ALL_1_100("🏢 1–120", 1..120),
    SEC_1_20("1–20", 1..20),
    SEC_21_40("21–40", 21..40),
    SEC_41_60("41–60", 41..60),
    SEC_61_80("61–80", 61..80),
    SEC_81_100("81–100", 81..100),
    SEC_101_120("101–120", 101..120)
}

/**
 * High-performance, tactile 1–100 elevator button matrix component.
 * Displays all skyscraper floors as an authentic illuminated elevator button board.
 */
@Composable
fun FloorMatrixComponent(
    currentFloor: Int,
    targetFloor: Int,
    visitedFloors: Set<Int>,
    isMoving: Boolean,
    onSelectFloor: (Int) -> Unit,
    onTravelToFloor: ((Int) -> Unit)? = null,
    onExpandFullscreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false
) {
    var selectedRange by remember { mutableStateOf(MatrixRangeFilter.ALL_1_100) }
    var searchQuery by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    val displayedFloors = remember(selectedRange, searchQuery) {
        val baseList = SkyscraperFloorRegistry.getAllFloors()
        val rangeFiltered = when (selectedRange) {
            MatrixRangeFilter.ALL_1_100 -> baseList
            else -> baseList.filter { it.floorNumber in selectedRange.range }
        }
        if (searchQuery.isBlank()) {
            rangeFiltered
        } else {
            val q = searchQuery.trim().lowercase()
            val parsedNum = q.toIntOrNull()
            if (parsedNum != null) {
                baseList.filter { it.floorNumber == parsedNum }
            } else {
                rangeFiltered.filter {
                    it.name.lowercase().contains(q) ||
                            it.tagline.lowercase().contains(q) ||
                            it.highlights.any { h -> h.lowercase().contains(q) }
                }
            }
        }
    }

    val targetFloorData = remember(targetFloor) {
        SkyscraperFloorRegistry.getFloor(targetFloor)
    }

    // Auto-scroll to target floor when range changes
    LaunchedEffect(targetFloor, selectedRange) {
        val idx = displayedFloors.indexOfFirst { it.floorNumber == targetFloor }
        if (idx >= 0) {
            coroutineScope.launch {
                gridState.animateScrollToItem((idx / 5) * 5)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1C1C1E))
            .border(1.dp, Color(0xFF38383A), RoundedCornerShape(18.dp))
            .padding(if (compactMode) 10.dp else 14.dp)
            .testTag("floor_matrix_component")
    ) {
        // Top Header: Range Selector & Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🔢", fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Alle Etagen (1–100)",
                        fontSize = if (compactMode) 12.sp else 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "${visitedFloors.count { it in 1..100 }}/100 besucht • Antippen zum Wählen",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            if (onExpandFullscreen != null) {
                IconButton(
                    onClick = onExpandFullscreen,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Vollbild Tastenfeld",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Decade Range Switcher Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(MatrixRangeFilter.entries) { filter ->
                val isSelected = selectedRange == filter
                val activeBg = if (filter == MatrixRangeFilter.ALL_1_100) Color(0xFF00F0FF) else Color(0xFFFFB703)
                Surface(
                    color = if (isSelected) activeBg else Color(0xFF162032),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isSelected) activeBg else Color(0xFF2A3C54)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedRange = filter }
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) Color(0xFF090D18) else Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of Illuminated Elevator Buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compactMode) 155.dp else 220.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070A12))
                .border(1.dp, Color(0xFF162032), RoundedCornerShape(12.dp))
                .padding(6.dp)
        ) {
            if (displayedFloors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Keine Etage gefunden", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    state = gridState,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = displayedFloors,
                        key = { it.floorNumber }
                    ) { floor ->
                        val isCurrent = floor.floorNumber == currentFloor
                        val isTarget = floor.floorNumber == targetFloor
                        val isVisited = floor.floorNumber in visitedFloors

                        ElevatorMatrixButton(
                            floor = floor,
                            isCurrent = isCurrent,
                            isTarget = isTarget,
                            isVisited = isVisited,
                            disabled = isMoving,
                            onClick = {
                                onSelectFloor(floor.floorNumber)
                                if (onTravelToFloor != null && floor.floorNumber != currentFloor) {
                                    onTravelToFloor(floor.floorNumber)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Floor Live Preview Strip & Quick Travel
        Surface(
            color = Color(0xFF131D2E),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(targetFloorData.primaryColor).copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "E${targetFloorData.floorNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color(targetFloorData.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = targetFloorData.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = targetFloorData.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "▲ ${targetFloorData.altitudeMeters}m • ${targetFloorData.tagline}",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (onTravelToFloor != null && targetFloor != currentFloor) {
                    Button(
                        onClick = { onTravelToFloor(targetFloor) },
                        enabled = !isMoving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB703),
                            contentColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fahrt", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * Tactile metallic elevator button with LED ring and status badge.
 */
@Composable
fun ElevatorMatrixButton(
    floor: SkyscraperFloor,
    isCurrent: Boolean,
    isTarget: Boolean,
    isVisited: Boolean,
    disabled: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Dynamic button appearance
    val borderColor = when {
        isTarget -> Color(0xFF00F0FF)
        isCurrent -> Color(0xFF10B981)
        floor.isLandmark -> Color(0xFFFFB703).copy(alpha = 0.7f)
        else -> Color(0xFF23354E)
    }

    val bgBrush = when {
        isTarget -> Brush.verticalGradient(
            listOf(Color(0xFF00F0FF).copy(alpha = 0.35f), Color(0xFF0F172A))
        )
        isCurrent -> Brush.verticalGradient(
            listOf(Color(0xFF10B981).copy(alpha = 0.35f), Color(0xFF0F172A))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    }

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isTarget || isCurrent) 2.dp else 1.dp,
            color = if (isTarget) borderColor.copy(alpha = pulseGlow) else borderColor
        ),
        modifier = Modifier
            .height(52.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !disabled, onClick = onClick)
            .testTag("floor_btn_${floor.floorNumber}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Main Floor Number
                Text(
                    text = "${floor.floorNumber}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    color = when {
                        isTarget -> Color(0xFF00F0FF)
                        isCurrent -> Color(0xFF10B981)
                        isVisited -> Color.White
                        else -> Color(0xFFCBD5E1)
                    }
                )

                Spacer(modifier = Modifier.height(1.dp))

                // Subtitle: Status or Emoji
                Text(
                    text = when {
                        isCurrent -> "HIER"
                        isTarget -> "ZIEL"
                        else -> floor.emoji
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isCurrent -> Color(0xFF10B981)
                        isTarget -> Color(0xFF00F0FF)
                        else -> Color(0xFF94A3B8)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Full-screen modal dialog showing the complete interactive 1–100 elevator floor matrix.
 */
@Composable
fun FloorMatrixDialog(
    currentFloor: Int,
    targetFloor: Int,
    visitedFloors: Set<Int>,
    isMoving: Boolean,
    onSelectFloor: (Int) -> Unit,
    onTravelToFloor: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF090D18),
            border = BorderStroke(1.5.dp, Color(0xFF00F0FF).copy(alpha = 0.6f)),
            tonalElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🔢", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Alle 1–100 Etagen",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFB703).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SKYSCRAPER",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFB703),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Tippe auf eine Etage, um sie anzuwählen oder direkt hinzufahren",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Floor Matrix inside dialog
                FloorMatrixComponent(
                    currentFloor = currentFloor,
                    targetFloor = targetFloor,
                    visitedFloors = visitedFloors,
                    isMoving = isMoving,
                    onSelectFloor = onSelectFloor,
                    onTravelToFloor = onTravelToFloor,
                    onExpandFullscreen = null,
                    compactMode = false,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Fertig / Zurück zum Fahrstuhl", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
