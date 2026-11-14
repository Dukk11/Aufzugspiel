package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Elevator
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MallCatalog
import com.example.model.Shop
import com.example.model.ShopItem
import com.example.model.SkyscraperFloorRegistry
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.ElevatorGameState

@Composable
fun MallFloorScreen(
    state: ElevatorGameState,
    onEnterShop: (Shop) -> Unit,
    onReturnToElevator: () -> Unit,
    onClaimFountainCoins: () -> Unit,
    onOpenBackpack: () -> Unit,
    onOpenCoinShop: () -> Unit = {},
    onBuyItem: (ShopItem) -> Unit = {},
    onUseItem: (ShopItem) -> Unit = {},
    onDismissItemEffect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val floorData = MallCatalog.getFloor(state.currentFloor)
    val skyFloor = SkyscraperFloorRegistry.getFloor(state.currentFloor)
    val zone = floorData.zone
    val zoneImageRes = MallCatalog.getZoneDrawableRes(state.currentFloor)
    val allFloorItems = remember(floorData) {
        floorData.shops.flatMap { it.items }
    }
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Material 3 Expressive Mall Atrium Top Bar
        Surface(
            color = studioColors.surfaceContainer,
            tonalElevation = 4.dp,
            border = BorderStroke(0.5.dp, studioColors.hairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onReturnToElevator()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("back_to_elevator_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Elevator,
                        contentDescription = "Fahrstuhl betreten",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Zum Fahrstuhl",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = studioColors.surfaceContainerHigh,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, studioColors.gold.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onOpenCoinShop)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${state.coins}",
                                style = TelemetryNumberStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                color = studioColors.gold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.labelLarge,
                                color = studioColors.emerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(studioColors.surfaceContainerHigh)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                            .clickable(onClick = onOpenBackpack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Rucksack",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. ARCHITECTURAL ZONE PANORAMA HERO CARD
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, Color(zone.primaryColor).copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(188.dp)
                        ) {
                            Image(
                                painter = painterResource(id = zoneImageRes),
                                contentDescription = zone.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF070A10).copy(alpha = 0.25f),
                                                Color(0xFF070A10).copy(alpha = 0.52f),
                                                studioColors.surfaceContainer
                                            )
                                        )
                                    )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .align(Alignment.TopCenter),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF090D16).copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(zone.secondaryColor).copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = "ETAGE ${state.currentFloor} • ${zone.title.uppercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(zone.secondaryColor),
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }

                                Surface(
                                    color = Color(0xFF090D16).copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, studioColors.sky.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "▲ ${skyFloor.altitudeMeters} m",
                                        style = TelemetryNumberStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = studioColors.sky,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .align(Alignment.BottomStart),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(studioColors.surfaceContainerLowest.copy(alpha = 0.94f))
                                        .border(1.5.dp, Color(zone.secondaryColor), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = zone.emoji, fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = floorData.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = skyFloor.tagline,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = zone.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                skyFloor.highlights.take(3).forEachIndexed { idx, highlight ->
                                    Surface(
                                        color = studioColors.surfaceContainerHigh,
                                        shape = RoundedCornerShape(20.dp),
                                        border = BorderStroke(0.8.dp, studioColors.hairlineBorder),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(zone.primaryColor).copy(alpha = 0.25f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    style = TelemetryNumberStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = Color(zone.secondaryColor)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = highlight,
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
                                                color = MaterialTheme.colorScheme.onSurface,
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
            }

            // 2. LIVE INTERACTIVE ITEM STAGE
            item {
                AnimatedVisibility(
                    visible = state.activeItemEffect != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    state.activeItemEffect?.let { activeItem ->
                        InteractiveItemEffectBanner(
                            item = activeItem,
                            comboCount = state.activeItemEffectCombo,
                            onReplayEffect = { onUseItem(activeItem) },
                            onDismiss = onDismissItemEffect
                        )
                    }
                }
            }

            // 3. INTERACTIVE QUICK-TRY & BUY SHOWCASE ROW
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✨ AUSPROBIEREN & ENTDECKEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = studioColors.gold
                        )
                        Text(
                            text = "${allFloorItems.size} Highlights",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allFloorItems, key = { it.id }) { item ->
                            val ownedCount = state.inventory[item.id]?.second ?: 0
                            val canAfford = state.coins >= item.price

                            Card(
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        state.activeItemEffect?.id == item.id -> studioColors.gold
                                        ownedCount > 0 -> studioColors.emerald.copy(alpha = 0.7f)
                                        else -> studioColors.hairlineBorder
                                    }
                                ),
                                modifier = Modifier.width(220.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(studioColors.surfaceContainerHigh)
                                                .border(1.dp, Color(zone.secondaryColor).copy(alpha = 0.7f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = item.emoji, fontSize = 22.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "🪙 ${item.price} Münzen",
                                                style = TelemetryNumberStyle.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                                                color = studioColors.gold,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.height(34.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onUseItem(item) },
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "✨ Testen",
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        }

                                        Button(
                                            onClick = { onBuyItem(item) },
                                            enabled = canAfford,
                                            shape = RoundedCornerShape(14.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (canAfford) studioColors.emerald else studioColors.surfaceContainerHighest,
                                                contentColor = if (studioColors.isDark && canAfford) Color(0xFF062314) else Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (ownedCount > 0) "Besitz (${ownedCount}x)" else "Kaufen",
                                                style = MaterialTheme.typography.labelMedium,
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
            }

            // 4. INTERACTIVE MALL WISHING FOUNTAIN
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, studioColors.sky.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .clickable(onClick = onClaimFountainCoins)
                        .testTag("mall_fountain_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(studioColors.surfaceContainerHigh)
                                .border(1.5.dp, studioColors.sky, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "⛲", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Glücks-Münzbrunnen",
                                style = MaterialTheme.typography.titleMedium,
                                color = studioColors.sky,
                                maxLines = 1
                            )
                            Text(
                                text = "Tippe für kostenlose Glücksmünzen auf dieser Etage!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onClaimFountainCoins,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = "Wünschen ✨",
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.5.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "GESCHÄFTE & ERLEBNIS-SHOPS AUF ETAGE ${state.currentFloor}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(floorData.shops) { shop ->
                StorefrontCard(
                    shop = shop,
                    onEnter = {
                        if (state.hapticsEnabled) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onEnterShop(shop)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun InteractiveItemEffectBanner(
    item: ShopItem,
    comboCount: Int,
    onReplayEffect: () -> Unit,
    onDismiss: () -> Unit
) {
    val studioColors = StudioTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "itemEffectPulse")
    val emojiScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiScale"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainerHigh),
        border = BorderStroke(1.2.dp, studioColors.gold.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_item_effect_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            studioColors.gold.copy(alpha = 0.14f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = studioColors.gold.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, studioColors.gold.copy(alpha = 0.65f))
                    ) {
                        Text(
                            text = "✨ LIVE-BÜHNE",
                            style = MaterialTheme.typography.labelSmall,
                            color = studioColors.gold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (comboCount > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = studioColors.emerald.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${comboCount}x COMBO",
                                style = MaterialTheme.typography.labelSmall,
                                color = studioColors.emerald,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .scale(emojiScale)
                        .clip(CircleShape)
                        .background(studioColors.surfaceContainerLowest)
                        .border(1.5.dp, studioColors.gold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.actionFeedback,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = studioColors.gold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onReplayEffect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = studioColors.gold,
                        contentColor = Color(0xFF1A1200)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.actionText,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun StorefrontCard(
    shop: Shop,
    onEnter: () -> Unit
) {
    val studioColors = StudioTheme.colors
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
        border = BorderStroke(1.dp, Color(shop.bannerColor).copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onEnter)
            .testTag("shop_card_${shop.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(shop.bannerColor).copy(alpha = 0.82f),
                                studioColors.surfaceContainerHigh
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
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
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(studioColors.surfaceContainerLowest.copy(alpha = 0.85f))
                                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = shop.emoji, fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shop.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = shop.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = studioColors.surfaceContainerLowest.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(0.8.dp, studioColors.gold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${shop.items.size} Artikel",
                            style = MaterialTheme.typography.labelMedium,
                            color = studioColors.gold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(studioColors.surfaceContainerHigh)
                        .border(1.5.dp, Color(shop.bannerColor), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = shop.shopkeeperAvatar, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop.shopkeeperName,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(shop.bannerColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "\"${shop.welcomeMessage}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onEnter,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(shop.bannerColor),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "Betreten",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
