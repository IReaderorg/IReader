package ireader.presentation.ui.readinghub

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import ireader.presentation.ui.readingbuddy.components.QuoteCard
import ireader.presentation.ui.readingbuddy.components.ReadingBuddyCharacter
import kotlinx.coroutines.launch

/**
 * Tabs for the Reading Hub screen
 */
enum class ReadingHubTab(val title: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Dashboard),
    STATS("Stats", Icons.Default.BarChart),
    QUOTES("Quotes", Icons.Default.FormatQuote),
    SUBMIT("Submit", Icons.Default.Add)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHubScreen(
    vm: ReadingHubViewModel,
    onBack: () -> Unit,
    onShareQuote: (Quote, QuoteCardStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { ReadingHubTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
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
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissResetConfirmDialog() }) {
                    Text("Cancel")
                }
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Modern Tab Row with pills
            ModernTabRow(
                selectedTab = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }
            )
            
            // Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ModernOverviewTab(state, isWideScreen)
                    1 -> ModernStatsTab(state, isWideScreen)
                    2 -> QuotesTab(
                        state = state,
                        onToggleLike = { vm.toggleLike(it) },
                        onShare = { quote -> onShareQuote(quote, state.selectedCardStyle) },
                        onStyleChange = { vm.setCardStyle(it) },
                        isWideScreen = isWideScreen
                    )
                    3 -> SubmitQuoteTab(
                        state = state,
                        onSubmit = { t, b, a, c -> vm.submitQuote(t, b, a, c) },
                        isWideScreen = isWideScreen
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernReadingHubTopBar(
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            
            // Animated emoji
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
            
            Column(modifier = Modifier.weight(1f)) {
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
            
            IconButton(onClick = onReset) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = "Reset Statistics",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModernTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ReadingHubTab.entries.size) { index ->
            val tab = ReadingHubTab.entries[index]
            val isSelected = selectedTab == index
            
            FilterChip(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                label = {
                    Text(
                        text = tab.title,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}


// ==================== MODERN OVERVIEW TAB ====================

@Composable
private fun ModernOverviewTab(state: ReadingHubState, isWideScreen: Boolean) {
    val contentPadding = if (isWideScreen) 20.dp else 16.dp
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = contentPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card with Buddy
        item {
            ModernHeroCard(state)
        }
        
        // Stats Grid
        item {
            ModernStatsGrid(state.statistics)
        }
        
        // Streak & Level Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStreakCard(
                    currentStreak = state.statistics.readingStreak,
                    longestStreak = state.statistics.longestStreak,
                    modifier = Modifier.weight(1f)
                )
                ModernLevelCard(
                    level = state.buddyState.level,
                    progress = state.levelProgress,
                    experience = state.buddyState.experience,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Achievements
        item {
            ModernAchievementsCard(state.unlockedAchievements)
        }
        
        // Daily Quote
        if (state.dailyQuote != null) {
            item {
                ModernDailyQuoteCard(state.dailyQuote!!)
            }
        }
    }
}

@Composable
private fun ModernHeroCard(state: ReadingHubState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(28.dp)),
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
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ReadingBuddyCharacter(
                    state = state.buddyState,
                    modifier = Modifier
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = state.buddyState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernStatsGrid(statistics: ReadingStatisticsType1) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ModernStatChip(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            value = statistics.totalChaptersRead.toString(),
            label = "Chapters",
            gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2)),
            modifier = Modifier.weight(1f)
        )
        ModernStatChip(
            icon = Icons.Default.CheckCircle,
            value = statistics.booksCompleted.toString(),
            label = "Books",
            gradient = listOf(Color(0xFF11998E), Color(0xFF38EF7D)),
            modifier = Modifier.weight(1f)
        )
        ModernStatChip(
            icon = Icons.Default.Schedule,
            value = formatReadingTimeCompact(statistics.totalReadingTimeMinutes),
            label = "Time",
            gradient = listOf(Color(0xFFF093FB), Color(0xFFF5576C)),
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ModernStatChip(
    icon: ImageVector,
    value: String,
    label: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(20.dp)),
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun ModernStreakCard(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6B35).copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🔥", fontSize = 32.sp)
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Best: $longestStreak",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ModernLevelCard(
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "⭐", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
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
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$experience XP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
private fun ModernAchievementsCard(achievements: List<BuddyAchievement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${achievements.size}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (achievements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start reading to unlock achievements!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(achievements.take(5)) { achievement ->
                        ModernAchievementChip(achievement)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernAchievementChip(achievement: BuddyAchievement) {
    val emoji = getAchievementEmoji(achievement.name)
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModernDailyQuoteCard(quote: Quote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "💭", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Quote of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "— ${quote.bookTitle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ==================== MODERN STATS TAB ====================

@Composable
private fun ModernStatsTab(state: ReadingHubState, isWideScreen: Boolean) {
    val contentPadding = if (isWideScreen) 20.dp else 16.dp
    val stats = state.statistics
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = contentPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reading Time Card
        item {
            ModernDetailCard(
                title = "Reading Time",
                emoji = "⏱️",
                gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernStatRow("Total Time", formatDetailedTime(stats.totalReadingTimeMinutes))
                    ModernStatRow(
                        "Average/Day",
                        formatDetailedTime(
                            if (stats.readingStreak > 0) stats.totalReadingTimeMinutes / stats.readingStreak else 0
                        )
                    )
                    ModernStatRow("Reading Speed", "${stats.averageReadingSpeedWPM} WPM")
                }
            }
        }
        
        // Reading Progress Card
        item {
            ModernDetailCard(
                title = "Reading Progress",
                emoji = "📖",
                gradient = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernStatRow("Chapters Read", stats.totalChaptersRead.toString())
                    ModernStatRow("Books Completed", stats.booksCompleted.toString())
                    ModernStatRow("Currently Reading", stats.currentlyReading.toString())
                }
            }
        }
        
        // Streaks Card
        item {
            ModernDetailCard(
                title = "Streaks",
                emoji = "🔥",
                gradient = listOf(Color(0xFFFF6B35), Color(0xFFFF9A5A))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ModernStatRow("Current Streak", "${stats.readingStreak} days")
                    ModernStatRow("Longest Streak", "${stats.longestStreak} days")
                    ModernStatRow("Last Read", formatLastRead(stats.lastReadDate))
                }
            }
        }
        
        // Favorite Genres
        if (stats.favoriteGenres.isNotEmpty()) {
            item {
                ModernGenresCard(stats.favoriteGenres)
            }
        }
        
        // All Achievements
        item {
            ModernAllAchievementsCard(state.unlockedAchievements, state.allAchievements)
        }
    }
}

@Composable
private fun ModernDetailCard(
    title: String,
    emoji: String,
    gradient: List<Color>,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(gradient))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            // Content
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ModernStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun ModernGenresCard(genres: List<ireader.domain.models.entities.GenreCount>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📚", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Favorite Genres",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(genres) { genreCount ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = genreCount.genre,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${genreCount.bookCount}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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

@Composable
private fun ModernAllAchievementsCard(
    unlockedAchievements: List<BuddyAchievement>,
    allAchievements: List<BuddyAchievement>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🏆", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All Achievements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${unlockedAchievements.size}/${allAchievements.size}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            allAchievements.forEach { achievement ->
                val isUnlocked = unlockedAchievements.any { it.name == achievement.name }
                ModernAchievementRow(achievement, isUnlocked)
                if (achievement != allAchievements.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernAchievementRow(achievement: BuddyAchievement, isUnlocked: Boolean) {
    val emoji = getAchievementEmoji(achievement.name)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with background
        Surface(
            shape = CircleShape,
            color = if (isUnlocked) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = if (isUnlocked) emoji else "🔒",
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (isUnlocked) 0.8f else 0.4f
                )
            )
        }
        
        if (isUnlocked) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF4CAF50).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "+${achievement.xpReward}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}


// ==================== QUOTES TAB ====================

@Composable
private fun QuotesTab(
    state: ReadingHubState,
    onToggleLike: (Quote) -> Unit,
    onShare: (Quote) -> Unit,
    onStyleChange: (QuoteCardStyle) -> Unit,
    isWideScreen: Boolean
) {
    val contentPadding = if (isWideScreen) 20.dp else 16.dp
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = contentPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Style selector
        item {
            QuoteStyleSelector(
                selectedStyle = state.selectedCardStyle,
                onStyleChange = onStyleChange
            )
        }
        
        // Daily Quote
        if (state.dailyQuote != null) {
            item {
                Text(
                    text = "Quote of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                QuoteCard(
                    quote = state.dailyQuote!!,
                    style = state.selectedCardStyle,
                    onLike = { onToggleLike(state.dailyQuote!!) },
                    onShare = { onShare(state.dailyQuote!!) }
                )
            }
        }
        
        // All Quotes
        if (state.quotes.isNotEmpty()) {
            item {
                Text(
                    text = "All Quotes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }
            
            items(state.quotes, key = { it.id }) { quote ->
                QuoteCard(
                    quote = quote,
                    style = state.selectedCardStyle,
                    onLike = { onToggleLike(quote) },
                    onShare = { onShare(quote) }
                )
            }
        }
        
        if (state.quotes.isEmpty() && state.dailyQuote == null) {
            item {
                EmptyQuotesPlaceholder()
            }
        }
    }
}

@Composable
private fun QuoteStyleSelector(
    selectedStyle: QuoteCardStyle,
    onStyleChange: (QuoteCardStyle) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Card Style",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val styleList = remember { QuoteCardStyle.entries.toList() }
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(styleList) { style ->
                    FilterChip(
                        selected = selectedStyle == style,
                        onClick = { onStyleChange(style) },
                        label = { 
                            Text(style.name.lowercase().replaceFirstChar { it.uppercase() }) 
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyQuotesPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📚", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No quotes yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Submit your favorite book quotes!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// ==================== SUBMIT TAB ====================

@Composable
private fun SubmitQuoteTab(
    state: ReadingHubState,
    onSubmit: (text: String, bookTitle: String, author: String, category: String) -> Unit,
    isWideScreen: Boolean
) {
    val contentPadding = if (isWideScreen) 20.dp else 16.dp
    
    var quoteText by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = contentPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "✍️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Submit a Quote",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = quoteText,
                        onValueChange = { quoteText = it },
                        label = { Text("Quote") },
                        placeholder = { Text("Enter the quote text...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = bookTitle,
                        onValueChange = { bookTitle = it },
                        label = { Text("Book Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = author,
                        onValueChange = { author = it },
                        label = { Text("Author (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (optional)") },
                        placeholder = { Text("e.g., Inspirational, Romance, Fantasy") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = {
                            if (quoteText.isNotBlank() && bookTitle.isNotBlank()) {
                                onSubmit(quoteText, bookTitle, author, category)
                                quoteText = ""
                                bookTitle = ""
                                author = ""
                                category = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = quoteText.isNotBlank() && bookTitle.isNotBlank() && !state.isSubmitting,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Submit Quote")
                    }
                }
            }
        }
        
        // Guidelines
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submission Guidelines",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val guidelines = listOf(
                        "Quote must be from a book you've read",
                        "Keep quotes under 500 characters",
                        "Include accurate book title",
                        "No spoilers in the quote"
                    )
                    
                    guidelines.forEach { guideline ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = guideline,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
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

private fun formatReadingTimeCompact(minutes: Long): String {
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
