package ireader.presentation.ui.leaderboard

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.LeaderboardEntry
import ireader.domain.models.gamification.ReaderTier
import kotlin.math.roundToInt

private val HallGradient = Brush.verticalGradient(listOf(Color(0xFF7B2FF7), Color(0xFF4A1FB0)))
private val Gold = Color(0xFFFFC73C)
private val Silver = Color(0xFFC6CCD6)
private val Bronze = Color(0xFFD08B4B)

private fun medalColor(rank: Int): Color = when (rank) {
    1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color(0xFF8C7BAE)
}

private fun formatHrs(minutes: Long): String =
    ireader.presentation.ui.community.formatReadingTimeCompact(minutes)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    vm: LeaderboardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsState()
    var selected by remember { mutableStateOf<LeaderboardEntry?>(null) }

    selected?.let { entry ->
        UserProfileSheet(entry = entry, onDismiss = { selected = null })
    }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { HallHeader(onBack = onBack, onSync = { vm.syncUserStats() }, isSyncing = state.isSyncing) }

            when {
                state.isLoading && state.leaderboard.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.leaderboard.isEmpty() -> item {
                    EmptyHall(onSync = { vm.syncUserStats() }, isSyncing = state.isSyncing)
                }
                else -> {
                    val list = state.leaderboard
                    item { Podium(list.take(3)) }
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("All Readers", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text("${list.size}", style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    itemsIndexed(list, key = { _, e -> e.userId }) { _, entry ->
                        RankRow(
                            entry = entry,
                            isMe = entry.userId == state.userRank?.userId,
                            onClick = { selected = entry },
                        )
                    }
                }
            }
        }

        // Sticky "YOU" chase bar
        state.userRank?.let { me ->
            YouBar(me, state.leaderboard.size, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun HallHeader(onBack: () -> Unit, onSync: () -> Unit, isSyncing: Boolean) {
    Box(Modifier.fillMaxWidth().background(HallGradient).padding(bottom = 16.dp)) {
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(40.dp).clip(CircleShape).clickable(enabled = !isSyncing, onClick = onSync),
                    contentAlignment = Alignment.Center) {
                    if (isSyncing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    else Icon(Icons.Filled.Sync, "Sync", tint = Color.White)
                }
            }
            Text("🏆  Hall of Readers", color = Color.White, fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp), textAlign = TextAlign.Center)
            Text("Ranked by total reading time", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 6.dp), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun Podium(top: List<LeaderboardEntry>) {
    if (top.isEmpty()) return
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        top.getOrNull(1)?.let { PodiumPlace(it, 2, 96.dp) }
        top.getOrNull(0)?.let { PodiumPlace(it, 1, 128.dp) }
        top.getOrNull(2)?.let { PodiumPlace(it, 3, 76.dp) }
    }
}

@Composable
private fun PodiumPlace(entry: LeaderboardEntry, place: Int, height: Dp) {
    val color = medalColor(place)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(104.dp)) {
        if (place == 1) Text("👑", fontSize = 22.sp)
        Box(
            Modifier.size(if (place == 1) 64.dp else 52.dp).clip(CircleShape)
                .border(3.dp, color, CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(entry.username.take(1).uppercase(), fontWeight = FontWeight.Bold,
                fontSize = if (place == 1) 26.sp else 20.sp,
                color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.height(6.dp))
        Text(entry.username, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp, modifier = Modifier.widthIn(max = 96.dp), textAlign = TextAlign.Center)
        Text(formatHrs(entry.totalReadingTimeMinutes), color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.width(86.dp).height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.95f), color.copy(alpha = 0.4f)))),
            contentAlignment = Alignment.Center,
        ) {
            Text("#$place", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun RankRow(entry: LeaderboardEntry, isMe: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (isMe) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isMe) 3.dp else 1.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(34.dp), contentAlignment = Alignment.Center) {
                Text("${entry.rank}", fontWeight = FontWeight.Bold,
                    color = if (entry.rank <= 3) medalColor(entry.rank) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .border(2.dp, medalColor(entry.rank).copy(alpha = if (entry.rank <= 3) 1f else 0.3f), CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(entry.username.take(1).uppercase(), fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.username, fontWeight = FontWeight.SemiBold, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    if (isMe) {
                        Spacer(Modifier.width(6.dp))
                        Text("YOU", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
                Text("Lv ${entry.level} · ${entry.levelTitle}", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(formatHrs(entry.totalReadingTimeMinutes), fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun YouBar(me: LeaderboardEntry, total: Int, modifier: Modifier = Modifier) {
    val tier = if (total > 0) ReaderTier.fromPercentile((me.rank.toFloat()) / total) else ReaderTier.BRONZE
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, tonalElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("#${me.rank}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Box(Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                Text(me.username.take(1).uppercase(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("You · ${tier.emblem} ${tier.display}", color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                if (total > 0) {
                    val pct = ((total - me.rank + 1).toFloat() / total * 100).roundToInt()
                    Text("Top $pct% of readers", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
            Text(formatHrs(me.totalReadingTimeMinutes), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyHall(onSync: () -> Unit, isSyncing: Boolean) {
    Column(
        Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🏆", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))
        Text("No rankings yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Be the first to sync your reading stats.", color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSync, enabled = !isSyncing) {
            if (isSyncing) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.width(8.dp))
            }
            Text("Sync my stats")
        }
    }
}
