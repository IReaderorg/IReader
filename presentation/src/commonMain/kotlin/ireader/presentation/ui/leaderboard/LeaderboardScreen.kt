package ireader.presentation.ui.leaderboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.LeaderboardEntry
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlin.math.roundToInt
import ireader.domain.utils.extensions.currentTimeToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    vm: LeaderboardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Leaderboard",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                },
                actions = {
                    // Sync button with animation
                    IconButton(
                        onClick = { vm.syncUserStats() },
                        enabled = !state.isSyncing
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = localizeHelper.localize(Res.string.sync_stats),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading && state.leaderboard.isEmpty()) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading leaderboard...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (state.leaderboard.isEmpty() && !state.isLoading) {
                // Empty state
                EmptyLeaderboardState(
                    onSync = { vm.syncUserStats() },
                    isSyncing = state.isSyncing
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // User's rank card - ALWAYS VISIBLE AT TOP
                    item {
                        EnhancedUserRankCard(
                            userRank = state.userRank,
                            lastSyncTime = state.lastSyncTime,
                            onSync = { vm.syncUserStats() },
                            isSyncing = state.isSyncing,
                            totalUsers = state.leaderboard.size
                        )
                    }
                    
                    // Realtime toggle
                    item {
                        CompactRealtimeToggle(
                            isEnabled = state.isRealtimeEnabled,
                            onToggle = { vm.toggleRealtimeUpdates(it) }
                        )
                    }
                
                    // Top 3 podium with enhanced design
                    if (state.leaderboard.size >= 3) {
                        item {
                            EnhancedTopThreePodium(
                                entries = state.leaderboard.take(3)
                            )
                        }
                    }
                    
                    // Section divider
                    item {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    
                    // Section header with count
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizeHelper.localize(Res.string.all_rankings),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${state.leaderboard.size} readers",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    
                    // Leaderboard list with enhanced cards
                    itemsIndexed(state.leaderboard, key = { _, entry -> entry.odId }) { index, entry ->
                        EnhancedLeaderboardEntryCard(
                            entry = entry,
                            isCurrentUser = entry.userId == state.userRank?.userId,
                            animationDelay = index * 50
                        )
                    }
                    
                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            
            // Error snackbar
            state.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { vm.clearError() }) {
                            Text(localizeHelper.localize(Res.string.dismiss))
                        }
                    }
                ) {
                    Text(error)
                }
            }
            
            state.syncError?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { vm.clearError() }) {
                            Text(localizeHelper.localize(Res.string.dismiss))
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }
}

// Empty state when no leaderboard data
@Composable
private fun EmptyLeaderboardState(
    onSync: () -> Unit,
    isSyncing: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = localizeHelper.localize(Res.string.no_rankings_yet),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.be_the_first_to_sync_your_reading_stats),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onSync,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(localizeHelper.localize(Res.string.sync_my_stats))
            }
        }
    }
}

@Composable
private fun EnhancedUserRankCard(
    userRank: LeaderboardEntry?,
    lastSyncTime: Long,
    onSync: () -> Unit,
    isSyncing: Boolean,
    totalUsers: Int
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.your_rank),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (userRank != null) {
                    AnimatedRankBadge(rank = userRank.rank, size = 56.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (userRank != null) {
                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnhancedStatItem(
                        icon = Icons.Default.Timer,
                        label = localizeHelper.localize(Res.string.reading_time),
                        value = formatReadingTime(userRank.totalReadingTimeMinutes),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    EnhancedStatItem(
                        icon = Icons.Default.TrendingUp,
                        label = localizeHelper.localize(Res.string.percentile),
                        value = if (totalUsers > 0) {
                            val percentile = ((totalUsers - userRank.rank + 1).toFloat() / totalUsers * 100).roundToInt()
                            "Top $percentile%"
                        } else "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (userRank.hasBadge) {
                    Spacer(modifier = Modifier.height(12.dp))
                    EnhancedBadgeIndicator(badgeType = userRank.badgeType)
                }
                
                if (lastSyncTime > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Last synced: ${formatSyncTime(lastSyncTime)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Not synced yet
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.sync_your_stats_to_appear_on_the_leaderboard),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSync,
                        enabled = !isSyncing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactRealtimeToggle(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isEnabled) 
            MaterialTheme.colorScheme.tertiaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isEnabled) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEnabled)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.Bolt else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (isEnabled)
                            MaterialTheme.colorScheme.onTertiary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = localizeHelper.localize(Res.string.realtime_updates),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isEnabled) "Live updates active" else "Manual refresh only",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnabled)
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.tertiary,
                    checkedTrackColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            )
        }
    }
}

@Composable
private fun EnhancedTopThreePodium(entries: List<LeaderboardEntry>) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with sparkles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.top_readers),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd place
                if (entries.size >= 2) {
                    EnhancedPodiumPlace(entry = entries[1], place = 2, height = 110.dp)
                }
                
                // 1st place
                if (entries.isNotEmpty()) {
                    EnhancedPodiumPlace(entry = entries[0], place = 1, height = 140.dp)
                }
                
                // 3rd place
                if (entries.size >= 3) {
                    EnhancedPodiumPlace(entry = entries[2], place = 3, height = 90.dp)
                }
            }
        }
    }
}

@Composable
private fun PodiumPlace(
    entry: LeaderboardEntry,
    place: Int,
    height: Dp
) {
    val color = when (place) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val medal = when (place) {
        1 -> "??"
        2 -> "??"
        3 -> "??"
        else -> ""
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        // Medal
        Text(
            text = medal,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Username
        Text(
            text = entry.username,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        
        // Reading time
        Text(
            text = formatReadingTime(entry.totalReadingTimeMinutes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Podium
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.8f),
                            color.copy(alpha = 0.4f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$place",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LeaderboardEntryCard(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val backgroundColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            RankBadge(rank = entry.rank, size = 40.dp)
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (entry.hasBadge) {
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeIcon(badgeType = entry.badgeType)
                    }
                    
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.you),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatReadingTime(entry.totalReadingTimeMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Trophy for top 10
            if (entry.rank <= 10) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = when {
                        entry.rank <= 3 -> Color(0xFFFFD700)
                        entry.rank <= 10 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Composable
private fun RankBadge(rank: Int, size: Dp) {
    val backgroundColor = when {
        rank == 1 -> Color(0xFFFFD700) // Gold
        rank == 2 -> Color(0xFFC0C0C0) // Silver
        rank == 3 -> Color(0xFFCD7F32) // Bronze
        rank <= 10 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        rank <= 3 -> Color.White
        rank <= 10 -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = backgroundColor.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = (size.value / 3).sp
        )
    }
}

@Composable
private fun BadgeIcon(badgeType: String?) {
    val icon = when (badgeType) {
        "supporter" -> "??"
        "nft" -> "??"
        "premium" -> "?"
        else -> "??"
    }
    
    Text(
        text = icon,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun BadgeIndicator(badgeType: String?) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeIcon(badgeType)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badgeType?.replaceFirstChar { it.uppercase() } ?: "Badge",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

private fun formatReadingTime(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins > 0) "${hours}h ${mins}m" else "${hours}h"
        }
        else -> {
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            if (hours > 0) "${days}d ${hours}h" else "${days}d"
        }
    }
}

private fun formatSyncTime(timestamp: Long): String {
    val now = currentTimeToLong()
    val diff = now - timestamp
    val minutes = diff / 60000
    
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> "${minutes / 1440}d ago"
    }
}


@Composable
private fun EnhancedPodiumPlace(
    entry: LeaderboardEntry,
    place: Int,
    height: Dp
) {
    val color = when (place) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val medal = when (place) {
        1 -> "??"
        2 -> "??"
        3 -> "??"
        else -> ""
    }
    
    val scale by animateFloatAsState(
        targetValue = if (place == 1) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .scale(scale)
    ) {
        // Medal with glow effect
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (place == 1) 56.dp else 48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
        ) {
            Text(
                text = medal,
                style = if (place == 1) 
                    MaterialTheme.typography.displaySmall 
                else 
                    MaterialTheme.typography.headlineMedium
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Username with badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = entry.username,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (place == 1) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 70.dp)
            )
            if (entry.hasBadge) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (entry.badgeType) {
                        "supporter" -> "??"
                        "nft" -> "??"
                        "premium" -> "?"
                        else -> "??"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Reading time
        Text(
            text = formatReadingTime(entry.totalReadingTimeMinutes),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Podium with gradient
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color.copy(alpha = 0.6f),
                            color.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "#$place",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (place == 1) {
                    Text(
                        text = "??",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun EnhancedLeaderboardEntryCard(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    animationDelay: Int = 0
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val backgroundColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.secondary
    } else {
        Color.Transparent
    }
    
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCurrentUser) {
                        Modifier.border(
                            width = 2.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                ),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isCurrentUser) 4.dp else 1.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank badge with enhanced design
                EnhancedRankBadge(rank = entry.rank, size = 48.dp)
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // User info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = entry.username,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (entry.hasBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            BadgeIcon(badgeType = entry.badgeType)
                        }
                        
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Text(
                                    text = localizeHelper.localize(Res.string.you_1),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatReadingTime(entry.totalReadingTimeMinutes),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                // Trophy for top 10
                if (entry.rank <= 10) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    entry.rank <= 3 -> Color(0xFFFFD700).copy(alpha = 0.2f)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = when {
                                entry.rank <= 3 -> Color(0xFFFFD700)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedRankBadge(rank: Int, size: Dp) {
    val backgroundColor = when {
        rank == 1 -> Color(0xFFFFD700) // Gold
        rank == 2 -> Color(0xFFC0C0C0) // Silver
        rank == 3 -> Color(0xFFCD7F32) // Bronze
        rank <= 10 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        rank <= 3 -> Color.White
        rank <= 10 -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = Modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 3.dp,
                color = backgroundColor.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = (size.value / 3.5).sp
        )
    }
}

@Composable
private fun EnhancedRankBadge(rank: Int, size: Dp) {
    val backgroundColor = when {
        rank == 1 -> Color(0xFFFFD700)
        rank == 2 -> Color(0xFFC0C0C0)
        rank == 3 -> Color(0xFFCD7F32)
        rank <= 10 -> MaterialTheme.colorScheme.primary
        rank <= 50 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when {
        rank <= 10 -> Color.White
        rank <= 50 -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = backgroundColor.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontSize = (size.value / 3.2).sp
        )
    }
}

@Composable
private fun EnhancedStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EnhancedBadgeIndicator(badgeType: String?) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (badgeType) {
                    "supporter" -> "??"
                    "nft" -> "??"
                    "premium" -> "?"
                    else -> "??"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${badgeType?.replaceFirstChar { it.uppercase() } ?: "Badge"} Member",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
