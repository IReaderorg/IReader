# Download Unread Enhancement - COMPLETE FIX

## Overview
Fixed and enhanced the "Download unread" feature with TWO major improvements:
1. Downloads chapters starting from the user's most recently read chapter (not first unread)
2. Added new "Download Chapter Range" dialog to download specific chapter ranges

## Problem (From User Feedback)

### Issue 1: Wrong Starting Point
Previously, when a user skipped earlier chapters and began reading from a later chapter (e.g., starting from chapter 100), selecting "Download unread" would download chapters starting from chapter 1, leading to:
- Unnecessary downloads
- Wasted bandwidth and storage
- Delays in getting the chapters the user actually wants

### Issue 2: No Range Download Option
Users had no way to download chapters between specific numbers (e.g., chapters 1-10).

## Solution Implemented

### Feature 1: Download from Most Recently Read Chapter

Modified `BookDetailViewModel.downloadUnreadChapters()` to:
1. Query the `History` table to find the most recently read chapter for the book
2. Download only chapters AFTER the last read chapter that don't have content
3. Fall back to downloading all unread chapters if no reading history exists

**Key Changes:**
- `BookDetailViewModel.kt` lines 1375-1427: Complete rewrite of `downloadUnreadChapters()` method
- Now uses `historyUseCase.findHistoriesByBookId()` to find reading history
- Finds the most recently read chapter using `maxByOrNull { it.readAt ?: 0L }`
- Downloads only chapters after that index

### Feature 2: Download Chapter Range Dialog

Added a new dialog that allows users to:
1. Enter a start chapter number
2. Enter an end chapter number
3. Download all chapters in that range (inclusive)

**New Files:**
- `ChapterRangeDownloadDialog.kt`: New dialog component with number input fields

**Modified Files:**
1. **BookDetailViewModel.kt**:
   - Added state variables: `showChapterRangeDownloadDialog`, `chapterRangeStart`, `chapterRangeEnd`
   - Added functions:
     - `showChapterRangeDownloadDialog()`: Shows the dialog
     - `hideChapterRangeDownloadDialog()`: Hides the dialog
     - `updateChapterRangeStart(value: String)`: Updates start value
     - `updateChapterRangeEnd(value: String)`: Updates end value
     - `downloadChapterRange()`: Downloads chapters in specified range with validation

2. **ChapterBar.kt**:
   - Added "Download Chapter Range" menu item to the download dropdown

3. **BookDetailScreen.kt**:
   - Added `ChapterRangeDownloadDialog` component to show the dialog
   - Added import for the new dialog

## Implementation Details

### Modified Files

#### 1. `BookDetailViewModel.kt`
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/viewmodel/BookDetailViewModel.kt`

**Lines 172-185:** Added dialog state variables
```kotlin
var showChapterRangeDownloadDialog by mutableStateOf(false)
var chapterRangeStart by mutableStateOf("")
    private set
var chapterRangeEnd by mutableStateOf("")
    private set
```

**Lines 1375-1427:** Rewrote `downloadUnreadChapters()` method
- Queries reading history via `historyUseCase.findHistoriesByBookId(bookId)`
- Finds most recently read chapter using `maxByOrNull { it.readAt ?: 0L }`
- Downloads only chapters after last read position
- Falls back to all unread if no history

**Lines 1455-1552:** Added chapter range download functions
- Input validation (must be positive numbers, start ≤ end, within bounds)
- Converts 1-based UI indices to 0-based internal indices
- Downloads chapters using `downloadService.queueChapters()`

#### 2. `ChapterRangeDownloadDialog.kt` (NEW FILE)
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/components/ChapterRangeDownloadDialog.kt`

**Features:**
- AlertDialog with two OutlinedTextField inputs
- Number keyboard type for easy input
- Validation happens in ViewModel
- Shows total chapter count to user
- Localized strings for buttons

#### 3. `ChapterBar.kt`
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/components/ChapterBar.kt`

**Lines 136-143:** Added menu item
```kotlin
DropdownMenuItem(
    text = { Text("Download Chapter Range") },
    onClick = {
        showDownloadMenu = false
        vm.showChapterRangeDownloadDialog()
    }
)
```

#### 4. `BookDetailScreen.kt`
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/book/BookDetailScreen.kt`

**Line 53:** Added import
```kotlin
import ireader.presentation.ui.book.components.ChapterRangeDownloadDialog
```

**Lines 240-245:** Added dialog component
```kotlin
ChapterRangeDownloadDialog(
    vm = vm,
    totalChapters = chapters.value.size
)
```

## Behavior Examples

### Example 1: Download from Last Read (NEW)
1. User has read chapters 1-100 (most recent read was chapter 100 at 2:30 PM)
2. User selects "Download unread" from bulk download menu
3. System finds chapter 100 as most recently read (via History.readAt timestamp)
4. System downloads chapters 101+ (that don't have content)
5. Chapters 1-100 are skipped entirely ✅

### Example 2: Download from Last Read with Skipped Chapters (NEW)
1. User reads chapter 1, then skips to chapter 100 (most recent read is chapter 100)
2. User selects "Download unread"
3. System downloads from chapter 101+ (not from chapter 2)
4. This is correct because user wants chapters after their current position ✅

### Example 3: No History Fallback
1. User has never read any chapters from a book
2. User selects "Download unread"
3. System downloads all chapters without content (legacy behavior)
4. Ensures new users aren't affected ✅

### Example 4: Download Chapter Range (NEW FEATURE)
1. User selects "Download Chapter Range" from download menu
2. Dialog appears asking for start and end chapter numbers
3. User enters "1" → "10"
4. System downloads chapters 1-10 (inclusive)
5. Dialog closes and shows success message ✅

### Example 5: Range Validation (NEW)
1. User enters start: "10", end: "5"
2. System shows error: "Start chapter must be less than or equal to end chapter"
3. User enters start: "1", end: "999" (book only has 200 chapters)
4. System shows error: "Chapter range exceeds available chapters (200)"
5. User enters valid range and download succeeds ✅

## Benefits

✅ **Feature 1 Benefits:**
- Reduces unnecessary downloads dramatically
- Saves bandwidth and storage
- Improves UX for readers who skip chapters or start from later points
- Backward compatible (falls back to legacy behavior when no history exists)
- Uses actual reading history (not just "read" flag)

✅ **Feature 2 Benefits:**
- Gives users precise control over downloads
- Useful for downloading specific arcs or volumes
- Helpful when re-downloading corrupted chapters
- Simple and intuitive UI

## Testing Recommendations

### Test Feature 1: Download from Last Read
1. **Test with reading history:**
   - Read chapters 1-50 of a book
   - Select "Download unread" from Chapter Bar dropdown
   - Verify only chapters 51+ are queued for download

2. **Test without reading history:**
   - Add a new book to library
   - Select "Download unread"
   - Verify all unread chapters are downloaded (legacy behavior)

3. **Test with skipped chapters:**
   - Read chapter 100 (skipping 1-99)
   - Select "Download unread"
   - Verify only chapters 101+ are downloaded (uses readAt timestamp)

4. **Test with multiple books:**
   - Select multiple books in library with different reading progress
   - Select "Download unread" (library view)
   - Verify each book downloads from its own last read chapter

### Test Feature 2: Chapter Range Download
1. **Test valid range:**
   - Open download menu → "Download Chapter Range"
   - Enter start: 1, end: 10
   - Verify chapters 1-10 are downloaded
   - Message shows "Downloading chapters 1 to 10 (10 chapters)"

2. **Test validation:**
   - Test start > end: Should show error
   - Test negative numbers: Should show error
   - Test exceeding total: Should show error
   - Test non-numbers: Should show error

3. **Test edge cases:**
   - Single chapter: start: 5, end: 5 (should download just chapter 5)
   - Full range: start: 1, end: (total chapters)
   - Last chapter only: start: total, end: total

## Notes
- **Feature 1** uses the `History` table's `readAt` timestamp to determine the most recently read chapter (most accurate method)
- **Feature 1** only downloads chapters without content (empty or < 10 characters)
- **Feature 1** works in both BookDetailView (single book) and LibraryView (multiple books)
- **Feature 2** uses 1-based numbering in UI (chapter 1 = first chapter) but converts to 0-based internally
- **Feature 2** downloads ALL chapters in range, regardless of content (unlike Feature 1)
- Both features maintain memory efficiency by processing downloads in batches
- Error handling ensures failed operations don't block other downloads

## Database Schema Reference

### History Table
```kotlin
data class History(
    val id: Long,
    val chapterId: Long,  // Links to Chapter.id
    val readAt: Long?,    // Timestamp of when chapter was read (KEY FIELD)
    val readDuration: Long,
    val progress: Float = 0f,
)
```

## Fixed Issues from User Feedback

✅ **Issue 1 - FIXED**: Download unread now downloads from **most recently read chapter** instead of first unread  
✅ **Issue 2 - FIXED**: Added "Download Chapter Range" dialog to download chapters between specific numbers

Both features are fully integrated into the UI and ready for testing!

## Overview
Enhanced the "Download unread" feature to download chapters starting from the user's most recently read chapter, rather than from the first unread chapter.

## Problem
Previously, when a user skipped earlier chapters and began reading from a later chapter (e.g., starting from chapter 100), selecting "Download unread" would download chapters starting from chapter 1, leading to:
- Unnecessary downloads
- Wasted bandwidth and storage
- Delays in getting the chapters the user actually wants

## Solution
Modified `DownloadUnreadChaptersUseCase` to:
1. Query the `History` table to find the most recently read chapter for each book
2. Download only chapters AFTER the last read chapter that don't have content
3. Fall back to downloading all unread chapters if no reading history exists

## Implementation Details

### Modified Files

#### 1. `DownloadUnreadChaptersUseCase.kt`
**Location:** `IReader/domain/src/commonMain/kotlin/ireader/domain/usecases/local/book_usecases/DownloadUnreadChaptersUseCase.kt`

**Changes:**
- Added `HistoryUseCase` dependency to query reading history
- Added `fromLastRead` parameter (default: `true`) to control behavior
- Enhanced logic to:
  - Find the most recently read chapter via `History` table
  - Calculate the index of the last read chapter in the chapter list
  - Download only chapters after that index
  - Filter out chapters that already have content (length >= 10)
  - Fall back to legacy behavior if no history exists

**Key Code:**
```kotlin
suspend fun downloadUnreadChapters(
    bookIds: List<Long>,
    fromLastRead: Boolean = true
): DownloadResult {
    // For each book:
    // 1. Get all chapters
    // 2. Find last read chapter from History
    // 3. Download chapters after last read that don't have content
    // 4. Fall back to all unread if no history
}
```

#### 2. `HistoryUseCase.kt`
**Location:** `IReader/domain/src/commonMain/kotlin/ireader/domain/usecases/history/HistoryUseCase.kt`

**Changes:**
- Added `findHistoriesByBookId(bookId: Long): List<History>` method
- This method returns all history entries for a given book, allowing us to find the most recently read chapter

#### 3. `UseCasesInject.kt`
**Location:** `IReader/domain/src/commonMain/kotlin/ireader/domain/di/UseCasesInject.kt`

**Changes:**
- Updated DI configuration to provide all three required dependencies:
  - `LocalGetChapterUseCase` (first `get()`)
  - `HistoryUseCase` (second `get()`)
  - `DownloadService` (third `get()`)
- Updated both the `DownloadUseCases` factory and the standalone factory

#### 4. `strings.xml`
**Location:** `IReader/i18n/src/commonMain/composeResources/values/strings.xml`

**Changes:**
- Added `download_from_last_read` string resource
- Added `download_unread_from_last_read_desc` description string

### Dependency Injection
The three dependencies are automatically resolved by Koin DI in `UseCasesInject.kt`:
```kotlin
factory<DownloadUseCases> {
    DownloadUseCases(
        // ...
        downloadUnreadChapters = DownloadUnreadChaptersUseCase(get(), get(), get()),
        // First get() = LocalGetChapterUseCase
        // Second get() = HistoryUseCase
        // Third get() = DownloadService
    )
}

// Also as standalone factory
factory { DownloadUnreadChaptersUseCase(get(), get(), get()) }
```

## Behavior

### Default Behavior (fromLastRead = true)
1. User has read chapters 1-100
2. User selects "Download unread"
3. System downloads chapters 101+ (that don't have content)
4. Skips chapters 1-100 entirely

### Legacy Behavior (fromLastRead = false)
1. User has read chapters 1-100
2. System downloads ALL chapters without content (including 1-100 if they lack content)

### No History Fallback
If a user has never read any chapters from a book:
- System downloads all chapters without content (same as legacy behavior)
- Ensures new users aren't affected

## Benefits
✅ Reduces unnecessary downloads  
✅ Saves bandwidth and storage  
✅ Improves user experience for readers who skip chapters  
✅ Backward compatible (falls back to legacy behavior when no history exists)  
✅ Configurable via `fromLastRead` parameter for future UI options  

## Future Enhancements (Optional)
Consider adding a user preference setting:
- "Download unread from last read" toggle in Settings > Downloads
- Allow users to choose between:
  - "From last read" (new behavior)
  - "All unread" (legacy behavior)

## Testing Recommendations
1. **Test with reading history:**
   - Read chapters 1-50 of a book
   - Select "Download unread"
   - Verify only chapters 51+ are downloaded

2. **Test without reading history:**
   - Add a new book to library
   - Select "Download unread"
   - Verify all unread chapters are downloaded

3. **Test with skipped chapters:**
   - Read chapter 100 (skipping 1-99)
   - Select "Download unread"
   - Verify only chapters 101+ are downloaded

4. **Test with multiple books:**
   - Select multiple books with different reading progress
   - Select "Download unread"
   - Verify each book downloads from its own last read chapter

## Database Schema Reference

### History Table
```kotlin
data class History(
    val id: Long,
    val chapterId: Long,  // Links to Chapter.id
    val readAt: Long?,    // Timestamp of when chapter was read
    val readDuration: Long,
    val progress: Float = 0f,
)
```

### Chapter Table
```kotlin
data class Chapter(
    val id: Long,
    val bookId: Long,
    val key: String,
    val name: String,
    val read: Boolean,
    val content: List<Page>,
    // ... other fields
)
```

## Compilation Fixes Applied

### Issue 1: Missing `downloadService` parameter
**Error:** `No value passed for parameter 'downloadService'`

**Fix:** Updated DI configuration to pass three parameters instead of two:
```kotlin
// Before
DownloadUnreadChaptersUseCase(get(), get())

// After
DownloadUnreadChaptersUseCase(get(), get(), get())
```

### Issue 2: Missing `findHistoriesByBookId` method
**Error:** `Unresolved reference 'findHistoriesByBookId'`

**Fix:** Added the method to `HistoryUseCase`:
```kotlin
suspend fun findHistoriesByBookId(bookId: Long): List<History> {
    return historyRepository.findHistoriesByBookId(bookId)
}
```

## Notes
- The enhancement uses the `History` table's `readAt` timestamp to determine the most recently read chapter
- Chapters are considered "unread" if `content.joinToString().length < 10`
- The implementation is memory-efficient, processing one book at a time
- Error handling ensures failed books don't block others from downloading
- All compilation errors have been resolved

