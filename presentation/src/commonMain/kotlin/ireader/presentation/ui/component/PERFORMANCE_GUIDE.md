# Performance Optimization Guide

This guide provides instructions for profiling and optimizing Compose UI performance in the IReader application.

## Table of Contents
1. [Profiling Tools](#profiling-tools)
2. [Performance Metrics](#performance-metrics)
3. [Optimization Techniques](#optimization-techniques)
4. [Common Performance Issues](#common-performance-issues)
5. [Testing Performance](#testing-performance)

## Profiling Tools

### 1. Compose Layout Inspector
The Layout Inspector in Android Studio provides real-time insights into your Compose UI:

**How to use:**
1. Run your app in debug mode
2. Open Tools → Layout Inspector
3. Select your app process
4. Enable "Show Recomposition Counts" in the Layout Inspector toolbar

**What to look for:**
- High recomposition counts (red/orange highlights)
- Unnecessary recompositions in list items
- Composables that recompose on every scroll

### 2. Compose Compiler Metrics
Enable Compose compiler metrics to analyze composition performance:

**Add to build.gradle.kts:**
```kotlin
android {
    kotlinOptions {
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.buildDir}/compose_metrics",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir}/compose_reports"
        )
    }
}
```

**Generated reports:**
- `*-composables.txt`: Lists all composables and their stability
- `*-classes.txt`: Shows class stability information
- `*-module.json`: Module-level metrics

### 3. Android Profiler
Use the Android Profiler to monitor CPU, memory, and frame rendering:

**How to use:**
1. Open View → Tool Windows → Profiler
2. Select your app process
3. Monitor CPU usage during scrolling
4. Check frame rendering times (should be < 16ms for 60 FPS)

## Performance Metrics

### Target Metrics
- **Frame Rate**: 60 FPS (16.67ms per frame)
- **Recomposition Count**: < 5 per scroll event for list items
- **Memory Usage**: Stable during scrolling (no memory leaks)
- **CPU Usage**: < 30% during normal scrolling

### Measuring Scroll Performance

```kotlin
// Add to your list composable for debugging
LaunchedEffect(scrollState.isScrollInProgress) {
    if (scrollState.isScrollInProgress) {
        val startTime = System.currentTimeMillis()
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .collect {
                val frameTime = System.currentTimeMillis() - startTime
                if (frameTime > 16) {
                    Log.d("Performance", "Slow frame: ${frameTime}ms")
                }
            }
    }
}
```

## Optimization Techniques

### 1. List Rendering Optimizations

#### Use Proper Keys
```kotlin
// ✅ Good - Stable, unique key
items(
    items = books,
    key = { book -> book.id }
) { book ->
    BookItem(book)
}

// ❌ Bad - Index as key
items(
    items = books,
    key = { index -> index }
) { book ->
    BookItem(book)
}
```

#### Add ContentType
```kotlin
// ✅ Good - Helps Compose recycle views
items(
    items = books,
    key = { it.id },
    contentType = { "book_item" }
) { book ->
    BookItem(book)
}
```

#### Use Remember for Computed Values
```kotlin
// ✅ Good - Cached computation
@Composable
fun BookItem(book: Book) {
    val progress = remember(book.progress, book.unread, book.totalChapters) {
        calculateProgress(book)
    }
    // ...
}

// ❌ Bad - Recomputed on every recomposition
@Composable
fun BookItem(book: Book) {
    val progress = calculateProgress(book) // Recalculated every time
    // ...
}
```

#### Use DerivedStateOf for Derived State
```kotlin
// ✅ Good - Only recomputes when dependencies change
val shouldShowLoadingSpace by remember {
    derivedStateOf { isLoading && books.isNotEmpty() }
}

// ❌ Bad - Creates new value on every recomposition
val shouldShowLoadingSpace = isLoading && books.isNotEmpty()
```

### 2. Image Loading Optimizations

#### Use Appropriate Image Sizes
```kotlin
// ✅ Good - Loads image at display size
val imageRequest = remember(book.id) {
    ImageRequest.Builder(context)
        .data(book.cover)
        .size(Size(240, 320)) // Actual display size
        .build()
}

// ❌ Bad - Loads full resolution
val imageRequest = ImageRequest.Builder(context)
    .data(book.cover)
    .build()
```

#### Enable Hardware Bitmaps
```kotlin
ImageRequest.Builder(context)
    .data(book.cover)
    .allowHardware(true) // More memory efficient
    .build()
```

#### Use Proper Cache Keys
```kotlin
ImageRequest.Builder(context)
    .data(bookCover)
    .memoryCacheKey(bookCover.cover)
    .diskCacheKey("${bookCover.cover};${bookCover.lastModified}")
    .build()
```

### 3. Minimize Recomposition Scope

#### Keep Composables Small
```kotlin
// ✅ Good - Small, focused composables
@Composable
fun BookItem(book: Book) {
    Column {
        BookCover(book)
        BookTitle(book.title)
        BookMetadata(book)
    }
}

// ❌ Bad - Large, monolithic composable
@Composable
fun BookItem(book: Book) {
    Column {
        // 100+ lines of UI code
    }
}
```

#### Use Stable Data Classes
```kotlin
// ✅ Good - Stable data class
@Stable
data class BookDisplayData(
    val id: Long,
    val title: String,
    val coverUrl: String?
)

// ❌ Bad - Unstable class
class BookDisplayData(
    var id: Long,
    var title: String,
    var coverUrl: String?
)
```

### 4. Limit Animations

```kotlin
// ✅ Good - Limited animation delay
val animationDelay = remember(index) { 
    if (index < 20) index * 20L else 0L 
}

// ❌ Bad - Animates all items
val animationDelay = index * 20L
```

## Common Performance Issues

### Issue 1: High Recomposition Count
**Symptoms:** List items recompose frequently during scrolling

**Solutions:**
- Use `remember` for computed values
- Use `derivedStateOf` for derived state
- Ensure data classes are stable
- Check for unnecessary state reads

### Issue 2: Slow Image Loading
**Symptoms:** Images load slowly or cause scroll jank

**Solutions:**
- Use appropriate image sizes
- Enable hardware bitmaps
- Implement proper caching
- Use placeholder images
- Consider lazy loading

### Issue 3: Dropped Frames
**Symptoms:** Scroll feels janky, frame rate drops below 60 FPS

**Solutions:**
- Simplify list item layouts
- Reduce number of composables per item
- Use `graphicsLayer` for complex items
- Limit animations
- Profile with Layout Inspector

### Issue 4: Memory Leaks
**Symptoms:** Memory usage increases during scrolling

**Solutions:**
- Check for leaked coroutines
- Ensure proper cleanup in `DisposableEffect`
- Monitor with Memory Profiler
- Use weak references where appropriate

## Testing Performance

### Manual Testing Checklist
- [ ] Scroll through list of 100+ items smoothly
- [ ] No visible frame drops during fast scrolling
- [ ] Images load without blocking scroll
- [ ] Memory usage remains stable
- [ ] CPU usage is reasonable (< 30%)
- [ ] No ANR (Application Not Responding) errors

### Automated Performance Tests
```kotlin
@Test
fun testScrollPerformance() {
    composeTestRule.setContent {
        BookList(books = generateTestBooks(1000))
    }
    
    // Measure scroll performance
    val startTime = System.currentTimeMillis()
    composeTestRule.onNodeWithTag("book_list")
        .performScrollToIndex(500)
    val scrollTime = System.currentTimeMillis() - startTime
    
    // Assert reasonable scroll time
    assertTrue(scrollTime < 1000) // Should scroll in < 1 second
}
```

### Performance Benchmarks
Use Jetpack Macrobenchmark for detailed performance testing:

```kotlin
@RunWith(AndroidJUnit4::class)
class ScrollBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()
    
    @Test
    fun scrollList() = benchmarkRule.measureRepeated(
        packageName = "com.example.ireader",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        setupBlock = {
            // Setup
        }
    ) {
        // Scroll through list
    }
}
```

## Best Practices Summary

1. **Always use proper keys** in `items()` - Use stable, unique identifiers
2. **Add contentType** to help Compose recycle views efficiently
3. **Use remember** for computed values to avoid recalculation
4. **Use derivedStateOf** for derived state that depends on other state
5. **Optimize images** - Use appropriate sizes and enable hardware bitmaps
6. **Keep composables small** - Break down large composables into smaller ones
7. **Limit animations** - Only animate visible items, limit stagger delays
8. **Profile regularly** - Use Layout Inspector and Profiler to identify issues
9. **Test on real devices** - Emulators don't reflect real performance
10. **Monitor metrics** - Track frame rate, recomposition count, and memory usage

## Additional Resources

- [Compose Performance Documentation](https://developer.android.com/jetpack/compose/performance)
- [Compose Stability](https://developer.android.com/jetpack/compose/performance/stability)
- [Compose Phases](https://developer.android.com/jetpack/compose/phases)
- [Compose Recomposition](https://developer.android.com/jetpack/compose/mental-model#recomposition)
