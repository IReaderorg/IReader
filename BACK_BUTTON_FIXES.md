# Back Button Behavior Fixes

## Problems Fixed

### 1. Library Screen Modal Sheet
**Problem**: When the filter modal sheet was open in the library screen, pressing back would close the entire app instead of just closing the modal sheet.

**Solution**: Added `BackHandler` to intercept back press when the filter sheet is visible.

### 2. Double Back Press to Exit App
**Problem**: Users could accidentally exit the app with a single back press from the main screen.

**Solution**: Implemented double back press confirmation - users must press back twice within 2 seconds to exit, with a toast message on the first press.

### 3. Reader Screen Drawer
**Problem**: When the chapter drawer was open in the reader screen, pressing back would close the entire screen instead of just closing the drawer.

**Solution**: Added `BackHandler` to intercept back press when the drawer is open.

---

## Implementation Details

### 1. Library Screen Filter Sheet

**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/LibraryScreen.kt`

**Change**:
```kotlin
// Handle back button to close filter sheet instead of closing screen
androidx.activity.compose.BackHandler(enabled = showFilterSheet) {
    onHideFilterSheet()
}
```

**How it works**:
- `BackHandler` is enabled only when `showFilterSheet` is true
- When back is pressed, it calls `onHideFilterSheet()` to close the sheet
- If the sheet is closed, the BackHandler is disabled and back press propagates normally

### 2. Double Back Press to Exit

**File**: `android/src/main/java/org/ireader/app/ConfirmExit.kt` (new file)

**Implementation**:
```kotlin
@Composable
fun ConfirmExit() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }
    
    BackHandler(enabled = true) {
        if (backPressedOnce) {
            // Second press - exit the app
            (context as? MainActivity)?.finish()
        } else {
            // First press - show toast and set flag
            backPressedOnce = true
            Toast.makeText(
                context,
                "Press back again to exit",
                Toast.LENGTH_SHORT
            ).show()
            
            // Reset the flag after 2 seconds
            scope.launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }
}
```

**How it works**:
1. First back press: Sets `backPressedOnce` flag and shows toast
2. Second back press (within 2 seconds): Exits the app
3. After 2 seconds: Resets the flag, requiring two presses again

**Usage**: Already integrated in `MainActivity.kt`:
```kotlin
if (navController.previousBackStackEntry == null) {
    ConfirmExit()
}
```

### 3. Reader Screen Drawer

**File**: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ReaderScreenSpec.kt`

**Change**:
```kotlin
// Handle back button to close drawer instead of closing screen
androidx.activity.compose.BackHandler(enabled = drawerState.isOpen) {
    scope.launch {
        drawerState.close()
    }
}
```

**How it works**:
- `BackHandler` is enabled only when `drawerState.isOpen` is true
- When back is pressed, it closes the drawer with animation
- If the drawer is closed, the BackHandler is disabled and back press propagates normally

---

## User Experience Improvements

### Before Fixes
❌ Pressing back with filter sheet open → App closes  
❌ Single back press from main screen → App exits immediately  
❌ Pressing back with drawer open → Reader screen closes  

### After Fixes
✅ Pressing back with filter sheet open → Sheet closes  
✅ Single back press from main screen → Shows "Press back again to exit" toast  
✅ Double back press within 2 seconds → App exits  
✅ Pressing back with drawer open → Drawer closes  

---

## Technical Details

### BackHandler Priority
Android's `BackHandler` works with a priority system:
1. Most recently added handler (highest priority)
2. Next handler in the stack
3. Default system back behavior (lowest priority)

Our implementation:
- Filter sheet handler (when sheet is open)
- Drawer handler (when drawer is open)
- Double back press handler (on main screen)
- Default navigation back (all other screens)

### State Management
All handlers use `enabled` parameter to control when they're active:
```kotlin
BackHandler(enabled = condition) {
    // Handle back press
}
```

This ensures:
- No conflicts between handlers
- Clean enable/disable based on UI state
- Proper back press propagation

### Coroutine Usage
Drawer closing uses coroutine scope because `drawerState.close()` is a suspend function:
```kotlin
scope.launch {
    drawerState.close()
}
```

---

## Testing

### Test Cases

1. **Library Filter Sheet**
   - Open library screen
   - Tap filter button to open sheet
   - Press back → Sheet should close
   - Press back again → Should navigate away from library

2. **Double Back to Exit**
   - Navigate to main screen (library/explore/etc)
   - Press back → Toast appears
   - Wait 3 seconds
   - Press back → Toast appears again (flag reset)
   - Press back → Toast appears
   - Quickly press back again → App exits

3. **Reader Drawer**
   - Open a chapter in reader
   - Tap to show UI
   - Tap chapter list icon to open drawer
   - Press back → Drawer should close
   - Press back again → Should exit reader

### Edge Cases Handled
- Multiple rapid back presses
- Back press during animations
- Configuration changes (rotation)
- State restoration

---

## Related Files

### Modified Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/LibraryScreen.kt`
2. `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ReaderScreenSpec.kt`

### New Files
1. `android/src/main/java/org/ireader/app/ConfirmExit.kt`

### Referenced Files
1. `android/src/main/java/org/ireader/app/MainActivity.kt` (already had ConfirmExit() call)

---

## Future Enhancements

Consider adding:
1. **Customizable exit delay**: Let users configure the 2-second timeout
2. **Haptic feedback**: Vibrate on first back press
3. **Custom toast message**: Allow localization
4. **Analytics**: Track accidental exit attempts
5. **Gesture navigation**: Handle gesture back differently from button back

---

## Similar Patterns

This same pattern can be applied to other modal UI elements:
- Bottom sheets
- Dialogs
- Popup menus
- Search bars
- Any temporary overlay

Example:
```kotlin
BackHandler(enabled = isModalVisible) {
    hideModal()
}
```

---

## Android Best Practices

This implementation follows Android's predictive back gesture guidelines:
- ✅ Clear visual feedback (toast message)
- ✅ Reversible action (2-second timeout)
- ✅ Consistent behavior across the app
- ✅ No accidental exits
- ✅ Proper state management

---

## Troubleshooting

### Issue: Back button still closes app with sheet open
**Solution**: Ensure `showFilterSheet` state is properly managed and BackHandler is placed before the sheet composable.

### Issue: Double back doesn't work
**Solution**: Check that `ConfirmExit()` is only called when `navController.previousBackStackEntry == null`.

### Issue: Drawer doesn't close on back press
**Solution**: Verify `drawerState.isOpen` returns true when drawer is visible.

### Issue: Toast doesn't show
**Solution**: Ensure context is properly obtained and MainActivity is accessible.
