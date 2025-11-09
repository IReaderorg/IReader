# Implementation Plan

- [x] 1. Fix Library Screen Filter Functionality




  - [x] 1.1 Implement LibraryFilterState data model


    - Create sealed class for LibraryFilter types (Unread, Completed, Downloaded, InProgress)
    - Create enums for SortOption and SortDirection
    - Implement LibraryFilterState data class with all filter properties
    - _Requirements: 1.1, 1.2, 1.3_
  
  - [x] 1.2 Update LibraryViewModel to handle filter state


    - Add StateFlow for LibraryFilterState
    - Implement filter toggle functions that update state immediately
    - Implement sort change functions with direction toggle
    - Add filter persistence using preferences repository
    - _Requirements: 1.1, 1.2, 1.4_
  
  - [x] 1.3 Implement real-time filter application


    - Update book list query to apply active filters using Flow operators
    - Implement AND logic for multiple filters
    - Add debouncing for filter changes to optimize performance
    - Ensure UI updates immediately when filters change
    - _Requirements: 1.1, 1.2_
  
  - [x] 1.4 Update filter UI to show active state


    - Add visual indicators (checkmarks, colors) for active filters
    - Display current sort option and direction in UI
    - Update filter bottom sheet to replace full-screen modal
    - _Requirements: 1.4, 1.5_

- [x] 2. Implement Category Management Features




  - [x] 2.1 Add hide empty categories functionality


    - Add "Show Empty Categories" toggle in settings
    - Update category query to filter out zero-count categories by default
    - Persist user preference for empty category visibility
    - _Requirements: 2.1, 2.2_
  
  - [x] 2.2 Implement category rename functionality


    - Create long-press context menu for category tabs
    - Implement rename dialog with validation
    - Update category in database and refresh UI
    - _Requirements: 2.3_
  
  - [x] 2.3 Implement category delete functionality

    - Add delete option in category context menu
    - Create confirmation dialog asking what to do with books
    - Implement delete logic with book reassignment
    - _Requirements: 2.3_
  
  - [x] 2.4 Implement category reordering


    - Add drag-and-drop support to category tabs using LazyRow
    - Update category order in database on drop
    - Animate reordering transitions
    - _Requirements: 2.4_
  
  - [x] 2.5 Implement batch category assignment


    - Add "Add to Category" option in selection mode
    - Create category picker dialog
    - Update multiple books' categories in single transaction
    - _Requirements: 2.5_
-

- [x] 3. Improve Library Filter Menu UX




  - [x] 3.1 Replace full-screen modal with bottom sheet


    - Implement ModalBottomSheet for filter UI
    - Set appropriate peek height to show content behind
    - Add smooth show/hide animations
    - _Requirements: 3.1_
  
  - [x] 3.2 Implement real-time column slider updates


    - Use derivedStateOf for column count changes
    - Update grid layout immediately as slider moves
    - Add haptic feedback on slider changes
    - _Requirements: 3.2_
  
  - [x] 3.3 Add sort direction toggle UI


    - Add arrow icon next to sort options showing direction
    - Implement tap to toggle between ascending/descending
    - Update sort immediately on direction change
    - _Requirements: 3.3, 3.4, 3.5_
-

- [x] 4. Enhance Library Visual Elements



  - [x] 4.1 Implement clear badge system for book covers


    - Create BookCoverBadge composable
    - Display "X Unread" badge instead of cryptic numbers
    - Display "X Downloaded" badge with distinct styling
    - Add pin indicator icon for pinned books
    - _Requirements: 4.1, 4.2_
  
  - [x] 4.2 Add text labels to selection mode icons


    - Update SelectionModeBottomBar with icon + text layout
    - Add labels: "Download", "Add to Category", "Mark Read", "Delete"
    - Ensure proper spacing and accessibility
    - _Requirements: 4.3_
  
  - [x] 4.3 Add library refresh button


    - Add "Refresh Library" or "Check for Updates" button to toolbar
    - Implement refresh action that checks for new chapters
    - Show progress indicator during refresh
    - _Requirements: 4.4_
  
  - [x] 4.4 Implement library-specific search


    - Add search bar to library screen
    - Filter only books in user's library (not global search)
    - Highlight search matches in results
    - _Requirements: 4.5_



- [x] 5. Implement Advanced Library Features




  - [x] 5.1 Create list view mode


    - Implement BookListItem composable with title, author, unread count
    - Add display mode toggle in filter menu (Grid/List)
    - Implement smooth transition animation between modes
    - Persist display mode preference
    - _Requirements: 5.1_
  
  - [x] 5.2 Implement pin to top feature


    - Add isPinned and pinnedOrder fields to Book model
    - Create "Pin to Top" option in book long-press menu
    - Update sort logic to show pinned books first
    - Allow reordering pinned books by dragging
    - _Requirements: 5.2_
  
  - [x] 5.3 Implement smart categories


    - Create SmartCategory sealed class
    - Implement queries for Currently Reading, Recently Added, Completed, Unread
    - Display smart categories before user categories
    - Make smart categories non-editable
    - _Requirements: 5.3_
  
  - [x] 5.4 Implement custom cover art feature


    - Add "Change Cover" option in book long-press menu
    - Implement file picker for local image selection
    - Store custom cover URL in database
    - Display custom cover with fallback to original
    - _Requirements: 5.4_
  
  - [x] 5.5 Implement archive system


    - Add isArchived field to Book model
    - Create "Archive" option in book context menu
    - Hide archived books from main library by default
    - Add "Show Archived" toggle in settings
    - Create "Archive" smart category
    - _Requirements: 5.5_
-

- [x] 6. Implement Selection Mode Batch Operations




  - [x] 6.1 Add "Download all unread chapters" batch operation


    - Implement batch download logic for selected books
    - Show progress dialog during batch download
    - Display success/failure summary
    - _Requirements: 6.1, 6.4_
  
  - [x] 6.2 Add "Mark all as Read/Unread" batch operations


    - Implement batch mark as read for all chapters in selected books
    - Implement batch mark as unread for all chapters in selected books
    - Update UI immediately after operation
    - Show confirmation snackbar with undo option
    - _Requirements: 6.2, 6.3, 6.5_
- [x] 7. Redesign Updates Screen



- [ ] 7. Redesign Updates Screen

  - [x] 7.1 Fix grammar and improve empty state


    - Change empty state text to "No New Updates are Available"
    - Replace emoji face with professional icon (bell or checkmark)
    - Add helpful message suggesting manual refresh
    - _Requirements: 7.1, 7.2_
  
  - [x] 7.2 Add clear refresh action button


    - Add FAB or prominent "Check for Updates" button
    - Implement refresh action that checks all library books
    - Show progress indicator during check
    - _Requirements: 7.3_
  
  - [x] 7.3 Implement selective update system


    - Add checkbox to each update item
    - Implement "Update Selected" button
    - Implement "Update All" button
    - Track selected updates in state
    - _Requirements: 7.4_
  
  - [x] 7.4 Add update history section


    - Create UpdateHistoryItem data model
    - Store update history in database
    - Display past updates with timestamps
    - Show chapter count for each update
    - _Requirements: 7.5_
  
  - [x] 7.5 Implement update progress indicator


    - Show progress bar during update check
    - Display which book is currently being checked
    - Show estimated time remaining
    - _Requirements: 8.1_
  
  - [x] 7.6 Add category filtering for updates


    - Add category filter dropdown to updates screen
    - Filter displayed updates by selected category
    - Implement "Update All in Category" action
    - _Requirements: 8.3_

- [x] 8. Improve History Screen





  - [x] 8.1 Improve empty state and UI consistency


    - Replace emoji face with professional icon
    - Remove redundant delete icons (keep only toolbar icon)
    - Ensure consistent spacing and styling
    - _Requirements: 9.1, 9.3_
  
  - [x] 8.2 Add confirmation dialog for clear all


    - Implement confirmation dialog with warning message
    - Require explicit confirmation before clearing
    - Add "This action cannot be undone" warning
    - _Requirements: 9.2_
  
  - [x] 8.3 Implement history search


    - Add search bar to history screen
    - Filter history by chapter title or novel name
    - Highlight search matches
    - _Requirements: 9.4_
  
  - [x] 8.4 Implement group by novel feature


    - Add "Group by Novel" toggle
    - Implement expandable list grouped by book
    - Show book title as group header with chapter count
    - Persist grouping preference
    - _Requirements: 9.5_
  


  - [x] 8.5 Add context menu for history items





    - Implement long-press context menu
    - Add "Go to Chapter" action
    - Add "View Novel Details" action
    - Add "Remove from History" action

    - _Requirements: 10.1, 10.2, 10.3, 10.4_
  -

  - [x] 8.6 Implement date filtering




    - Add date filter options (Today, Yesterday, Past 7 Days)
    - Update query to filter by selected date range
    - Display active filter in UI
    - _Requirements: 10.5_



- [x] 9. Fix Explore Screen Browser Launch



  - [x] 9.1 Fix "Open in Browser" functionality


    - Implement platform-specific openInBrowser function
    - Add proper error handling for browser launch failures
    - Display error message if browser cannot open
    - Ensure correct URL formatting before launch
    - _Requirements: 11.1, 11.2, 11.3, 11.4_
  
  - [x] 9.2 Maintain screen state after browser return


    - Save current scroll position before launching browser
    - Restore state when user returns to app
    - _Requirements: 11.5_

- [x] 10. Improve Explore Screen Terminology





  - [x] 10.1 Update navigation and tab labels


    - Rename main nav item from "Explore" to "Sources"
    - Rename "Sources" tab to "Browse"
    - Rename "Extensions" tab to "Installed"
    - Update all references throughout codebase
    - _Requirements: 12.1, 12.2_
  


  - [x] 10.2 Add tooltips and help text

    - Add tooltip to pin icon explaining "Pin to top"
    - Add help text for unclear UI elements
    - Ensure consistent terminology across screens

    - _Requirements: 12.3, 12.5_
  
  - [x] 10.3 Implement source details page

    - Create source detail screen showing language, description, status
    - Add "Report as broken" button
    - Display source metadata and statistics
    - _Requirements: 12.4_
-

- [x] 11. Implement Global Search Feature




  - [x] 11.1 Create global search architecture


    - Implement GlobalSearchUseCase interface
    - Create SearchResult data model with source info
    - Set up parallel search across sources using coroutines
    - _Requirements: 13.1_
  
  - [x] 11.2 Implement search UI


    - Create GlobalSearchScreen composable
    - Display results grouped by source
    - Show loading indicators per source
    - Handle and display errors gracefully
    - _Requirements: 13.2, 13.4_
  
  - [x] 11.3 Implement progressive result loading


    - Emit results as they arrive from each source
    - Implement 30-second timeout per source
    - Continue searching other sources if one fails
    - Display total results count
    - _Requirements: 13.3, 13.5_
  
  - [x] 11.4 Add result interaction


    - Implement tap to open novel detail from source
    - Show source name for each result
    - Add "Add to Library" quick action
    - _Requirements: 13.3_
-

- [x] 12. Implement Source Status Indicators



  - [x] 12.1 Create source health checking system


    - Implement SourceHealthChecker interface
    - Create SourceStatus sealed class
    - Implement periodic health checks
    - Cache status results
    - _Requirements: 14.1, 14.5_
  
  - [x] 12.2 Add status indicators to UI


    - Create SourceStatusIndicator composable
    - Display colored dots (green/red/yellow) based on status
    - Show status in both Browse and Installed tabs
    - _Requirements: 14.1_
  
  - [x] 12.3 Implement source login functionality


    - Add "Login" button for sources requiring authentication
    - Create login dialog for credentials
    - Store credentials securely
    - Update status after successful login
    - _Requirements: 14.2_
  
  - [x] 12.4 Add repository management


    - Implement "Add Repository" functionality
    - Parse repository URL and fetch source list
    - Display available sources from repository
    - _Requirements: 14.4_

- [x] 13. Implement Reader Settings Real-Time Preview





  - [x] 13.1 Redesign reader settings UI


    - Replace full-screen modal with ModalBottomSheet
    - Set peek height to 60% showing content behind
    - Ensure settings panel doesn't cover reading content
    - _Requirements: 15.5, 16.1_
  
  - [x] 13.2 Implement real-time font size updates


    - Use derivedStateOf for immediate font size changes
    - Update reader content as slider moves
    - Persist changes immediately
    - _Requirements: 15.1, 16.4_
  
  - [x] 13.3 Implement real-time line height updates


    - Apply line height changes immediately
    - Show preview in visible content
    - Add smooth transition animations
    - _Requirements: 15.2, 16.4_
  
  - [x] 13.4 Implement real-time color updates


    - Apply background and text color changes immediately
    - Show live preview of color combinations
    - Ensure proper contrast
    - _Requirements: 15.3, 16.4_
  
  - [x] 13.5 Hide or disable non-functional TTS button


    - Check if TTS is implemented
    - Hide button if not available
    - Or disable with explanation tooltip
    - _Requirements: 15.4_
  
  - [x] 13.6 Reorganize reader settings tabs


    - Move global settings out of reader settings
    - Keep only chapter/book-specific settings in General tab
    - Add clear section headers
    - _Requirements: 16.3_



- [x] 14. Implement Reader Navigation Features





  - [x] 14.1 Add volume key navigation


    - Implement key event handling for volume keys
    - Add setting toggle for volume key navigation
    - Support both page turn and scroll modes
    - Add haptic feedback on page turn
    - _Requirements: 17.1_
  
  - [x] 14.2 Add brightness control in reader


    - Create brightness slider component
    - Implement brightness adjustment
    - Add quick access button in reader toolbar
    - Persist brightness preference
    - _Requirements: 17.2_
  
  - [x] 14.3 Implement keep screen on feature


    - Add "Keep Screen On" toggle in settings
    - Use FLAG_KEEP_SCREEN_ON when enabled
    - Clear flag when leaving reader or disabling
    - _Requirements: 17.3_
  
  - [x] 14.4 Add quick font size adjuster


    - Create "Aa" button in reader toolbar
    - Show only font size slider on tap
    - Implement as compact overlay
    - Apply changes in real-time
    - _Requirements: 17.4_
  
  - [x] 14.5 Implement autoscroll speed controls


    - Add on-screen speed adjustment buttons when autoscroll active
    - Implement speed increase/decrease actions
    - Display current speed indicator
    - Persist speed preference
    - _Requirements: 17.5_

- [x] 15. Implement Reader Content Features





  - [x] 15.1 Add find in chapter functionality


    - Create FindInChapterBar composable
    - Implement text search with regex
    - Highlight all matches in content
    - Add next/previous navigation
    - Implement wrap-around at end/beginning
    - _Requirements: 18.1_
  

  
  - [x] 15.3 Add report broken chapter feature


    - Add "Report Broken Chapter" option in menu
    - Create report dialog with issue categories
    - Store report in database
    - Mark chapter as potentially broken
    - _Requirements: 18.4_
  
  - [x] 15.4 Implement reading time estimation


    - Calculate words per minute based on chapter length
    - Display estimated time remaining at bottom
    - Update estimate as user scrolls
    - Account for user's reading speed
    - _Requirements: 18.5_
-

- [x] 16. Implement Translation Features




  - [x] 16.1 Add paragraph translation


    - Implement long-press context menu on text
    - Add "Translate" option in context menu
    - Display translation inline or in popup
    - Handle translation errors gracefully
    - _Requirements: 19.2, 19.3, 19.4_
  
  - [x] 16.2 Implement bilingual mode


    - Create BilingualText composable
    - Add side-by-side layout option
    - Add paragraph-by-paragraph layout option
    - Allow switching between modes
    - _Requirements: 19.1_
  
  - [x] 16.3 Add translation API key validation


    - Disable translate button when no API key set
    - Show setup prompt when translate tapped without key
    - Add "Test Connection" button in settings
    - Display clear error messages
    - _Requirements: 19.5_
-

- [x] 17. Implement Custom Font Support




  - [x] 17.1 Create font management system


    - Implement CustomFont data model
    - Create FontRepository interface
    - Add font import functionality from file picker
    - Store font files in app directory
    - _Requirements: 20.1, 20.2_
  
  - [x] 17.2 Create font picker UI


    - Implement FontPicker composable
    - Display system fonts and custom fonts separately
    - Show font preview for each option
    - Add "Import Font" button
    - _Requirements: 20.1, 20.2_
  
  - [x] 17.3 Apply custom fonts to reader


    - Load custom font files as FontFamily
    - Apply selected font to reader content
    - Handle font loading errors
    - Provide fallback to system font
    - _Requirements: 20.2_
-

- [x] 18. Implement Reading Statistics



  - [x] 18.1 Create statistics tracking system


    - Implement ReadingStatistics data model
    - Track reading time using foreground service
    - Increment chapter count at 80% completion
    - Calculate reading speed (WPM)
    - Extract and count genres from metadata
    - _Requirements: 20.4_
  

  - [x] 18.2 Create statistics screen

    - Implement StatisticsScreen composable
    - Display total chapters read
    - Show total reading time formatted
    - Display books completed count
    - Show favorite genres chart
    - Display reading streak
    - _Requirements: 20.4_
  

  - [x] 18.3 Implement default reading mode setting

    - Add default reading mode preference
    - Apply to newly opened books
    - Allow per-book override
    - _Requirements: 20.3_


-

- [x] 19. Fix Settings Screen Bugs



  - [x] 19.1 Fix typos and labels


    - Correct "Advance Setting" to "Advanced Settings"
    - Change "Show History" to "Enable Reading History"
    - Change "Show Update" to "Show App Update Notifications"
    - Review and fix all other typos
    - _Requirements: 21.1, 22.1, 22.2_
  
  - [x] 19.2 Fix backup functionality


    - Implement file picker for "Create Backup" button
    - Generate backup file with proper format
    - Save backup to user-selected location
    - Display success/failure message
    - _Requirements: 21.2_
  
  - [x] 19.3 Fix theme export functionality


    - Implement file picker for "Export Theme" button
    - Serialize theme configuration to file
    - Save theme file to user-selected location
    - Display success/failure message
    - _Requirements: 21.3_
  
  - [x] 19.4 Implement Security settings page


    - Remove blank Security page
    - Create functional Security settings screen
    - Add security options (covered in task 21)
    - _Requirements: 21.4, 21.5_
  
  - [x] 19.5 Fix translation models loading


    - Debug translation models API call
    - Implement proper error handling
    - Display helpful error messages
    - Add retry functionality
    - _Requirements: 21.6_
-

- [x] 20. Improve Settings Screen Clarity



  - [x] 20.1 Add helper text to catalog settings


    - Add subtitle to "Show System Extensions" explaining purpose
    - Add subtitle to "Show Local Extensions" explaining purpose
    - Add subtitle to "Auto Installer" explaining functionality
    - _Requirements: 22.3_
  
  - [x] 20.2 Add help to Repository settings


    - Add help icon with explanation of repositories
    - Display example repository URL
    - Add "What is a repository?" info dialog
    - _Requirements: 22.4_
  
  - [x] 20.3 Improve empty states


    - Add "No active downloads" text to empty download queue
    - Add "No repositories added" text to empty repository list
    - Add helpful actions to empty states
    - _Requirements: 22.5_
  
  - [x] 20.4 Add feedback to update check


    - Show "Checking..." toast when checking for updates
    - Display "You are on the latest version" or "Update found"
    - Add progress indicator during check
    - _Requirements: 22.6_
-

- [x] 21. Implement Settings Safety Features




  - [x] 21.1 Create Danger Zone section


    - Implement DangerZoneSection composable
    - Group destructive actions with red styling
    - Add warning icon and "Danger Zone" header
    - Move dangerous actions into this section
    - _Requirements: 23.1_
  
  - [x] 21.2 Add destructive action confirmations


    - Implement DestructiveActionDialog requiring typed confirmation
    - Require typing "DELETE" or "RESET" to confirm
    - Add clear warning messages
    - Apply to all dangerous actions
    - _Requirements: 23.2_
  
  - [x] 21.3 Display cache size in clear cache button


    - Calculate current cache size
    - Display size in button label (e.g., "Clear All Cache (150.2 MB)")
    - Update size when cache changes
    - _Requirements: 23.3_
  
  - [x] 21.4 Add update check feedback


    - Show "Checking..." indicator
    - Display result message
    - Add error handling for network failures
    - _Requirements: 23.4_
  
  - [x] 21.5 Add confirmation to history clear all


    - Implement confirmation dialog for "Clear All" button
    - Add "This action cannot be undone" warning
    - Require explicit confirmation
    - _Requirements: 23.5_

- [x] 22. Implement Security Features




  - [x] 22.1 Implement app lock system


    - Create AuthMethod sealed class (PIN, Password, Biometric)
    - Implement SecurityRepository interface
    - Add authentication screen shown on app launch
    - Store credentials securely using EncryptedSharedPreferences
    - _Requirements: 24.1_
  
  - [x] 22.2 Add biometric authentication


    - Implement BiometricAuthScreen composable
    - Use BiometricPrompt API
    - Handle authentication success/failure
    - Provide fallback to PIN/password
    - _Requirements: 24.1_
  
  - [x] 22.3 Implement secure screen feature


    - Add "Secure Screen" toggle in settings
    - Set FLAG_SECURE when enabled
    - Block screenshots and screen recording
    - Clear flag when disabled
    - _Requirements: 24.2_
  
  - [x] 22.4 Implement content blur feature


    - Add "Hide Content" toggle in settings
    - Implement BlurredBookCover composable
    - Blur library covers until tapped
    - Add reveal icon overlay
    - _Requirements: 24.3_
  
  - [x] 22.5 Implement 18+ source lock


    - Add "18+ Source Lock" toggle in settings
    - Require authentication to access adult sources
    - Mark sources as 18+ in metadata
    - Show lock icon on restricted sources
    - _Requirements: 24.4_
  
  - [x] 22.6 Add security setup instructions


    - Create onboarding flow for security features
    - Add clear instructions for each option
    - Provide examples and best practices
    - _Requirements: 24.5_


-

- [x] 23. Implement Advanced Settings Features




  - [x] 23.1 Add translation API test connection


    - Create "Test Connection" button in translation settings
    - Implement API key validation
    - Display success/failure message
    - Show specific error details
    - _Requirements: 25.1_
  
  - [x] 23.2 Enhance download queue display


    - Display current download speed
    - Show estimated time remaining
    - Format speed (B/s, KB/s, MB/s)
    - Update in real-time
    - _Requirements: 25.2_
  
  - [x] 23.3 Implement download priority management


    - Add drag-and-drop to download queue
    - Allow reordering items to change priority
    - Update download order immediately
    - Persist priority changes
    - _Requirements: 25.3_
  
  - [x] 23.4 Add retry for failed downloads


    - Display "Retry" button next to failed downloads
    - Implement retry logic
    - Track retry attempts
    - Show error reason
    - _Requirements: 25.4_
  
  - [x] 23.5 Implement automatic backups


    - Create BackupConfig data model
    - Add "Automatic Backup" toggle in settings
    - Implement backup frequency selection (daily/weekly/monthly)
    - Create AutoBackupWorker using WorkManager
    - Schedule periodic backups
    - _Requirements: 25.5_
-

- [x] 24. Enhance Book Detail Screen




  - [x] 24.1 Implement book info editing


    - Create EditBookInfoDialog composable
    - Add edit button to book detail screen
    - Allow editing title, author, cover URL, description
    - Validate and save changes to database
    - Update library display immediately
    - _Requirements: 26.1, 26.5_
  
  - [x] 24.2 Add chapter list filters


    - Create ChapterListFilterBar composable
    - Add "Hide Read Chapters" toggle
    - Add "Hide Duplicate Chapters" toggle
    - Implement duplicate detection algorithm
    - Apply filters to chapter list
    - _Requirements: 26.2, 26.3_
  
  - [x] 24.3 Add bulk chapter download options


    - Add "Download..." option in chapter menu
    - Implement "Download all unread" action
    - Implement "Download all un-downloaded" action
    - Show progress during bulk download
    - _Requirements: 26.4_

- [x] 25. Implement Download Management Features





  - [x] 25.1 Add completed downloads tab


    - Create "Completed" tab in download queue
    - Display download history
    - Show completion timestamp
    - Allow clearing completed items
    - _Requirements: 27.1_
  
  - [x] 25.2 Implement download speed display


    - Calculate current download speed
    - Display speed in download queue
    - Show per-item and total speed
    - Format appropriately (KB/s, MB/s)
    - _Requirements: 27.3_
  
  - [x] 25.3 Add download completion notifications


    - Show notification when download completes
    - Include chapter name in notification
    - Add action to open chapter
    - Group notifications for multiple downloads
    - _Requirements: 27.5_

- [x] 26. Enhance Category Management




  - [x] 26.1 Add category rename/delete to Categories page


    - Add edit icon to each category item
    - Implement rename dialog
    - Implement delete confirmation
    - Ask what to do with books when deleting
    - _Requirements: 28.1, 28.2, 28.3_
  
  - [x] 26.2 Add category name validation


    - Check for duplicate category names
    - Prevent empty category names
    - Show validation errors
    - _Requirements: 28.4_
  
  - [x] 26.3 Display book count per category


    - Query book count for each category
    - Display count next to category name
    - Update count when books added/removed
    - _Requirements: 28.5_
-

- [x] 27. Implement True Black AMOLED Mode



  - [x] 27.1 Add true black theme option


    - Add "Use True Black" toggle in appearance settings
    - Create true black color scheme with #000000 background
    - Apply to dark theme when enabled
    - Ensure proper contrast for text
    - _Requirements: 30.1_
  
  - [x] 27.2 Implement app UI font customization


    - Add font selector for app interface
    - Apply selected font to all UI elements except reader
    - Provide system font options
    - Support custom font import for UI
    - _Requirements: 30.2_
  
  - [x] 27.3 Enhance theme customization preview


    - Show live preview of color changes
    - Update preview in real-time as colors adjust
    - Display preview of all UI elements
    - _Requirements: 30.3_
  
  - [x] 27.4 Implement theme import/export


    - Create shareable theme file format
    - Implement theme export functionality
    - Implement theme import with validation
    - Handle import errors gracefully
    - _Requirements: 30.4, 30.5_
-

- [x] 29. Implement Additional Features



  - [x] 29.1 Add "What's New" changelog


    - Create changelog screen
    - Link from About section
    - Display version history with changes
    - Highlight new features
    - _Requirements: 29.3_
  
  - [x] 29.2 Implement automatic backup to cloud


    - Add cloud storage provider selection
    - Implement Google Drive integration
    - Implement Dropbox integration
    - Handle authentication and permissions
    - _Requirements: 29.4_
  
  - [x] 29.3 Add app version display


    - Show current version in About section
    - Display build number
    - Add copy-to-clipboard functionality
    - _Requirements: 29.5_
