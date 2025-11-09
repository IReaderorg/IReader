# Requirements Document

## Introduction

This specification covers the integration of existing but unused features in the IReader application. Through codebase analysis, we discovered that approximately 70% of implemented functionality is not exposed to users. This includes complete screens, use cases, UI components, and database infrastructure that are fully implemented but not wired into the application navigation and user interface.

The goal is to activate these dormant features to provide users with enhanced functionality including reading statistics, improved security settings, cloud backups, reader enhancements, and reporting capabilities.

## Glossary

- **IReader Application**: The main Android/Desktop reading application
- **Use Case**: A domain layer class that encapsulates business logic
- **Repository**: A data layer class that manages data persistence
- **ViewModel**: A presentation layer class that manages UI state
- **DI (Dependency Injection)**: The Koin framework used for dependency management
- **Navigation**: The Voyager framework used for screen navigation
- **StatisticsScreen**: A screen displaying reading statistics and progress
- **SecuritySettingsScreen**: An enhanced security configuration screen
- **CloudBackupScreen**: A screen for managing cloud backup providers
- **WorkManager**: Android's background task scheduling framework
- **ReportBrokenChapter**: Functionality to report problematic chapters
- **Reading Statistics**: Aggregated data about user reading habits
- **Smart Categories**: Dynamic book categorization based on rules

## Requirements

### Requirement 1: Statistics Screen Integration

**User Story:** As a reader, I want to view my reading statistics, so that I can track my reading progress and habits.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL create a StatisticsScreenSpec navigation wrapper
2. WHEN THE user navigates to Settings, THE IReader Application SHALL display a "Statistics" menu item with a chart icon
3. WHEN THE user taps the Statistics menu item, THE IReader Application SHALL navigate to the StatisticsScreen
4. WHEN THE StatisticsScreen loads, THE IReader Application SHALL display total chapters read from the ReadingStatistics repository
5. WHEN THE StatisticsScreen loads, THE IReader Application SHALL display total reading time formatted as hours and minutes
6. WHEN THE StatisticsScreen loads, THE IReader Application SHALL display books completed count
7. WHEN THE StatisticsScreen loads, THE IReader Application SHALL display current reading streak in days
8. WHEN THE StatisticsScreen loads, THE IReader Application SHALL display average reading speed in words per minute
9. WHEN THE StatisticsScreen loads AND favorite genres exist, THE IReader Application SHALL display a list of favorite genres with book counts
10. WHEN THE user taps the back button, THE IReader Application SHALL return to the Settings screen

### Requirement 2: Statistics Tracking Implementation

**User Story:** As a reader, I want my reading activity to be automatically tracked, so that my statistics are accurate and up-to-date.

#### Acceptance Criteria

1. WHEN THE user opens a chapter, THE IReader Application SHALL record the chapter open timestamp
2. WHEN THE user closes a chapter, THE IReader Application SHALL calculate reading duration in minutes
3. WHEN THE reading duration exceeds 10 seconds, THE IReader Application SHALL invoke TrackReadingProgressUseCase with chapter details
4. WHEN THE TrackReadingProgressUseCase executes, THE IReader Application SHALL insert or update reading statistics in the database
5. WHEN THE user completes a book, THE IReader Application SHALL increment the books completed counter
6. WHEN THE user reads on consecutive days, THE IReader Application SHALL update the reading streak counter
7. WHEN THE reading streak is broken, THE IReader Application SHALL reset the streak to zero
8. WHEN THE System calculates reading speed, THE IReader Application SHALL use word count divided by reading time

### Requirement 3: Enhanced Security Settings Integration

**User Story:** As a privacy-conscious user, I want comprehensive security settings, so that I can protect my reading content and app access.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL replace the old SecuritySettingSpec with the new SecuritySettingsScreen implementation
2. WHEN THE user navigates to Security settings, THE IReader Application SHALL display an "App Lock" section header
3. WHEN THE user enables App Lock, THE IReader Application SHALL display authentication method options for PIN, Password, and Biometric
4. WHEN THE user selects PIN authentication, THE IReader Application SHALL display a dialog to set up a 4-6 digit PIN
5. WHEN THE user selects Password authentication, THE IReader Application SHALL display a dialog to set up a password
6. WHEN THE device supports biometric authentication, THE IReader Application SHALL display a biometric authentication toggle
7. WHEN THE user navigates to Security settings, THE IReader Application SHALL display a "Privacy" section with Secure Screen, Hide Content, and 18+ Source Lock options
8. WHEN THE user enables Secure Screen, THE IReader Application SHALL block screenshots and screen recording
9. WHEN THE user enables Hide Content, THE IReader Application SHALL blur library covers until tapped
10. WHEN THE user enables 18+ Source Lock, THE IReader Application SHALL require authentication before accessing adult sources
11. WHEN THE user taps Security Guide, THE IReader Application SHALL display the SecurityOnboardingDialog with best practices

### Requirement 4: Report Broken Chapter Functionality

**User Story:** As a reader, I want to report broken or missing chapters, so that issues can be tracked and resolved.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL create a ReportBrokenChapterUseCase with ChapterReportRepository dependency
2. WHEN THE user is reading a chapter, THE IReader Application SHALL display a "Report Issue" button in the reader menu
3. WHEN THE user taps Report Issue, THE IReader Application SHALL display the ReportBrokenChapterDialog
4. WHEN THE ReportBrokenChapterDialog opens, THE IReader Application SHALL display reason options including "Missing Content", "Incorrect Order", "Formatting Issues", and "Other"
5. WHEN THE user selects a reason, THE IReader Application SHALL enable a description text field
6. WHEN THE user submits the report, THE IReader Application SHALL invoke ReportBrokenChapterUseCase with chapter ID, book ID, source ID, reason, and description
7. WHEN THE ReportBrokenChapterUseCase executes, THE IReader Application SHALL insert a ChapterReport record with status "pending"
8. WHEN THE report is saved successfully, THE IReader Application SHALL display a success message "Chapter reported successfully. Thank you for your feedback!"
9. WHEN THE report fails to save, THE IReader Application SHALL display an error message with failure reason

### Requirement 5: Configurable Reading Speed

**User Story:** As a reader, I want to configure my reading speed, so that reading time estimates are accurate for my pace.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL add a readingSpeedWPM preference to ReaderPreferences with default value 225
2. WHEN THE user navigates to Reader Settings, THE IReader Application SHALL display a "Reading Speed" slider
3. WHEN THE Reading Speed slider is displayed, THE IReader Application SHALL show the current value in words per minute
4. WHEN THE user adjusts the slider, THE IReader Application SHALL allow values between 150 and 400 WPM
5. WHEN THE user changes the reading speed, THE IReader Application SHALL save the new value to preferences
6. WHEN THE System calculates reading time estimates, THE IReader Application SHALL use the configured reading speed from preferences
7. WHEN THE reading time is displayed, THE IReader Application SHALL format it as "X min" or "X hours Y min" based on duration

### Requirement 6: Cache Size Calculation

**User Story:** As a user managing storage, I want to see how much space cover images are using, so that I can decide whether to clear the cache.

#### Acceptance Criteria

1. WHEN THE user navigates to Advanced Settings, THE IReader Application SHALL display a "Cover Cache Size" row
2. WHEN THE Cover Cache Size row is displayed, THE IReader Application SHALL invoke getCoverCacheSize method
3. WHEN THE getCoverCacheSize method executes, THE IReader Application SHALL locate the cover cache directory
4. WHEN THE cover cache directory exists, THE IReader Application SHALL calculate total size of all files recursively
5. WHEN THE total size is calculated, THE IReader Application SHALL format the size as bytes, KB, MB, or GB based on magnitude
6. WHEN THE cover cache directory does not exist, THE IReader Application SHALL display "0 MB"
7. WHEN THE calculation fails, THE IReader Application SHALL display "Error calculating size"

### Requirement 7: WorkManager Automatic Backups

**User Story:** As a user, I want automatic backups to run in the background, so that my reading data is protected without manual intervention.

#### Acceptance Criteria

1. WHEN THE System builds the Android application, THE IReader Application SHALL include WorkManager dependency version 2.8.1
2. WHEN THE System initializes ScheduleAutomaticBackupImpl, THE IReader Application SHALL inject Android Context
3. WHEN THE user enables automatic backups, THE IReader Application SHALL invoke ScheduleAutomaticBackupImpl.schedule with selected frequency
4. WHEN THE schedule method executes, THE IReader Application SHALL create WorkManager constraints requiring battery not low and storage not low
5. WHEN THE frequency is Every6Hours, THE IReader Application SHALL schedule a periodic work request with 6-hour interval
6. WHEN THE frequency is Every12Hours, THE IReader Application SHALL schedule a periodic work request with 12-hour interval
7. WHEN THE frequency is Daily, THE IReader Application SHALL schedule a periodic work request with 24-hour interval
8. WHEN THE frequency is Every2Days, THE IReader Application SHALL schedule a periodic work request with 48-hour interval
9. WHEN THE frequency is Weekly, THE IReader Application SHALL schedule a periodic work request with 7-day interval
10. WHEN THE work request is created, THE IReader Application SHALL tag it as "automatic_backup"
11. WHEN THE user disables automatic backups, THE IReader Application SHALL cancel the "automatic_backup" work
12. WHEN THE System checks backup status, THE IReader Application SHALL query WorkManager for enqueued or running "automatic_backup" work

### Requirement 8: Cloud Backup Screen Integration

**User Story:** As a user, I want to backup my data to cloud storage, so that I can restore it on other devices or after reinstallation.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL create a CloudBackupScreenSpec navigation wrapper
2. WHEN THE user navigates to Backup Settings, THE IReader Application SHALL display a "Cloud Backup" menu item
3. WHEN THE user taps Cloud Backup, THE IReader Application SHALL navigate to CloudBackupScreen
4. WHEN THE CloudBackupScreen loads, THE IReader Application SHALL display Dropbox and Google Drive provider options
5. WHEN THE user selects a provider, THE IReader Application SHALL initiate OAuth authentication flow
6. WHEN THE authentication succeeds, THE IReader Application SHALL save provider credentials to SourceCredentialsRepository
7. WHEN THE user taps Backup Now, THE IReader Application SHALL invoke CloudBackupManager to upload backup file
8. WHEN THE backup completes, THE IReader Application SHALL display success message with timestamp
9. WHEN THE backup fails, THE IReader Application SHALL display error message with failure reason

### Requirement 9: Source Detail Report Functionality

**User Story:** As a user, I want to report broken sources, so that extension maintainers can be notified of issues.

#### Acceptance Criteria

1. WHEN THE user views SourceDetailScreen, THE IReader Application SHALL display a "Report as Broken" button
2. WHEN THE user taps Report as Broken, THE IReader Application SHALL display a confirmation dialog
3. WHEN THE user confirms the report, THE IReader Application SHALL create a source report with source ID, package name, and version
4. WHEN THE report is created, THE IReader Application SHALL attempt to submit it to a reporting endpoint or create a GitHub issue
5. WHEN THE submission succeeds, THE IReader Application SHALL display "Source reported successfully"
6. WHEN THE submission fails, THE IReader Application SHALL save the report locally for later submission

### Requirement 10: Changelog Screen Integration

**User Story:** As a user, I want to view recent changes and updates, so that I know what features have been added or fixed.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL create a ChangelogScreenSpec navigation wrapper
2. WHEN THE user navigates to About Settings, THE IReader Application SHALL display a "Changelog" menu item
3. WHEN THE user taps Changelog, THE IReader Application SHALL navigate to ChangelogScreen
4. WHEN THE ChangelogScreen loads, THE IReader Application SHALL display version history with changes grouped by version
5. WHEN THE user scrolls the changelog, THE IReader Application SHALL display all historical versions

### Requirement 11: Reader Enhancement Components

**User Story:** As a reader, I want advanced reading controls, so that I can customize my reading experience.

#### Acceptance Criteria

1. WHEN THE user opens the reader settings, THE IReader Application SHALL display a brightness control slider
2. WHEN THE user adjusts brightness, THE IReader Application SHALL apply the brightness level to the reader screen only
3. WHEN THE user opens font settings, THE IReader Application SHALL display a font picker with system and custom fonts
4. WHEN THE user selects a custom font, THE IReader Application SHALL load and apply the font to reader text
5. WHEN THE user enables auto-scroll, THE IReader Application SHALL display an auto-scroll speed control
6. WHEN THE user adjusts auto-scroll speed, THE IReader Application SHALL update scrolling velocity in real-time
7. WHEN THE user enables volume key navigation, THE IReader Application SHALL navigate to next/previous chapter using volume buttons
8. WHEN THE user opens search, THE IReader Application SHALL display a find-in-chapter bar
9. WHEN THE user searches for text, THE IReader Application SHALL highlight all matches and allow navigation between them

### Requirement 12: Library Batch Operations

**User Story:** As a user managing many books, I want to perform batch operations, so that I can efficiently organize my library.

#### Acceptance Criteria

1. WHEN THE user long-presses a book in the library, THE IReader Application SHALL enter selection mode
2. WHEN THE selection mode is active, THE IReader Application SHALL display a batch operations toolbar
3. WHEN THE user selects multiple books, THE IReader Application SHALL display a count of selected items
4. WHEN THE user taps batch operations, THE IReader Application SHALL display a BatchOperationDialog
5. WHEN THE BatchOperationDialog opens, THE IReader Application SHALL display options for Mark as Read, Mark as Unread, Download, Delete, Change Category, and Archive
6. WHEN THE user selects an operation, THE IReader Application SHALL apply it to all selected books
7. WHEN THE operation completes, THE IReader Application SHALL display a success message with count of affected books

### Requirement 13: Smart Categories

**User Story:** As a user, I want automatic categorization of books, so that my library is organized without manual effort.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL create default smart categories for "Recently Added", "Currently Reading", "Completed", and "Unread"
2. WHEN THE user navigates to library categories, THE IReader Application SHALL display smart categories with dynamic icons
3. WHEN THE user taps a smart category, THE IReader Application SHALL invoke GetSmartCategoryBooksUseCase with category rules
4. WHEN THE GetSmartCategoryBooksUseCase executes, THE IReader Application SHALL filter books based on category criteria
5. WHEN THE smart category is "Recently Added", THE IReader Application SHALL display books added in the last 7 days
6. WHEN THE smart category is "Currently Reading", THE IReader Application SHALL display books with reading progress between 1% and 99%
7. WHEN THE smart category is "Completed", THE IReader Application SHALL display books with 100% reading progress
8. WHEN THE smart category is "Unread", THE IReader Application SHALL display books with 0% reading progress

### Requirement 14: Translation Features

**User Story:** As a reader of foreign language content, I want paragraph translation, so that I can understand the text.

#### Acceptance Criteria

1. WHEN THE user long-presses a paragraph in the reader, THE IReader Application SHALL display a translation option in the context menu
2. WHEN THE user selects translate, THE IReader Application SHALL invoke TranslateParagraphUseCase with the paragraph text
3. WHEN THE TranslateParagraphUseCase executes, THE IReader Application SHALL send text to the configured translation engine
4. WHEN THE translation completes, THE IReader Application SHALL display the translated text below the original
5. WHEN THE user has not configured a translation API key, THE IReader Application SHALL display TranslationApiKeyPrompt
6. WHEN THE user enters an API key, THE IReader Application SHALL save it to preferences and retry translation
7. WHEN THE user enables bilingual mode, THE IReader Application SHALL display original and translated text side-by-side

### Requirement 15: Font Management

**User Story:** As a reader, I want to use custom fonts, so that I can read with my preferred typography.

#### Acceptance Criteria

1. WHEN THE System initializes, THE IReader Application SHALL invoke SystemFontsInitializer to load system fonts
2. WHEN THE user navigates to Font Settings, THE IReader Application SHALL display a list of available fonts
3. WHEN THE user taps Add Font, THE IReader Application SHALL display a file picker for font files
4. WHEN THE user selects a font file, THE IReader Application SHALL invoke FontManagementUseCase to import the font
5. WHEN THE font is imported, THE IReader Application SHALL insert a CustomFont record in the database
6. WHEN THE user selects a font, THE IReader Application SHALL cache the font using FontCache
7. WHEN THE user deletes a custom font, THE IReader Application SHALL remove it from the database and cache

## Constraints

- The implementation MUST use existing code and avoid rewriting functionality
- The implementation MUST maintain backward compatibility with existing user data
- The implementation MUST follow the existing architecture patterns (MVVM, Clean Architecture)
- The implementation MUST use the existing DI framework (Koin)
- The implementation MUST use the existing navigation framework (Voyager)
- The implementation MUST support both Android and Desktop platforms where applicable
- The implementation MUST handle errors gracefully with user-friendly messages
- The implementation MUST not break existing functionality
- The implementation MUST include proper null safety checks
- The implementation MUST follow Kotlin coding conventions
- The implementation MUST use existing string resources or add new ones to i18n
- The implementation MUST respect user privacy and security settings
- The implementation MUST be performant and not block the UI thread
- The implementation MUST use coroutines for asynchronous operations
- The implementation MUST properly dispose of resources and cancel jobs when screens are destroyed
