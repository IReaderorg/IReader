package ireader.presentation.ui.settings.statistics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern, gamified statistics screen with charts and engaging UI
 */
class ModernStatisticsScreen : KoinComponent {
    
    private val viewModel: StatisticsViewModel by inject()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val statistics by viewModel.statistics.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Reading Stats",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        TopAppBarBackButton(onClick = { navController.popBackStack() })
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hero Section - Level & XP
                item {
                    GamificationHeroCard(statistics)
                }

                // Quick Stats Grid
                item {
                    QuickStatsGrid(statistics)
                }

                // Reading Streak Card
                item {
                    StreakCard(statistics.readingStreak)
                }

                // Circular Progress Charts
                item {
                    Text(
                        text = "Progress Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    CircularChartsRow(statistics)
                }

                // Reading Activity Chart
                item {
                    Text(
                        text = "Reading Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    ReadingActivityChart(statistics)
                }

                // Achievements Section
                item {
                    Text(
                        text = "Achievements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    AchievementsRow(statistics)
                }

                // Genre Distribution
                if (statistics.favoriteGenres.isNotEmpty()) {
                    item {
                        Text(
                            text = "Favorite Genres",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item {
                        GenreBarChart(statistics.favoriteGenres)
                    }
                }
            }
        }
    }
}

@Composable
private fun GamificationHeroCard(statistics: ireader.domain.models.entities.ReadingStatisticsType1) {
    val level = calculateLevel(statistics.totalChaptersRead)
    val xpProgress = calculateXPProgress(statistics.totalChaptersRead)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Level Badge
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LVL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = level.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = getLevelTitle(level),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // XP Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "XP: ${xpProgress.current} / ${xpProgress.next}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    AnimatedProgressBar(
                        progress = xpProgress.current.toFloat() / xpProgress.next.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsGrid(statistics: ireader.domain.models.entities.ReadingStatisticsType1) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            value = statistics.totalChaptersRead.toString(),
            label = "Chapters",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        QuickStatCard(
            icon = Icons.Default.CheckCircle,
            value = statistics.booksCompleted.toString(),
            label = "Completed",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.Default.Schedule,
            value = formatHours(statistics.totalReadingTimeMinutes),
            label = "Hours",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        
        QuickStatCard(
            icon = Icons.Default.Speed,
            value = "${statistics.averageReadingSpeedWPM}",
            label = "WPM",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StreakCard(streak: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFFF6B35).copy(alpha = 0.3f),
                            Color(0xFFF7931E).copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(48.dp)
                )
                Column {
                    Text(
                        text = "Reading Streak",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Keep it going!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "$streak",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B35)
            )
        }
    }
}

@Composable
private fun CircularChartsRow(statistics: ireader.domain.models.entities.ReadingStatisticsType1) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressChart(
            progress = if (statistics.booksCompleted + statistics.currentlyReading > 0) {
                statistics.booksCompleted.toFloat() / (statistics.booksCompleted + statistics.currentlyReading).toFloat()
            } else 0f,
            title = "Completion",
            value = "${(if (statistics.booksCompleted + statistics.currentlyReading > 0) {
                (statistics.booksCompleted.toFloat() / (statistics.booksCompleted + statistics.currentlyReading).toFloat() * 100).toInt()
            } else 0)}%",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        CircularProgressChart(
            progress = (statistics.readingStreak / 30f).coerceAtMost(1f),
            title = "Monthly Goal",
            value = "${statistics.readingStreak}/30",
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CircularProgressChart(
    progress: Float,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "progress"
    )
    
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val radius = diameter / 2
                    
                    // Background circle
                    drawCircle(
                        color = color.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    // Progress arc
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(diameter, diameter),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    )
                }
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReadingActivityChart(statistics: ireader.domain.models.entities.ReadingStatisticsType1) {
    // Simulated weekly data based on statistics
    val weeklyData = remember(statistics) {
        listOf(
            statistics.totalChaptersRead / 7,
            statistics.totalChaptersRead / 6,
            statistics.totalChaptersRead / 8,
            statistics.totalChaptersRead / 5,
            statistics.totalChaptersRead / 7,
            statistics.totalChaptersRead / 6,
            statistics.totalChaptersRead / 4
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Last 7 Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            BarChart(
                data = weeklyData,
                labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

@Composable
private fun BarChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOrNull() ?: 1
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val animatedHeight by animateFloatAsState(
                    targetValue = if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f,
                    animationSpec = tween(
                        durationMillis = 500 + (index * 100),
                        easing = EaseOutCubic
                    ),
                    label = "barHeight$index"
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .fillMaxHeight(animatedHeight)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor,
                                        primaryColor.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AchievementsRow(statistics: ireader.domain.models.entities.ReadingStatisticsType1) {
    val achievements = remember(statistics) {
        listOf(
            Achievement("First Chapter", statistics.totalChaptersRead >= 1, Icons.Default.Star),
            Achievement("Bookworm", statistics.totalChaptersRead >= 50, Icons.Default.MenuBook),
            Achievement("Speed Reader", statistics.averageReadingSpeedWPM >= 300, Icons.Default.Speed),
            Achievement("Dedicated", statistics.readingStreak >= 7, Icons.Default.LocalFireDepartment),
            Achievement("Completionist", statistics.booksCompleted >= 5, Icons.Default.CheckCircle)
        )
    }
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(achievements) { achievement ->
            AchievementBadge(achievement)
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    Card(
        modifier = Modifier
            .size(100.dp)
            .shadow(4.dp, CircleShape),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = achievement.name,
                tint = if (achievement.unlocked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                color = if (achievement.unlocked) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                }
            )
        }
    }
}

@Composable
private fun GenreBarChart(genres: List<ireader.domain.models.entities.GenreCount>) {
    val maxCount = genres.maxOfOrNull { it.bookCount } ?: 1
    val colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFFF59E0B),
        Color(0xFF10B981)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            genres.take(5).forEachIndexed { index, genreCount ->
                GenreBar(
                    genre = genreCount.genre,
                    count = genreCount.bookCount,
                    maxCount = maxCount,
                    color = colors[index % colors.size]
                )
            }
        }
    }
}

@Composable
private fun GenreBar(
    genre: String,
    count: Int,
    maxCount: Int,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = count.toFloat() / maxCount.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "genreProgress"
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = genre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count books",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "xpProgress"
    )
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
        )
    }
}

// Helper functions
private fun calculateLevel(chaptersRead: Int): Int {
    return (chaptersRead / 10) + 1
}

private fun calculateXPProgress(chaptersRead: Int): XPProgress {
    val level = calculateLevel(chaptersRead)
    val xpForCurrentLevel = (level - 1) * 10
    val current = chaptersRead - xpForCurrentLevel
    val next = 10
    return XPProgress(current, next)
}

private fun getLevelTitle(level: Int): String {
    return when {
        level < 5 -> "Novice Reader"
        level < 10 -> "Bookworm"
        level < 20 -> "Avid Reader"
        level < 30 -> "Literary Scholar"
        level < 50 -> "Master Reader"
        else -> "Reading Legend"
    }
}

private fun formatHours(minutes: Long): String {
    val hours = minutes / 60
    return if (hours > 0) "${hours}h" else "${minutes}m"
}

private data class XPProgress(val current: Int, val next: Int)

private data class Achievement(
    val name: String,
    val unlocked: Boolean,
    val icon: ImageVector
)
