package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.StudioTheme
import com.example.ui.theme.TelemetryNumberStyle
import com.example.viewmodel.ElevatorGameState

@Composable
fun ShopDetailScreen(
    shop: Shop,
    state: ElevatorGameState,
    onLeaveShop: () -> Unit,
    onBuyItem: (ShopItem) -> Unit,
    onUseItem: (ShopItem) -> Unit,
    onOpenBackpack: () -> Unit,
    onOpenCoinShop: () -> Unit = {},
    onDismissItemEffect: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val zoneImageRes = MallCatalog.getZoneDrawableRes(state.currentFloor)
    val studioColors = StudioTheme.colors
    val haptics = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Material 3 Expressive Top Shop Header
        Surface(
            color = studioColors.surfaceContainer,
            tonalElevation = 4.dp,
            border = BorderStroke(0.5.dp, studioColors.hairlineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = {
                            if (state.hapticsEnabled) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            onLeaveShop()
                        },
                        modifier = Modifier.testTag("leave_shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück zur Mall",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${shop.emoji} ${shop.name}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Etage ${state.currentFloor} • ${shop.subtitle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = studioColors.surfaceContainerHigh,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, studioColors.gold.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenCoinShop)
                        .testTag("shop_coins_chip")
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
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(1.dp, Color(shop.bannerColor).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = zoneImageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF070A10).copy(alpha = 0.92f),
                                            Color(0xFF070A10).copy(alpha = 0.72f),
                                            Color(0xFF070A10).copy(alpha = 0.88f)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(studioColors.surfaceContainerLowest)
                                    .border(2.dp, Color(shop.bannerColor), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = shop.shopkeeperAvatar, fontSize = 30.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = shop.shopkeeperName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(shop.bannerColor),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "\"${shop.welcomeMessage}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

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

            item {
                Text(
                    text = "REGALE & HIGHLIGHTS (LIVE TESTBAR)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(shop.items, key = { it.id }) { item ->
                val ownedCount = state.inventory[item.id]?.second ?: 0
                val canAfford = state.coins >= item.price
                val isBeingTested = state.activeItemEffect?.id == item.id

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = studioColors.surfaceContainer),
                    border = BorderStroke(
                        width = if (isBeingTested || ownedCount > 0) 1.5.dp else 0.8.dp,
                        color = when {
                            isBeingTested -> studioColors.gold
                            ownedCount > 0 -> studioColors.emerald.copy(alpha = 0.7f)
                            else -> studioColors.hairlineBorder
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("item_card_${item.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(studioColors.surfaceContainerHigh)
                                    .border(1.5.dp, Color(shop.bannerColor).copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = item.emoji, fontSize = 27.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = studioColors.surfaceContainerHigh,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(0.8.dp, studioColors.gold.copy(alpha = 0.55f))
                                    ) {
                                        Text(
                                            text = "🪙 ${item.price} Münzen",
                                            style = TelemetryNumberStyle.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Bold),
                                            color = studioColors.gold,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }

                                    if (ownedCount > 0) {
                                        Surface(
                                            color = studioColors.emerald.copy(alpha = 0.16f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(0.8.dp, studioColors.emerald.copy(alpha = 0.55f))
                                        ) {
                                            Text(
                                                text = "Im Rucksack: ${ownedCount}x",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = studioColors.emerald,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onUseItem(item) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("try_button_${item.id}")
                            ) {
                                Text(
                                    text = "✨ ${item.actionText}",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = {
                                    if (state.hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    onBuyItem(item)
                                },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) Color(shop.bannerColor) else studioColors.surfaceContainerHighest,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("buy_button_${item.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (ownedCount > 0) "Mehr kaufen" else "Kaufen",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
