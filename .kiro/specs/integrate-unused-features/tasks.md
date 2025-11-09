# Implementation Plan

This implementation plan breaks down the integration of unused features into discrete, manageable coding tasks. Each task builds incrementally on previous tasks and references specific requirements from the requirements document.

---

## Phase 1: Statistics Feature Integration

- [x] 1. Integrate Statistics Screen into navigation





  - Create `StatisticsScreenSpec.kt` wrapper class in `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/`
  - Add Statistics menu item to `SettingScreenSpec.kt` with chart icon
  - Add "statistics" string resource to `i18n/src/commonMain/moko-resources/values/base/strings.xml`
  - Verify `StatisticsViewModel` is registered in `PresentationModules.kt`
  - Test navigation from Settings to Statistics screen
  - _Requirements: 1.1, 1.2, 1.3_




- [x] 2. Implement reading statistics tracking in ReaderScreenViewModel


  - Add `TrackReadingProgressUseCase` parameter to `ReaderScreenViewModel` constructor
  - Add chapter open timestamp tracking in `onChapterOpened()` method
  - Calculate reading duration when chapter closes in `onChapterClosed()` method
  - Invoke `TrackReadingProgressUseCase` when duration exceeds 10 seconds
  - Update `PresentationModules.kt` to inject `TrackReadingProgressUseCase`
  - Test statistics are recorded when reading chapters

  - _Requirements: 2.1, 2.2, 2.3, 2.4_
-

- [x] 3. Implement reading streak calculation



  - Add streak calculation logic to `TrackReadingProgressUseCase`
  - Check if user read on consecutive days
  - Increment streak counter for consecutive days
  - Reset streak to zero when broken

  - Test streak increments and resets correctly
  - _Requirements: 2.6, 2.7_
- [x] 4. Implement books completed tracking














- [ ] 4. Implement books completed tracking
  - Add completion detection in `ReaderScreenViewModel` when last chapter is read
  - Invoke `TrackReadingProgressUseCase` with completion flag
  - Update `ReadingStatisticsRepository` to increment books completed counter
  - Test books completed counter increments correctly
  - _Requirements: 2.5_

---

## Phase 2: Security Settings Enhancement
-

- [ ] 5. Create SecuritySettingsViewModel






- [ ] 5. Create SecuritySettingsViewModel

  - Create `SecuritySettingsViewModel.kt` in `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/security/`
  - Add state properties for app lock, secure screen, hide content, adult source lock, biometric
  - Implement `toggleAppLock()`, `toggleSecureScreen()`, `toggleHideContent()`, `toggleAdultSourceLock()` methods
  - Implement `showSetupDialog()`, `hideSetupDialog()`, `setupAuthMethod()` methods
  - Add `showOnboarding()` and `hideOnboarding()` methods
  - Register ViewModel in `PresentationModules.kt`
  - _Requirements: 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11_
-

- [x] 6. Update Android SecuritySettingSpec implementation




  - Edit `presentation/src/androidMain/kotlin/ireader/presentation/core/ui/SecuritySettingSpec.kt`
  - Replace Content() function to use `SecuritySettingsScreen` composable
  - Pass `SecuritySettingsViewModel` to `SecuritySettingsScreen`
  - Wrap in IScaffold with TitleToolbar
  - Test Android security settings display correctly
  - _Requirements: 3.1_
- [ ] 7. Create Desktop SecuritySettingSpec implementation













- [ ] 7. Create Desktop SecuritySettingSpec implementation

  - Edit `presentation/src/desktopMain/kotlin/ireader/presentation/core/ui/SecuritySettingSpec.kt`
  - Implement Content() function using `SecuritySettingsScreen` composable
  - Pass `SecuritySettingsViewModel` to `SecuritySettingsScreen`
  - Wrap in IScaffold with TitleToolbar
  - Test Desktop security settings display correctly
  - _Requirements: 3.1_

- [x] 8. Create SecurityRepository implementation




  - Create `SecurityRepositoryImpl.kt` in `data/src/commonMain/kotlin/ireader/data/security/`
  - Implement `saveAuthMethod()`, `getAuthMethod()`, `verifyAuth()`, `clearAuth()` methods
  - Use encrypted preferences for storing auth data
  - Register repository in DI modules
  - Test auth methods are saved and retrieved correctly
  - _Requirements: 3.4, 3.5, 3.6_

---

## Phase 3: Report Broken Chapter
- [x] 9. Create ReportBrokenChapterUseCase




- [ ] 9. Create ReportBrokenChapterUseCase

  - Create `ReportBrokenChapterUseCase.kt` in `domain/src/commonMain/kotlin/ireader/domain/usecases/chapter/`
  - Implement invoke method with chapterId, bookId, sourceId, reason, description parameters
  - Create ChapterReport entity with timestamp and "pending" status
  - Call `ChapterReportRepository.insertReport()` to save report
  - Return Result<Unit> with success or error
  - Register use case in `UseCasesInject.kt`
  - _Requirements: 4.1, 4.7, 4.8, 4.9_

- [x] 10. Integrate report functionality into ReaderScreenViewModel





  - Add `ReportBrokenChapterUseCase` parameter to `ReaderScreenViewModel` constructor
  - Create `reportBrokenChapter(reason: String, description: String)` method
  - Get current chapter and book from state
  - Invoke `ReportBrokenChapterUseCase` with chapter details
  - Show success or error snackbar based on result
  - Update `PresentationModules.kt` to inject use case
  - _Requirements: 4.2, 4.8, 4.9_

- [x] 11. Add Report Issue button to reader menu





  - Edit `ReaderScreen.kt` to add "Report Issue" menu item
  - Show `ReportBrokenChapterDialog` when tapped
  - Pass `reportBrokenChapter` callback to dialog
  - Test dialog appears and submits reports correctly
  - _Requirements: 4.2, 4.3_

---

## Phase 4: Configurable Reading Speed
-

- [x] 12. Add reading speed preference




  - Edit `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt`
  - Add `fun readingSpeedWPM() = preferenceStore.getInt("reading_speed_wpm", 225)` method
  - Test preference saves and retrieves correctly
  - _Requirements: 5.1_

- [x] 13. Update reading time calculation to use preference




  - Edit `ReaderScreenViewModel.kt` line 1117
  - Replace hardcoded `val wordsPerMinute = 225` with `val wordsPerMinute = readerPreferences.readingSpeedWPM().get()`
  - Test reading time estimates use configured speed
  - _Requirements: 5.6_
- [x] 14. Add reading speed slider to Reader Settings




- [ ] 14. Add reading speed slider to Reader Settings

  - Edit `ReaderSettingScreenViewModel.kt` to add `val readingSpeedWPM = readerPreferences.readingSpeedWPM().asState()`
  - Edit Reader Settings UI to add slider component
  - Set slider range to 150-400 WPM
  - Display current value in WPM
  - Save value when changed
  - Test slider updates preference correctly
  - _Requirements: 5.2, 5.3, 5.4, 5.5_

---

## Phase 5: Cache Size Calculation

- [x] 15. Implement cache size calculation







  - Edit `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/advance/AdvanceSettingViewModel.kt`
  - Replace `getCoverCacheSize()` method implementation
  - Locate cover cache directory
  - Calculate total size of all files recursively using `walkTopDown()`
  - Format size as bytes, KB, MB, or GB based on magnitude
  - Handle directory not exists case (return "0 MB")
  - Handle errors gracefully (return "Error calculating size")
  - Test cache size displays correctly
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7_

---

## Phase 6: WorkManager Automatic Backups

- [x] 16. Add WorkManager dependency





  - Edit `android/build.gradle.kts` or `domain/build.gradle.kts`
  - Add `implementation("androidx.work:work-runtime-ktx:2.8.1")` to dependencies
  - Sync Gradle
  - _Requirements: 7.1_


- [x] 17. Update ScheduleAutomaticBackupImpl with WorkManager



  - Edit `domain/src/androidMain/kotlin/ireader/domain/usecases/backup/ScheduleAutomaticBackupImpl.kt`
  - Add `private val context: Context` parameter to constructor
  - Uncomment all WorkManager code in `schedule()` method
  - Uncomment WorkManager code in `cancel()` method
  - Uncomment WorkManager code in `isScheduled()` method
  - Test backup scheduling works correctly
  - _Requirements: 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 7.9, 7.10, 7.11, 7.12_
- [x] 18. Update DI to inject Context




- [ ] 18. Update DI to inject Context

  - Edit `domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt`
  - Update `ScheduleAutomaticBackupImpl` binding to inject `androidContext()`
  - Test DI provides Context correctly
  - _Requirements: 7.2_
-

- [x] 19. Verify AutoBackupWorker implementation




  - Check `domain/src/androidMain/kotlin/ireader/domain/usecases/backup/AutoBackupWorker.kt` exists
  - Verify `doWork()` method invokes `CreateBackup` use case
  - Verify worker is registered in AndroidManifest if needed
  - Test worker executes backup correctly
  - _Requirements: 7.3_

---

## Phase 7: Cloud Backup Integration
-

- [x] 20. Create CloudBackupScreenSpec




  - Create `CloudBackupScreenSpec.kt` in `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/`
  - Implement Content() function to display `CloudBackupScreen`
  - Pass navigation callback to screen
  - Test screen displays correctly
  - _Requirements: 8.1, 8.3_

- [x] 21. Add Cloud Backup navigation to Backup Settings




  - Edit `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/backups/Backup.kt`
  - Add "Cloud Backup" menu item with cloud icon
  - Navigate to `CloudBackupScreenSpec` when tapped
  - Test navigation works correctly
  - _Requirements: 8.2_
-

- [x] 22. Implement cloud provider authentication




  - Verify `DropboxProvider` and `GoogleDriveProvider` OAuth implementations
  - Test Dropbox authentication flow
  - Test Google Drive authentication flow
  - Verify credentials are saved to `SourceCredentialsRepository`
  - Test authentication status displays correctly
  - _Requirements: 8.4, 8.5, 8.6_


- [x] 23. Implement cloud backup upload




  - Verify `CloudBackupManager.uploadBackup()` implementation
  - Test backup file upload to Dropbox
  - Test backup file upload to Google Drive
  - Display success message with timestamp
  - Display error message on failure
  - _Requirements: 8.7, 8.8, 8.9_

---

## Phase 8: Source Detail Report Functionality
-

- [x] 24. Implement Report as Broken functionality







  - Edit `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/SourceDetailScreen.kt`
  - Replace TODO comment on line 157 with actual implementation
  - Create confirmation dialog when button tapped
  - Create source report with source ID, package name, version
  - Attempt to submit report (store locally for now)
  - Display success or error message
  - Test report functionality works correctly
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

---

## Phase 9: Changelog Screen Integration
- [x] 25. Create ChangelogScreenSpec




- [ ] 25. Create ChangelogScreenSpec



  - Create `ChangelogScreenSpec.kt` in `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/`
  - Implement Content() function to display `ChangelogScreen`
  - Pass navigation callback to screen
  - Test screen displays correctly
  - _Requirements: 10.1, 10.3_

- [x] 26. Add Changelog navigation to About Settings




- [ ] 26. Add Changelog navigation to About Settings


  - Edit `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/about/AboutSettingScreen.kt`
  - Add "Changelog" menu item with history icon
  - Navigate to `ChangelogScreenSpec` when tapped
  - Test navigation works correctly
  - _Requirements: 10.2_

---

## Phase 10: Reader Enhancement Components

- [x] 27. Integrate BrightnessControl into ReaderScreen





  - Add brightness state to `ReaderScreenViewModel`
  - Add `BrightnessControl` composable to reader settings bottom sheet
  - Implement brightness adjustment logic
  - Apply brightness to reader screen only
  - Test brightness control works correctly
  - _Requirements: 11.1, 11.2_

- [x] 28. Integrate FontPicker into ReaderScreen





  - Add selected font state to `ReaderScreenViewModel`
  - Add `FontPicker` composable to reader settings
  - Load system and custom fonts
  - Apply selected font to reader text
  - Test font picker works correctly
  - _Requirements: 11.3, 11.4_

-

- [x] 29. Integrate AutoScrollSpeedControl into ReaderScreen



  - Add auto-scroll speed state to `ReaderScreenViewModel`
  - Add `AutoScrollSpeedControl` composable to reader settings
  - Implement auto-scroll logic with configurable speed
  - Update scrolling velocity in real-time
  - Test auto-scroll works correctly
  - _Requirements: 11.5, 11.6_

-

- [x] 30. Integrate VolumeKeyHandler into ReaderScreen



  - Add volume key handling to `ReaderScreen`
  - Implement platform-specific volume key detection
  - Navigate to next/previous chapter on volume key press
  - Add preference to enable/disable volume key navigation
  - Test volume key navigation works correctly
  - _Requirements: 11.7_
-

- [x] 31. Integrate FindInChapterBar into ReaderScreen




  - Add search query state to `ReaderScreenViewModel`
  - Add `FindInChapterBar` composable to reader screen
  - Implement text search in current chapter
  - Highlight all matches
  - Add navigation between matches
  - Test find in chapter works correctly
  - _Requirements: 11.8, 11.9_

---

## Phase 11: Library Batch Operations


- [x] 32. Implement selection mode in LibraryScreen




  - Add selected books state to `LibraryViewModel`
  - Implement long-press to enter selection mode
  - Add `toggleSelection(bookId)` method
  - Display selection count in toolbar
  - Test selection mode activates correctly
  - _Requirements: 12.1, 12.2, 12.3_
-

- [x] 33. Create BatchOperationDialog




  - Verify `BatchOperationDialog.kt` exists in library components
  - Add dialog state to `LibraryViewModel`
  - Display dialog when batch operations button tapped
  - Show operation options (Mark as Read, Download, Delete, etc.)
  - Test dialog displays correctly
  - _Requirements: 12.4, 12.5_


- [x] 34. Implement batch operation execution




  - Add `performBatchOperation(operation)` method to `LibraryViewModel`
  - Implement Mark as Read/Unread for selected books
  - Implement Download chapters for selected books using `DownloadUnreadChaptersUseCase`
  - Implement Delete for selected books
  - Implement Change category for selected books
  - Implement Archive for selected books using `ArchiveBookUseCase`
  - Display progress and completion message
  - Test batch operations complete successfully
  - _Requirements: 12.6, 12.7_

---

## Phase 12: Smart Categories

- [x] 35. Create default smart categories





  - Create smart category definitions for "Recently Added", "Currently Reading", "Completed", "Unread"
  - Add smart category icons
  - Register smart categories in library initialization
  - Test smart categories appear in library
  - _Requirements: 13.1, 13.2_
-

- [x] 36. Implement smart category filtering




  - Integrate `GetSmartCategoryBooksUseCase` into `LibraryViewModel`
  - Implement "Recently Added" filter (books added in last 7 days)
  - Implement "Currently Reading" filter (1-99% progress)
  - Implement "Completed" filter (100% progress)
  - Implement "Unread" filter (0% progress)
  - Test smart category filtering works correctly
  - _Requirements: 13.3, 13.4, 13.5, 13.6, 13.7, 13.8_

---

## Phase 13: Translation Features
-

- [x] 37. Integrate paragraph translation into ReaderScreen




  - Add translation state to `ReaderScreenViewModel`
  - Add long-press handler to paragraph text
  - Display translation option in context menu
  - Invoke `TranslateParagraphUseCase` when selected
  - Display translated text below original
  - Test paragraph translation works correctly
  - _Requirements: 14.1, 14.2, 14.3, 14.4_

- [x] 38. Implement translation API key prompt





  - Check if translation API key is configured
  - Display `TranslationApiKeyPrompt` if not configured
  - Save API key to preferences when entered
  - Retry translation after key is saved
  - Test API key prompt works correctly
  - _Requirements: 14.5, 14.6_
-

- [x] 39. Implement bilingual mode




  - Add bilingual mode preference
  - Add bilingual mode toggle to reader settings
  - Display original and translated text side-by-side when enabled
  - Test bilingual mode works correctly
  - _Requirements: 14.7_

---

## Phase 14: Font Management

-

- [x] 40. Initialize system fonts on app start



  - Invoke `SystemFontsInitializer` during app initialization
  - Load system fonts into font cache
  - Test system fonts are available
  - _Requirements: 15.1_
-

- [x] 41. Create Font Settings screen




  - Create font settings UI to display available fonts
  - List system and custom fonts
  - Add "Add Font" button
  - Test font list displays correctly
  - _Requirements: 15.2_

- [x] 42. Implement custom font import




  - Add file picker for font files (.ttf, .otf)
  - Invoke `FontManagementUseCase` to import selected font
  - Insert `CustomFont` record in database
  - Cache font using `FontCache`
  - Test custom font import works correctly
  - _Requirements: 15.3, 15.4, 15.5, 15.6_
-

- [x] 43. Implement custom font deletion




  - Add delete option for custom fonts
  - Remove font from database
  - Remove font from cache
  - Test custom font deletion works correctly
  - _Requirements: 15.7_

---

## Phase 15: Testing and Polish

- [ ] 44. Write unit tests for use cases
- [ ]* 44.1 Write tests for `TrackReadingProgressUseCase`
- [ ]* 44.2 Write tests for `ReportBrokenChapterUseCase`
- [ ]* 44.3 Write tests for `GetReadingStatisticsUseCase`
- [ ]* 44.4 Write tests for batch operation use cases
- [ ]* 44.5 Write tests for translation use cases
- [ ]* 44.6 Ensure all tests pass

- [ ] 45. Write UI tests for new screens
- [ ]* 45.1 Write Compose tests for `StatisticsScreen`
- [ ]* 45.2 Write Compose tests for `SecuritySettingsScreen`
- [ ]* 45.3 Write Compose tests for `CloudBackupScreen`
- [ ]* 45.4 Write Compose tests for reader enhancements
- [ ]* 45.5 Ensure all tests pass

- [ ] 46. Perform integration testing
- [ ]* 46.1 Test complete statistics tracking flow
- [ ]* 46.2 Test complete security setup flow
- [ ]* 46.3 Test complete cloud backup flow
- [ ]* 46.4 Test complete batch operations flow
- [ ]* 46.5 Test complete translation flow
- [ ]* 46.6 Fix any issues found

- [ ] 47. Optimize performance
- [ ]* 47.1 Profile database queries and add indexes if needed
- [ ]* 47.2 Optimize image loading and caching
- [ ]* 47.3 Optimize font loading and caching
- [ ]* 47.4 Reduce memory usage where possible
- [ ]* 47.5 Test app performance on low-end devices

- [x] 48. Update documentation





- [x] 48.1 Update README with new features





- [ ]* 48.2 Create user guide for statistics
- [ ]* 48.3 Create user guide for security settings
- [ ]* 48.4 Create user guide for cloud backup
- [ ]* 48.5 Create developer documentation for new components

---

## Notes

- Each task should be completed and tested before moving to the next
- Tasks within a phase can be worked on in parallel if they don't have dependencies
- All code should follow existing Kotlin coding conventions
- All strings should be added to i18n resources
- All new classes should be registered in appropriate DI modules
- All database changes should include proper migrations
- All features should handle errors gracefully
- All features should work on both Android and Desktop where applicable
