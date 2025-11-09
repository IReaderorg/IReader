# Implementation Plan

## Overview

This implementation plan breaks down the advanced reader features into discrete, actionable coding tasks. Tasks are ordered by priority: reliability features first (Auto-Chapter Repair, Smart Source Switching), followed by AI enhancements, reading experience improvements, lifecycle management, visual enhancements, and monetization features. Each task builds incrementally on previous work.

---

## Tasks

- [ ] 1. Implement Auto-Chapter Repair System
  - Create `ChapterHealthChecker` class with detection logic for broken chapters (word count < 50, empty content, scrambled text)
  - Implement `AutoRepairChapterUseCase` to search all installed sources for working chapter replacements
  - Add `ChapterHealth` database table and repository methods for tracking broken chapters
  - Integrate health checking into `ReaderViewModel.loadChapter()` method
  - Create repair banner UI component that displays when broken chapter is detected
  - Add caching mechanism to avoid repeated repair attempts (24-hour cache)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9_

- [ ] 2. Implement Smart Source Switching
  - Create `CheckSourceAvailabilityUseCase` to compare chapter counts across installed sources
  - Implement `SourceComparison` data model and caching repository (24-hour TTL)
  - Add background source checking when user opens novel detail page
  - Create `SourceSwitchingBanner` composable with "Switch" and "Dismiss" buttons
  - Implement `MigrateToSourceUseCase` to handle source migration with progress tracking
  - Add dismiss logic that prevents banner from showing again for 7 days
  - Wire banner into `NovelDetailScreen` and `ReaderScreen`
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9_


- [ ] 4. Implement True Read-Along TTS
  - Create `TTSReadAlongManager` class to handle word boundary callbacks from TTS engine
  - Implement word highlighting logic using `AnnotatedString` with background color spans
  - Build `ReadAlongText` composable that highlights current word being spoken
  - Add auto-scroll logic with `TTSAutoScroller` class to keep current word visible
  - Implement user scroll detection to pause auto-scrolling for 5 seconds
  - Add tap-to-jump functionality allowing users to tap a word to jump TTS playback
  - Integrate with existing TTS service and handle platform differences (Android/Desktop)
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_



- [ ] 6. Implement Reading Break Reminder System
  - Create `ReadingTimerManager` class with start, pause, resume, and reset methods
  - Add reading break interval preference to settings (30, 45, 60, 90, 120 minutes)
  - Implement timer logic that tracks continuous reading time
  - Create `ReadingBreakReminderDialog` with "Take a Break" and "Continue Reading" buttons
  - Add timer state persistence to handle app restarts
  - Implement sentence boundary detection to avoid interrupting mid-sentence
  - Wire timer into `ReaderViewModel` lifecycle (start on chapter open, pause on close)
  - Add auto-dismiss logic (15 seconds) if user doesn't respond to reminder
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 12.1, 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8_

- [ ] 7. Implement Resume Last Read Feature
  - Create `GetLastReadNovelUseCase` that queries reading history for most recent chapter
  - Implement `LastReadInfo` data model with novel details, chapter info, and progress
  - Build `ResumeReadingCard` composable with cover image, title, chapter, and progress bar
  - Add card to top of `LibraryScreen` with one-tap navigation to last read chapter
  - Implement logic to hide card when no reading history exists
  - Add automatic update when user finishes a chapter to show next chapter
  - Handle novel switching to always show most recently read novel
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7_

- [ ] 8. Implement End of Life Management - Archive
  - Create `ArchiveNovelUseCase` to move completed novels to archived category
  - Add "Archived" category to database and category management system
  - Implement `EndOfLifeOptionsDialog` that appears when user marks novel as completed
  - Add "Show Archived" toggle to library settings
  - Create visual indicator (badge/icon) for archived novels
  - Implement "Unarchive" functionality to restore novels to original category
  - Wire dialog into novel detail screen's "Mark as Completed" action
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7_

- [ ] 9. Implement End of Life Management - ePub Export
  - Create `EpubGenerator` class to build ePub file structure (META-INF, OEBPS, mimetype)
  - Implement HTML content cleaning to remove scripts, styles, ads, and watermarks
  - Build `ExportNovelAsEpubUseCase` to fetch all chapters and generate ePub
  - Add ePub metadata generation (title, author, cover, table of contents)
  - Implement chapter-to-XHTML conversion with proper formatting
  - Create toc.ncx file for navigation
  - Add file picker integration to save ePub to user-selected location
  - Show progress dialog during export with chapter count progress
  - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.9, 15.10_


- [ ] 11. Implement Cryptocurrency Donation Page
  - Create `DonationScreen` composable with wallet addresses for Bitcoin, Ethereum, Litecoin
  - Add QR code generation for each wallet address using ZXing library
  - Implement copy-to-clipboard functionality with toast confirmation
  - Build explanation section describing why donations are needed (server costs, APIs, development)
  - Add navigation from Settings to DonationScreen via "Support Development" menu item
  - Create wallet address configuration in app constants or remote config
  - Implement QR code enlargement on tap for easier scanning
  - Add cryptocurrency disclaimer about non-refundable donations
  - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5, 18.6, 18.7, 18.8_

- [ ] 12. Implement Donation Trigger System
  - Create `DonationTrigger` sealed class for different trigger events
  - Implement `DonationTriggerManager` to check for trigger conditions
  - Add trigger detection for book completion (500+ chapters)
  - Add trigger detection for first successful source migration
  - Add trigger detection for chapter milestones (every 1,000 chapters)
  - Create `DonationPromptDialog` with contextual messages for each trigger
  - Implement 30-day cooldown between prompts to avoid spam
  - Add "Donate Now" and "Maybe Later" buttons with proper navigation
  - Store last prompt time in preferences to enforce cooldown
  - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6, 19.7, 19.8, 19.9_

- [ ] 13. Implement Cryptocurrency Wallet Integration
  - Create `WalletIntegrationManager` class for deep link handling
  - Implement deep links for Trust Wallet, MetaMask, and Coinbase Wallet
  - Add "Pay with [Wallet]" buttons to donation screen for each supported wallet
  - Implement wallet app detection and "not installed" error handling
  - Create payment URI generation for cryptocurrency addresses
  - Add QR code display for desktop users to scan with mobile wallets
  - Implement hover tooltips with scanning instructions
  - Handle ActivityNotFoundException gracefully with user-friendly messages
  - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.5, 20.6, 20.7_

- [ ] 14. Implement Fund-a-Feature Progress System
  - Create `FundingGoal` data model with title, description, target, and current amount
  - Build `FundaFeatureSection` composable with progress bar and goal description
  - Add funding goal configuration (monthly server costs, feature-specific goals)
  - Implement progress bar UI showing current/target with percentage
  - Add "Goal Reached!" state when target is met
  - Create automatic goal rollover for monthly recurring goals
  - Implement goal detail dialog showing feature description and funding purpose
  - Add manual goal update mechanism (admin-controlled or remote config)
  - _Requirements: 21.1, 21.2, 21.3, 21.4, 21.5, 21.6, 21.7, 21.8_

- [ ] 15. Implement Supporter Badge Verification
  - Create `SupporterBadgeVerificationDialog` with TXID and User ID input fields
  - Implement `SubmitSupporterVerificationUseCase` to send data to Supabase
  - Add "I've donated! Get my badge" button to donation screen
  - Create Supabase table for verification submissions with pending status
  - Build verification form validation (non-empty fields, valid format)
  - Implement submission success message with 24-hour review timeline
  - Add error handling for network failures and invalid submissions
  - Create instructions showing where to find User ID in About page
  - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 22.7_

- [ ] 16. Implement Supporter Badge Display and Features
  - Create `SupporterStatus` data model with isSupporter flag and tier
  - Add `is_supporter` field to user profile in Supabase
  - Implement supporter status sync from Supabase on app launch
  - Add 💖 supporter icon next to username in About section
  - Display "Supporter" badge on user reviews and comments
  - Create "Supporter Themes" section in Appearance settings
  - Implement 3-5 exclusive supporter themes (Gold, Platinum, Diamond)
  - Add "Supporter since [Date]" label to user profile
  - Implement theme locking/unlocking based on supporter status
  - _Requirements: 23.1, 23.2, 23.3, 23.4, 23.5, 23.6, 23.7, 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7_

- [ ] 17. Add Feature Flags and Configuration
  - Create `FeatureFlags` object with toggles for all new features
  - Implement remote config integration for feature flag control
  - Add feature flag checks before showing new UI components
  - Create admin interface or config file for flag management
  - Implement gradual rollout capability for risky features (voice commands)
  - Add analytics events for feature usage tracking
  - _Requirements: All_




- [ ] 20. Create Settings and Configuration UI
  - Add "Reading Break Reminder" toggle and interval selector to Reader settings
  - Implement "Enable Dynamic Theme" toggle in Appearance settings
  - Add "Support Development" menu item to main Settings screen
  - Create glossary management screen accessible from Reader settings
  - Add "Show Archived" toggle to Library settings
  - Implement test interface for voice commands
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 11.1, 11.2, 17.7_

- [ ] 21. Database Migrations and Schema Updates
  - Create migration for `ChapterHealth` table
  - Create migration for `SourceComparisonCache` table
  - Create migration for `GlossaryEntry` table
  - Create migration for `ReadingTimerState` table
  - Create migration for `SupporterStatus` table
  - Add indexes for performance optimization
  - Implement migration rollback procedures
  - Test migrations on existing databases
  - _Requirements: All database-related_

- [ ] 22. Platform-Specific Implementations
  - Implement Android-specific TTS word boundary callbacks
  - Create Desktop TTS implementation with word timing estimation
  - Add Android deep link handling for wallet apps
  - Implement Desktop QR code display for wallet integration
  - _Requirements: All platform-specific requirements_

- [ ] 23. Integration and Wiring
  - Wire `ChapterHealthChecker` into `ReaderViewModel.loadChapter()`
  - Integrate `CheckSourceAvailabilityUseCase` into `NovelDetailScreen`
  - Add glossary context menu to reader long-press handler
  - Wire `TTSReadAlongManager` into existing TTS service
  - Integrate `VoiceCommandManager` with TTS activation
  - Add `ReadingTimerManager` to `ReaderViewModel` lifecycle
  - Wire `EndOfLifeOptionsDialog` into "Mark as Completed" action
  - Integrate `DonationTriggerManager` into relevant user events
  - Connect all use cases to repositories via Koin DI
  - _Requirements: All integration requirements_

---

## Notes

- Tasks are ordered by priority: reliability → AI → reading experience → lifecycle → visual → monetization
- Each task includes specific implementation details and references to requirements
- Testing tasks are marked as optional (*) to focus on core functionality first
- Platform-specific implementations should be tested on both Android and Desktop
- Feature flags allow for gradual rollout and easy rollback if needed
- Caching and performance optimizations are critical for good user experience
- Error handling should be comprehensive with user-friendly messages
- Documentation is essential for user adoption and developer maintenance
