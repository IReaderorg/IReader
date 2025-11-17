# More Screen Scroll State Fix

## Problem

The More screen scroll position was not being saved when navigating away and back. Users would always return to the top of the screen instead of their previous scroll position.

## Root Cause

The scroll state was using `rememberSaveable` which doesn't persist across navigation in this architecture. The state needs to be stored in the ViewModel to survive navigation events.

## Solution

### 1. Added Scroll State Properties to ViewModel

In `MainSettingScreenViewModel`:
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

### 2. Updated MoreScreen to Use ViewModel State

In `MoreScreen` composable:
```kotlin
// Initialize with saved position from ViewModel
val listState = rememberLazyListState(
    initialFirstVisibleItemIndex = vm.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
)

// Save scroll position when it changes
LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
    vm.saveScrollPosition(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    )
}
```

## How It Works

1. **Initialization**: When the More screen is created, the `LazyListState` is initialized with the saved scroll position from the ViewModel

2. **Tracking**: A `LaunchedEffect` monitors changes to the scroll position

3. **Saving**: Whenever the user scrolls, the new position is saved to the ViewModel

4. **Restoration**: When the user navigates back to the More screen, the scroll position is restored from the ViewModel

## Benefits

- ✅ Scroll position is maintained when navigating away and back
- ✅ Smooth user experience - users don't lose their place
- ✅ Works across configuration changes
- ✅ Minimal performance impact
- ✅ Consistent with other screens (Library, Explore, BookDetail)

## Testing

To verify the fix works:

1. Open the More screen
2. Scroll down to any section (e.g., "Information & Support")
3. Navigate to another screen (e.g., tap "Settings")
4. Press back to return to the More screen
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
- ViewModel survives navigation and configuration changes
- State is cleared when the app is closed or ViewModel is destroyed

## Consistency

This implementation follows the same pattern used in other screens:

### ExploreScreen
```kotlin
val scrollState = rememberLazyListState(
    initialFirstVisibleItemIndex = vm.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
)

LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
    vm.savedScrollIndex = scrollState.firstVisibleItemIndex
    vm.savedScrollOffset = scrollState.firstVisibleItemScrollOffset
}
```

### BookDetailScreen
```kotlin
val scrollState = rememberLazyListState(
    initialFirstVisibleItemIndex = vm.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
)

LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
    vm.saveScrollPosition(
        scrollState.firstVisibleItemIndex,
        scrollState.firstVisibleItemScrollOffset
    )
}
```

## Related Files

### Modified Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/MoreScreen.kt`
   - Added scroll state tracking in MoreScreen composable
   - Added scroll state properties to MainSettingScreenViewModel

### Unchanged Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/MoreScreenSpec.kt`
   - Screen spec (no changes needed)

## Future Enhancements

Consider adding:
1. **Persistence across app restarts**: Save to preferences or database
2. **Smart scrolling**: Auto-scroll to last interacted item
3. **Scroll to top FAB**: Quick way to return to top when scrolled far down
4. **Section anchors**: Quick navigation to specific sections

## Performance Considerations

- **Minimal overhead**: LaunchedEffect only triggers on actual scroll changes
- **No memory leaks**: State is properly managed by ViewModel lifecycle
- **Efficient updates**: Uses Compose's recomposition system
- **No unnecessary saves**: Only saves when scroll position actually changes

## Troubleshooting

### Issue: Scroll position still not saved
**Solution**: Ensure the ViewModel is properly scoped and not being recreated on navigation.

### Issue: Scroll jumps to wrong position
**Solution**: Check that `savedScrollIndex` and `savedScrollOffset` are being properly initialized.

### Issue: Performance issues
**Solution**: Verify that LaunchedEffect is not triggering too frequently. It should only trigger on scroll position changes.

## Summary

This fix ensures that users maintain their scroll position in the More screen when navigating away and back, providing a much better user experience. The implementation is consistent with other screens in the app and follows Compose best practices.
