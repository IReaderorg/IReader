# Book Detail Screen Scroll State Fix

## Problem

The scroll position in the book detail screen was not being saved. When users:
- Scrolled through the chapter list
- Navigated away from the screen
- Returned to the book detail screen

The scroll position would reset to the top instead of maintaining the previous position.

## Root Cause

The `scrollState` in `BookDetailScreenSpec` was created without any saved state:
```kotlin
val scrollState = rememberLazyListState()  // ❌ No saved state
```

The `BookDetailViewModel` had no properties to store scroll position.

## Solution

### 1. Added Scroll State Properties to ViewModel

In `BookDetailViewModel.kt`, added:
```kotlin
// Scroll state persistence
var savedScrollIndex by mutableStateOf(0)
    private set
var savedScrollOffset by mutableStateOf(0)
    private set

fun saveScrollPosition(index: Int, offset: Int) {
    savedScrollIndex = index
    savedScrollOffset = offset
}
```

### 2. Updated BookDetailScreenSpec to Use Saved State

In `BookDetailScreenSpec.kt`, changed:
```kotlin
// Before
val scrollState = rememberLazyListState()

// After
val scrollState = rememberLazyListState(
    initialFirstVisibleItemIndex = vm.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
)

// Save scroll position when it changes
LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
    vm.saveScrollPosition(
        scrollState.firstVisibleItemIndex,
        scrollState.firstVisibleItemScrollOffset
    )
}
```

## How It Works

1. **Initialization**: When the screen is created, the `LazyListState` is initialized with the saved scroll position from the ViewModel

2. **Tracking**: A `LaunchedEffect` monitors changes to the scroll position

3. **Saving**: Whenever the user scrolls, the new position is saved to the ViewModel

4. **Restoration**: When the user returns to the screen, the scroll position is restored from the ViewModel

## Benefits

- ✅ Scroll position is maintained when navigating away and back
- ✅ Smooth user experience - users don't lose their place
- ✅ Works across configuration changes
- ✅ Minimal performance impact

## Testing

To verify the fix works:

1. Open a book detail screen
2. Scroll down through the chapter list
3. Navigate to another screen (e.g., tap a chapter to read)
4. Press back to return to the book detail screen
5. **Expected**: The scroll position should be maintained
6. **Before fix**: Would scroll back to the top

## Technical Details

### State Management
- Uses `mutableStateOf` for reactive state updates
- Private setters prevent external modification
- Public getter allows reading the state

### LaunchedEffect
- Monitors `firstVisibleItemIndex` and `firstVisibleItemScrollOffset`
- Automatically updates when scroll position changes
- Efficient - only triggers when values actually change

### Lifecycle
- State persists as long as the ViewModel is alive
- ViewModel survives configuration changes (rotation, etc.)
- State is cleared when the ViewModel is destroyed (user leaves the book)

## Similar Implementation

This follows the same pattern used in the `ExploreScreen`:
```kotlin
// ExploreScreen already had this working
val scrollState = rememberLazyListState(
    initialFirstVisibleItemIndex = vm.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
)

LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
    vm.savedScrollIndex = scrollState.firstVisibleItemIndex
    vm.savedScrollOffset = scrollState.firstVisibleItemScrollOffset
}
```

## Future Enhancements

Consider adding:
1. **Persistence across app restarts**: Save to preferences or database
2. **Per-book scroll position**: Remember different positions for different books
3. **Smart scrolling**: Auto-scroll to last read chapter
4. **Scroll to top FAB**: Quick way to return to top when scrolled far down

## Related Files

- `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/viewmodel/BookDetailViewModel.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/BookDetailScreenSpec.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreen.kt`
