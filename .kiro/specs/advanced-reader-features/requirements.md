# Requirements Document

## Introduction

This specification covers a comprehensive set of advanced features for the IReader application designed to significantly enhance user experience, reliability, and monetization. The features are prioritized by impact, starting with critical reliability improvements (Auto-Chapter Repair, Smart Source Switching), followed by AI-powered enhancements (Custom AI Glossary), reading experience improvements (TTS Read-Along, Voice Commands, Reading Break Reminders), lifecycle management (End of Life Management), visual enhancements (Dynamic Reader Theme), and monetization features (Cryptocurrency Donations, Fund-a-Feature, Supporter Badges).

These features address key pain points: broken chapters that disrupt reading flow, inconsistent translations, lack of hands-free reading options, and the need for sustainable revenue in restricted markets.

## Glossary

- **IReader Application**: The main Kotlin Multiplatform application for reading novels
- **Source**: An extension or plugin that provides novel content from a specific website
- **Chapter**: A single unit of novel content
- **Novel**: A book or story composed of multiple chapters
- **TTS**: Text-to-Speech engine that reads text aloud
- **AI Translation Engine**: The service used to translate novel content (e.g., Google Translate, DeepL)
- **Glossary Entry**: A user-defined mapping of translation variants to a preferred term
- **Read-Along**: Synchronized highlighting of text as it is spoken by TTS
- **ePub**: Electronic Publication format for portable e-books
- **Transaction Hash (TXID)**: A unique identifier for a blockchain transaction
- **Supporter Badge**: A cosmetic indicator showing a user has donated
- **Fund-a-Feature**: A crowdfunding mechanism for specific app features
- **Repository**: A source of extension packages

## Requirements

### Requirement 1: Auto-Chapter Repair Detection

**User Story:** As a reader, I want the app to automatically detect broken chapters, so that I don't waste time trying to read empty or corrupted content.

#### Acceptance Criteria

1. WHEN THE user opens a chapter, THE IReader Application SHALL count the number of words in the chapter content
2. WHEN THE word count is less than 50, THE IReader Application SHALL mark the chapter as potentially broken
3. WHEN THE chapter content contains only whitespace or special characters, THE IReader Application SHALL mark the chapter as potentially broken
4. WHEN THE chapter returns a 404 error, THE IReader Application SHALL mark the chapter as broken
5. WHEN THE chapter content is scrambled (detected by high ratio of non-alphabetic characters), THE IReader Application SHALL mark the chapter as potentially broken
6. WHEN THE chapter is marked as broken, THE IReader Application SHALL log the detection event with chapter ID, source ID, and detection reason
7. WHEN THE chapter is marked as broken, THE IReader Application SHALL display a warning banner to the user stating "This chapter appears to be broken or empty"

### Requirement 2: Auto-Chapter Repair Search and Replace

**User Story:** As a reader, I want the app to automatically find and load working versions of broken chapters from other pinned sources, so that I can continue reading without manual intervention.

#### Acceptance Criteria

1. WHEN THE chapter is marked as broken, THE IReader Application SHALL invoke AutoRepairChapterUseCase with the chapter number and novel title
2. WHEN THE AutoRepairChapterUseCase executes, THE IReader Application SHALL query all installed sources for the same novel
3. WHEN THE novel is found in another source, THE IReader Application SHALL fetch the same chapter number from that source
4. WHEN THE fetched chapter has more than 50 words, THE IReader Application SHALL consider it a valid replacement
5. WHEN THE valid replacement is found, THE IReader Application SHALL display a notification "Found working chapter from [Source Name]. Loading..."
6. WHEN THE valid replacement is loaded, THE IReader Application SHALL replace the broken chapter content with the working content
7. WHEN THE replacement succeeds, THE IReader Application SHALL log the repair event with original source, replacement source, and chapter ID
8. WHEN THE no valid replacement is found after searching all sources, THE IReader Application SHALL display "Unable to find working chapter from other sources"
9. WHEN THE user dismisses the broken chapter warning, THE IReader Application SHALL not attempt auto-repair again for that chapter until next app restart

### Requirement 3: Smart Source Switching Detection

**User Story:** As a reader, I want the app to notify me when another pinned source has more chapters available, so that I can switch sources and continue reading.

#### Acceptance Criteria

1. WHEN THE user opens a novel detail page, THE IReader Application SHALL invoke CheckSourceAvailabilityUseCase with the novel title
2. WHEN THE CheckSourceAvailabilityUseCase executes, THE IReader Application SHALL query all installed sources for the same novel
3. WHEN THE novel is found in multiple sources, THE IReader Application SHALL compare the total chapter count across sources
4. WHEN THE another source has at least 5 more chapters than the current source, THE IReader Application SHALL mark it as a better source
5. WHEN THE better source is found, THE IReader Application SHALL calculate the chapter difference
6. WHEN THE chapter difference is calculated, THE IReader Application SHALL cache the result for 24 hours to avoid repeated checks
7. WHEN THE user is reading from the current source, THE IReader Application SHALL check for better sources in the background

### Requirement 4: Smart Source Switching Banner

**User Story:** As a reader, I want a non-intrusive notification about better pinned sources, so that I can decide whether to switch without disrupting my reading.

#### Acceptance Criteria

1. WHEN THE better source is detected, THE IReader Application SHALL display a banner at the top of the reader view
2. WHEN THE banner is displayed, THE IReader Application SHALL show the message "Source [Name] has [X] new chapters. Switch now?"
3. WHEN THE banner is displayed, THE IReader Application SHALL provide two buttons: "Switch" and "Dismiss"
4. WHEN THE user taps "Switch", THE IReader Application SHALL invoke MigrateToSourceUseCase with the target source
5. WHEN THE migration starts, THE IReader Application SHALL display a progress indicator
6. WHEN THE migration completes, THE IReader Application SHALL update the novel's source reference and reload the chapter list
7. WHEN THE user taps "Dismiss", THE IReader Application SHALL hide the banner and not show it again for this novel for 7 days
8. WHEN THE banner is displayed, THE IReader Application SHALL auto-dismiss after 10 seconds if no action is taken
9. WHEN THE banner is dismissed automatically, THE IReader Application SHALL show it again the next time the user opens the novel


### Requirement 7: True Read-Along TTS Word Highlighting

**User Story:** As a reader, I want the exact word being spoken to be highlighted, so that I can follow along visually while listening.

#### Acceptance Criteria

1. WHEN THE TTS is active and speaking, THE IReader Application SHALL highlight the current word being spoken
2. WHEN THE TTS engine provides word boundary callbacks, THE IReader Application SHALL use them to determine the current word
3. WHEN THE word boundary callback is not available, THE IReader Application SHALL estimate word timing based on speech rate
4. WHEN THE current word is highlighted, THE IReader Application SHALL apply a distinct background color or underline
5. WHEN THE TTS moves to the next word, THE IReader Application SHALL remove the highlight from the previous word
6. WHEN THE TTS pauses, THE IReader Application SHALL maintain the highlight on the last spoken word
7. WHEN THE TTS resumes, THE IReader Application SHALL continue highlighting from the paused position
8. WHEN THE user taps a word while TTS is active, THE IReader Application SHALL jump TTS playback to that word

### Requirement 8: True Read-Along TTS Auto-Scroll

**User Story:** As a reader, I want the page to automatically scroll as TTS reads, so that the current word is always visible.

#### Acceptance Criteria

1. WHEN THE TTS is speaking and the current word is near the bottom of the screen, THE IReader Application SHALL scroll down to keep the word centered
2. WHEN THE TTS reaches the end of the visible area, THE IReader Application SHALL scroll smoothly to the next section
3. WHEN THE TTS moves to a new paragraph, THE IReader Application SHALL ensure the paragraph is fully visible
4. WHEN THE TTS reaches the end of the chapter, THE IReader Application SHALL stop auto-scrolling
5. WHEN THE user manually scrolls while TTS is active, THE IReader Application SHALL pause auto-scrolling for 5 seconds
6. WHEN THE 5-second pause expires, THE IReader Application SHALL resume auto-scrolling to the current word
7. WHEN THE user disables auto-scroll in TTS settings, THE IReader Application SHALL highlight words without scrolling


### Requirement 11: Reading Break Reminder Configuration

**User Story:** As a reader, I want to set reading break reminders, so that I can maintain healthy reading habits.

#### Acceptance Criteria

1. WHEN THE user opens Reader Settings, THE IReader Application SHALL display a "Reading Break Reminder" toggle
2. WHEN THE Reading Break Reminder is enabled, THE IReader Application SHALL display interval options: 30, 45, 60, 90, and 120 minutes
3. WHEN THE user selects an interval, THE IReader Application SHALL save the preference
4. WHEN THE user starts reading, THE IReader Application SHALL start a timer based on the selected interval
5. WHEN THE user closes the reader, THE IReader Application SHALL pause the timer
6. WHEN THE user reopens the reader, THE IReader Application SHALL resume the timer from the paused state
7. WHEN THE app is closed, THE IReader Application SHALL reset the timer

### Requirement 12: Reading Break Reminder Notification

**User Story:** As a reader, I want gentle reminders to take breaks, so that I can rest my eyes without losing my place.

#### Acceptance Criteria

1. WHEN THE reading timer reaches the configured interval, THE IReader Application SHALL display a non-intrusive pop-up
2. WHEN THE pop-up is displayed, THE IReader Application SHALL show the message "You've been reading for [X] minutes. Time to stretch your eyes!"
3. WHEN THE pop-up is displayed, THE IReader Application SHALL provide buttons: "Take a Break" and "Continue Reading"
4. WHEN THE user taps "Take a Break", THE IReader Application SHALL pause TTS if active and dim the screen
5. WHEN THE user taps "Continue Reading", THE IReader Application SHALL dismiss the pop-up and reset the timer
6. WHEN THE pop-up is displayed, THE IReader Application SHALL auto-dismiss after 15 seconds if no action is taken
7. WHEN THE pop-up auto-dismisses, THE IReader Application SHALL reset the timer and continue tracking
8. WHEN THE user is in the middle of a sentence, THE IReader Application SHALL wait until the sentence ends before showing the reminder

### Requirement 13: Resume Last Read Button

**User Story:** As a reader, I want a quick way to resume my last read chapter, so that I can continue reading with one tap.

#### Acceptance Criteria

1. WHEN THE user opens the Library screen, THE IReader Application SHALL display a "Resume" button or card at the top
2. WHEN THE Resume button is displayed, THE IReader Application SHALL show the novel cover, title, and last read chapter
3. WHEN THE Resume button is displayed, THE IReader Application SHALL show reading progress percentage
4. WHEN THE user taps the Resume button, THE IReader Application SHALL open the last read chapter at the last scroll position
5. WHEN THE user has not read any chapters, THE IReader Application SHALL hide the Resume button
6. WHEN THE user finishes a chapter, THE IReader Application SHALL update the Resume button to show the next chapter
7. WHEN THE user switches between novels, THE IReader Application SHALL update the Resume button to show the most recently read novel

### Requirement 14: End of Life Archive Option

**User Story:** As a reader, I want to archive completed novels, so that they don't clutter my active library but remain accessible.

#### Acceptance Criteria

1. WHEN THE user marks a novel as "Completed", THE IReader Application SHALL display an "End of Life Options" dialog
2. WHEN THE dialog is displayed, THE IReader Application SHALL provide two options: "Archive" and "Download as ePub"
3. WHEN THE user selects "Archive", THE IReader Application SHALL move the novel to an "Archived" category
4. WHEN THE novel is archived, THE IReader Application SHALL hide it from the main library view
5. WHEN THE user enables "Show Archived", THE IReader Application SHALL display archived novels with a distinct visual indicator
6. WHEN THE user opens an archived novel, THE IReader Application SHALL provide an "Unarchive" option
7. WHEN THE user unarchives a novel, THE IReader Application SHALL restore it to its original category

### Requirement 15: End of Life ePub Export

**User Story:** As a reader, I want to export completed novels as ePub files, so that I can read them on other devices or preserve them permanently.

#### Acceptance Criteria

1. WHEN THE user selects "Download as ePub" from the End of Life dialog, THE IReader Application SHALL invoke ExportNovelAsEpubUseCase
2. WHEN THE ExportNovelAsEpubUseCase executes, THE IReader Application SHALL fetch all chapters for the novel
3. WHEN THE chapters are fetched, THE IReader Application SHALL clean up HTML formatting and remove ads or watermarks
4. WHEN THE content is cleaned, THE IReader Application SHALL generate ePub metadata including title, author, cover image, and chapter list
5. WHEN THE metadata is generated, THE IReader Application SHALL create an ePub file structure with OEBPS and META-INF directories
6. WHEN THE ePub structure is created, THE IReader Application SHALL write all chapters as XHTML files
7. WHEN THE chapters are written, THE IReader Application SHALL generate a table of contents (toc.ncx)
8. WHEN THE ePub is complete, THE IReader Application SHALL save it to the user's Downloads folder or selected location
9. WHEN THE export succeeds, THE IReader Application SHALL display "ePub exported successfully: [filename]"
10. WHEN THE export fails, THE IReader Application SHALL display an error message with the failure reason

### Requirement 16: Dynamic Reader Theme Color Extraction

**User Story:** As a reader, I want the book detail screen theme to match each book's cover art, so that every book feels unique and immersive.

#### Acceptance Criteria

1. WHEN THE user opens a chapter, THE IReader Application SHALL extract the dominant colors from the book's cover image
2. WHEN THE color extraction executes, THE IReader Application SHALL use a palette extraction algorithm to identify primary, secondary, and accent colors
3. WHEN THE colors are extracted, THE IReader Application SHALL select the most vibrant color as the accent color
4. WHEN THE accent color is too bright or too dark, THE IReader Application SHALL adjust it to ensure readability
5. WHEN THE colors are selected, THE IReader Application SHALL cache them for the novel to avoid repeated extraction
6. WHEN THE cover image is not available, THE IReader Application SHALL use default theme colors
7. WHEN THE color extraction fails, THE IReader Application SHALL fall back to default theme colors


### Requirement 18: Cryptocurrency Donation Page

**User Story:** As a developer, I want to accept cryptocurrency donations, so that I can receive international support despite banking restrictions.

#### Acceptance Criteria

1. WHEN THE user navigates to Settings, THE IReader Application SHALL display a "Support Development" menu item
2. WHEN THE user taps "Support Development", THE IReader Application SHALL navigate to the DonationScreen
3. WHEN THE DonationScreen loads, THE IReader Application SHALL display an explanation: "Support IReader development to cover server costs for sync, AI translation APIs, and ongoing development"
4. WHEN THE DonationScreen is displayed, THE IReader Application SHALL show wallet addresses for Bitcoin, Ethereum, and Litecoin
5. WHEN THE user taps a wallet address, THE IReader Application SHALL copy it to the clipboard
6. WHEN THE address is copied, THE IReader Application SHALL display a toast message "Address copied to clipboard"
7. WHEN THE DonationScreen is displayed, THE IReader Application SHALL show QR codes for each wallet address
8. WHEN THE user taps a QR code, THE IReader Application SHALL enlarge it for easier scanning

### Requirement 19: Cryptocurrency Donation Triggers

**User Story:** As a developer, I want to request donations at moments of maximum user satisfaction, so that users are more likely to contribute.

#### Acceptance Criteria

1. WHEN THE user marks a novel with 500+ chapters as "Completed", THE IReader Application SHALL display a donation prompt
2. WHEN THE donation prompt is displayed after completing a book, THE IReader Application SHALL show the message "Congratulations on finishing the novel! If IReader made this 500-chapter journey better, please consider a small crypto donation to support development."
3. WHEN THE user successfully uses the "Migrate Source" feature for the first time, THE IReader Application SHALL display a donation prompt
4. WHEN THE donation prompt is displayed after migration, THE IReader Application SHALL show the message "Migration Complete! Saved you a headache, right? 😉 If you find these power-features useful, please consider supporting the app."
5. WHEN THE user reads their 1,000th chapter, THE IReader Application SHALL display a donation prompt
6. WHEN THE donation prompt is displayed after 1,000 chapters, THE IReader Application SHALL show the message "You've read 1,000 chapters! That's amazing. To help us build the app for the next 1,000, please consider donating."
7. WHEN THE donation prompt is displayed, THE IReader Application SHALL provide buttons: "Donate Now" and "Maybe Later"
8. WHEN THE user taps "Donate Now", THE IReader Application SHALL navigate to the DonationScreen
9. WHEN THE user taps "Maybe Later", THE IReader Application SHALL dismiss the prompt and not show it again for 30 days

### Requirement 20: Cryptocurrency Wallet Integration

**User Story:** As a user, I want easy donation with wallet integration, so that I can donate in 3 clicks without copy-pasting addresses.

#### Acceptance Criteria

1. WHEN THE DonationScreen is displayed on Android, THE IReader Application SHALL provide "Pay with Trust Wallet" and "Pay with MetaMask" buttons
2. WHEN THE user taps a wallet button, THE IReader Application SHALL use deep links to open the wallet app with the address pre-filled
3. WHEN THE wallet app is not installed, THE IReader Application SHALL display "Wallet app not found. Please install [Wallet Name] or copy the address manually."
4. WHEN THE DonationScreen is displayed on Desktop, THE IReader Application SHALL display a QR code for mobile wallet scanning
5. WHEN THE user hovers over a QR code, THE IReader Application SHALL display instructions: "Scan with your mobile crypto wallet"
6. WHEN THE user taps "Generate Payment Link", THE IReader Application SHALL create a cryptocurrency payment URI
7. WHEN THE payment URI is generated, THE IReader Application SHALL open it in the default handler or display it for copying

### Requirement 21: Fund-a-Feature Progress Bar

**User Story:** As a user, I want to see funding progress for specific features, so that I can contribute to features I want developed.

#### Acceptance Criteria

1. WHEN THE DonationScreen is displayed, THE IReader Application SHALL show a "Fund-a-Feature" section
2. WHEN THE Fund-a-Feature section is displayed, THE IReader Application SHALL show the current funding goal with a progress bar
3. WHEN THE progress bar is displayed, THE IReader Application SHALL show the format: "Monthly Goal: $50 / $100 [Progress Bar] 50%"
4. WHEN THE funding goal is displayed, THE IReader Application SHALL show the purpose: "Our Supabase backend and AI translation APIs have real costs. Help us cover the server bill for this month to keep real-time sync and AI summaries free for everyone!"
5. WHEN THE funding goal is reached, THE IReader Application SHALL display "Goal Reached! Thank you for your support!"
6. WHEN THE funding goal is reached, THE IReader Application SHALL automatically create a new goal for the next month
7. WHEN THE user views Fund-a-Feature, THE IReader Application SHALL display one-time feature goals with specific funding targets
8. WHEN THE user taps a feature goal, THE IReader Application SHALL show details about the feature and why funding is needed

### Requirement 22: Supporter Badge Verification

**User Story:** As a donor, I want to receive a supporter badge, so that I can show my support and be recognized in the community.

#### Acceptance Criteria

1. WHEN THE DonationScreen is displayed, THE IReader Application SHALL show a button "I've donated! Get my badge"
2. WHEN THE user taps "Get my badge", THE IReader Application SHALL open the SupporterBadgeVerificationDialog
3. WHEN THE verification dialog opens, THE IReader Application SHALL display input fields for Transaction Hash (TXID) and User ID
4. WHEN THE user views the dialog, THE IReader Application SHALL show instructions: "Enter your transaction hash from the blockchain and your user ID from the About page"
5. WHEN THE user submits the verification form, THE IReader Application SHALL send the data to Supabase for manual approval
6. WHEN THE submission succeeds, THE IReader Application SHALL display "Verification submitted! Your badge will be activated within 24 hours after manual review."
7. WHEN THE submission fails, THE IReader Application SHALL display an error message with the failure reason

### Requirement 23: Supporter Badge Display

**User Story:** As a supporter, I want my badge to be visible in the app, so that others can see my contribution.

#### Acceptance Criteria

1. WHEN THE user's supporter status is activated in Supabase, THE IReader Application SHALL set is_supporter = true in the user profile
2. WHEN THE user opens the app, THE IReader Application SHALL sync the supporter status from Supabase
3. WHEN THE supporter status is true, THE IReader Application SHALL display a 💖 icon next to the user's name in the About section
4. WHEN THE supporter writes a review or comment, THE IReader Application SHALL display a "Supporter" badge next to their username
5. WHEN THE supporter opens Appearance settings, THE IReader Application SHALL unlock a "Supporter Themes" section
6. WHEN THE Supporter Themes section is displayed, THE IReader Application SHALL show exclusive color themes available only to supporters
7. WHEN THE supporter selects a supporter theme, THE IReader Application SHALL apply it to the entire app

### Requirement 24: Supporter Badge Cosmetic Rewards

**User Story:** As a supporter, I want exclusive cosmetic rewards, so that I feel appreciated for my contribution.

#### Acceptance Criteria

1. WHEN THE supporter status is active, THE IReader Application SHALL unlock 3-5 exclusive color themes
2. WHEN THE supporter opens the app, THE IReader Application SHALL display a subtle "Thank you for supporting IReader!" message once
3. WHEN THE supporter views their profile, THE IReader Application SHALL display a "Supporter since [Date]" label
4. WHEN THE supporter themes are displayed, THE IReader Application SHALL show theme names like "Supporter Gold", "Supporter Platinum", and "Supporter Diamond"
5. WHEN THE supporter selects a theme, THE IReader Application SHALL apply unique accent colors and gradients
6. WHEN THE supporter status expires or is revoked, THE IReader Application SHALL revert to standard themes
7. WHEN THE supporter views the donation page, THE IReader Application SHALL display "You are a supporter! Thank you for your contribution."

## Constraints

- The implementation MUST use existing architecture patterns (MVVM, Clean Architecture)
- The implementation MUST support both Android and Desktop platforms where applicable
- The implementation MUST handle errors gracefully with user-friendly messages
- The implementation MUST not break existing functionality
- The implementation MUST use coroutines for asynchronous operations
- The implementation MUST properly dispose of resources and cancel jobs when screens are destroyed
- The implementation MUST respect user privacy and not collect personal data without consent
- The implementation MUST store cryptocurrency wallet addresses securely
- The implementation MUST validate all user inputs to prevent injection attacks
- The implementation MUST use existing DI framework (Koin) for dependency injection
- The implementation MUST use existing navigation framework (Voyager) for screen navigation
- The implementation MUST follow Kotlin coding conventions and best practices
- The implementation MUST include proper null safety checks
- The implementation MUST use existing string resources or add new ones to i18n
- The implementation MUST be performant and not block the UI thread
- The implementation MUST cache expensive operations (color extraction, source checks) appropriately
- The implementation MUST handle network failures gracefully for cloud-dependent features
- The implementation MUST support offline functionality where possible
- The implementation MUST comply with cryptocurrency regulations and display appropriate disclaimers
- The implementation MUST not store private keys or sensitive financial information
