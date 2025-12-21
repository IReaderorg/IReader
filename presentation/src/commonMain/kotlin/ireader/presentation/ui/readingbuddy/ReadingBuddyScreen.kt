package ireader.presentation.ui.readingbuddy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.quote.BuddyAchievement
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.readingbuddy.components.QuoteCard
import ireader.presentation.ui.readingbuddy.components.ReadingBuddyCharacter
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

enum class ReadingBuddyTab(val title: String, val icon: @Composable () -> Unit) {
    BUDDY("Buddy", { Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(20.dp)) }),
    DAILY_QUOTE("Daily", { Icon(Icons.Default.FormatQuote, contentDescription = null, modifier = Modifier.size(20.dp)) }),
    QUOTES("Quotes", { Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(20.dp)) }),
    SUBMIT("Submit", { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingBuddyScreen(
    vm: ReadingBuddyViewModel,
    onBack: () -> Unit,
    onShareQuote: (Quote, QuoteCardStyle) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues
) {
    val state by vm.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { ReadingBuddyTab.entries.size })
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
    
    if (state.showAchievementDialog && state.newAchievement != null) {
        ModernAchievementDialog(
            achievement = state.newAchievement!!,
            onDismiss = { vm.dismissAchievementDialog() }
        )
    }

    
    Scaffold(
        modifier = Modifier.padding(paddingValues),
        topBar = {
            ModernTopBar(onBack = onBack, isWideScreen = isWideScreen)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isWideScreen) {
            // Desktop/Tablet: Side navigation with content
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Side Navigation Rail
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ReadingBuddyTab.entries.forEachIndexed { index, tab ->
                        NavigationRailItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            icon = { tab.icon() },
                            label = { Text(tab.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                
                // Main Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false
                    ) { page ->
                        when (page) {
                            0 -> ModernBuddyTab(state = state, isWideScreen = true)
                            1 -> ModernDailyQuoteTab(state, { vm.setCardStyle(it) }, 
                                { state.dailyQuote?.let { vm.toggleLike(it.id) } },
                                { state.dailyQuote?.let { q -> onShareQuote(q, state.selectedCardStyle) } }, true)
                            2 -> ModernAllQuotesTab(state, { vm.toggleLike(it) }, 
                                { quote -> onShareQuote(quote, state.selectedCardStyle) }, true)
                            3 -> ModernSubmitQuoteTab(state, 
                                { t, b, a, c -> vm.submitQuote(t, b, a, c) }, true)
                        }
                    }
                }
            }
        } else {
            // Mobile: Bottom tabs with pager
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> ModernBuddyTab(state = state, isWideScreen = false)
                        1 -> ModernDailyQuoteTab(state, { vm.setCardStyle(it) },
                            { state.dailyQuote?.let { vm.toggleLike(it.id) } },
                            { state.dailyQuote?.let { q -> onShareQuote(q, state.selectedCardStyle) } }, false)
                        2 -> ModernAllQuotesTab(state, { vm.toggleLike(it) },
                            { quote -> onShareQuote(quote, state.selectedCardStyle) }, false)
                        3 -> ModernSubmitQuoteTab(state,
                            { t, b, a, c -> vm.submitQuote(t, b, a, c) }, false)
                    }
                }
                
                // Modern Bottom Navigation
                ModernBottomNav(
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = { coroutineScope.launch { pagerState.animateScrollToPage(it) } }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernTopBar(onBack: () -> Unit, isWideScreen: Boolean) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Animated rabbit emoji
            val infiniteTransition = rememberInfiniteTransition(label = localizeHelper.localize(Res.string.bounce))
            val bounce by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = localizeHelper.localize(Res.string.bounce)
            )
            
            Text(
                text = "🐰",
                fontSize = 28.sp,
                modifier = Modifier.offset { IntOffset(0, bounce.dp.roundToPx()) }
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.reading_buddy),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = localizeHelper.localize(Res.string.your_reading_companion),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModernBottomNav(selectedIndex: Int, onTabSelected: (Int) -> Unit) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ReadingBuddyTab.entries.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index
                val animatedWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.5f else 1f,
                    animationSpec = spring(dampingRatio = 0.8f),
                    label = localizeHelper.localize(Res.string.weight)
                )
                
                Surface(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .padding(horizontal = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        Color.Transparent,
                    onClick = { onTabSelected(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        tab.icon()
                        AnimatedVisibility(visible = isSelected) {
                            Row {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==================== BUDDY TAB ====================

@Composable
private fun ModernBuddyTab(state: ReadingBuddyScreenState, isWideScreen: Boolean) {
    val contentPadding = if (isWideScreen) 24.dp else 16.dp
    val maxWidth = if (isWideScreen) 800.dp else Dp.Unspecified
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(if (maxWidth != Dp.Unspecified) Modifier.widthIn(max = maxWidth) else Modifier),
        contentPadding = PaddingValues(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Section with Buddy
        item {
            HeroBuddySection(state = state, isWideScreen = isWideScreen)
        }
        
        // Stats Grid
        item {
            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ModernStatsCard(state, Modifier.weight(1f))
                    ModernLevelCard(state.buddyState.level, state.levelProgress, Modifier.weight(1f))
                }
            } else {
                ModernStatsCard(state, Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                ModernLevelCard(state.buddyState.level, state.levelProgress, Modifier.fillMaxWidth())
            }
        }
        
        // Achievements
        item {
            ModernAchievementsCard(state.unlockedAchievements, isWideScreen)
        }
    }
}

@Composable
private fun HeroBuddySection(state: ReadingBuddyScreenState, isWideScreen: Boolean) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val gradientColors = listOf(
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.background
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(gradientColors))
            .padding(if (isWideScreen) 32.dp else 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            ReadingBuddyCharacter(
                state = state.buddyState,
                modifier = Modifier
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick stats row
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                QuickStat(emoji = "🔥", value = "${state.buddyState.currentStreak}", label = localizeHelper.localize(Res.string.streak))
                QuickStat(emoji = "⭐", value = "Lv.${state.buddyState.level}", label = localizeHelper.localize(Res.string.level))
                QuickStat(emoji = "📚", value = "${state.buddyState.totalBooksRead}", label = localizeHelper.localize(Res.string.books))
            }
        }
    }
}

@Composable
private fun QuickStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
private fun ModernStatsCard(state: ReadingBuddyScreenState, modifier: Modifier = Modifier) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.reading_stats),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItemModern("📚", "${state.buddyState.totalBooksRead}", "Books Read")
                StatItemModern("📖", "${state.buddyState.totalChaptersRead}", "Chapters")
                StatItemModern("🏆", "${state.buddyState.longestStreak}", "Best Streak")
            }
        }
    }
}

@Composable
private fun StatItemModern(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModernLevelCard(level: Int, progress: Float, modifier: Modifier = Modifier) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = localizeHelper.localize(Res.string.progress_1)
    )
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⭐", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Level $level",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(6.dp))
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = localizeHelper.localize(Res.string.keep_reading_to_level_up_1),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}


@Composable
private fun ModernAchievementsCard(achievements: List<BuddyAchievement>, isWideScreen: Boolean) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.achievements),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${achievements.size}/14",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (achievements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎯", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.start_reading_to_unlock),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(achievements, key = { it.name }) { achievement ->
                        ModernAchievementChip(achievement)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernAchievementChip(achievement: BuddyAchievement) {
    val emoji = when {
        achievement.name.contains("BOOK") -> "📚"
        achievement.name.contains("CHAPTER") -> "📖"
        achievement.name.contains("STREAK") -> "🔥"
        achievement.name.contains("NIGHT") -> "🌙"
        achievement.name.contains("EARLY") -> "🌅"
        achievement.name.contains("MARATHON") -> "🏃"
        achievement.name.contains("QUOTE") -> "💬"
        else -> "🏅"
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


// ==================== DAILY QUOTE TAB ====================

@Composable
private fun ModernDailyQuoteTab(
    state: ReadingBuddyScreenState,
    onStyleChange: (QuoteCardStyle) -> Unit,
    onLike: () -> Unit,
    onShare: () -> Unit,
    isWideScreen: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val contentPadding = if (isWideScreen) 24.dp else 16.dp
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.todays_quote_1),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.a_daily_dose_of_inspiration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Box(
                modifier = Modifier
                    .then(if (isWideScreen) Modifier.widthIn(max = 500.dp) else Modifier.fillMaxWidth())
            ) {
                if (state.dailyQuote != null) {
                    QuoteCard(
                        quote = state.dailyQuote,
                        style = state.selectedCardStyle,
                        onLike = onLike,
                        onShare = onShare
                    )
                } else {
                    EmptyQuoteCard()
                }
            }
        }
        
        item {
            // Style Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.card_style),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Cache the list to avoid recreation on each recomposition
                    val styleList = remember { QuoteCardStyle.entries.toList() }
                    
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(styleList, key = { it.name }) { style ->
                            FilterChip(
                                selected = state.selectedCardStyle == style,
                                onClick = { onStyleChange(style) },
                                label = { Text(style.displayName) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyQuoteCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📚", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localizeHelper.localize(Res.string.no_quotes_yet_1),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.be_the_first_to_submit_a_quote),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


// ==================== ALL QUOTES TAB ====================

@Composable
private fun ModernAllQuotesTab(
    state: ReadingBuddyScreenState,
    onLike: (String) -> Unit,
    onShare: (Quote) -> Unit,
    isWideScreen: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val contentPadding = if (isWideScreen) 24.dp else 16.dp
    
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (state.approvedQuotes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📚", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = localizeHelper.localize(Res.string.no_quotes_yet),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.submit_the_first_quote),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        if (isWideScreen) {
            // Grid layout for desktop
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                contentPadding = PaddingValues(contentPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.approvedQuotes, key = { it.id }) { quote ->
                    ModernQuoteListItem(quote, { onLike(quote.id) }, { onShare(quote) })
                }
            }
        } else {
            // List layout for mobile
            LazyColumn(
                contentPadding = PaddingValues(contentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.approvedQuotes, key = { it.id }) { quote ->
                    ModernQuoteListItem(quote, { onLike(quote.id) }, { onShare(quote) })
                }
            }
        }
    }
}

@Composable
private fun ModernQuoteListItem(
    quote: Quote,
    onLike: () -> Unit,
    onShare: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Quote text
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Book info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = quote.bookTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (quote.author.isNotBlank()) {
                Text(
                    text = "by ${quote.author}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 22.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Like button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (quote.isLikedByUser) 
                        Color(0xFFFFE4EC) 
                    else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    onClick = onLike
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (quote.isLikedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = localizeHelper.localize(Res.string.like),
                            modifier = Modifier.size(18.dp),
                            tint = if (quote.isLikedByUser) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${quote.likesCount}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                
                // Share button
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = localizeHelper.localize(Res.string.share),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


// ==================== SUBMIT QUOTE TAB ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernSubmitQuoteTab(
    state: ReadingBuddyScreenState,
    onSubmit: (String, String, String, String) -> Unit,
    isWideScreen: Boolean
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var quoteText by remember { mutableStateOf("") }
    var bookTitle by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var chapterTitle by remember { mutableStateOf("") }
    
    val contentPadding = if (isWideScreen) 24.dp else 16.dp
    val maxWidth = if (isWideScreen) 600.dp else Dp.Unspecified
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .then(if (maxWidth != Dp.Unspecified) Modifier.widthIn(max = maxWidth) else Modifier),
        contentPadding = PaddingValues(contentPadding),
        horizontalAlignment = if (isWideScreen) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.submit_a_quote),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.share_favorite_quotes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Quote text field
                    OutlinedTextField(
                        value = quoteText,
                        onValueChange = { quoteText = it },
                        label = { Text(localizeHelper.localize(Res.string.quote)) },
                        placeholder = { Text(localizeHelper.localize(Res.string.enter_quote_text)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Text("${quoteText.length}/1000 characters")
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Book title
                    OutlinedTextField(
                        value = bookTitle,
                        onValueChange = { bookTitle = it },
                        label = { Text(localizeHelper.localize(Res.string.book_title_1)) },
                        placeholder = { Text(localizeHelper.localize(Res.string.enter_book_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Outlined.MenuBook, contentDescription = null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Author and Chapter in row for wide screens
                    if (isWideScreen) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = author,
                                onValueChange = { author = it },
                                label = { Text(localizeHelper.localize(Res.string.author)) },
                                placeholder = { Text(localizeHelper.localize(Res.string.optional)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null)
                                }
                            )
                            OutlinedTextField(
                                value = chapterTitle,
                                onValueChange = { chapterTitle = it },
                                label = { Text(localizeHelper.localize(Res.string.chapter)) },
                                placeholder = { Text(localizeHelper.localize(Res.string.optional)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Icon(Icons.Outlined.Bookmark, contentDescription = null)
                                }
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text(localizeHelper.localize(Res.string.author_optional)) },
                            placeholder = { Text(localizeHelper.localize(Res.string.enter_author_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = chapterTitle,
                            onValueChange = { chapterTitle = it },
                            label = { Text(localizeHelper.localize(Res.string.chapter_optional)) },
                            placeholder = { Text(localizeHelper.localize(Res.string.enter_chapter_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = {
                                Icon(Icons.Outlined.Bookmark, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
        
        item {
            // Submit button
            Button(
                onClick = {
                    onSubmit(quoteText, bookTitle, author, chapterTitle)
                    quoteText = ""
                    bookTitle = ""
                    author = ""
                    chapterTitle = ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = quoteText.length >= 10 && bookTitle.isNotBlank() && !state.isSubmitting,
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit for Review", fontWeight = FontWeight.SemiBold)
            }
        }
        
        item {
            // Info card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.quotes_will_be_reviewed_by),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


// ==================== DIALOGS ====================

@Composable
private fun ModernAchievementDialog(
    achievement: BuddyAchievement,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val emoji = when {
        achievement.name.contains("BOOK") -> "📚"
        achievement.name.contains("CHAPTER") -> "📖"
        achievement.name.contains("STREAK") -> "🔥"
        achievement.name.contains("NIGHT") -> "🌙"
        achievement.name.contains("EARLY") -> "🌅"
        achievement.name.contains("MARATHON") -> "🏃"
        achievement.name.contains("QUOTE") -> "💬"
        else -> "🏅"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFFFFD700).copy(alpha = 0.3f),
                                Color(0xFFFFD700).copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 48.sp)
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉", fontSize = 32.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.achievement_unlocked),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "+${achievement.xpReward} XP",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Awesome! 🎊", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ==================== UTILITIES ====================

private fun formatQuoteForShare(quote: Quote): String {
    return buildString {
        append("\"${quote.text}\"\n\n")
        append("— ${quote.bookTitle}")
        if (quote.author.isNotBlank()) {
            append(" by ${quote.author}")
        }
        append("\n\n📚 Shared via IReader")
    }
}
