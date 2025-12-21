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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.LeaderboardEntry
import kotlin.math.roundToInt
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res
import ireader.domain.utils.extensions.currentTimeToLong

@Composable
fun ReadingLeaderboardContent(
    state: LeaderboardScreenState,
    onSync: () -> Unit,
    onToggleRealtime: (Boolean) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (state.isLoading && state.leaderboard.isEmpty()) {
            LoadingState()
        } else if (state.leaderboard.isEmpty() && !state.isLoading) {
            EmptyReadingLeaderboardState(onSync = onSync, isSyncing = state.isSyncing)
        } else {
            ReadingLeaderboardList(state = state, onSync = onSync, onToggleRealtime = onToggleRealtime)
        }
        ShowErrors(state = state, onClearError = onClearError)
    }
}


@Composable
private fun LoadingState() {
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
}

@Composable
private fun EmptyReadingLeaderboardState(onSync: () -> Unit, isSyncing: Boolean) {
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
private fun ReadingLeaderboardList(
    state: LeaderboardScreenState,
    onSync: () -> Unit,
    onToggleRealtime: (Boolean) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ReadingUserRankCard(
                userRank = state.userRank,
                lastSyncTime = state.lastSyncTime,
                onSync = onSync,
                isSyncing = state.isSyncing,
                totalUsers = state.leaderboard.size
            )
        }
        
        item {
            ReadingRealtimeToggle(
                isEnabled = state.isRealtimeEnabled,
                onToggle = onToggleRealtime
            )
        }
        
        if (state.leaderboard.size >= 3) {
            item {
                ReadingTopThreePodium(entries = state.leaderboard.take(3))
            }
        }
        
        item {
            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        
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
        
        itemsIndexed(state.leaderboard, key = { _, entry -> entry.userId }) { index, entry ->
            ReadingLeaderboardEntryCard(
                entry = entry,
                isCurrentUser = entry.userId == state.userRank?.userId,
                animationDelay = index * 50
            )
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ShowErrors(state: LeaderboardScreenState, onClearError: () -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    state.error?.let { error ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = onClearError) { Text(localizeHelper.localize(Res.string.dismiss)) }
            }
        ) { Text(error) }
    }
    
    state.syncError?.let { error ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = onClearError) { Text(localizeHelper.localize(Res.string.dismiss)) }
            }
        ) { Text(error) }
    }
}


@Composable
private fun ReadingUserRankCard(
    userRank: LeaderboardEntry?,
    lastSyncTime: Long,
    onSync: () -> Unit,
    isSyncing: Boolean,
    totalUsers: Int
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Your Rank", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                if (userRank != null) {
                    ReadingRankBadge(rank = userRank.rank, size = 56.dp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (userRank != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ReadingStatItem(icon = Icons.Default.Timer, label = localizeHelper.localize(Res.string.reading_time), value = formatReadingTime(userRank.totalReadingTimeMinutes), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(12.dp))
                    ReadingStatItem(
                        icon = Icons.Default.TrendingUp,
                        label = localizeHelper.localize(Res.string.percentile),
                        value = if (totalUsers > 0) "Top ${((totalUsers - userRank.rank + 1).toFloat() / totalUsers * 100).roundToInt()}%" else "N/A",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (userRank.hasBadge) {
                    Spacer(modifier = Modifier.height(12.dp))
                    ReadingBadgeIndicator(badgeType = userRank.badgeType)
                }
                
                if (lastSyncTime > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Last synced: ${formatSyncTime(lastSyncTime)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
            } else {
                NotSyncedYetContent(onSync = onSync, isSyncing = isSyncing)
            }
        }
    }
}

@Composable
private fun NotSyncedYetContent(onSync: () -> Unit, isSyncing: Boolean) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Sync your stats to appear on the leaderboard", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSync, enabled = !isSyncing, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            if (isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.Sync, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sync Now", fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
private fun ReadingRealtimeToggle(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isEnabled) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = if (isEnabled) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (isEnabled) Icons.Default.Bolt else Icons.Default.Refresh, null, tint = if (isEnabled) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Realtime Updates", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = if (isEnabled) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (isEnabled) "Live updates active" else "Manual refresh only", style = MaterialTheme.typography.bodySmall, color = if (isEnabled) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            Switch(checked = isEnabled, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.tertiary, checkedTrackColor = MaterialTheme.colorScheme.tertiaryContainer))
        }
    }
}

@Composable
private fun ReadingTopThreePodium(entries: List<LeaderboardEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 20.dp)) {
                Text("?", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Top Readers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("?", style = MaterialTheme.typography.headlineMedium)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                if (entries.size >= 2) ReadingPodiumPlace(entry = entries[1], place = 2, height = 110.dp)
                if (entries.isNotEmpty()) ReadingPodiumPlace(entry = entries[0], place = 1, height = 140.dp)
                if (entries.size >= 3) ReadingPodiumPlace(entry = entries[2], place = 3, height = 90.dp)
            }
        }
    }
}


@Composable
private fun ReadingPodiumPlace(entry: LeaderboardEntry, place: Int, height: Dp) {
    val color = when (place) { 1 -> Color(0xFFFFD700); 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32); else -> MaterialTheme.colorScheme.surfaceVariant }
    val medal = when (place) { 1 -> "??"; 2 -> "??"; 3 -> "??"; else -> "" }
    val scale by animateFloatAsState(targetValue = if (place == 1) 1.05f else 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp).graphicsLayer { scaleX = scale; scaleY = scale }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(if (place == 1) 56.dp else 48.dp).clip(CircleShape).background(color.copy(alpha = 0.2f))) {
            Text(medal, style = if (place == 1) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(entry.username, style = MaterialTheme.typography.bodyMedium, fontWeight = if (place == 1) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 70.dp))
            if (entry.hasBadge) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(when (entry.badgeType) { "supporter" -> "??"; "nft" -> "??"; "premium" -> "?"; else -> "??" }, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(formatReadingTime(entry.totalReadingTimeMinutes), style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.width(90.dp).height(height).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(Brush.verticalGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.6f), color.copy(alpha = 0.3f)))),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("#$place", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                if (place == 1) Text("??", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun ReadingLeaderboardEntryCard(entry: LeaderboardEntry, isCurrentUser: Boolean, animationDelay: Int = 0) {
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isCurrentUser) MaterialTheme.colorScheme.secondary else Color.Transparent
    
    // Direct rendering without AnimatedVisibility for better list performance
    Card(
        modifier = Modifier.fillMaxWidth().then(if (isCurrentUser) Modifier.border(2.dp, borderColor, RoundedCornerShape(16.dp)) else Modifier),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 4.dp else 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            ReadingRankBadge(rank = entry.rank, size = 48.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.username, style = MaterialTheme.typography.titleMedium, fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (entry.hasBadge) { Spacer(modifier = Modifier.width(6.dp)); Text(when (entry.badgeType) { "supporter" -> "??"; "nft" -> "??"; "premium" -> "?"; else -> "??" }, style = MaterialTheme.typography.bodyMedium) }
                    if (isCurrentUser) { Spacer(modifier = Modifier.width(8.dp)); Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondary) { Text("YOU", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) } }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formatReadingTime(entry.totalReadingTimeMinutes), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            if (entry.rank <= 10) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(if (entry.rank <= 3) Color(0xFFFFD700).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.EmojiEvents, null, tint = if (entry.rank <= 3) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}


@Composable
private fun ReadingRankBadge(rank: Int, size: Dp) {
    val backgroundColor = when { rank == 1 -> Color(0xFFFFD700); rank == 2 -> Color(0xFFC0C0C0); rank == 3 -> Color(0xFFCD7F32); rank <= 10 -> MaterialTheme.colorScheme.primary; rank <= 50 -> MaterialTheme.colorScheme.tertiary; else -> MaterialTheme.colorScheme.surfaceVariant }
    val textColor = when { rank <= 10 -> Color.White; rank <= 50 -> MaterialTheme.colorScheme.onTertiary; else -> MaterialTheme.colorScheme.onSurfaceVariant }
    
    Box(modifier = Modifier.size(size).clip(CircleShape).background(backgroundColor).border(2.dp, backgroundColor.copy(alpha = 0.4f), CircleShape), contentAlignment = Alignment.Center) {
        Text("#$rank", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = textColor, fontSize = (size.value / 3.2).sp)
    }
}

@Composable
private fun ReadingStatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ReadingBadgeIndicator(badgeType: String?) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(when (badgeType) { "supporter" -> "??"; "nft" -> "??"; "premium" -> "?"; else -> "??" }, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text("${badgeType?.replaceFirstChar { it.uppercase() } ?: "Badge"} Member", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
    }
}

private fun formatReadingTime(minutes: Long): String = when {
    minutes < 60 -> "${minutes}m"
    minutes < 1440 -> { val hours = minutes / 60; val mins = minutes % 60; if (mins > 0) "${hours}h ${mins}m" else "${hours}h" }
    else -> { val days = minutes / 1440; val hours = (minutes % 1440) / 60; if (hours > 0) "${days}d ${hours}h" else "${days}d" }
}

private fun formatSyncTime(timestamp: Long): String {
    val diff = currentTimeToLong() - timestamp
    val minutes = diff / 60000
    return when { minutes < 1 -> "Just now"; minutes < 60 -> "${minutes}m ago"; minutes < 1440 -> "${minutes / 60}h ago"; else -> "${minutes / 1440}d ago" }
}
