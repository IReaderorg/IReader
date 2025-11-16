# Performance Monitoring and Analytics System

This package provides a comprehensive performance monitoring and analytics system for the IReader application.

## Components

### 1. AnalyticsManager
Central manager that coordinates all analytics components. Use this as the main entry point.

```kotlin
val analyticsManager = AnalyticsManager(privacyMode = PrivacyMode.BALANCED)

// Initialize on app start
analyticsManager.initialize()

// Shutdown on app exit
analyticsManager.shutdown()
```

### 2. PerformanceMonitor
Tracks performance metrics across the application.

```kotlin
// Record TTS synthesis time
analyticsManager.performanceMonitor.recordSynthesisTime(durationMs = 150)

// Record UI frame time
analyticsManager.performanceMonitor.recordFrameTime(durationMs = 12.5)

// Record network latency
analyticsManager.performanceMonitor.recordNetworkLatency(durationMs = 250)

// Record database query time
analyticsManager.performanceMonitor.recordDatabaseQueryTime(durationMs = 45)

// Measure execution time automatically
val result = analyticsManager.measureTime(MetricType.SYNTHESIS_TIME) {
    // Your code here
    synthesizeText(text)
}
```

### 3. UsageAnalytics
Tracks feature usage and user sessions.

```kotlin
// Record feature usage
analyticsManager.trackFeature("book_opened", mapOf("source" to "library"))
analyticsManager.trackFeature("chapter_read")
analyticsManager.trackFeature("search_performed")

// Get session statistics
val sessionStats = analyticsManager.usageAnalytics.getSessionStatistics()
println("Average session: ${sessionStats.averageDuration / 1000}s")
```

### 4. ErrorTracker
Tracks application errors with context.

```kotlin
// Track an error
try {
    riskyOperation()
} catch (e: Exception) {
    analyticsManager.trackError(
        error = e,
        screen = "BookDetailScreen",
        userAction = "load_chapters",
        appState = mapOf("bookId" to "123")
    )
}

// Get error statistics
val errorStats = analyticsManager.errorTracker.getErrorStatistics()
println("Error rate: ${errorStats.errorRate} errors/hour")
```

### 5. PerformanceReporter
Generates comprehensive performance reports.

```kotlin
// Generate report
val report = analyticsManager.performanceReporter.generateReport()

// Export as JSON
val json = analyticsManager.performanceReporter.exportReportAsJson()

// Export as CSV
val csv = analyticsManager.performanceReporter.exportReportAsCsv()

// Log report to console
analyticsManager.logPerformanceReport()
```

## Privacy Modes

### STRICT
- Disables all analytics collection
- Use when user opts out completely

### BALANCED (Default)
- Collects anonymized data only
- Removes PII (names, emails, etc.)
- Hashes user IDs
- Aggregates metrics

### FULL
- Collects detailed analytics
- Feature usage tracking
- Performance metrics
- Error tracking with stack traces
- Still respects PII removal

```kotlin
// Set privacy mode
analyticsManager.setPrivacyMode(PrivacyMode.STRICT)
```

## Integration Examples

### TTS Service Integration

```kotlin
class DesktopTTSService(
    private val analyticsManager: AnalyticsManager
) {
    suspend fun synthesizeText(text: String): ByteArray {
        return analyticsManager.measureTimeSuspend(MetricType.SYNTHESIS_TIME) {
            // Actual synthesis code
            synthesizer.synthesize(text)
        }
    }
}
```

### Network Request Integration

```kotlin
class BookRepository(
    private val networkInterceptor: NetworkAnalyticsInterceptor
) {
    suspend fun fetchBook(id: String): Book {
        return networkInterceptor.trackRequest(
            url = "https://api.example.com/books/$id",
            method = "GET"
        ) {
            httpClient.get("/books/$id")
        }
    }
}
```

### Database Query Integration

```kotlin
class BookDao(
    private val dbTracker: DatabaseAnalyticsTracker
) {
    suspend fun getAllBooks(): List<Book> {
        return dbTracker.trackQuerySuspend(
            queryType = QueryType.SELECT,
            tableName = "books"
        ) {
            database.bookQueries.selectAll().executeAsList()
        }
    }
}
```

### UI Performance Integration

```kotlin
@Composable
fun BookListScreen(
    uiTracker: UIPerformanceTracker
) {
    LaunchedEffect(Unit) {
        uiTracker.onFrameStart()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            uiTracker.onFrameEnd("BookListScreen")
        }
    }
    
    // Your composable content
    LazyColumn {
        items(books) { book ->
            uiTracker.trackComposition("BookItem") {
                BookItem(book)
            }
        }
    }
}
```

## Best Practices

1. **Always wrap analytics calls in try-catch**: Analytics should never crash the app
2. **Use appropriate privacy mode**: Respect user privacy preferences
3. **Remove PII**: Never log sensitive user information
4. **Aggregate data**: Don't store individual user actions indefinitely
5. **Monitor performance**: Use analytics to identify bottlenecks
6. **Generate reports**: Regularly review performance reports
7. **Act on insights**: Use insights to improve app performance

## Performance Targets

- TTS synthesis: < 200ms average, < 500ms p95
- UI frame time: < 16.67ms for 60 FPS
- Network latency: < 1000ms average, < 3000ms p95
- Database queries: < 50ms average, < 100ms p95

## Error Handling

All analytics methods are designed to fail silently:

```kotlin
try {
    analyticsManager.trackFeature("feature_name")
} catch (e: Exception) {
    // Analytics failure is logged but never thrown
    // App continues normal operation
}
```

## Testing

```kotlin
@Test
fun testAnalytics() {
    val analytics = AnalyticsManager(PrivacyMode.FULL)
    
    // Track some metrics
    analytics.performanceMonitor.recordSynthesisTime(100)
    analytics.trackFeature("test_feature")
    
    // Verify statistics
    val stats = analytics.performanceMonitor.getStatistics(MetricType.SYNTHESIS_TIME)
    assertEquals(100.0, stats?.average)
}
```

## Cleanup

```kotlin
// Clear all analytics data
analyticsManager.clearAllData()

// Or clear specific components
analyticsManager.performanceMonitor.clear()
analyticsManager.usageAnalytics.clear()
analyticsManager.errorTracker.clear()
```
