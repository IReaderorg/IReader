package ireader.presentation.ui.readinghub

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.ReadingStatisticsType1
import ireader.domain.models.quote.BuddyAchievement
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.readingbuddy.components.ReadingBuddyCharacter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHubScreen(
    vm: ReadingHubViewModel,
    onBack: () -> Unit,
    onShareQuote: (Quote, QuoteCardStyle) -> Unit,
    onNavigateToQuotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isWideScreen = isTableUi()

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearError()
        }
    }
    
    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccessMessage()
        }
    }
    
    // Achievement dialog
    if (state.showAchievementDialog && state.newAchievement != null) {
        AchievementUnlockedDialog(
            achievement = state.newAchievement!!,
            onDismiss = { vm.dismissAchievementDialog() }
        )
    }
    
    // Reset confirmation dialog
    if (state.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissResetConfirmDialog() },
            title = { Text("Reset Statistics?") },
            text = { Text("This will reset all your reading statistics, streaks, and achievements. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.resetStatistics() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissResetConfirmDialog() }) { Text("Cancel") }
            }
        )
    }
    
    Scaffold(
        topBar = {
            ModernReadingHubTopBar(
                onBack = onBack,
                onReset = { vm.showResetConfirmDialog() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        UnifiedHubContent(
            state = state,
            isWideScreen = isWideScreen,
            onNavigateToQuotes = onNavigateToQuotes,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernReadingHubTopBar(
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val infiniteTransition = rememberInfiniteTransition(label = "bounce")
                val bounce by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bounce"
                )
                
                Text(
                    text = "📚",
                    fontSize = 28.sp,
                    modifier = Modifier.offset(y = bounce.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Reading Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track your reading journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onReset) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Reset Statistics",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}


// ==================== UNIFIED HUB CONTENT ====================

@Composable
private fun UnifiedHubContent(
    state: ReadingHubState,
    isWideScreen: Boolean,
    onNavigateToQuotes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentPadding = if (isWideScreen) 24.dp else 16.dp
    
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = contentPadding, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section with Buddy
        item { HeroSection(state) }
        
        // Quick Stats Row
        item { QuickStatsRow(state.statistics) }
        
        // Streak & Level Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StreakCard(
                    currentStreak = state.statistics.readingStreak,
                    longestStreak = state.statistics.longestStreak,
                    modifier = Modifier.weight(1f)
                )
                LevelCard(
                    level = state.buddyState.level,
                    progress = state.levelProgress,
                    experience = state.buddyState.experience,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Detailed Stats Section
        item { DetailedStatsSection(state.statistics) }
        
        // Achievements Section
        item { AchievementsSection(state.unlockedAchievements, state.allAchievements) }
        
        // Quotes Entry Card
        item { QuotesEntryCard(state.dailyQuote, onNavigateToQuotes) }
        
        // Favorite Genres
        if (state.statistics.favoriteGenres.isNotEmpty()) {
            item { GenresSection(state.statistics.favoriteGenres) }
        }
    }
}

@Composable
private fun HeroSection(state: ReadingHubState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ReadingBuddyCharacter(state = state.buddyState, modifier = Modifier)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = state.buddyState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(statistics: ReadingStatisticsType1) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatChip(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            value = statistics.totalChaptersRead.toString(),
            label = "Chapters",
            gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Default.CheckCircle,
            value = statistics.booksCompleted.toString(),
            label = "Books",
            gradient = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            modifier = Modifier.weight(1f)
        )
        StatChip(
            icon = Icons.Default.Schedule,
            value = formatTimeCompact(statistics.totalReadingTimeMinutes),
            label = "Time",
            gradient = listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradient))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}


@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF6B35).copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🔥", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$currentStreak",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            )
            Text(
                text = "day streak",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "Best: $longestStreak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: Int,
    progress: Float,
    experience: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⭐", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$experience XP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailedStatsSection(statistics: ReadingStatisticsType1) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📊", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Detailed Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Grid
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        emoji = "⏱️",
                        label = "Total Time",
                        value = formatDetailedTime(statistics.totalReadingTimeMinutes),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        emoji = "📖",
                        label = "Currently Reading",
                        value = statistics.currentlyReading.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        emoji = "⚡",
                        label = "Reading Speed",
                        value = "${statistics.averageReadingSpeedWPM} WPM",
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        emoji = "📅",
                        label = "Last Read",
                        value = formatLastRead(statistics.lastReadDate),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatItem(
                        emoji = "📚",
                        label = "Avg/Day",
                        value = formatDetailedTime(
                            if (statistics.readingStreak > 0) statistics.totalReadingTimeMinutes / statistics.readingStreak else 0
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    DetailStatItem(
                        emoji = "🏆",
                        label = "Longest Streak",
                        value = "${statistics.longestStreak} days",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailStatItem(
    emoji: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
private fun AchievementsSection(
    unlockedAchievements: List<BuddyAchievement>,
    allAchievements: List<BuddyAchievement>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${unlockedAchievements.size}/${allAchievements.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (unlockedAchievements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Start reading to unlock achievements!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Unlocked achievements row
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(unlockedAchievements.take(6)) { achievement ->
                        AchievementBadge(achievement, isUnlocked = true)
                    }
                }
                
                // Show locked achievements preview
                if (allAchievements.size > unlockedAchievements.size) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Next to unlock:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val lockedAchievements = allAchievements.filter { all ->
                        unlockedAchievements.none { it.name == all.name }
                    }.take(3)
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(lockedAchievements) { achievement ->
                            AchievementBadge(achievement, isUnlocked = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(achievement: BuddyAchievement, isUnlocked: Boolean) {
    val emoji = getAchievementEmoji(achievement.name)
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUnlocked) 
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier
                .width(80.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isUnlocked) emoji else "🔒",
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (isUnlocked) 
                    MaterialTheme.colorScheme.onPrimaryContainer
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (isUnlocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+${achievement.xpReward}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuotesEntryCard(dailyQuote: Quote?, onNavigateToQuotes: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToQuotes() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💭", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Quotes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "View Quotes",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            if (dailyQuote != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✨", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quote of the Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "\"${dailyQuote.text}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "— ${dailyQuote.bookTitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Tap to explore all quotes →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun GenresSection(genres: List<ireader.domain.models.entities.GenreCount>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📚", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Favorite Genres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(genres) { genreCount ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = genreCount.genre,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${genreCount.bookCount}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================== DIALOGS ====================

@Composable
private fun AchievementUnlockedDialog(
    achievement: BuddyAchievement,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Achievement Unlocked!")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = achievement.description,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "+${achievement.xpReward} XP",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Awesome!")
            }
        }
    )
}

// ==================== HELPER FUNCTIONS ====================

private fun formatTimeCompact(minutes: Long): String {
    return when {
        minutes < 60 -> "${minutes}m"
        minutes < 1440 -> "${minutes / 60}h"
        else -> "${minutes / 1440}d"
    }
}

private fun formatDetailedTime(minutes: Long): String {
    return when {
        minutes < 60 -> "$minutes min"
        minutes < 1440 -> "${minutes / 60}h ${minutes % 60}m"
        else -> "${minutes / 1440}d ${(minutes % 1440) / 60}h"
    }
}

private fun formatLastRead(timestamp: Long): String {
    if (timestamp == 0L) return "Never"
    val now = ireader.domain.utils.extensions.currentTimeToLong()
    val diff = now - timestamp
    val days = diff / (1000 * 60 * 60 * 24)
    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        days < 30 -> "${days / 7} weeks ago"
        else -> "${days / 30} months ago"
    }
}

private fun getAchievementEmoji(name: String): String {
    return when {
        name.contains("BOOK", ignoreCase = true) -> "📚"
        name.contains("CHAPTER", ignoreCase = true) -> "📖"
        name.contains("STREAK", ignoreCase = true) -> "🔥"
        name.contains("NIGHT", ignoreCase = true) -> "🌙"
        name.contains("EARLY", ignoreCase = true) -> "🌅"
        name.contains("MARATHON", ignoreCase = true) -> "🏃"
        name.contains("QUOTE", ignoreCase = true) -> "💬"
        name.contains("SPEED", ignoreCase = true) -> "⚡"
        name.contains("TIME", ignoreCase = true) -> "⏰"
        else -> "🏅"
    }
}
