package ireader.presentation.ui.leaderboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.SyncedBookSummary
import ireader.domain.models.gamification.ReaderTier
import ireader.presentation.ui.core.ui.AsyncImage

private val HeaderGradient = Brush.verticalGradient(listOf(Color(0xFF7B2FF7), Color(0xFF4A1FB0)))
private val Gold = Color(0xFFFFC73C)
private val Silver = Color(0xFFC6CCD6)
private val Bronze = Color(0xFFD08B4B)

private fun rankColor(rank: Int): Color = when (rank) {
    1 -> Gold; 2 -> Silver; 3 -> Bronze; else -> Color(0xFF8C7BAE)
}

private fun tierColor(tier: ReaderTier): Color = when (tier) {
    ReaderTier.BRONZE -> Color(0xFFCD7F32)
    ReaderTier.SILVER -> Color(0xFFC6CCD6)
    ReaderTier.GOLD -> Color(0xFFFFD700)
    ReaderTier.PLATINUM -> Color(0xFF00BCD4)
    ReaderTier.DIAMOND -> Color(0xFFE91E63)
    ReaderTier.LEGEND -> Color(0xFF9C27B0)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    vm: LeaderboardViewModel,
    userId: String,
    onBack: () -> Unit,
    onBookClick: (String, Long) -> Unit,
) {
    val state by vm.state.collectAsState()

    val userEntry = state.leaderboard.find { it.userId == userId }

    LaunchedEffect(userId) {
        vm.loadUserBooks(userId)
    }

    if (userEntry == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val cs = MaterialTheme.colorScheme
    val rc = rankColor(userEntry.rank)
    val tier = if (state.leaderboard.isNotEmpty()) ReaderTier.fromPercentile(userEntry.rank.toFloat() / state.leaderboard.size) else ReaderTier.BRONZE
    val tierCol = tierColor(tier)
    val progress by animateFloatAsState(
        targetValue = if (userEntry.xpToNextLevel > 0) (userEntry.xp.toFloat() / userEntry.xpToNextLevel.toFloat()).coerceIn(0f, 1f) else 0f,
        animationSpec = tween(800),
        label = "xp"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userEntry.username, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                Box(Modifier.fillMaxWidth().background(HeaderGradient).padding(bottom = 24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomCenter) {
                            Box(Modifier.size(100.dp).clip(CircleShape).border(4.dp, rc, CircleShape).background(cs.onPrimary.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
                                if (userEntry.avatarUrl != null) {
                                    AsyncImage(model = userEntry.avatarUrl, contentDescription = "Avatar", modifier = Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                } else {
                                    Text(userEntry.username.take(1).uppercase(), color = cs.onPrimary, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(rc).padding(horizontal = 10.dp, vertical = 2.dp)) {
                                Text("Lv ${userEntry.level}", color = Color(0xFF2A1466), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(userEntry.username, color = cs.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(userEntry.levelTitle, color = cs.onPrimary.copy(alpha = 0.85f), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.clip(RoundedCornerShape(20.dp)).background(tierCol.copy(alpha = 0.2f)).border(1.dp, tierCol.copy(alpha = 0.5f), RoundedCornerShape(20.dp)).padding(horizontal = 14.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = tierCol, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${tier.emblem} ${tier.display}", color = cs.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Stats title
            item(span = { GridItemSpan(2) }) { Text("Reading Stats", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp)) }

            // Stats tiles
            item { StatTile(icon = Icons.Filled.EmojiEvents, value = "#${userEntry.rank}", label = "Rank", accent = rc) }
            item { StatTile(icon = Icons.Filled.Star, value = "${userEntry.totalReadingTimeMinutes / 60}h ${userEntry.totalReadingTimeMinutes % 60}m", label = "Reading Time", accent = MaterialTheme.colorScheme.primary) }
            item { StatTile(icon = Icons.Filled.MenuBook, value = "${userEntry.totalChaptersRead}", label = "Chapters", accent = Color(0xFF4CAF50)) }
            item { StatTile(icon = Icons.Filled.LocalFireDepartment, value = "${userEntry.readingStreak}d", label = "Streak", accent = Color(0xFFFF6B35)) }
            item { StatTile(icon = Icons.Filled.Speed, value = "${userEntry.booksCompleted}", label = "Books Done", accent = Color(0xFF2196F3)) }
            item { StatTile(icon = Icons.Filled.Star, value = "${userEntry.totalReadingTimeMinutes}", label = "Total Min", accent = Color(0xFF9C27B0)) }

            // XP Progress
            item(span = { GridItemSpan(2) }) {
                Surface(shape = RoundedCornerShape(14.dp), color = cs.surfaceVariant) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Level ${userEntry.level}", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.weight(1f))
                            Text("${userEntry.xp} / ${userEntry.xpToNextLevel} XP", color = cs.onSurfaceVariant, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = rc, trackColor = cs.onSurface.copy(alpha = 0.08f))
                    }
                }
            }

            // Books title
            item(span = { GridItemSpan(2) }) { Text("Currently Reading", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp)) }

            if (state.isLoadingBooks) {
                item(span = { GridItemSpan(2) }) { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(32.dp)) } }
            } else if (state.selectedUserBooks.isEmpty()) {
                item(span = { GridItemSpan(2) }) { Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { Text("No synced books yet", color = cs.onSurfaceVariant, fontSize = 13.sp) } }
            } else {
                items(state.selectedUserBooks) { book -> BookCard(book = book, onClick = { onBookClick(book.bookUrl, book.sourceId) }) }
            }

            item(span = { GridItemSpan(2) }) { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatTile(icon: ImageVector, value: String, label: String, accent: Color) {
    val cs = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(14.dp), color = cs.surfaceVariant) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, color = accent, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
            Text(label, color = cs.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

@Composable
private fun BookCard(book: SyncedBookSummary, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Surface(modifier = Modifier.clickable(onClick = onClick), shape = RoundedCornerShape(12.dp), color = cs.surface) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(cs.surfaceVariant)) {
                if (book.coverUrl.isNotBlank()) {
                    AsyncImage(model = book.coverUrl, contentDescription = book.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = cs.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(40.dp).align(Alignment.Center))
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 14.sp)
                if (book.sourceName.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(book.sourceName, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.sp, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}
