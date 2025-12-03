package ireader.presentation.ui.settings.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.core.LocalNavigator
import ireader.domain.models.entities.*
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.utils.formatPercentage
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Enhanced statistics screen with comprehensive analytics and discovery features
 * Implements Mihon's comprehensive statistics system with tabs and visualizations
 */
class EnhancedStatisticsScreen : KoinComponent {
    
    private val screenModel: StatsScreenModel by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navigator = LocalNavigator.current
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localizeHelper.localize(Res.string.statistics_and_analytics)) },
                    navigationIcon = {
                        TopAppBarBackButton(onClick = { navigator?.popBackStack() })
                    },
                    actions = {
                        IconButton(onClick = { screenModel.refresh() }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                        IconButton(onClick = { screenModel.exportStatisticsToJson() }) {
                            Icon(Icons.Default.Download, "Export")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Tab row
                ScrollableTabRow(
                    selectedTabIndex = state.selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatsScreenModel.StatsTab.values().forEach { tab ->
                        Tab(
                            selected = state.selectedTab == tab,
                            onClick = { screenModel.selectTab(tab) },
                            text = { Text(tab.name.replace('_', ' ')) },
                            icon = {
                                Icon(
                                    imageVector = getTabIcon(tab),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Content based on selected tab
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> {
                        ErrorContent(
                            error = state.error!!,
                            onRetry = { screenModel.refresh() },
                            onDismiss = { screenModel.clearError() }
                        )
                    }
                    else -> {
                        when (state.selectedTab) {
                            StatsScreenModel.StatsTab.OVERVIEW -> OverviewTab(state)
                            StatsScreenModel.StatsTab.ANALYTICS -> AnalyticsTab(state)
                            StatsScreenModel.StatsTab.UPCOMING -> UpcomingTab(state)
                            StatsScreenModel.StatsTab.RECOMMENDATIONS -> RecommendationsTab(state)
                            StatsScreenModel.StatsTab.SEARCH -> SearchTab(state, screenModel)
                            StatsScreenModel.StatsTab.FILTERS -> FiltersTab(state, screenModel)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun OverviewTab(state: StatsScreenModel.State) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = localizeHelper.localize(Res.string.library_overview),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            // Library stats cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = localizeHelper.localize(Res.string.total_books),
                        value = state.libraryInsights.totalBooks.toString(),
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = localizeHelper.localize(Res.string.in_library_1),
                        value = state.libraryInsights.booksInLibrary.toString(),
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = localizeHelper.localize(Res.string.completed),
                        value = state.libraryInsights.booksCompleted.toString(),
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = localizeHelper.localize(Res.string.in_progress),
                        value = state.libraryInsights.booksInProgress.toString(),
                        icon = Icons.Default.AutoStories,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                CompletionRateCard(state.libraryInsights.completionRate)
            }

            // Genre distribution
            if (state.libraryInsights.genreDistribution.isNotEmpty()) {
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.top_genres),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.libraryInsights.genreDistribution.take(5)) { genreCount ->
                    GenreCard(genreCount)
                }
            }

            // Top authors
            if (state.libraryInsights.topAuthors.isNotEmpty()) {
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.top_authors),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.libraryInsights.topAuthors.take(5)) { authorCount ->
                    AuthorCard(authorCount)
                }
            }
        }
    }

    @Composable
    private fun AnalyticsTab(state: StatsScreenModel.State) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = localizeHelper.localize(Res.string.reading_analytics),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                StatCard(
                    title = localizeHelper.localize(Res.string.total_reading_time),
                    value = formatReadingTime(state.readingAnalytics.totalReadingTimeMinutes),
                    icon = Icons.Default.Schedule
                )
            }

            item {
                StatCard(
                    title = localizeHelper.localize(Res.string.reading_speed),
                    value = "${state.readingAnalytics.averageReadingSpeedWPM} WPM",
                    icon = Icons.Default.Speed
                )
            }

            item {
                StatCard(
                    title = localizeHelper.localize(Res.string.words_read),
                    value = formatNumber(state.readingAnalytics.totalWordsRead),
                    icon = Icons.Default.Article
                )
            }

            item {
                StatCard(
                    title = localizeHelper.localize(Res.string.reading_streak),
                    value = "${state.libraryInsights.readingPatterns.readingStreak} days",
                    icon = Icons.Default.LocalFireDepartment
                )
            }

            // Recent reading sessions
            if (state.readingAnalytics.readingSessions.isNotEmpty()) {
                item {
                    Text(
                        text = localizeHelper.localize(Res.string.recent_sessions),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(state.readingAnalytics.readingSessions.take(10)) { session ->
                    ReadingSessionCard(session)
                }
            }
        }
    }

    @Composable
    private fun UpcomingTab(state: StatsScreenModel.State) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = localizeHelper.localize(Res.string.upcoming_releases),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.upcomingReleases.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "No upcoming releases tracked",
                        icon = Icons.Default.CalendarToday
                    )
                }
            } else {
                items(state.upcomingReleases) { release ->
                    UpcomingReleaseCard(release)
                }
            }
        }
    }

    @Composable
    private fun RecommendationsTab(state: StatsScreenModel.State) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = localizeHelper.localize(Res.string.recommended_for_you),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.recommendations.isEmpty()) {
                item {
                    EmptyStateCard(
                        message = "No recommendations available",
                        icon = Icons.Default.Recommend
                    )
                }
            } else {
                items(state.recommendations) { recommendation ->
                    RecommendationCard(recommendation)
                }
            }
        }
    }

    @Composable
    private fun SearchTab(state: StatsScreenModel.State, screenModel: StatsScreenModel) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.global_search),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { /* Update search query */ },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(localizeHelper.localize(Res.string.search_across_all_sources_1)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            )

            Button(
                onClick = { screenModel.performGlobalSearch(state.searchQuery) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.searchQuery.isNotBlank() && !state.isSearching
            ) {
                Text(localizeHelper.localize(Res.string.search))
            }

            // Search results
            if (state.searchResults != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.searchResults.sourceResults.forEach { sourceResult ->
                        item {
                            SourceSearchResultCard(sourceResult)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FiltersTab(state: StatsScreenModel.State, screenModel: StatsScreenModel) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = localizeHelper.localize(Res.string.advanced_filters),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text(localizeHelper.localize(Res.string.filter_your_library_with_advanced_criteria))
            }

            // Genre filters
            item {
                Text(
                    text = localizeHelper.localize(Res.string.genres),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text("Available genres: ${state.availableGenres.size}")
            }

            // Author filters
            item {
                Text(
                    text = localizeHelper.localize(Res.string.authors),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Text("Available authors: ${state.availableAuthors.size}")
            }

            // Filtered results
            if (state.filteredBooks.isNotEmpty()) {
                item {
                    Text(
                        text = "Filtered Results (${state.filteredBooks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(state.filteredBooks.take(20)) { book ->
                    BookItemCard(book)
                }
            }
        }
    }

    @Composable
    private fun StatCard(
        title: String,
        value: String,
        icon: ImageVector,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun CompletionRateCard(completionRate: Float) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.completion_rate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = completionRate / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )
                Text(
                    text = formatPercentage(completionRate.toDouble()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun GenreCard(genreCount: GenreCount) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = genreCount.genre,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${genreCount.bookCount} books",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun AuthorCard(authorCount: AuthorCount) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = authorCount.author,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${authorCount.bookCount} books ? ${authorCount.chaptersRead} chapters read",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun ReadingSessionCard(session: ReadingSession) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.bookTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.durationMinutes} min ? ${session.chaptersRead} chapters",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun UpcomingReleaseCard(release: UpcomingRelease) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = release.bookTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Frequency: ${release.releaseFrequency.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    @Composable
    private fun RecommendationCard(recommendation: BookRecommendation) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = recommendation.bookTitle,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(recommendation.score * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = recommendation.author,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Reason: ${recommendation.reason.name.replace('_', ' ')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun SourceSearchResultCard(sourceResult: SourceSearchResult) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sourceResult.sourceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (sourceResult.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = "${sourceResult.results.size} results",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                if (sourceResult.error != null) {
                    Text(
                        text = "Error: ${sourceResult.error}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    @Composable
    private fun BookItemCard(book: BookItem) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = book.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (book.favorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    @Composable
    private fun EmptyStateCard(message: String, icon: ImageVector) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    @Composable
    private fun ErrorContent(
        error: String,
        onRetry: () -> Unit,
        onDismiss: () -> Unit
    ) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = localizeHelper.localize(Res.string.download_notifier_title_error),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss) {
                    Text(localizeHelper.localize(Res.string.dismiss))
                }
                Button(onClick = onRetry) {
                    Text(localizeHelper.localize(Res.string.retry))
                }
            }
        }
    }

    private fun getTabIcon(tab: StatsScreenModel.StatsTab): ImageVector {
        return when (tab) {
            StatsScreenModel.StatsTab.OVERVIEW -> Icons.Default.Dashboard
            StatsScreenModel.StatsTab.ANALYTICS -> Icons.Default.Analytics
            StatsScreenModel.StatsTab.UPCOMING -> Icons.Default.CalendarToday
            StatsScreenModel.StatsTab.RECOMMENDATIONS -> Icons.Default.Recommend
            StatsScreenModel.StatsTab.SEARCH -> Icons.Default.Search
            StatsScreenModel.StatsTab.FILTERS -> Icons.Default.FilterList
        }
    }

    private fun formatReadingTime(minutes: Long): String {
        return when {
            minutes < 60 -> "$minutes min"
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

    private fun formatNumber(number: Long): String {
        return when {
            number < 1000 -> number.toString()
            number < 1000000 -> "${number / 1000}K"
            else -> "${number / 1000000}M"
        }
    }
}
