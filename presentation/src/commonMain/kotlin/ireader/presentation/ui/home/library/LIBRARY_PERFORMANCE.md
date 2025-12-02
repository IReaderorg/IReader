# Library Screen Performance Optimizations

This document outlines the performance optimizations implemented for the Library screen to ensure smooth 60 FPS scrolling and instant navigation.

## Key Optimizations Implemented

### 1. Device-Aware Performance Configuration

The app now detects device capabilities and adjusts rendering accordingly:

- **High-end devices (>6GB RAM)**: Full animations, high-quality images, 256px thumbnails
- **Medium devices (3-6GB RAM)**: Reduced animations, 192px thumbnails
- **Low-end devices (<3GB RAM)**: Minimal animations, 128px thumbnails, RGB_565 format

Configuration is provided at the app level via `LocalPerformanceConfig` in `MainActivity.kt`.

### 2. Flow Caching and Sharing

The `LibraryViewModel` now caches category flows to avoid recreating them on every recomposition:

```kotlin
private val categoryFlowCache = mutableMapOf<Long, Flow<List<BookItem>>>()
```

Flows are shared using `shareIn(scope, SharingStarted.WhileSubscribed(5000), replay = 1)` to:
- Avoid multiple database subscriptions
- Keep data warm for 5 seconds after last subscriber
- Provide instant data on re-subscription

### 3. Instant Navigation with Cached Data

When navigating back to the library screen:
- Cached data is displayed immediately as the initial value
- Fresh data loads in the background without blocking UI
- No loading spinner shown for cached content

### 4. Image Loading Optimizations

- **Size constraints**: Images are limited based on device tier (128-256px)
- **Crossfade disabled on low-end**: Instant display instead of animation
- **Proper cache keys**: Using `BookCover.cover` and `lastModified` for cache hits
- **Fast scroll detection**: Defers image loading during rapid scrolling

### 5. List Rendering Optimizations

- **Stable keys**: Using `book.id` as stable key for efficient diffing
- **Content types**: Added `contentType = "book_item"` for better recycling
- **Layer promotion**: Using `graphicsLayer` for complex items
- **Scroll position preservation**: Saves/restores scroll position per category

### 6. State Management

- **Immutable state**: Using `ImmutableList` and `ImmutableSet` from kotlinx.collections
- **Batched updates**: State updates are batched to minimize recompositions
- **Derived state**: Using `derivedStateOf` for computed values

## Performance Metrics

Target metrics for the library screen:
- **Initial load**: < 500ms to first content
- **Navigation back**: < 100ms (instant with cache)
- **Scroll FPS**: 60 FPS sustained
- **Memory**: < 100MB for 1000 books

## Testing Performance

1. Enable performance logging:
```kotlin
DatabaseOptimizations.logPerformanceReport()
```

2. Monitor recomposition counts in Android Studio Layout Inspector

3. Use systrace/perfetto for frame timing analysis

## Future Improvements

1. **Pagination**: Load books in pages for very large libraries (>1000 books)
2. **Prefetching**: Preload adjacent category data
3. **Image preloading**: Preload visible + buffer images
4. **Database indices**: Add indices for common sort columns
