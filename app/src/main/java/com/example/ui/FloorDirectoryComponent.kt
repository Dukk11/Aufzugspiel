package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FloorCategory
import com.example.model.FloorSector
import com.example.model.SkyscraperFloor
import com.example.model.SkyscraperFloorRegistry
import kotlinx.coroutines.launch

/**
 * Filter mode for floor directory browsing.
 */
enum class DirectoryFilterMode {
    ALL,
    LANDMARKS,
    FOUNTAINS,
    VISITED
}

enum class DirectoryViewType {
    LIST,
    MATRIX_1_100
}

/**
 * Comprehensive UI Component to browse, inspect, and select among all 120 floors of the Skyscraper Mall,
 * allowing users to either select as target or travel directly with one tap.
 */
@Composable
fun MallFloorDirectoryComponent(
    currentFloor: Int,
    targetFloor: Int,
    visitedFloors: Set<Int>,
    isMoving: Boolean,
    doorsOpen: Boolean,
    onSelectFloor: (Int) -> Unit,
    onTravelToFloor: (Int) -> Unit,
    onStepIntoMall: () -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf<FloorSector?>(null) }
    var filterMode by remember { mutableStateOf(DirectoryFilterMode.ALL) }
    var selectedCategory by remember { mutableStateOf<FloorCategory?>(null) }
    var viewType by remember { mutableStateOf(DirectoryViewType.LIST) }

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredFloors = remember(searchQuery, selectedSector, filterMode, selectedCategory, visitedFloors) {
        SkyscraperFloorRegistry.filterFloors(
            query = searchQuery,
            sector = selectedSector,
            category = selectedCategory,
            landmarksOnly = filterMode == DirectoryFilterMode.LANDMARKS,
            fountainsOnly = filterMode == DirectoryFilterMode.FOUNTAINS,
            visitedOnly = filterMode == DirectoryFilterMode.VISITED,
            visitedFloors = visitedFloors
        )
    }

    val selectedTargetFloorData = remember(targetFloor) {
        SkyscraperFloorRegistry.getFloor(targetFloor)
    }

    val currentFloorData = remember(currentFloor) {
        SkyscraperFloorRegistry.getFloor(currentFloor)
    }

    // Scroll to target floor on initial opening
    LaunchedEffect(Unit) {
        val targetIndex = filteredFloors.indexOfFirst { it.floorNumber == targetFloor }
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex.coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(14.dp)
            .testTag("mall_floor_directory_component")
    ) {
        // 1. TOP HEADER & ELEVATOR SHAFT STATS
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
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🛗", fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Etagen-Verzeichnis",
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
                                text = "120 ETAGEN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFB703),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Aktuell: E$currentFloor ${currentFloorData.emoji} ${currentFloorData.name} (${currentFloorData.altitudeMeters}m)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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

        Spacer(modifier = Modifier.height(10.dp))

        // VIEW MODE TOGGLE: [ 📋 Detaillierte Liste ] vs [ 🔢 Alle Etagen (1–100) ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF131D2E))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isList = viewType == DirectoryViewType.LIST
            Surface(
                color = if (isList) Color(0xFF00F0FF) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewType = DirectoryViewType.LIST }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "📋", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Detaillierte Liste",
                        fontSize = 11.sp,
                        fontWeight = if (isList) FontWeight.Black else FontWeight.Bold,
                        color = if (isList) Color(0xFF090D18) else Color(0xFFCBD5E1)
                    )
                }
            }

            val isMatrix = viewType == DirectoryViewType.MATRIX_1_100
            Surface(
                color = if (isMatrix) Color(0xFFFFB703) else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewType = DirectoryViewType.MATRIX_1_100 }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔢", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alle Etagen (1–100)",
                        fontSize = 11.sp,
                        fontWeight = if (isMatrix) FontWeight.Black else FontWeight.Bold,
                        color = if (isMatrix) Color(0xFF090D18) else Color(0xFFCBD5E1)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (viewType == DirectoryViewType.MATRIX_1_100) {
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
        } else {
            // 2. SEARCH BAR & QUICK NUMBER JUMP
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Etage suchen (z.B. 42, Dino, Eis, Arcade, 100)...",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Löschen",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF131D2E),
                unfocusedContainerColor = Color(0xFF101725),
                focusedBorderColor = Color(0xFF00F0FF),
                unfocusedBorderColor = Color(0xFF23354E),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("directory_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 3. SECTOR QUICK-JUMP TABS (1-20, 21-40, 41-60, 61-80, 81-100, 101-120)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isSelected = selectedSector == null
                Surface(
                    color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedSector = null }
                ) {
                    Text(
                        text = "Alle Sektoren",
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF090D16) else Color(0xFFCBD5E1),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            items(SkyscraperFloorRegistry.SECTORS) { sector ->
                val isSelected = selectedSector?.id == sector.id
                Surface(
                    color = if (isSelected) Color(0xFFFFB703) else Color(0xFF162032),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFFFFB703) else Color(0xFF2A3C54)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            selectedSector = if (isSelected) null else sector
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(text = sector.emoji, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sector.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) Color(0xFF090D16) else Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. QUICK FILTER CHIPS (Landmarks, Fountains, Visited)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectoryFilterChip(
                label = "Alle (${SkyscraperFloorRegistry.TOTAL_FLOORS})",
                selected = filterMode == DirectoryFilterMode.ALL,
                onClick = { filterMode = DirectoryFilterMode.ALL }
            )
            DirectoryFilterChip(
                label = "🌟 Landmarks (${SkyscraperFloorRegistry.getLandmarkFloors().size})",
                selected = filterMode == DirectoryFilterMode.LANDMARKS,
                accentColor = Color(0xFFFFB703),
                onClick = { filterMode = DirectoryFilterMode.LANDMARKS }
            )
            DirectoryFilterChip(
                label = "🪙 Goldbrunnen (${SkyscraperFloorRegistry.getFountainFloors().size})",
                selected = filterMode == DirectoryFilterMode.FOUNTAINS,
                accentColor = Color(0xFF10B981),
                onClick = { filterMode = DirectoryFilterMode.FOUNTAINS }
            )
            DirectoryFilterChip(
                label = "👣 Besucht (${visitedFloors.size})",
                selected = filterMode == DirectoryFilterMode.VISITED,
                accentColor = Color(0xFF38BDF8),
                onClick = { filterMode = DirectoryFilterMode.VISITED }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. MAIN 100+ FLOORS LIST
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (filteredFloors.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keine Etage gefunden für \"$searchQuery\"",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedSector = null
                                filterMode = DirectoryFilterMode.ALL
                                selectedCategory = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Text("Filter zurücksetzen", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredFloors,
                        key = { it.floorNumber }
                    ) { floor ->
                        val isCurrent = floor.floorNumber == currentFloor
                        val isTarget = floor.floorNumber == targetFloor
                        val isVisited = floor.floorNumber in visitedFloors

                        SkyscraperFloorCard(
                            floor = floor,
                            isCurrent = isCurrent,
                            isTarget = isTarget,
                            isVisited = isVisited,
                            currentFloor = currentFloor,
                            isMoving = isMoving,
                            doorsOpen = doorsOpen,
                            onSelect = { onSelectFloor(floor.floorNumber) },
                            onTravelNow = { onTravelToFloor(floor.floorNumber) },
                            onStepIntoMall = onStepIntoMall
                        )
                    }
                }
            }
        }
    }

        // 6. STICKY DOCKED TRAVEL BAR AT BOTTOM
        Surface(
            color = Color(0xFF111827),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.5.dp, Color(selectedTargetFloorData.primaryColor)),
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, shape = RoundedCornerShape(18.dp))
                .testTag("directory_travel_dock")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ziel: Etage ${selectedTargetFloorData.floorNumber}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(selectedTargetFloorData.primaryColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedTargetFloorData.emoji,
                            fontSize = 12.sp
                        )
                    }
                    val deltaFloors = selectedTargetFloorData.floorNumber - currentFloor
                    val estTime = selectedTargetFloorData.estimateTravelSeconds(currentFloor)
                    val travelDescription = when {
                        deltaFloors == 0 -> "Aktuell auf dieser Etage"
                        deltaFloors > 0 -> "▲ +$deltaFloors Etagen nach oben (~$estTime s)"
                        else -> "▼ $deltaFloors Etagen nach unten (~$estTime s)"
                    }
                    Text(
                        text = travelDescription,
                        fontSize = 11.sp,
                        color = if (deltaFloors == 0) Color(0xFF10B981) else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Unified Action Button
                if (targetFloor == currentFloor && doorsOpen && !isMoving) {
                    Button(
                        onClick = {
                            onDismiss()
                            onStepIntoMall()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("directory_dock_enter_mall_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mall betreten", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    val canStart = !isMoving && targetFloor != currentFloor
                    Button(
                        onClick = { onTravelToFloor(targetFloor) },
                        enabled = canStart,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB703),
                            contentColor = Color(0xFF0F172A),
                            disabledContainerColor = Color(0xFF1E293B),
                            disabledContentColor = Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("directory_dock_travel_button")
                    ) {
                        Icon(
                            imageVector = if (targetFloor > currentFloor) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMoving) "Fährt..." else "Fahrt starten 🛗",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Floor Card representing a single floor among the 100+ floors.
 */
@Composable
private fun SkyscraperFloorCard(
    floor: SkyscraperFloor,
    isCurrent: Boolean,
    isTarget: Boolean,
    isVisited: Boolean,
    currentFloor: Int,
    isMoving: Boolean,
    doorsOpen: Boolean,
    onSelect: () -> Unit,
    onTravelNow: () -> Unit,
    onStepIntoMall: () -> Unit
) {
    val borderColor = when {
        isTarget -> Color(0xFF00F0FF)
        isCurrent -> Color(0xFF10B981)
        floor.isLandmark -> Color(0xFFFFB703).copy(alpha = 0.7f)
        else -> Color(0xFF1E293B)
    }

    val containerColor = when {
        isTarget -> Color(0xFF0B2138)
        isCurrent -> Color(0xFF0B2D24)
        floor.isLandmark -> Color(0xFF1B192E)
        else -> Color(0xFF121927)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isTarget || isCurrent) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("floor_card_${floor.floorNumber}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Row 1: Floor Number Badge, Title, Category and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Number Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(floor.primaryColor),
                                        Color(floor.secondaryColor)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${floor.floorNumber}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0F172A)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${floor.emoji} ${floor.name}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = floor.tagline,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Status Indicator Chip (HIER / ZIEL / LANDMARK)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isTarget) {
                        Surface(
                            color = Color(0xFF00F0FF),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ZIEL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF090D16),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (isCurrent) {
                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "HIER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (floor.isLandmark) {
                        Surface(
                            color = Color(0xFFFFB703).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFFFFB703).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "★ TOP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB703),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row 2: Metadata Tags (Altitude, Crowd, Fountain, Visited)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Altitude Tag
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "▲ ${floor.altitudeMeters}m",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Category Tag
                    Surface(
                        color = Color(floor.category.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${floor.category.emoji} ${floor.category.title.split(" ").first()}",
                            fontSize = 10.sp,
                            color = Color(floor.category.colorHex),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Crowd Tag
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${floor.crowdLevel.emoji} ${floor.crowdLevel.label.split(" ").first()}",
                            fontSize = 10.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Coin Fountain Tag
                    if (floor.hasCoinFountain) {
                        Surface(
                            color = Color(0xFFFFD166).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🪙 Brunnen",
                                fontSize = 10.sp,
                                color = Color(0xFFFFD166),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Visited Tag
                    if (isVisited) {
                        Surface(
                            color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "👣 Besucht",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Travel Shortcut Button
                if (isCurrent && doorsOpen && !isMoving) {
                    Button(
                        onClick = onStepIntoMall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Betreten 🛒", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (!isMoving && floor.floorNumber != currentFloor) {
                    Surface(
                        color = Color(0xFF00F0FF).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onTravelNow)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Reisen 🛗",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00F0FF)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter Chip pill.
 */
@Composable
private fun DirectoryFilterChip(
    label: String,
    selected: Boolean,
    accentColor: Color = Color(0xFF00F0FF),
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) accentColor else Color(0xFF162032),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (selected) accentColor else Color(0xFF2A3C54)),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
            color = if (selected) Color(0xFF090D16) else Color(0xFFCBD5E1),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}

/**
 * Fullscreen / modal dialog wrapper for MallFloorDirectoryComponent.
 */
@Composable
fun MallFloorDirectoryDialog(
    currentFloor: Int,
    targetFloor: Int,
    visitedFloors: Set<Int>,
    isMoving: Boolean,
    doorsOpen: Boolean,
    onSelectFloor: (Int) -> Unit,
    onTravelToFloor: (Int) -> Unit,
    onStepIntoMall: () -> Unit = {},
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
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF090D16),
            border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
        ) {
            MallFloorDirectoryComponent(
                currentFloor = currentFloor,
                targetFloor = targetFloor,
                visitedFloors = visitedFloors,
                isMoving = isMoving,
                doorsOpen = doorsOpen,
                onSelectFloor = onSelectFloor,
                onTravelToFloor = onTravelToFloor,
                onStepIntoMall = onStepIntoMall,
                onDismiss = onDismiss
            )
        }
    }
}
