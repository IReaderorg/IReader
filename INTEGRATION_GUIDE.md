# Quick Integration Guide

This guide shows how to integrate the unused screens and features into the IReader app.

---

## 1. Integrate StatisticsScreen (15 minutes)

### Step 1: Create StatisticsScreenSpec.kt

Create: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/StatisticsScreenSpec.kt`

```kotlin
package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.statistics.StatisticsScreen

class StatisticsScreenSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        StatisticsScreen()
    }
}
```

### Step 2: Add to Settings Menu

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/SettingScreenSpec.kt`

Add this entry to the `sections` list (after Security section):

```kotlin
SettingsSection(
    icon = Icons.Default.BarChart,  // or Icons.Default.Analytics
    titleRes = MR.strings.statistics,  // Add to strings.xml
    onClick = { navigator.push(StatisticsScreenSpec()) },
),
```

### Step 3: Add String Resource

Edit: `i18n/src/commonMain/moko-resources/values/base/strings.xml`

```xml
<string name="statistics">Statistics</string>
```

### Step 4: Ensure DI is configured

Check that `StatisticsViewModel` is injected in:
`presentation/src/commonMain/kotlin/ireader/presentation/core/di/PresentationModules.kt`

---

## 2. Update SecuritySettingsScreen (20 minutes)

### Step 1: Update Android Implementation

Edit: `presentation/src/androidMain/kotlin/ireader/presentation/core/ui/SecuritySettingSpec.kt`

Replace the entire `Content()` function with:

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
override fun Content() {
    val vm: SecuritySettingsViewModel = getIViewModel()
    val navigator = LocalNavigator.currentOrThrow

    IScaffold(
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = localize(MR.strings.security),
                scrollBehavior = scrollBehavior,
                popBackStack = { popBackStack(navigator) }
            )
        }
    ) { padding ->
        SecuritySettingsScreen(vm = vm, padding = padding)
    }
}
```

### Step 2: Create Desktop Implementation

Edit: `presentation/src/desktopMain/kotlin/ireader/presentation/core/ui/SecuritySettingSpec.kt`

```kotlin
package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.security.SecuritySettingsScreen
import ireader.presentation.ui.settings.security.SecuritySettingsViewModel

actual class SecuritySettingSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val vm: SecuritySettingsViewModel = getIViewModel()
        val navigator = LocalNavigator.currentOrThrow

        IScaffold(
            topBar = { scrollBehavior ->
                TitleToolbar(
                    title = localize(MR.strings.security),
                    scrollBehavior = scrollBehavior,
                    popBackStack = { popBackStack(navigator) }
                )
            }
        ) { padding ->
            SecuritySettingsScreen(vm = vm, padding = padding)
        }
    }
}
```

---

## 3. Implement Report Broken Chapter (30 minutes)

### Step 1: Create ReportBrokenChapterUseCase

Create: `domain/src/commonMain/kotlin/ireader/domain/usecases/chapter/ReportBrokenChapterUseCase.kt`

```kotlin
package ireader.domain.usecases.chapter

import ireader.domain.data.repository.ChapterReportRepository
import ireader.domain.models.entities.ChapterReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportBrokenChapterUseCase(
    private val chapterReportRepository: ChapterReportRepository
) {
    suspend operator fun invoke(
        chapterId: Long,
        bookId: Long,
        sourceId: Long,
        reason: String,
        description: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val report = ChapterReport(
                id = 0,
                chapterId = chapterId,
                bookId = bookId,
                sourceId = sourceId,
                reason = reason,
                description = description,
                timestamp = System.currentTimeMillis(),
                status = "pending"
            )
            
            chapterReportRepository.insertReport(report)
            
            // TODO: Send report to backend API or GitHub
            // For now, just store locally
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Step 2: Add to DI

Edit: `domain/src/commonMain/kotlin/ireader/domain/di/UseCasesInject.kt`

Add to the appropriate module:

```kotlin
single { ReportBrokenChapterUseCase(get()) }
```

### Step 3: Update ReaderScreenViewModel

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

Replace the TODO comment (line 1068) with:

```kotlin
class ReaderScreenViewModel(
    // ... existing parameters
    private val reportBrokenChapterUseCase: ReportBrokenChapterUseCase,
    // ... rest of parameters
) : BaseViewModel() {

    // ... existing code

    fun reportBrokenChapter(reason: String, description: String) {
        scope.launch {
            try {
                val currentChapter = stateChapter.value ?: return@launch
                val currentBook = stateBook.value ?: return@launch
                
                reportBrokenChapterUseCase(
                    chapterId = currentChapter.id,
                    bookId = currentBook.id,
                    sourceId = currentBook.sourceId,
                    reason = reason,
                    description = description
                ).onSuccess {
                    showSnackBar(UiText.DynamicString("Chapter reported successfully. Thank you for your feedback!"))
                }.onFailure { error ->
                    showSnackBar(UiText.DynamicString("Failed to report chapter: ${error.message}"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Error reporting chapter: ${e.message}"))
            }
        }
    }
}
```

---

## 4. Make Reading Speed Configurable (20 minutes)

### Step 1: Add Preference

Edit: `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt`

Add this preference:

```kotlin
fun readingSpeedWPM() = preferenceStore.getInt("reading_speed_wpm", 225)
```

### Step 2: Update ReaderScreenViewModel

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

Replace line 1117:

```kotlin
// Before:
val wordsPerMinute = 225

// After:
val wordsPerMinute = readerPreferences.readingSpeedWPM().get()
```

### Step 3: Add to Reader Settings

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/reader/ReaderSettingScreenViewModel.kt`

Add:

```kotlin
val readingSpeedWPM = readerPreferences.readingSpeedWPM().asState()
```

Then add a slider in the reader settings UI to adjust this value (150-400 WPM range).

---

## 5. Implement Cache Size Calculation (15 minutes)

### Step 1: Update AdvanceSettingViewModel

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/advance/AdvanceSettingViewModel.kt`

Replace the `getCoverCacheSize()` function:

```kotlin
fun getCoverCacheSize(): String {
    return try {
        val cacheDir = context.cacheDir // or appropriate cache directory
        val coverCacheDir = File(cacheDir, "covers") // adjust path as needed
        
        if (!coverCacheDir.exists()) {
            return "0 MB"
        }
        
        val totalSize = coverCacheDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
        
        formatFileSize(totalSize)
    } catch (e: Exception) {
        "Error calculating size"
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
```

---

## 6. Enable WorkManager for Automatic Backups (30 minutes)

### Step 1: Add Dependency

Edit: `android/build.gradle.kts`

Add to dependencies:

```kotlin
implementation("androidx.work:work-runtime-ktx:2.8.1")
```

### Step 2: Uncomment WorkManager Code

Edit: `domain/src/androidMain/kotlin/ireader/domain/usecases/backup/ScheduleAutomaticBackupImpl.kt`

1. Uncomment all the WorkManager code
2. Add Context parameter to constructor:

```kotlin
actual class ScheduleAutomaticBackupImpl(
    private val context: Context
) : ScheduleAutomaticBackup {
    // ... uncommented code
}
```

### Step 3: Update DI

Edit: `domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt`

Update the binding:

```kotlin
single<ScheduleAutomaticBackup> { 
    ScheduleAutomaticBackupImpl(androidContext()) 
}
```

### Step 4: Verify AutoBackupWorker

Ensure: `domain/src/androidMain/kotlin/ireader/domain/usecases/backup/AutoBackupWorker.kt` exists and is properly implemented.

---

## 7. Integrate Cloud Backup Screen (10 minutes)

### Step 1: Add to Backup Settings

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/backups/Backup.kt`

Add a navigation item:

```kotlin
Components.Row(
    title = "Cloud Backup",
    subtitle = "Backup to Dropbox or Google Drive",
    icon = Icons.Default.Cloud,
    onClick = { 
        navigator.push(CloudBackupScreenSpec()) 
    }
)
```

### Step 2: Create CloudBackupScreenSpec

Create: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/CloudBackupScreenSpec.kt`

```kotlin
package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.settings.backups.CloudBackupScreen

class CloudBackupScreenSpec : VoyagerScreen() {
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        CloudBackupScreen(
            onBack = { navigator.pop() }
        )
    }
}
```

---

## 8. Add Changelog Screen (5 minutes)

### Step 1: Add to About Settings

Edit: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/about/AboutSettingScreen.kt`

Add navigation:

```kotlin
Components.Row(
    title = "Changelog",
    subtitle = "View recent changes and updates",
    icon = Icons.Default.History,
    onClick = { 
        navigator.push(ChangelogScreenSpec()) 
    }
)
```

---

## Testing Checklist

After implementing these changes:

- [ ] Statistics screen appears in settings and displays data
- [ ] Security settings work on both Android and Desktop
- [ ] Report broken chapter creates database entry
- [ ] Reading speed can be configured in settings
- [ ] Cache size displays correctly
- [ ] Automatic backups schedule properly (Android)
- [ ] Cloud backup screen is accessible
- [ ] Changelog screen opens from About

---

## Common Issues

### Issue: ViewModel not found
**Solution:** Ensure ViewModel is registered in DI modules

### Issue: Navigation doesn't work
**Solution:** Check that Screen implements VoyagerScreen correctly

### Issue: Preferences not saving
**Solution:** Verify PreferenceStore is properly initialized

### Issue: Database queries fail
**Solution:** Check that repositories are injected and migrations are up to date

---

## Next Steps

After completing these integrations, consider:

1. Integrating reader enhancement components (brightness control, font picker, etc.)
2. Adding batch operations to library
3. Implementing smart categories
4. Adding translation features to reader
5. Implementing source health monitoring
