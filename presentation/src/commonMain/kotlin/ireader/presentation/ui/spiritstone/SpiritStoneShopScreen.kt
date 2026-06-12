package ireader.presentation.ui.spiritstone

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.presentation.ui.core.ui.AsyncImage

private val GoldColor = Color(0xFFFFD700)
private val DiamondColor = Color(0xFFE91E63)
private val RareBlue = Color(0xFF2196F3)
private val EpicPurple = Color(0xFF9C27B0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiritStoneShopScreen(
    vm: SpiritStoneShopViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<Pair<String, Int>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Spirit Stone Shop")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GoldColor.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💎", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${state.spiritStones}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GoldColor
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title Section
            item {
                Text(
                    "Titles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Equip titles to show off your achievements",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Title Items
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
                        showConfirmDialog = true
                    },
                    onEquip = { vm.equipTitle(title.id) }
                )
            }

            // Badge Section
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Badges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Display badges on your profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Badge Items
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
                        showConfirmDialog = true
                    }
                )
            }
        }
    }

    // Confirm Purchase Dialog
    if (showConfirmDialog && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Purchase") },
            text = {
                Text("Spend ${selectedItem?.second} 💎 to purchase this item?")
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedItem?.let { (id, _) ->
                        vm.purchaseItem(id)
                    }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) rarityColor.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rarity indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(rarityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = rarityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = rarityColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            rarity,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            color = rarityColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isOwned) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isActive) "Active" else "Owned",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isOwned) {
                if (isActive) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    TextButton(onClick = onEquip) {
                        Text("Equip")
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (canAfford) GoldColor.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(enabled = canAfford) { onBuy() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$cost",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) GoldColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
        modifier = Modifier.fillMaxWidth(),
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
            // Badge image from URL
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(rarityColor.copy(alpha = 0.4f), rarityColor.copy(alpha = 0.1f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = rarityColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = rarityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        rarity,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = rarityColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isOwned) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Owned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (canAfford) GoldColor.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable(enabled = canAfford) { onBuy() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💎", fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$cost",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) GoldColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getRarityColor(rarity: String): Color {
    return when (rarity.uppercase()) {
        "LEGENDARY" -> GoldColor
        "EPIC" -> EpicPurple
        "RARE" -> RareBlue
        "COMMON" -> Color(0xFF9E9E9E)
        else -> Color(0xFF2196F3) // Default blue
    }
}
