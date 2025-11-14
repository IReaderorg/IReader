# Implementation Plan

- [x] 1. Fix Settings UI Components (Sliders, Scrolling, Repository)








  - Update SliderPreference component to show real-time value changes during drag with proper formatting
  - Apply slider fix to Reader Settings (font size, line height) and General Settings (download concurrent)
  - Create desktop-specific scroll modifiers with mouse wheel and trackpad support
  - Apply smooth scrolling to Appearance Settings and all settings screens
  - Redesign AddRepositoryScreen with Material Design 3 components, validation, and loading states
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 2. Implement Download Features








  - Fix download logic to properly store chapters locally with progress tracking
  - Add download success/failure notifications with user-friendly error messages
  - Respect concurrent download limit setting
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 3. Implement Security and Font Management






  - Create font import functionality with file picker and TTF/OTF validation
  - Implement font storage, retrieval, and display in Font Settings screen
  - Implement password setup dialog with confirmation field and validation
  - Integrate platform keystore for secure password storage
  - Update Security Settings screen to handle complete password setup flow
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5_
-

- [x] 4. Enhance Reader Experience (Brightness, Panels, Reminders, TTS)








  - Create platform-specific BrightnessManager implementations with real-time adjustment
  - Add tap-outside-to-dismiss behavior for font size and brightness panels
  - Maintain existing toggle button functionality for all reader toolbar panels
  - Add reading time tracking and create RestReminderDialog with snooze functionality
  - Add reminder settings (enable/disable, interval configuration)
  - Increase TTS control icons to minimum 48dp with proper touch targets (Android)
  - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 15.1, 15.2, 15.3, 15.4, 15.5, 16.1, 16.2, 16.3, 16.4, 16.5, 17.1, 17.2, 17.3, 17.4, 17.5_

- [x] 5. Improve UI/UX Across Screens (Search, History, Auth, Detail, WebView)




  - Redesign GlobalSearchScreen with modern card-based layout and skeleton loading states
  - Add "Clear All" button to History screen with confirmation dialog and batch delete
  - Create error mapping for auth failures with user-friendly messages and recovery suggestions
  - Fix Book Detail back button to always be visible with elevated surface and proper contrast
  - Enhance WebView UI with Material Design 3 TopAppBar, loading progress, and error states
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 10.1, 10.2, 10.3, 10.4, 10.5, 11.1, 11.2, 11.3, 11.4, 11.5, 12.1, 12.2, 12.3, 12.4, 12.5, 13.1, 13.2, 13.3, 13.4, 13.5_





- [x] 6. Implement Real-time Extension Updates (Desktop Only)




  - Create ExtensionWatcher using Java WatchService to monitor extensions directory
  - Update extension list automatically when changes detected
  - Show notification when extensions are added/removed
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_
