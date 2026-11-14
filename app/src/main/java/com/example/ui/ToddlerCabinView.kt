package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SkyscraperFloorRegistry
import com.example.viewmodel.ElevatorGameState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Friendly toddler-friendly representation of floors 1 to 10.
 * Designed with large comforting symbols, cheerful colors, and relatable themes.
 */
data class ToddlerFloorItem(
    val floorNumber: Int,
    val emoji: String,
    val childName: String,
    val badgeColor: Long
)

val TODDLER_FLOORS = listOf(
    ToddlerFloorItem(1, "🧸", "Teddybären", 0xFFFFB703),
    ToddlerFloorItem(2, "🎈", "Luftballons", 0xFFFF6B6B),
    ToddlerFloorItem(3, "🍦", "Eiscreme", 0xFF4D96FF),
    ToddlerFloorItem(4, "🚂", "Eisenbahn", 0xFF6BCB77),
    ToddlerFloorItem(5, "🎨", "Malen", 0xFFFFD93D),
    ToddlerFloorItem(6, "🐠", "Bunte Fische", 0xFF38BDF8),
    ToddlerFloorItem(7, "☁️", "Hüpfwolke", 0xFFA78BFA),
    ToddlerFloorItem(8, "✈️", "Flugzeuge", 0xFFFB923C),
    ToddlerFloorItem(9, "🫧", "Seifenblasen", 0xFF34D399),
    ToddlerFloorItem(10, "☀️", "Sonne", 0xFFFBBF24)
)

/**
 * Calm, gentle, distraction-free Toddler Cabin View ("Ruhiger Kleinkind-Modus").
 * Eliminates visual restlessness, jittering, flashing indicators, and complicated buttons.
 * Provides clear 64dp buttons, friendly colors, spoken voice feedback, and a safe parental gate.
 */
@Composable
fun ToddlerCabinView(
    state: ElevatorGameState,
    modifier: Modifier = Modifier,
    onFloorSelected: (Int) -> Unit,
    onOpenDoors: () -> Unit,
    onCloseDoors: () -> Unit,
    onRequestParentExit: () -> Unit,
    onDirectExitSuccess: () -> Unit
) {
    val currentFloorData = SkyscraperFloorRegistry.getFloor(state.currentFloor)

    // Gentle door opening progress without flashing or rapid vibration
    val doorProgress by animateFloatAsState(
        targetValue = if (state.doorsOpen && !state.isMoving) 1f else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "toddlerDoorProgress"
    )

    // Calming, pastel-rich background gradient that is soothing to toddlers' eyes
    val cabinBackground = Brush.verticalGradient(
        listOf(
            Color(0xFF1E2433),
            Color(0xFF222B3D),
            Color(0xFF1B2230)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(26.dp))
            .background(cabinBackground)
            .border(2.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(26.dp))
            .testTag("toddler_cabin_view")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ══════════════════════════════════════════════════════════
            // 1. TOP HEADER: CALM PARENT GATE & BIG FRIENDLY FLOOR DISPLAY
            // ══════════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Friendly Mode Badge
                Surface(
                    color = Color(0xFFFFB703).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFFFB703))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🧸", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KLEINKIND-MODUS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD166),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Parent Exit Gate: 3-Second Press & Hold Gesture OR tap for PIN
                ParentHoldToExitButton(
                    onExitTriggered = onDirectExitSuccess,
                    onTapFallback = onRequestParentExit
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ══════════════════════════════════════════════════════════
            // 2. LARGE SOOTHING FLOOR CARD (NO FLICKERING, NO SPEEDOMETER)
            // ══════════════════════════════════════════════════════════
            val matchedFloor = TODDLER_FLOORS.firstOrNull { it.floorNumber == state.currentFloor }
            val displayEmoji = matchedFloor?.emoji ?: currentFloorData.emoji
            val displayName = matchedFloor?.childName ?: currentFloorData.name

            Surface(
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, Color(0xFF60A5FA).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isMoving) {
                        Text(
                            text = "🎈 Der Fahrstuhl fährt ganz sanft… 🎈",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ziel: Etage ${state.targetFloor} ✨",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = displayEmoji, fontSize = 34.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "ETAGE ${state.currentFloor}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFFD166)
                                )
                                Text(
                                    text = displayName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ══════════════════════════════════════════════════════════
            // 3. CALM ELEVATOR DOORS ANIMATION (SOFT SLIDE, NO JITTER)
            // ══════════════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B101B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Inside peek view (Reveals friendly illustration when doors open)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = displayEmoji, fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (state.doorsOpen) "Türen sind offen! Hereinspaziert!" else "Türen geschlossen",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.doorsOpen) Color(0xFF34D399) else Color(0xFF94A3B8)
                    )
                }

                // Left sliding soft pastel door
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterStart)
                        .offset(x = (-140 * doorProgress).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF334155), Color(0xFF475569))
                            )
                        )
                        .border(1.dp, Color(0xFF64748B))
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .background(Color(0xFFFFD166))
                    )
                }

                // Right sliding soft pastel door
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .align(Alignment.CenterEnd)
                        .offset(x = (140 * doorProgress).dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF475569), Color(0xFF334155))
                            )
                        )
                        .border(1.dp, Color(0xFF64748B))
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .align(Alignment.CenterStart)
                            .background(Color(0xFFFFD166))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ══════════════════════════════════════════════════════════
            // 4. BIG TACTILE TODDLER FLOOR BUTTONS (1 to 10)
            // ══════════════════════════════════════════════════════════
            Text(
                text = "Drücke auf eine Zahl:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(TODDLER_FLOORS, key = { it.floorNumber }) { item ->
                    val isCurrent = item.floorNumber == state.currentFloor
                    val isTarget = item.floorNumber == state.targetFloor && state.isMoving
                    val itemColor = Color(item.badgeColor)

                    Surface(
                        color = when {
                            isTarget -> Color(0xFF38BDF8)
                            isCurrent -> Color(0xFF10B981)
                            else -> Color(0xFF1E293B)
                        },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(
                            width = if (isTarget || isCurrent) 2.5.dp else 1.5.dp,
                            color = when {
                                isTarget -> Color.White
                                isCurrent -> Color(0xFF6EE7B7)
                                else -> itemColor.copy(alpha = 0.8f)
                            }
                        ),
                        modifier = Modifier
                            .height(68.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = !state.isMoving) {
                                onFloorSelected(item.floorNumber)
                            }
                            .testTag("toddler_floor_btn_${item.floorNumber}")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.emoji,
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${item.floorNumber}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isTarget || isCurrent) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ══════════════════════════════════════════════════════════
            // 5. LARGE DOOR BUTTONS: TÜREN ÖFFNEN / SCHLIESSEN
            // ══════════════════════════════════════════════════════════
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = if (state.doorsOpen) Color(0xFF059669) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF34D399)),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !state.isMoving, onClick = onOpenDoors)
                        .testTag("toddler_open_doors_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "◄||►", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tür auf",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = if (!state.doorsOpen) Color(0xFFD97706) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !state.isMoving, onClick = onCloseDoors)
                        .testTag("toddler_close_doors_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "►||◄", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tür zu",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3-Second Hold-to-Exit Button for parents.
 * Shows a filling progress ring when held down for 3 seconds.
 * Short tap offers the fallback parental PIN prompt.
 */
@Composable
fun ParentHoldToExitButton(
    onExitTriggered: () -> Unit,
    onTapFallback: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var isHolding by remember { mutableStateOf(false) }
    var holdJob by remember { mutableStateOf<Job?>(null) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E293B))
            .border(1.5.dp, Color(0xFF64748B), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        holdProgress = 0f
                        holdJob?.cancel()
                        holdJob = scope.launch {
                            val totalSteps = 60
                            for (step in 1..totalSteps) {
                                delay(50L) // 60 * 50ms = 3000ms (3 seconds)
                                holdProgress = step.toFloat() / totalSteps
                            }
                            // 3 seconds reached!
                            onExitTriggered()
                            isHolding = false
                            holdProgress = 0f
                        }
                        tryAwaitRelease()
                        isHolding = false
                        holdJob?.cancel()
                        if (holdProgress < 0.95f && holdProgress > 0.05f) {
                            // User released before 3s -> open PIN dialog as fallback
                            onTapFallback()
                        }
                        holdProgress = 0f
                    },
                    onTap = {
                        onTapFallback()
                    }
                )
            }
            .testTag("parent_gate_exit_button")
    ) {
        if (isHolding) {
            CircularProgressIndicator(
                progress = { holdProgress },
                color = Color(0xFF10B981),
                strokeWidth = 3.dp,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = "🔒",
            fontSize = 18.sp
        )
    }
}
