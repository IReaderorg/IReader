# Unused Features and TODO Analysis

## Summary
This document lists unversioned screens, TODO comments, and unused features in the IReader app that need to be integrated or implemented.

---

## 1. Unversioned Screens (Not Used in App)

### ✅ StatisticsScreen - READY TO USE
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/statistics/`
- `StatisticsScreen.kt` - Complete UI implementation
- `StatisticsViewModel.kt` - ViewModel with data logic

**Status:** Fully implemented but not integrated into navigation

**What it does:**
- Displays reading statistics (chapters read, reading time, books completed)
- Shows reading streak and average reading speed
- Lists favorite genres
- Uses `ReadingStatistics` entity and repository

**Integration needed:**
1. Add to `SettingScreenSpec.kt` navigation menu
2. Create `StatisticsScreenSpec.kt` wrapper
3. Ensure `StatisticsUseCases` are injected in DI

---

### ✅ SecuritySettingsScreen - READY TO USE
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/`

**Files:**
- `SecuritySettingsScreen.kt` - Main settings UI
- `SecuritySettingsViewModel.kt` - ViewModel
- `AuthenticationScreen.kt` - Auth flow
- `BiometricAuthScreen.kt` - Biometric auth
- `SecurityOnboardingDialog.kt` - User guide
- `SetupAuthDialog.kt` - Setup dialog
- `AdultSourceLock.kt` - 18+ content protection

**Status:** Fully implemented but uses old Android-specific implementation

**What it does:**
- App lock with PIN/Password/Biometric
- Secure screen (block screenshots)
- Hide content (blur library covers)
- 18+ source lock
- Security best practices guide

**Integration needed:**
1. Replace old `SecuritySettingSpec` implementation with new `SecuritySettingsScreen`
2. Update Android implementation to use new composable
3. Create desktop implementation

---

### ✅ SourceDetailScreen - READY TO USE
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreen.kt`

**Status:** Fully implemented but not called from navigation

**What it does:**
- Shows detailed information about a catalog/source
- Displays version, language, package name
- Shows source statistics
- "Report as Broken" button (TODO: needs implementation)

**Integration needed:**
Already integrated! Found in `ExtensionScreenSpec.kt`:
```kotlin
onShowDetails = { catalog ->
    navigator.push(SourceDetailScreen(catalog))
}
```
Just needs the "Report as Broken" functionality implemented.

---

## 2. TODO Comments to Address

### High Priority TODOs

#### 1. ReaderScreenViewModel.kt (Line 1068)
```kotlin
// TODO: Inject ReportBrokenChapterUseCase and use it here
```
**Context:** Report broken chapter functionality
**Action needed:** 
- Create `ReportBrokenChapterUseCase` 
- Inject into `ReaderScreenViewModel`
- Implement chapter reporting logic

---

#### 2. ReaderScreenViewModel.kt (Line 1115)
```kotlin
// TODO: Make this configurable in settings
val wordsPerMinute = 225
```
**Context:** Reading time estimation
**Action needed:**
- Add `readingSpeedWPM` preference to `ReaderPreferences`
- Create settings UI for user to configure reading speed
- Use preference value instead of hardcoded 225 WPM

---

#### 3. AdvanceSettingViewModel.kt (Line 119)
```kotlin
// TODO: Implement cache size calculation
fun getCoverCacheSize(): String {
    return "0 MB"
}
```
**Action needed:**
- Implement actual cache directory scanning
- Calculate total size of cached cover images
- Format size properly (KB/MB/GB)

---

#### 4. SourceDetailScreen.kt (Line 157)
```kotlin
onClick = { /* TODO: Implement report functionality */ }
```
**Context:** "Report as Broken" button
**Action needed:**
- Create `ReportBrokenSourceUseCase`
- Implement reporting mechanism (API call or GitHub issue)
- Add confirmation dialog

---

#### 5. MainBottonSettingComposable.kt (Line 67)
```kotlin
// TODO: implement TTS functionality in desktop
```
**Action needed:**
- Implement Text-to-Speech for desktop platform
- Create desktop-specific TTS service
- Add platform-specific expect/actual declarations

---

### Low Priority TODOs

#### 6. VerticalFastScroller.kt (Line 253)
```kotlin
// TODO: Sometimes item height is not available when scrolling up
```
**Context:** Fast scrolling bug
**Action needed:** Debug and fix scrolling calculation issue

---

#### 7. FastMappers.kt (Line 153)
```kotlin
// TODO: should be fastMaxByOrNull to match stdlib
```
**Action needed:** Rename function for consistency

---

## 3. Unused Database Tables/Entities

### ReadingStatistics
**Location:** `data/src/commonMain/sqldelight/data/readingStatistics.sq`
**Status:** Database table exists but not actively used

**Related files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/entities/ReadingStatistics.kt`
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/ReadingStatisticsRepository.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/statistics/`

**Integration:** Connect to `StatisticsScreen` (see section 1)

---

### ChapterReport
**Location:** `data/src/commonMain/sqldelight/data/chapterReport.sq`
**Status:** Database table exists but not used

**Related files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/entities/ChapterReport.kt`
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/ChapterReportRepository.kt`

**Integration:** Implement report functionality (see TODO #1 and #4)

---

### UpdateHistory
**Location:** `data/src/commonMain/sqldelight/data/updateHistory.sq`
**Status:** Database table exists

**Related files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/entities/UpdateHistory.kt`

**Purpose:** Track when books/chapters were updated
**Integration:** Could be used in Updates screen or Statistics

---

### CustomFont
**Location:** `data/src/commonMain/sqldelight/data/customFont.sq`
**Status:** Database table exists

**Related files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/fonts/CustomFont.kt`
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/FontRepository.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/fonts/`

**Purpose:** Custom font management
**Status:** Partially implemented, needs UI integration

---

## 4. Unused Use Cases

### Statistics Use Cases
- `TrackReadingProgressUseCase` - Track reading sessions
- `GetReadingStatisticsUseCase` - Retrieve statistics
**Status:** Implemented but not called anywhere
**Integration:** Use in `ReaderScreenViewModel` and `StatisticsViewModel`

---

### Font Management Use Cases
- `FontManagementUseCase` - Manage custom fonts
- `SystemFontsInitializer` - Initialize system fonts
- `FontCache` - Cache font files
**Status:** Implemented but not fully integrated
**Integration:** Add to font settings screen

---

### Translation Use Cases
- `TranslateParagraphUseCase` - Translate text paragraphs
- `TranslateChapterWithStorageUseCase` - Translate and cache chapters
**Status:** Implemented but UI components not connected
**Integration:** Already has UI components in reader, needs wiring

---

### Smart Categories
- `GetSmartCategoryBooksUseCase` - Get books for smart categories
**Related:** `SmartCategory.kt` entity exists
**Status:** Implemented but not used in library
**Integration:** Add smart category support to library screen

---

### Book Operations
- `ArchiveBookUseCase` - Archive books
- `ToggleBookPinUseCase` - Pin/unpin books
- `UpdateCustomCoverUseCase` - Custom cover images
- `DownloadUnreadChaptersUseCase` - Batch download
**Status:** Implemented but not exposed in UI

---

### Reader Features
- `ApplyDefaultReadingModeUseCase` - Auto-apply reading preferences
- `PreloadChapterUseCase` - Preload next chapter
**Status:** Implemented but not called

---

### Global Search
- `GlobalSearchUseCase` - Search across all sources
**Status:** Implemented, has UI (`GlobalSearchScreenSpec`)
**Integration:** Already integrated in `ExtensionScreenSpec`

---

## 5. Unused UI Components

### Reader Components (Unversioned)
- `AutoScrollSpeedControl.kt` - Auto-scroll speed adjustment
- `BilingualText.kt` - Side-by-side translation
- `BrightnessControl.kt` - In-reader brightness
- `FindInChapterBar.kt` - Search within chapter
- `FontPicker.kt` - Font selection UI
- `ParagraphTranslationMenu.kt` - Translation menu
- `QuickFontSizeAdjuster.kt` - Quick font size buttons
- `ReaderSettingsBottomSheet.kt` - Settings bottom sheet
- `ReaderTextWithCustomFont.kt` - Custom font rendering
- `ReadingTimeEstimator.kt` - Time to finish chapter
- `ReportBrokenChapterDialog.kt` - Report dialog
- `SelectableTranslatableText.kt` - Selectable text with translation
- `TranslationApiKeyPrompt.kt` - API key input
- `VolumeKeyHandler.kt` - Volume key navigation
- `FontLoader.kt` - Font loading utility

**Status:** All implemented but not integrated into `ReaderScreen`

---

### Library Components
- `BatchOperationDialog.kt` - Batch operations on books
- `LibraryFilterBottomSheet.kt` - Advanced filtering
- `ChapterListFilterBar.kt` - Chapter filtering

**Status:** Implemented but not used in library UI

---

### Source Components
- `AddRepositoryDialog.kt` - Add custom repositories
- `SourceLoginDialog.kt` - Source authentication
- `SourceStatusIndicator.kt` - Source health indicator

**Status:** Partially integrated, needs full implementation

---

### Other Components
- `BlurredBookCover.kt` - Blurred cover backgrounds
- `SecureScreen.kt` - Screen security wrapper
- `UpdateHistoryItem.kt` - Update history display

---

## 6. Unused Screens/Features

### ChangelogScreen
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/about/ChangelogScreen.kt`
**Status:** Implemented but not linked from About screen

---

### CloudBackupScreen
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/backups/CloudBackupScreen.kt`
**Status:** Implemented with Dropbox and Google Drive support
**Related:** 
- `CloudBackupManager.kt`
- `DropboxProvider.kt`
- `GoogleDriveProvider.kt`
- `AutoBackupWorker.kt`

**Integration needed:** Add to Backup settings

---

## 7. WorkManager Integration (ScheduleAutomaticBackupImpl)

**Location:** `domain/src/androidMain/kotlin/ireader/domain/usecases/backup/ScheduleAutomaticBackupImpl.kt`

**Current Status:** Placeholder implementation, WorkManager code commented out

**What needs to be done:**
1. Add WorkManager dependency to `build.gradle.kts`:
   ```kotlin
   implementation("androidx.work:work-runtime-ktx:2.8.1")
   ```

2. Uncomment WorkManager code in `ScheduleAutomaticBackupImpl.kt`

3. Ensure `AutoBackupWorker.kt` is properly implemented

4. Inject Android Context into `ScheduleAutomaticBackupImpl`

5. Test automatic backup scheduling with different frequencies

---

## 8. Implementation Priority

### Phase 1: Quick Wins (1-2 days)
1. ✅ Integrate `StatisticsScreen` into settings navigation
2. ✅ Replace old `SecuritySettingSpec` with new `SecuritySettingsScreen`
3. ✅ Implement "Report as Broken" functionality
4. ✅ Make reading speed configurable
5. ✅ Implement cache size calculation

### Phase 2: Reader Enhancements (3-5 days)
1. Integrate reader UI components (brightness, font picker, etc.)
2. Implement chapter reporting with `ReportBrokenChapterUseCase`
3. Add translation features to reader
4. Implement volume key navigation
5. Add reading time estimator

### Phase 3: Library Features (3-5 days)
1. Add batch operations dialog
2. Implement library filter bottom sheet
3. Add smart categories support
4. Implement book pinning/archiving
5. Add custom cover support

### Phase 4: Cloud & Backup (2-3 days)
1. Integrate `CloudBackupScreen`
2. Implement WorkManager for automatic backups
3. Test Dropbox and Google Drive providers

### Phase 5: Statistics & Tracking (2-3 days)
1. Wire up statistics tracking in reader
2. Implement reading progress tracking
3. Add statistics visualization
4. Track update history

---

## 9. Files to Modify for Integration

### For StatisticsScreen:
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/SettingScreenSpec.kt`
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/StatisticsScreenSpec.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/core/di/PresentationModules.kt`

### For SecuritySettingsScreen:
- `presentation/src/androidMain/kotlin/ireader/presentation/core/ui/SecuritySettingSpec.kt`
- `presentation/src/desktopMain/kotlin/ireader/presentation/core/ui/SecuritySettingSpec.kt`

### For Reader Components:
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderScreen.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`

### For WorkManager:
- `domain/build.gradle.kts` or `android/build.gradle.kts`
- `domain/src/androidMain/kotlin/ireader/domain/usecases/backup/ScheduleAutomaticBackupImpl.kt`

---

## 10. Unused Methods Summary

Based on the codebase analysis, here are categories of unused methods:

### Completely Unused Use Cases:
- All Statistics use cases (not called anywhere)
- Font management use cases (partially used)
- Smart category use cases
- Archive/Pin book use cases
- Preload chapter use case
- Apply default reading mode use case

### Partially Used:
- Translation use cases (implemented but UI not wired)
- Cloud backup providers (implemented but not in settings)
- Report functionality (UI exists but no backend)

### Ready to Use:
- Global search (already integrated)
- Source detail screen (already integrated)
- Volume key handler (implemented, needs enabling)

---

## Conclusion

The IReader app has significant functionality already implemented but not exposed to users:

- **3 complete screens** ready for integration
- **15+ reader enhancement components** waiting to be used
- **Multiple database tables** with full repository support
- **20+ use cases** fully implemented but not called
- **Cloud backup system** ready for deployment
- **Statistics tracking** system complete but dormant

Most of these features just need navigation wiring and DI configuration to become functional.
