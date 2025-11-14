# Paragraph Translation Menu Toggle Implementation

## Summary
Added a toggle button in the reader modal sheet's general tab that allows users to enable/disable the paragraph translation menu feature.

## Changes Made

### 1. Domain Layer - Added Preference
**File**: `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/ReaderPreferences.kt`
- Added `paragraphTranslationEnabled()` preference function that defaults to `false`
- This preference controls whether the paragraph translation menu appears on long-press

### 2. Presentation Layer - ViewModel
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModel.kt`
- Added `paragraphTranslationEnabled` state property that observes the preference

### 3. UI Layer - Settings Screen
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ReaderSettingComposable.kt`
- Added a `SwitchPreference` in the `GeneralScreenTab` function
- Placed after "Volume Key Navigation" toggle
- Uses localized string for the title

### 4. Component Layer - SelectableTranslatableText
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/SelectableTranslatableText.kt`
- Added `paragraphTranslationEnabled` parameter (defaults to `false`)
- Modified the condition to only show `ParagraphTranslationMenu` when both:
  - `paragraphTranslationEnabled` is `true`
  - User performs a long-press gesture

### 5. Component Layer - ReaderTextWithCustomFont
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ReaderTextWithCustomFont.kt`
- Added `paragraphTranslationEnabled` parameter to wrapper component
- Passes the parameter through to `SelectableTranslatableText`

### 6. Reader Screen
**File**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/ReaderText.kt`
- Updated `SelectableTranslatableText` call to pass `vm.paragraphTranslationEnabled.value`

### 7. Localization
**File**: `i18n/src/commonMain/resources/MR/en/strings.xml`
- Added new string resource: `paragraph_translation_menu` = "Paragraph Translation Menu"

## How It Works

1. User opens the reader settings modal sheet
2. Navigates to the "General" tab
3. Finds the "Paragraph Translation Menu" toggle under Display Settings
4. When enabled:
   - Long-pressing on a paragraph in the reader shows the translation menu
   - User can translate or copy the paragraph
5. When disabled (default):
   - Long-press gesture does nothing
   - The translation menu never appears

## Default Behavior
- The feature is **disabled by default** (`false`)
- Users must explicitly enable it in settings to use the paragraph translation menu
- This prevents accidental triggering of the translation menu during normal reading

## Testing Checklist
- [ ] Toggle appears in General tab of reader settings
- [ ] Toggle state persists across app restarts
- [ ] When disabled, long-press does not show translation menu
- [ ] When enabled, long-press shows translation menu
- [ ] Translation menu works correctly when enabled
- [ ] No impact on normal text selection when feature is disabled
