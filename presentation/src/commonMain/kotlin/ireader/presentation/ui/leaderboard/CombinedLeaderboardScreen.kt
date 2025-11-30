package ireader.presentation.ui.leaderboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

enum class LeaderboardTab(val title: String, val icon: @Composable () -> Unit) {
    READING(
        title = "Reading",
        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
    ),
    DONATIONS(
        title = "Donations",
        icon = { Icon(Icons.Default.Favorite, contentDescription = null) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedLeaderboardScreen(
    readingVm: LeaderboardViewModel,
    donationVm: DonationLeaderboardViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val readingState = readingVm.state
    val donationState = donationVm.state
    
    val pagerState = rememberPagerState(pageCount = { LeaderboardTab.entries.size })
    val coroutineScope = rememberCoroutineScope()
    
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
                        Icon(Icons.Default.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                },
                actions = {
                    // Sync/Refresh button based on current tab
                    IconButton(
                        onClick = {
                            when (pagerState.currentPage) {
                                0 -> readingVm.syncUserStats()
                                1 -> donationVm.loadDonationLeaderboard()
                            }
                        },
                        enabled = when (pagerState.currentPage) {
                            0 -> !readingState.isSyncing
                            else -> !donationState.isLoading
                        }
                    ) {
                        if ((pagerState.currentPage == 0 && readingState.isSyncing) ||
                            (pagerState.currentPage == 1 && donationState.isLoading)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                if (pagerState.currentPage == 0) Icons.Default.Sync else Icons.Default.Refresh,
                                contentDescription = localizeHelper.localize(Res.string.refresh),
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                LeaderboardTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                tab.icon()
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = tab.title,
                                    fontWeight = if (pagerState.currentPage == index) 
                                        FontWeight.Bold 
                                    else 
                                        FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }
            
            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ReadingLeaderboardContent(
                        state = readingState,
                        onSync = { readingVm.syncUserStats() },
                        onToggleRealtime = { readingVm.toggleRealtimeUpdates(it) },
                        onClearError = { readingVm.clearError() }
                    )
                    1 -> DonationLeaderboardContent(
                        state = donationState,
                        onToggleRealtime = { donationVm.toggleRealtimeUpdates(it) },
                        onRefresh = { donationVm.loadDonationLeaderboard() }
                    )
                }
            }
        }
    }
}
