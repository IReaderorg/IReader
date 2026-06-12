package ireader.presentation.ui.spiritstone

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.ui.AsyncImage

private val GoldColor = Color(0xFFFFD700)
private val DiamondColor = Color(0xFFE91E63)
private val RareBlue = Color(0xFF2196F3)
private val EpicPurple = Color(0xFF9C27B0)
private val CoinGold = Color(0xFFFFA000)

private const val STONE_IMAGE_URL = "https://raw.githubusercontent.com/IReaderorg/badge-repo/main/gold-min.png"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiritStoneShopScreen(
    vm: SpiritStoneShopViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var selectedName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = STONE_IMAGE_URL,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Spirit Stone Shop", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CoinGold.copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = STONE_IMAGE_URL,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${state.spiritStones}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = CoinGold
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance card
                item {
                    BalanceCard(spiritStones = state.spiritStones)
                }

                // Titles Section
                item {
                    SectionHeader("Titles", "Equip titles to show off your achievements")
                }

                items(state.availableTitles) { title ->
                    TitleShopItem(
                        title = title.name,
                        rarity = title.rarity,
                        cost = title.cost,
                        isOwned = title.isOwned,
                        isActive = title.isActive,
                        canAfford = state.spiritStones >= title.cost,
                        onBuy = {
                            selectedItem = Pair(title.id, title.cost)
                            selectedName = title.name
                            showConfirmDialog = true
                        },
                        onEquip = { vm.equipTitle(title.id) }
                    )
                }

                // Badges Section
                item {
                    Spacer(Modifier.height(4.dp))
                    SectionHeader("Badges", "Display badges on your profile")
                }

                items(state.availableBadges) { badge ->
                    BadgeShopItem(
                        name = badge.name,
                        description = badge.description,
                        rarity = badge.rarity,
                        cost = badge.cost,
                        isOwned = badge.isOwned,
                        canAfford = state.spiritStones >= badge.cost,
                        imageUrl = badge.imageUrl,
                        onBuy = {
                            selectedItem = Pair(badge.id, badge.cost)
                            selectedName = badge.name
                            showConfirmDialog = true
                        }
                    )
                }

                // Empty state
                if (state.availableTitles.isEmpty() && state.availableBadges.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = STONE_IMAGE_URL,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).clip(CircleShape)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "No items available yet",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Loading indicator
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = CoinGold,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Confirm Purchase Dialog
        if (showConfirmDialog && selectedItem != null) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                icon = {
                    AsyncImage(
                        model = STONE_IMAGE_URL,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                },
                title = { Text("Confirm Purchase", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Purchase $selectedName?")
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = STONE_IMAGE_URL,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${selectedItem?.second} stones",
                                fontWeight = FontWeight.Bold,
                                color = CoinGold
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedItem?.let { (id, _) -> vm.purchaseItem(id) }
                        showConfirmDialog = false
                        selectedItem = null
                    }) {
                        Text("Purchase")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@Composable
private fun BalanceCard(spiritStones: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(CoinGold.copy(alpha = 0.3f), CoinGold.copy(alpha = 0.1f))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = STONE_IMAGE_URL,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Your Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$spiritStones",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TitleShopItem(
    title: String,
    rarity: String,
    cost: Int,
    isOwned: Boolean,
    isActive: Boolean,
    canAfford: Boolean,
    onBuy: () -> Unit,
    onEquip: () -> Unit
) {
    val rarityColor = getRarityColor(rarity)
    val displayName = formatTitleName(title)

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) rarityColor.copy(alpha = 0.08f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(rarityColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RarityBadge(rarity, rarityColor)
                    if (isOwned) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isActive) "Active" else "Owned",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isOwned) {
                if (!isActive) {
                    TextButton(onClick = onEquip) {
                        Text("Equip", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                StoneButton(
                    cost = cost,
                    canAfford = canAfford,
                    onClick = onBuy
                )
            }
        }
    }
}

@Composable
private fun BadgeShopItem(
    name: String,
    description: String,
    rarity: String,
    cost: Int,
    isOwned: Boolean,
    canAfford: Boolean,
    imageUrl: String?,
    onBuy: () -> Unit
) {
    val rarityColor = getRarityColor(rarity)

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwned) rarityColor.copy(alpha = 0.05f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge image
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                RarityBadge(rarity, rarityColor)
            }

            if (isOwned) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Owned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                StoneButton(
                    cost = cost,
                    canAfford = canAfford,
                    onClick = onBuy
                )
            }
        }
    }
}

@Composable
private fun RarityBadge(rarity: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            rarity,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StoneButton(cost: Int, canAfford: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (canAfford) CoinGold.copy(alpha = 0.15f)
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable(enabled = canAfford) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = STONE_IMAGE_URL,
                contentDescription = null,
                modifier = Modifier.size(14.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "$cost",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (canAfford) CoinGold else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getRarityColor(rarity: String): Color {
    return when (rarity.uppercase()) {
        "LEGENDARY" -> GoldColor
        "EPIC" -> EpicPurple
        "RARE" -> RareBlue
        "COMMON" -> Color(0xFF9E9E9E)
        else -> Color(0xFF2196F3)
    }
}

private fun formatTitleName(raw: String): String {
    return raw
        .removePrefix("title_")
        .split("_")
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}
