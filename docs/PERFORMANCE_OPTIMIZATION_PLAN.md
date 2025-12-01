# Performance Optimization Plan

This document outlines the performance optimization strategy for IReader, based on comprehensive analysis of Mihon's architecture and best practices.

---

## Table of Contents
1. [Current State Analysis](#current-state-analysis)
2. [Mihon's Optimization Strategies](#mihons-optimization-strategies)
3. [Implementation Roadmap](#implementation-roadmap)
4. [Baseline Profile Strategy](#baseline-profile-strategy)
5. [Benchmarking Guide](#benchmarking-guide)
6. [Code Patterns Reference](#code-patterns-reference)

---

## Current State Analysis

### Identified Issues
| Issue | Impact | Root Cause |
|-------|--------|------------|
| Navigation freezing (1-2s) | High | Heavy ViewModel init, synchronous data loading |
| Tab switching delays | Medium | No state preservation, full recomposition |
| Excessive recomposition | Medium | Unstable lambdas, missing @Stable annotations |
| Memory pressure | Low | Mutable collections, no caching strategy |

### Completed Optimizations ✅
- [x] Navigator `popUpTo` for back stack management
- [x] `launchSingleTop` for all navigation calls
- [x] Pre-built `NavOptions` to reduce lambda allocations
- [x] `isCurrentRoute()` checks to prevent redundant navigation
- [x] Simple loading screen pattern (Mihon-style) for BookDetail
- [x] `derivedStateOf` for computed values in lists

---

## Mihon's Optimization Strategies

### 1. Sealed State Pattern (Critical)

Mihon uses a sealed class pattern for ALL screen states:

```kotlin
sealed interface State {
    @Immutable
    data object Loading : State
    
    @Immutable
    data class Success(
        val manga: Manga,
        val chapters: ImmutableList<Chapter>,
        // ... other fields
    ) : State
}
```

**Key Benefits:**
- Clear loading/success/error states
- Compose can skip recomposition when state type unchanged
- `@Immutable` annotation enables compiler optimizations

**Files Using This Pattern in Mihon:**
- `MangaScreenModel.kt` - Book detail
- `LibraryScreenModel.kt` - Library
- `HistoryScreenModel.kt` - History
- `UpdatesScreenModel.kt` - Updates
- `BrowseSourceScreenModel.kt` - Browse
- `CategoryScreenModel.kt` - Categories
- ALL screen models

### 2. Immutable Collections (Critical)

Mihon uses `kotlinx.collections.immutable` throughout:

```kotlin
// Instead of
data class State(val items: List<Item>)

// Mihon uses
data class State(val items: ImmutableList<Item>)

// Conversion
items.toImmutableList()
```

**Why It Matters:**
- `List<T>` is not stable - Compose assumes it may change
- `ImmutableList<T>` is stable - Compose can skip recomposition
- Significant performance improvement for large lists

**Dependency:**
```kotlin
implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")
```

### 3. StateScreenModel (Voyager Pattern)

Mihon extends `StateScreenModel` for all ViewModels:

```kotlin
class MangaScreenModel : StateScreenModel<State>(State.Loading) {
    
    private fun updateSuccessState(func: (State.Success) -> State.Success) {
        mutableState.update {
            when (it) {
                State.Loading -> it
                is State.Success -> func(it)
            }
        }
    }
    
    init {
        screenModelScope.launchIO {
            getMangaAndChapters.subscribe(mangaId)
                .collectLatest { (manga, chapters) ->
                    updateSuccessState {
                        it.copy(manga = manga, chapters = chapters.toImmutableList())
                    }
                }
        }
    }
}
```

### 4. Flow-Based Data Loading

All data loading uses Kotlin Flow with lifecycle awareness:

```kotlin
// In ViewModel
getMangaAndChapters.subscribe(mangaId)
    .flowWithLifecycle(lifecycle)  // Lifecycle-aware
    .distinctUntilChanged()         // Prevent duplicate emissions
    .collectLatest { ... }          // Cancel previous collection

// In Composable
val state by screenModel.state.collectAsStateWithLifecycle()
```

### 5. Fast Collection Operations

Mihon uses Compose's optimized collection functions:

```kotlin
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap

// Instead of
items.any { it.selected }
items.filter { it.read }

// Use
items.fastAny { it.selected }
items.fastFilter { it.read }
```

**Custom Extensions:**
```kotlin
// From Mihon's CollectionUtils.kt
inline fun <T> List<T>.fastFilterNot(predicate: (T) -> Boolean): List<T>
inline fun <T> List<T>.fastPartition(predicate: (T) -> Boolean): Pair<List<T>, List<T>>
inline fun <T> List<T>.fastCountNot(predicate: (T) -> Boolean): Int
```

### 6. Preference State Management

Efficient preference observation:

```kotlin
// PreferenceMutableState.kt
class PreferenceMutableState<T>(
    private val preference: Preference<T>,
    scope: CoroutineScope,
) : MutableState<T> {
    private val state = mutableStateOf(preference.get())
    
    init {
        preference.changes()
            .onEach { state.value = it }
            .launchIn(scope)
    }
    
    override var value: T
        get() = state.value
        set(value) { preference.set(value) }
}

// Usage
val skipFiltered by readerPreferences.skipFiltered().asState(screenModelScope)
```

### 7. LazyList Optimizations

```kotlin
// Always provide keys and content types
items(
    items = chapters,
    key = { it.id },
    contentType = { "chapter" }
) { chapter ->
    ChapterRow(chapter)
}

// FAB expansion based on scroll state
@Composable
fun LazyListState.shouldExpandFAB(): Boolean {
    return remember {
        derivedStateOf {
            (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) ||
                lastScrolledBackward ||
                !canScrollForward
        }
    }.value
}
```

### 8. Modifier Optimizations

```kotlin
// Selected background with efficient color calculation
@Composable
fun Modifier.selectedBackground(isSelected: Boolean): Modifier {
    if (!isSelected) return this
    val alpha = if (isSystemInDarkTheme()) 0.16f else 0.22f
    val color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
    return this.drawBehind { drawRect(color) }
}

// Secondary item alpha
fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SECONDARY_ALPHA)

// Clickable without ripple indication
fun Modifier.clickableNoIndication(
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = this.combinedClickable(
    interactionSource = null,
    indication = null,
    onLongClick = onLongClick,
    onClick = onClick,
)
```

### 9. Simple Loading Screen

Mihon uses a minimal loading screen - NO shimmer animations:

```kotlin
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
```

**Why No Shimmer:**
- Shimmer animations consume GPU resources
- Simple spinner is sufficient for short loads
- Reduces complexity and potential jank

---

## Implementation Roadmap

### Phase 1: Core Architecture (Week 1-2) 🔴 High Priority

#### 1.1 Add Immutable Collections Dependency
```kotlin
// libs.versions.toml
[versions]
kotlinx-immutable = "0.3.7"

[libraries]
kotlinx-collections-immutable = { module = "org.jetbrains.kotlinx:kotlinx-collections-immutable", version.ref = "kotlinx-immutable" }
```

#### 1.2 Create Base State Classes
```kotlin
// presentation/src/commonMain/kotlin/ireader/presentation/core/state/ScreenState.kt
sealed interface ScreenState<out T> {
    @Immutable
    data object Loading : ScreenState<Nothing>
    
    @Immutable
    data class Success<T>(val data: T) : ScreenState<T>
    
    @Immutable
    data class Error(val message: String) : ScreenState<Nothing>
}
```

#### 1.3 Migrate BookDetailViewModel
```kotlin
class BookDetailViewModel : BaseViewModel() {
    
    sealed interface State {
        @Immutable
        data object Loading : State
        
        @Immutable
        data class Success(
            val book: Book,
            val chapters: ImmutableList<Chapter>,
            val source: Source?,
            val isRefreshing: Boolean = false,
        ) : State
    }
    
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()
    
    init {
        scope.launch {
            combine(
                getBookUseCases.subscribeBookById(bookId),
                getChapterUseCase.subscribeChaptersByBookId(bookId),
            ) { book, chapters ->
                State.Success(
                    book = book,
                    chapters = chapters.toImmutableList(),
                    source = getLocalCatalog.get(book.sourceId)?.source,
                )
            }
            .catch { /* handle error */ }
            .collect { _state.value = it }
        }
    }
}
```

### Phase 2: UI Optimizations (Week 2-3) 🟡 Medium Priority

#### 2.1 Add Fast Collection Extensions
```kotlin
// core/src/commonMain/kotlin/ireader/core/util/CollectionUtils.kt
inline fun <T> List<T>.fastFilterNot(predicate: (T) -> Boolean): List<T> {
    return fastFilter { !predicate(it) }
}

inline fun <T> List<T>.fastPartition(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    val first = ArrayList<T>()
    val second = ArrayList<T>()
    fastForEach {
        if (predicate(it)) first.add(it) else second.add(it)
    }
    return Pair(first, second)
}
```

#### 2.2 Add Modifier Extensions
```kotlin
// presentation-core/src/commonMain/kotlin/ireader/presentation/core/util/Modifier.kt
@Composable
fun Modifier.selectedBackground(isSelected: Boolean): Modifier {
    if (!isSelected) return this
    val alpha = if (isSystemInDarkTheme()) 0.16f else 0.22f
    val color = MaterialTheme.colorScheme.secondary.copy(alpha = alpha)
    return this.drawBehind { drawRect(color) }
}
```

#### 2.3 Update All LazyLists
- Add `key` parameter to all `items()` calls
- Add `contentType` parameter
- Use `animateItem()` modifier for animations

### Phase 3: ViewModel Migration (Week 3-4) 🟡 Medium Priority

Migrate remaining ViewModels to sealed state pattern:
1. `LibraryViewModel`
2. `ReaderViewModel`
3. `ExploreViewModel`
4. `HistoryViewModel`
5. `UpdatesViewModel`
6. `SourcesViewModel`

### Phase 4: Baseline Profile (Week 4) 🟢 Low Priority

See [Baseline Profile Strategy](#baseline-profile-strategy) section.

---

## Baseline Profile Strategy

### What is a Baseline Profile?

A baseline profile is a list of classes and methods that should be pre-compiled (AOT) when the app is installed. This significantly improves:
- **Cold start time** - Up to 30% faster
- **Navigation transitions** - Smoother first-time renders
- **Scroll performance** - Less jank on first scroll

### Mihon's Baseline Profile Generator

```kotlin
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "eu.kanade.tachiyomi.benchmark",
        profileBlock = {
            pressHome()
            startActivityAndWait()
            
            // Navigate through critical paths
            device.findObject(By.text("Updates")).click()
            device.findObject(By.text("History")).click()
            device.findObject(By.text("More")).click()
            device.findObject(By.text("Settings")).click()
        },
    )
}
```

### Our Implementation Plan

```kotlin
// benchmark/src/main/kotlin/org/ireader/benchmark/BaselineProfileGenerator.kt
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "org.ireader.benchmark",
        profileBlock = {
            pressHome()
            startActivityAndWait()
            
            // Critical user paths
            // 1. Tab navigation
            device.findObject(By.text("Updates")).click()
            device.findObject(By.text("History")).click()
            device.findObject(By.text("Browse")).click()
            device.findObject(By.text("More")).click()
            
            // 2. Library interaction
            device.findObject(By.text("Library")).click()
            // Scroll library
            device.swipe(500, 1500, 500, 500, 10)
            
            // 3. Book detail (if possible)
            // device.findObject(By.descContains("book")).click()
            
            // 4. Settings
            device.findObject(By.text("More")).click()
            device.findObject(By.text("Settings")).click()
        },
    )
}
```

### Startup Benchmarks

```kotlin
// Measure different startup modes
class ColdStartupBenchmark : AbstractStartupBenchmark(StartupMode.COLD)
class WarmStartupBenchmark : AbstractStartupBenchmark(StartupMode.WARM)
class HotStartupBenchmark : AbstractStartupBenchmark(StartupMode.HOT)

abstract class AbstractStartupBenchmark(private val startupMode: StartupMode) {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    @Test
    fun startupBaselineProfile() = startup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    @Test
    fun startupFullCompilation() = startup(CompilationMode.Full())

    private fun startup(compilationMode: CompilationMode) = benchmarkRule.measureRepeated(
        packageName = "org.ireader.benchmark",
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        iterations = 10,
        startupMode = startupMode,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
```

---

## Benchmarking Guide

### Running Benchmarks

```bash
# Generate baseline profile
./gradlew :benchmark:pixel6Api31BenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=org.ireader.benchmark.BaselineProfileGenerator

# Run startup benchmarks
./gradlew :benchmark:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=org.ireader.benchmark.StartupBenchmark

# Run navigation benchmarks
./gradlew :benchmark:connectedBenchmarkAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=org.ireader.benchmark.NavigationBenchmark
```

### Metrics to Track

| Metric | Target | Current |
|--------|--------|---------|
| Cold start | < 1000ms | TBD |
| Warm start | < 500ms | TBD |
| Hot start | < 200ms | TBD |
| Book detail navigation | < 100ms | ~2000ms |
| Tab switch | < 50ms | TBD |
| Library scroll FPS | > 55 | TBD |

### Compose Compiler Metrics

Enable in `build.gradle.kts`:
```kotlin
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_metrics")
    metricsDestination = layout.buildDirectory.dir("compose_metrics")
}
```

Check for:
- Unstable classes (should be 0 for data classes)
- Skippable composables (should be high %)
- Restartable composables

---

## Code Patterns Reference

### Pattern 1: Screen with Sealed State

```kotlin
// ViewModel
class MyScreenModel : BaseViewModel() {
    sealed interface State {
        @Immutable data object Loading : State
        @Immutable data class Success(val data: ImmutableList<Item>) : State
        @Immutable data class Error(val message: String) : State
    }
    
    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()
}

// Screen
@Composable
fun MyScreen(viewModel: MyScreenModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    when (val s = state) {
        State.Loading -> LoadingScreen()
        is State.Success -> SuccessContent(s.data)
        is State.Error -> ErrorScreen(s.message)
    }
}
```

### Pattern 2: Efficient List Item

```kotlin
@Composable
fun ChapterListItem(
    chapter: Chapter,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .selectedBackground(isSelected)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Content
    }
}

// Usage in LazyColumn
items(
    items = chapters,
    key = { it.id },
    contentType = { "chapter" },
) { chapter ->
    val onClick = remember(chapter.id) { { onChapterClick(chapter) } }
    val onLongClick = remember(chapter.id) { { onChapterLongClick(chapter) } }
    
    ChapterListItem(
        chapter = chapter,
        isSelected = chapter.id in selectedIds,
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = Modifier.animateItem(),
    )
}
```

### Pattern 3: Preference State

```kotlin
// In ViewModel
val showChapterNumber by readerPreferences.showChapterNumber()
    .asState(scope)

// Or in Composable
val showChapterNumber by readerPreferences.showChapterNumber()
    .collectAsState()
```

---

## Priority Summary

| Priority | Task | Impact | Effort | Status |
|----------|------|--------|--------|--------|
| 🔴 P0 | Sealed state pattern | Very High | Medium | ✅ Done |
| 🔴 P0 | Immutable collections | Very High | Low | ✅ Done |
| 🔴 P1 | BookDetailViewModel migration | High | Medium | ✅ Done |
| 🟡 P2 | Fast collection extensions | Medium | Low | ✅ Done |
| 🟡 P2 | Modifier extensions | Medium | Low | ✅ Done |
| 🟡 P2 | LazyList keys/contentType | Medium | Low | Partial |
| 🟢 P3 | Baseline profile | Medium | Medium | Not Started |
| 🟢 P3 | Other ViewModel migrations | Medium | High | Not Started |

---

## Next Steps

1. **Immediate (This Week)**
   - Add `kotlinx-collections-immutable` dependency
   - Create base `ScreenState` sealed class
   - Migrate `BookDetailViewModel` to sealed state

2. **Short Term (Next 2 Weeks)**
   - Add fast collection extensions
   - Add modifier extensions
   - Update all LazyLists with keys

3. **Medium Term (Next Month)**
   - Migrate remaining ViewModels
   - Implement baseline profile
   - Run comprehensive benchmarks

4. **Ongoing**
   - Monitor Compose compiler metrics
   - Profile with Android Studio
   - Iterate based on benchmark results
