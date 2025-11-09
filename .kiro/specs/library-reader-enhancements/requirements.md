# Requirements Document

## Introduction

This document outlines the requirements for comprehensive enhancements to the IReader application's Library, Updates, History, Reader, and Settings screens. The goal is to fix critical bugs, improve user experience through better UI/UX patterns, and add highly-requested features that enhance the reading and library management experience. These improvements address real user feedback and focus on making the application more intuitive, efficient, and professional.

## Glossary

- **IReader Application**: The main Kotlin Multiplatform application for reading novels
- **Library Screen**: The main screen displaying the user's collection of novels with categories and filters
- **Updates Screen**: The screen showing available chapter updates for library books
- **History Screen**: The screen displaying the user's reading history
- **Reader View**: The in-chapter reading interface where users read novel content
- **Explore Screen**: The screen for browsing and managing novel sources/extensions
- **Filter Menu**: The interface for filtering and sorting library content
- **Selection Mode**: A mode where users can select multiple items for batch operations
- **Smart Categories**: Auto-populated categories based on reading status or other criteria
- **TTS**: Text-to-Speech functionality for audio reading
- **Bottom Sheet**: A modal interface that slides up from the bottom of the screen
- **Popover Menu**: A contextual menu anchored to a specific UI element
- **Empty State**: The UI displayed when a screen has no content to show
- **Material Design 3**: The design system used for UI components

## Requirements

### Requirement 1: Library Screen Filter Functionality

**User Story:** As a user, I want filters to work correctly and apply in real-time, so that I can quickly find specific books in my library.

#### Acceptance Criteria

1. WHEN the user checks a filter option (Unread, Completed), THE IReader Application SHALL immediately update the displayed library items to match the filter
2. WHEN the user unchecks a filter option, THE IReader Application SHALL immediately restore the filtered-out items to the display
3. WHEN multiple filters are selected, THE IReader Application SHALL apply all filters simultaneously using AND logic
4. WHEN the user opens the filter menu, THE IReader Application SHALL display the currently active filters with visual indicators
5. WHEN the user changes sort options, THE IReader Application SHALL display the currently active sort method and direction

### Requirement 2: Library Screen Category Management

**User Story:** As a user, I want better category management with the ability to hide empty categories and manage them easily, so that my library stays organized.

#### Acceptance Criteria

1. WHEN a category has zero items, THE IReader Application SHALL hide the category tab by default
2. WHERE the user enables "Show Empty Categories", THE IReader Application SHALL display all categories including those with zero items
3. WHEN the user long-presses a category tab, THE IReader Application SHALL display options to rename or delete the category
4. WHEN the user drags a category tab, THE IReader Application SHALL allow reordering of category tabs
5. WHEN the user selects multiple books, THE IReader Application SHALL provide an option to add them to a category in batch

### Requirement 3: Library Screen Filter Menu UX

**User Story:** As a user, I want the filter menu to be less disruptive and show real-time changes, so that I can adjust filters without losing context of my library.

#### Acceptance Criteria

1. WHEN the user opens the filter/sort menu, THE IReader Application SHALL display a popover menu or bottom sheet instead of a full-screen modal
2. WHEN the user adjusts the columns slider, THE IReader Application SHALL update the library grid in real-time
3. WHEN the user views the sort menu, THE IReader Application SHALL display the currently active sort option with a visual indicator
4. WHEN the user views the sort menu, THE IReader Application SHALL display the current sort direction (Ascending/Descending)
5. WHEN the user taps a sort direction toggle, THE IReader Application SHALL reverse the sort order immediately

### Requirement 4: Library Screen Visual Improvements

**User Story:** As a user, I want clearer visual indicators and labels on book covers and selection mode, so that I can understand the information at a glance.

#### Acceptance Criteria

1. WHEN the user views a book cover with unread chapters, THE IReader Application SHALL display a clear badge showing "X Unread" instead of cryptic numbers
2. WHEN the user views a book cover with downloaded chapters, THE IReader Application SHALL display a clear badge showing "X Downloaded"
3. WHEN the user enters selection mode, THE IReader Application SHALL display text labels below action icons (Download, Add to Category, Mark Read, Delete)
4. WHEN the user views the library screen, THE IReader Application SHALL provide a visible "Refresh Library" or "Check for Updates" button
5. WHEN the user searches in the library, THE IReader Application SHALL only search books already in the user's library

### Requirement 5: Library Screen Advanced Features

**User Story:** As a user, I want advanced library features like list view, pinning, and smart categories, so that I can manage my collection more effectively.

#### Acceptance Criteria

1. WHERE the user selects list view mode, THE IReader Application SHALL display novels in a text-based list with titles, authors, and unread counts
2. WHEN the user long-presses a novel and selects "Pin to Top", THE IReader Application SHALL keep that novel at the start of the list regardless of sort order
3. WHEN the user accesses smart categories, THE IReader Application SHALL provide auto-populated categories for "Currently Reading", "Recently Added", and "Completed"
4. WHEN the user long-presses a novel, THE IReader Application SHALL provide options to change cover art from local files
5. WHEN the user selects "Archive" for a novel, THE IReader Application SHALL move it to a hidden archive category

### Requirement 6: Library Screen Selection Mode Enhancements

**User Story:** As a user, I want more batch operations in selection mode, so that I can efficiently manage multiple books at once.

#### Acceptance Criteria

1. WHEN the user selects multiple books in selection mode, THE IReader Application SHALL provide an option to "Download all unread chapters"
2. WHEN the user selects multiple books in selection mode, THE IReader Application SHALL provide an option to "Mark all as Read"
3. WHEN the user selects multiple books in selection mode, THE IReader Application SHALL provide an option to "Mark all as Unread"
4. WHEN the user performs a batch operation, THE IReader Application SHALL display progress feedback
5. WHEN the user adds multiple books to a category, THE IReader Application SHALL confirm the operation with a success message

### Requirement 7: Updates Screen Improvements

**User Story:** As a user, I want a professional updates screen with clear actions and better information, so that I can manage novel updates efficiently.

#### Acceptance Criteria

1. WHEN the updates screen displays an empty state, THE IReader Application SHALL show the text "No New Updates are Available" with correct grammar
2. WHEN the updates screen displays an empty state, THE IReader Application SHALL use a professional icon instead of an emoji face
3. WHEN the user views the updates screen, THE IReader Application SHALL provide a clear "Check for Updates" or "Refresh All" button
4. WHEN updates are found, THE IReader Application SHALL display them with checkboxes for selective updating
5. WHEN the user views the updates screen, THE IReader Application SHALL show a log of past updates with timestamps

### Requirement 8: Updates Screen Progress and Filtering

**User Story:** As a user, I want to see update progress and filter updates by category, so that I can control which books to update.

#### Acceptance Criteria

1. WHEN updates are in progress, THE IReader Application SHALL display a progress bar indicating which book is being checked
2. WHEN the user taps "Update All", THE IReader Application SHALL update all books with available updates
3. WHERE the user filters by category, THE IReader Application SHALL only show and update books in that category
4. WHEN an update completes, THE IReader Application SHALL display the number of new chapters found
5. WHEN an update fails, THE IReader Application SHALL display an error message with a retry option

### Requirement 9: History Screen UX Improvements

**User Story:** As a user, I want a cleaner history screen with better organization and confirmation dialogs, so that I don't accidentally lose my reading history.

#### Acceptance Criteria

1. WHEN the history screen displays an empty state, THE IReader Application SHALL use a professional icon instead of an emoji face
2. WHEN the user taps "Clear All" in the history screen, THE IReader Application SHALL display a confirmation dialog stating "Are you sure you want to clear all reading history? This action cannot be undone."
3. WHEN the user views the history screen, THE IReader Application SHALL display only one delete action (in the toolbar) to avoid redundancy
4. WHEN the user searches history, THE IReader Application SHALL filter history items by chapter title or novel name
5. WHEN the user enables "Group by Novel", THE IReader Application SHALL display an expandable list grouped by book

### Requirement 10: History Screen Context Actions

**User Story:** As a user, I want more actions available for history items, so that I can quickly navigate or manage my reading history.

#### Acceptance Criteria

1. WHEN the user long-presses a history item, THE IReader Application SHALL display a context menu with options
2. WHEN the user selects "Go to Chapter" from the context menu, THE IReader Application SHALL open that chapter in the reader
3. WHEN the user selects "View Novel Details" from the context menu, THE IReader Application SHALL open the book detail screen
4. WHEN the user selects "Remove from History" from the context menu, THE IReader Application SHALL remove only that item
5. WHERE the user filters by date, THE IReader Application SHALL show history from "Today", "Yesterday", or "Past 7 Days"

### Requirement 11: Explore Screen Bug Fixes

**User Story:** As a user, I want the "Open in Browser" function to work correctly, so that I can view sources in my system browser.

#### Acceptance Criteria

1. WHEN the user taps "Open in browser" in the extension browse view, THE IReader Application SHALL launch the system browser with the correct URL
2. IF the system browser fails to open, THEN THE IReader Application SHALL display an error message
3. WHEN the user taps "Open in browser", THE IReader Application SHALL not display a blank white screen
4. WHEN the browser opens, THE IReader Application SHALL pass the complete and properly formatted URL
5. WHEN the user returns from the browser, THE IReader Application SHALL maintain the current screen state

### Requirement 12: Explore Screen Terminology and Navigation

**User Story:** As a user, I want clearer terminology and organization in the Explore section, so that I understand how to find and manage sources.

#### Acceptance Criteria

1. WHEN the user views the main navigation, THE IReader Application SHALL label the section as "Sources" instead of "Explore"
2. WHEN the user opens the Sources section, THE IReader Application SHALL display two tabs: "Browse" and "Installed"
3. WHEN the user views a pin icon next to a source, THE IReader Application SHALL display a tooltip explaining "Pin to top"
4. WHEN the user taps a source, THE IReader Application SHALL display a details page with language, description, and status
5. WHEN the user views the terminology, THE IReader Application SHALL use consistent naming throughout the interface

### Requirement 13: Explore Screen Global Search

**User Story:** As a user, I want to search for novels across all installed sources at once, so that I can find content more efficiently.

#### Acceptance Criteria

1. WHEN the user enters a search query in the Explore screen, THE IReader Application SHALL search across all installed sources simultaneously
2. WHEN search results are returned, THE IReader Application SHALL display which source each result came from
3. WHEN the user taps a search result, THE IReader Application SHALL open the novel detail page from that source
4. WHEN a source is offline or errors, THE IReader Application SHALL continue searching other sources
5. WHEN the search completes, THE IReader Application SHALL display the total number of results found

### Requirement 14: Explore Screen Source Management

**User Story:** As a user, I want better source management with status indicators and login options, so that I can maintain my sources effectively.

#### Acceptance Criteria

1. WHEN the user views installed sources, THE IReader Application SHALL display a status indicator (green for online, red for offline, yellow for login required)
2. WHERE a source requires login, THE IReader Application SHALL provide a "Login" button directly in the installed list
3. WHEN the user taps a source, THE IReader Application SHALL display a "Report as broken" button
4. WHEN the user adds a repository URL, THE IReader Application SHALL fetch and display available sources from that repository
5. WHEN a source fails to load, THE IReader Application SHALL display the error reason in the status

### Requirement 15: Reader View Critical Bug Fixes

**User Story:** As a user, I want reader settings to apply in real-time and non-functional features to be hidden, so that customizing my reading experience is smooth and intuitive.

#### Acceptance Criteria

1. WHEN the user adjusts font size in reader settings, THE IReader Application SHALL apply the change immediately without closing the menu
2. WHEN the user adjusts line height in reader settings, THE IReader Application SHALL apply the change immediately without closing the menu
3. WHEN the user adjusts colors in reader settings, THE IReader Application SHALL apply the change immediately without closing the menu
4. WHEN the TTS feature is not implemented, THE IReader Application SHALL hide or disable the TTS button
5. WHEN the user opens reader settings, THE IReader Application SHALL display a bottom sheet or popover that doesn't cover the entire screen

### Requirement 16: Reader View Settings UX

**User Story:** As a user, I want reader settings to be accessible and clear, so that I can customize my reading experience without trial and error.

#### Acceptance Criteria

1. WHEN the user opens reader settings, THE IReader Application SHALL keep the reading content visible behind the settings panel
2. WHEN no translation API key is set, THE IReader Application SHALL disable the translate button or prompt for API key setup
3. WHEN the user views the General tab in reader settings, THE IReader Application SHALL only show chapter-specific or book-specific settings
4. WHEN the user moves a slider in reader settings, THE IReader Application SHALL update the preview in real-time
5. WHEN the user closes reader settings, THE IReader Application SHALL persist all changes immediately

### Requirement 17: Reader View Navigation Features

**User Story:** As a user, I want multiple ways to navigate while reading, so that I can use the method most comfortable for me.

#### Acceptance Criteria

1. WHERE volume key navigation is enabled, THE IReader Application SHALL allow using volume keys to turn pages or scroll
2. WHEN the user taps the brightness icon, THE IReader Application SHALL display a brightness slider in the reader UI
3. WHEN the user enables "Keep Screen On", THE IReader Application SHALL prevent the device from sleeping during reading
4. WHEN the user taps the "Aa" button, THE IReader Application SHALL display only the font size slider for quick adjustments
5. WHEN autoscroll is enabled, THE IReader Application SHALL provide on-screen buttons to adjust scroll speed

### Requirement 18: Reader View Content Features

**User Story:** As a user, I want tools to interact with chapter content, so that I can search, bookmark, and report issues.

#### Acceptance Criteria

1. WHEN the user taps the search icon in reader, THE IReader Application SHALL provide a "Find in Chapter" function to search for text
2. WHEN the user taps the bookmark icon, THE IReader Application SHALL save the current scroll position
3. WHEN the user accesses bookmarks, THE IReader Application SHALL display a list of saved positions with chapter names
4. WHEN the user selects "Report Broken Chapter", THE IReader Application SHALL flag the chapter for review
5. WHEN the user views the reader, THE IReader Application SHALL display estimated time remaining to read the chapter

### Requirement 19: Reader View Translation Features

**User Story:** As a user, I want flexible translation options, so that I can read content in my preferred language.

#### Acceptance Criteria

1. WHERE bilingual mode is enabled, THE IReader Application SHALL display original and translated text side-by-side or paragraph-by-paragraph
2. WHEN the user long-presses a paragraph, THE IReader Application SHALL provide a "Translate" option in the context menu
3. WHEN the user translates a paragraph, THE IReader Application SHALL display the translation inline
4. WHEN translation fails, THE IReader Application SHALL display an error message with the reason
5. WHEN the user has no API key configured, THE IReader Application SHALL prompt them to configure translation settings

### Requirement 20: Reader View Customization

**User Story:** As a user, I want advanced customization options for the reader, so that I can create my ideal reading environment.

#### Acceptance Criteria

1. WHERE the user imports custom fonts, THE IReader Application SHALL allow selecting .ttf or .otf font files for the reader
2. WHEN the user selects a custom font, THE IReader Application SHALL apply it to all reading content
3. WHEN the user sets a default reading mode, THE IReader Application SHALL apply that mode to all newly opened books
4. WHEN the user views reading statistics, THE IReader Application SHALL display total chapters read and reading time
5. WHERE the user enables true black mode, THE IReader Application SHALL use pure black (#000000) for dark theme backgrounds

### Requirement 21: Settings Screen Bug Fixes

**User Story:** As a user, I want all settings features to work correctly without errors or typos, so that I can configure the application properly.

#### Acceptance Criteria

1. WHEN the user views the settings menu, THE IReader Application SHALL display "Advanced Settings" with correct spelling
2. WHEN the user taps "Create Backup", THE IReader Application SHALL open a file dialog to save the backup
3. WHEN the user taps "Export Theme", THE IReader Application SHALL open a file dialog to save the theme file
4. WHEN the user opens the Security settings page, THE IReader Application SHALL display security options instead of a blank screen
5. WHEN the user refreshes translation models with a valid API key, THE IReader Application SHALL load available models

### Requirement 22: Settings Screen Clarity Improvements

**User Story:** As a user, I want clearer labels and descriptions in settings, so that I understand what each option does.

#### Acceptance Criteria

1. WHEN the user views general settings, THE IReader Application SHALL display "Enable Reading History" instead of "Show History"
2. WHEN the user views general settings, THE IReader Application SHALL display "Show App Update Notifications" instead of "Show Update"
3. WHEN the user views catalog settings, THE IReader Application SHALL display helper text explaining each toggle
4. WHEN the user views the Repository settings page, THE IReader Application SHALL display a help icon with an explanation
5. WHEN empty settings pages are displayed, THE IReader Application SHALL show appropriate empty state text

### Requirement 23: Settings Screen Safety Features

**User Story:** As a user, I want dangerous actions to be clearly marked and require confirmation, so that I don't accidentally delete important data.

#### Acceptance Criteria

1. WHEN the user views Advanced Settings, THE IReader Application SHALL group destructive actions in a "Danger Zone" section with red styling
2. WHEN the user taps a destructive action, THE IReader Application SHALL display a confirmation dialog requiring typing "DELETE" or "RESET"
3. WHEN the user taps "Clear All Cache", THE IReader Application SHALL display the current cache size in the button label
4. WHEN the user taps "Check for Update", THE IReader Application SHALL display feedback ("Checking...", "Up to date", or "Update found")
5. WHEN the user taps "Clear All" in history, THE IReader Application SHALL display a confirmation dialog

### Requirement 24: Settings Screen Security Features

**User Story:** As a user, I want security options to protect my privacy, so that I can use the app safely in public.

#### Acceptance Criteria

1. WHERE app lock is enabled, THE IReader Application SHALL require PIN, password, or biometric authentication to open
2. WHERE secure screen is enabled, THE IReader Application SHALL block screenshots and screen recording
3. WHERE hide content is enabled, THE IReader Application SHALL blur library covers until tapped
4. WHERE 18+ source lock is enabled, THE IReader Application SHALL require authentication to access adult sources
5. WHEN the user sets up security, THE IReader Application SHALL provide clear instructions for each option

### Requirement 25: Settings Screen Advanced Features

**User Story:** As a user, I want advanced settings features for customization and management, so that I can tailor the app to my needs.

#### Acceptance Criteria

1. WHEN the user views translation settings, THE IReader Application SHALL provide a "Test Connection" button to verify the API key
2. WHEN the user views download queue, THE IReader Application SHALL display download speed and estimated time remaining
3. WHEN the user views download queue, THE IReader Application SHALL allow dragging items to change priority
4. WHEN a download fails, THE IReader Application SHALL provide a "Retry" button
5. WHEN the user enables automatic backups, THE IReader Application SHALL create backups daily or weekly as configured

### Requirement 26: Book Detail Screen Enhancements

**User Story:** As a user, I want more control over book information and chapters, so that I can manage individual books effectively.

#### Acceptance Criteria

1. WHEN the user taps the edit button on a book detail page, THE IReader Application SHALL allow editing title, author, and cover URL
2. WHEN the user views the chapter list, THE IReader Application SHALL provide a toggle to hide read chapters
3. WHEN the user views the chapter list, THE IReader Application SHALL provide a filter to hide duplicate chapters
4. WHEN the user opens the chapter menu, THE IReader Application SHALL provide options to download all unread or all un-downloaded chapters
5. WHEN the user saves edits to book info, THE IReader Application SHALL persist the changes and update the library display

### Requirement 27: Download Management Features

**User Story:** As a user, I want comprehensive download management, so that I can control what content is stored on my device.

#### Acceptance Criteria

1. WHEN the user views the download queue, THE IReader Application SHALL display a "Completed" tab showing download history
2. WHEN the user selects multiple books in the library, THE IReader Application SHALL provide a "Download all unread chapters" option
3. WHEN downloads are in progress, THE IReader Application SHALL display current download speed
4. WHEN the user changes download priority, THE IReader Application SHALL reorder the queue immediately
5. WHEN a download completes, THE IReader Application SHALL display a notification with the chapter name

### Requirement 28: Category Management Enhancements

**User Story:** As a user, I want full category management capabilities, so that I can organize my library exactly how I want.

#### Acceptance Criteria

1. WHEN the user views the Categories page, THE IReader Application SHALL provide options to rename or delete categories
2. WHEN the user deletes a category, THE IReader Application SHALL ask what to do with books in that category
3. WHEN the user renames a category, THE IReader Application SHALL update all references immediately
4. WHEN the user creates a category, THE IReader Application SHALL validate that the name is unique
5. WHEN the user views categories, THE IReader Application SHALL display the number of books in each category

### Requirement 29: Advanced User Features

**User Story:** As a user, I want advanced features like statistics and remote reading, so that I can enhance my reading experience.

#### Acceptance Criteria

1. WHEN the user opens the Statistics page, THE IReader Application SHALL display total chapters read, reading time, and favorite genres
2. WHERE WebUI is enabled, THE IReader Application SHALL host a web server for remote reading from a computer browser
3. WHEN the user views the About section, THE IReader Application SHALL provide a "What's New" link showing the changelog
4. WHEN the user enables automatic backups, THE IReader Application SHALL link to cloud storage or local folder
5. WHEN the user views app information, THE IReader Application SHALL display the current version with build number

### Requirement 30: Appearance Customization

**User Story:** As a user, I want extensive appearance customization options, so that I can make the app look exactly how I want.

#### Acceptance Criteria

1. WHERE true black AMOLED mode is enabled, THE IReader Application SHALL use pure black backgrounds to save power on AMOLED screens
2. WHEN the user changes app UI font, THE IReader Application SHALL apply the font to all interface elements except the reader
3. WHEN the user customizes colors, THE IReader Application SHALL provide a live preview of changes
4. WHEN the user exports a theme, THE IReader Application SHALL create a shareable theme file
5. WHEN the user imports a theme, THE IReader Application SHALL validate and apply the theme configuration

