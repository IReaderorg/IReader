package ireader.presentation.ui.settings.statistics

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StatisticsScreen : KoinComponent {
    
    private val viewModel: StatisticsViewModel by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val statistics by viewModel.statistics.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reading Statistics") },
                    navigationIcon = {
                        TopAppBarBackButton(onClick = { navController.popBackStack() })
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Your Reading Journey",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    StatCard(
                        title = "Chapters Read",
                        value = statistics.totalChaptersRead.toString(),
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    StatCard(
                        title = "Reading Time",
                        value = formatReadingTime(statistics.totalReadingTimeMinutes),
                        icon = Icons.Default.Schedule,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                item {
                    StatCard(
                        title = "Books Completed",
                        value = statistics.booksCompleted.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                item {
                    StatCard(
                        title = "Currently Reading",
                        value = statistics.currentlyReading.toString(),
                        icon = Icons.Default.AutoStories,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    StatCard(
                        title = "Reading Streak",
                        value = "${statistics.readingStreak} days",
                        icon = Icons.Default.LocalFireDepartment,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                item {
                    StatCard(
                        title = "Average Reading Speed",
                        value = "${statistics.averageReadingSpeedWPM} WPM",
                        icon = Icons.Default.Speed,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (statistics.favoriteGenres.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favorite Genres",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    item {
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
                                statistics.favoriteGenres.forEach { genreCount ->
                                    GenreItem(
                                        genre = genreCount.genre,
                                        count = genreCount.bookCount
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GenreItem(
    genre: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = genre,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "$count books",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
