# Reading Analytics Plugin API

Comprehensive reading statistics and tracking for plugins.

## Features

- **Session Tracking**: Track reading sessions with duration, words read, pages
- **Daily/Weekly/Monthly Stats**: Aggregated reading statistics
- **Reading Speed**: Track and analyze reading speed (WPM)
- **Streaks**: Track consecutive reading days
- **Goals**: Set and track reading goals
- **Achievements**: Unlock achievements based on reading activity
- **Milestones**: Record significant reading milestones
- **Patterns**: Analyze reading habits and patterns

## Usage

### Session Tracking

```kotlin
// Start a reading session
val session = analyticsManager.startSession(
    bookId = bookId,
    bookTitle = "My Book",
    chapterId = chapterId,
    position = 0,
    deviceType = "android"
)

// Update session progress
analyticsManager.updateSession(
    chapterId = newChapterId,
    position = newPosition,
    wordsRead = 500,
    pagesRead = 2
)

// Pause/Resume
analyticsManager.pauseSession()
analyticsManager.resumeSession()

// End session
val completedSession = analyticsManager.endSession()
```

### Statistics

```kotlin
// Today's stats
val todayStats = analyticsManager.todayStats.value

// Weekly stats
val weeklyStats = analyticsManager.getWeeklyStats()

// Book-specific stats
val bookStats = analyticsManager.getBookStats(bookId)

// Overall stats
val overallStats = analyticsManager.getOverallStats()
// Returns: totalReadingTime, totalBooks, averageWpm, streaks, etc.
```

### Reading Speed

```kotlin
// Get speed analysis
val speedAnalysis = analyticsManager.getSpeedAnalysis()
// Returns: overallWpm, byGenre, byTimeOfDay, trend, percentile
```

### Streaks

```kotlin
// Get current streak
val currentStreak = analyticsManager.getCurrentStreak()

// Get longest streak
val longestStreak = analyticsManager.getLongestStreak()
```

### Goals

```kotlin
// Create a goal
analyticsManager.createGoal(
    type = GoalType.BOOKS,
    target = 12,
    period = GoalPeriod.YEARLY
)

// Create daily reading time goal
analyticsManager.createGoal(
    type = GoalType.TIME_MINUTES,
    target = 30,
    period = GoalPeriod.DAILY
)

// Get active goals
val goals = analyticsManager.activeGoals.value

// Update goal progress (usually automatic)
analyticsManager.updateGoalProgress(goalId, newProgress)
```

### Achievements

```kotlin
// Get all achievements
val achievements = analyticsManager.loadAchievements()

// Get unlocked achievements
val unlocked = analyticsManager.getUnlockedAchievements()

// Get total points
val points = achievements.filter { it.isUnlocked }.sumOf { it.points }
```

### Milestones

```kotlin
// Get milestones
val milestones = analyticsManager.getMilestones()

// Record milestone (usually automatic)
analyticsManager.recordMilestone(
    type = MilestoneType.BOOKS_10,
    value = 10,
    bookId = bookId,
    bookTitle = "My 10th Book"
)
```

### Reading Patterns

```kotlin
// Get reading pattern analysis
val pattern = analyticsManager.getReadingPattern()
// Returns: hourlyDistribution, dailyDistribution, preferredSessionLength, etc.

// Get heatmap data
val heatmap = analyticsManager.getReadingHeatmap(2024)
```

### Period Comparison

```kotlin
// Compare this week vs last week
val comparison = analyticsManager.comparePeriods(
    currentStart = thisWeekStart,
    currentEnd = thisWeekEnd,
    previousStart = lastWeekStart,
    previousEnd = lastWeekEnd
)
// Returns: readingTimeChange, sessionsChange, speedChange, etc.
```

### Events

```kotlin
// Subscribe to events
analyticsManager.events.collect { event ->
    when (event) {
        is ReadingAnalyticsEvent.SessionStarted -> { }
        is ReadingAnalyticsEvent.SessionEnded -> { }
        is ReadingAnalyticsEvent.GoalCompleted -> showCelebration(event.goal)
        is ReadingAnalyticsEvent.AchievementUnlocked -> showAchievement(event.achievement)
        is ReadingAnalyticsEvent.MilestoneReached -> showMilestone(event.milestone)
        is ReadingAnalyticsEvent.StreakUpdated -> updateStreakUI(event.currentStreak)
    }
}
```

## Plugin API

Plugins can access reading analytics through `ReadingAnalyticsApi`:

```kotlin
class MyPlugin : FeaturePlugin {
    private lateinit var analyticsApi: ReadingAnalyticsApi
    
    override fun initialize(context: PluginContext) {
        analyticsApi = context.getApi(ReadingAnalyticsApi::class)
    }
    
    suspend fun showReadingStats() {
        val stats = analyticsApi.getOverallStats()
        val streak = analyticsApi.getCurrentStreak()
        val goals = analyticsApi.getActiveGoals()
        // Display stats...
    }
    
    fun trackReading() {
        analyticsApi.subscribeToEvents().collect { event ->
            when (event) {
                is ReadingAnalyticsEventInfo.GoalCompleted -> {
                    // Show notification
                }
            }
        }
    }
}
```

## Data Models

### GoalType
- BOOKS - Number of books to read
- CHAPTERS - Number of chapters to read
- PAGES - Number of pages to read
- WORDS - Number of words to read
- TIME_MINUTES - Reading time in minutes
- SESSIONS - Number of reading sessions

### GoalPeriod
- DAILY
- WEEKLY
- MONTHLY
- YEARLY
- CUSTOM

### AchievementCategory
- READING_TIME
- BOOKS_COMPLETED
- STREAK
- SPEED
- EXPLORATION
- SOCIAL
- SPECIAL

### AchievementTier
- BRONZE
- SILVER
- GOLD
- PLATINUM
- DIAMOND

### MilestoneType
- FIRST_BOOK_COMPLETED
- BOOKS_10, BOOKS_50, BOOKS_100
- HOURS_10, HOURS_100, HOURS_1000
- WORDS_100K, WORDS_1M
- STREAK_7, STREAK_30, STREAK_100, STREAK_365

## Default Achievements

The system includes predefined achievements:

| Name | Category | Requirement |
|------|----------|-------------|
| First Steps | READING_TIME | Read for 1 hour |
| Bookworm | READING_TIME | Read for 10 hours |
| Speed Reader | SPEED | Reach 300 WPM |
| Consistent | STREAK | 7-day streak |
| Dedicated | STREAK | 30-day streak |
| First Book | BOOKS_COMPLETED | Complete 1 book |
| Avid Reader | BOOKS_COMPLETED | Complete 10 books |
