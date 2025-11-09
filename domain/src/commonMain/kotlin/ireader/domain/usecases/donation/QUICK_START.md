# Donation Trigger System - Quick Start Guide

## 5-Minute Integration

### Step 1: Add to Your ViewModel

```kotlin
class BookDetailViewModel(
    private val donationUseCases: DonationUseCases,
    // ... other dependencies
) : ViewModel() {
    
    suspend fun markBookAsCompleted(book: Book, totalChapters: Int) {
        // Your existing logic
        updateBook(book.copy(status = Book.COMPLETED))
        
        // Add this line
        val trigger = donationUseCases.donationTriggerManager.checkBookCompletion(
            chapterCount = totalChapters,
            bookTitle = book.title
        )
        
        if (trigger != null) {
            // Show prompt (see Step 2)
        }
    }
}
```

### Step 2: Add Dialog to Your Screen

```kotlin
@Composable
fun BookDetailScreen(
    vm: BookDetailViewModel,
    navigator: Navigator
) {
    // Your existing screen content
    BookDetailContent()
    
    // Add this
    val donationViewModel = getScreenModel<DonationTriggerViewModel>()
    
    donationViewModel.currentPrompt?.let { promptMessage ->
        DonationPromptDialog(
            promptMessage = promptMessage,
            onDonateNow = {
                donationViewModel.onDonateNow()
                navigator.push(DonationScreen())
            },
            onMaybeLater = {
                donationViewModel.onMaybeLater()
            }
        )
    }
}
```

### Step 3: Trigger the Check

```kotlin
// In your ViewModel
fun onMarkAsCompletedClick() {
    viewModelScope.launch {
        markBookAsCompleted(book, totalChapters)
        
        // Trigger donation check
        donationViewModel.checkBookCompletion(
            chapterCount = totalChapters,
            bookTitle = book.title
        )
    }
}
```

## That's It!

The system will:
- ✅ Check if conditions are met (500+ chapters, cooldown passed)
- ✅ Show contextual prompt if appropriate
- ✅ Handle cooldown automatically
- ✅ Navigate to donation screen on "Donate Now"
- ✅ Dismiss on "Maybe Later"

## All Three Triggers

### 1. Book Completion (500+ chapters)

```kotlin
donationViewModel.checkBookCompletion(
    chapterCount = totalChapters,
    bookTitle = book.title
)
```

### 2. First Migration Success

```kotlin
donationViewModel.checkSourceMigration(
    sourceName = targetSourceName,
    chapterDifference = chapterDiff
)
```

### 3. Chapter Milestone (every 1,000 chapters)

```kotlin
donationViewModel.checkChapterMilestone()
```

## Global Integration (Recommended)

Instead of adding the dialog to each screen, add it once at the app level:

```kotlin
@Composable
fun App() {
    val donationViewModel = getScreenModel<DonationTriggerViewModel>()
    val navigator = rememberNavigator()
    
    Navigator(screen = HomeScreen()) { navigator ->
        CurrentScreen()
        
        // Global donation prompt - works everywhere
        donationViewModel.currentPrompt?.let { promptMessage ->
            DonationPromptDialog(
                promptMessage = promptMessage,
                onDonateNow = {
                    donationViewModel.onDonateNow()
                    navigator.push(DonationScreen())
                },
                onMaybeLater = {
                    donationViewModel.onMaybeLater()
                }
            )
        }
    }
}
```

Now just call the check methods from any ViewModel:

```kotlin
class AnyViewModel(
    private val donationViewModel: DonationTriggerViewModel
) {
    fun onEvent() {
        donationViewModel.checkBookCompletion(...)
        // Dialog appears automatically
    }
}
```

## Testing Your Integration

### 1. Reset Cooldown (for testing)

```kotlin
// In your test or debug code
appPreferences.lastDonationPromptTime().set(0L)
```

### 2. Trigger a Prompt

```kotlin
// Complete a 500+ chapter book
donationViewModel.checkBookCompletion(600, "Test Book")
```

### 3. Verify Dialog Appears

The dialog should appear with the message:
> "Congratulations! 🎉
> 
> You've finished "Test Book" with 600 chapters! If IReader made this journey better, please consider a small crypto donation to support development."

### 4. Test Cooldown

```kotlin
// Try triggering again immediately
donationViewModel.checkBookCompletion(700, "Another Book")
// Should NOT show (cooldown active)
```

## Common Issues

### Dialog Not Showing

**Check 1**: Is cooldown active?
```kotlin
donationViewModel.getDaysUntilNextPrompt { days ->
    println("Days until next prompt: $days")
}
```

**Check 2**: Are conditions met?
- Book completion: 500+ chapters?
- Migration: First time?
- Milestone: Exactly 1000, 2000, 3000, etc.?

**Check 3**: Is the dialog added to UI?
```kotlin
// Make sure this is in your Composable
donationViewModel.currentPrompt?.let { ... }
```

### Dialog Showing Too Often

**Issue**: `recordPromptShown()` not being called

**Solution**: The ViewModel handles this automatically. If using direct integration:
```kotlin
val trigger = donationUseCases.donationTriggerManager.checkXXX()
if (trigger != null) {
    showPrompt(trigger)
    // Add this line
    donationUseCases.donationTriggerManager.recordPromptShown()
}
```

## Configuration

Want to change the thresholds? Edit `DonationTriggerManager.kt`:

```kotlin
companion object {
    private const val COOLDOWN_DAYS = 30              // Change cooldown
    private const val BOOK_COMPLETION_CHAPTER_THRESHOLD = 500  // Change threshold
    private const val CHAPTER_MILESTONE_INTERVAL = 1000        // Change interval
}
```

## Need Help?

1. Check the [README.md](README.md) for detailed documentation
2. Check the [INTEGRATION_GUIDE.md](../../presentation/ui/settings/donation/INTEGRATION_GUIDE.md) for examples
3. Check the [ARCHITECTURE.md](ARCHITECTURE.md) for system design

## Example: Complete Integration

Here's a complete example showing all three triggers:

```kotlin
// ViewModel
class MyViewModel(
    private val donationViewModel: DonationTriggerViewModel,
    private val statisticsUseCases: StatisticsUseCases,
    private val localInsertUseCases: LocalInsertUseCases
) : ViewModel() {
    
    // Trigger 1: Book Completion
    fun markAsCompleted(book: Book, totalChapters: Int) {
        viewModelScope.launch {
            localInsertUseCases.updateBook(book.copy(status = Book.COMPLETED))
            statisticsUseCases.trackReadingProgress.trackBookCompletion()
            
            donationViewModel.checkBookCompletion(totalChapters, book.title)
        }
    }
    
    // Trigger 2: Source Migration
    fun onMigrationSuccess(sourceName: String, chapterDiff: Int) {
        viewModelScope.launch {
            donationViewModel.checkSourceMigration(sourceName, chapterDiff)
        }
    }
    
    // Trigger 3: Chapter Milestone
    fun onChapterRead(progress: Float, wordCount: Int) {
        viewModelScope.launch {
            if (progress >= 0.8f) {
                statisticsUseCases.trackReadingProgress.onChapterProgressUpdate(
                    progress, wordCount
                )
                donationViewModel.checkChapterMilestone()
            }
        }
    }
}

// Screen
@Composable
fun MyScreen(
    vm: MyViewModel,
    navigator: Navigator
) {
    val donationViewModel = getScreenModel<DonationTriggerViewModel>()
    
    // Your content
    MyScreenContent(
        onMarkCompleted = { book, chapters ->
            vm.markAsCompleted(book, chapters)
        }
    )
    
    // Donation prompt
    donationViewModel.currentPrompt?.let { promptMessage ->
        DonationPromptDialog(
            promptMessage = promptMessage,
            onDonateNow = {
                donationViewModel.onDonateNow()
                navigator.push(DonationScreen())
            },
            onMaybeLater = {
                donationViewModel.onMaybeLater()
            }
        )
    }
}
```

That's all you need! The system handles everything else automatically.
