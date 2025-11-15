# Badge Management Implementation

## Overview
This document describes the Badge Management screen implementation for IReader's monetization system.

## Files Created

### 1. BadgeManagementViewModel.kt
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/manage/BadgeManagementViewModel.kt`

**Features:**
- Manages state for owned badges, primary badge selection, and featured badges selection
- Tracks changes to detect when save button should be enabled
- Handles loading user badges from repository
- Implements save functionality for primary and featured badge selections
- Provides error handling and success feedback

**State:**
- `ownedBadges`: List of badges the user owns
- `primaryBadgeId`: Currently selected primary badge (for reviews)
- `featuredBadgeIds`: Currently selected featured badges (for profile, max 3)
- `isLoading`: Loading state for initial data fetch
- `isSaving`: Saving state for save operation
- `hasChanges`: Tracks if user has made changes
- `error`, `saveSuccess`, `saveError`: Error and success states

### 2. BadgeManagementScreen.kt
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/badges/manage/BadgeManagementScreen.kt`

**Components:**

#### BadgeManagementScreen (Main)
- Uses IScaffold with TitleToolbar
- Shows "Save" button in toolbar when changes are made
- Displays loading, error, and empty states
- Contains three main sections when badges are available

#### BadgeSelector
- Single-selection grid for choosing primary badge (for reviews)
- Includes "None" option to clear selection
- Uses LazyVerticalGrid with adaptive columns (120.dp min)
- Shows selection indicator with checkmark

#### BadgeMultiSelector
- Multi-selection grid for choosing featured badges (for profile)
- Displays selection count (e.g., "Selected: 2/3")
- Disables unselected badges when max (3) is reached
- Shows checkmarks on selected badges

#### SelectableBadgeCard
- Reusable card component for both selectors
- Shows badge icon (64.dp) and name
- Highlights selected badges with primary color border (3.dp)
- Displays checkmark overlay when selected
- Supports disabled state for multi-selector

#### BadgePreviewSection
- Live preview of badge selections
- Profile preview: Shows how featured badges appear on user profile
- Review preview: Shows how primary badge appears next to username in reviews
- Updates in real-time as user makes selections

## Navigation Updates

### NavigationRoutes.kt
Added: `const val badgeManagement = "badgeManagement"`

### MoreScreen.kt
- Added `onBadgeManagement` parameter
- Added "Manage Badges" menu item in "Badges & Customization" section
- Description: "Customize which badges appear on your profile and reviews"
- Icon: Settings icon

## Integration Requirements

To complete the integration, the following steps are needed:

### 1. Add Navigation Route to CommonNavHost.kt
Add this composable to the navigation graph:

```kotlin
composable(NavigationRoutes.badgeManagement) {
    BadgeManagementScreenSpec().Content()
}
```

### 2. Create ScreenSpec
Create `BadgeManagementScreenSpec.kt` following the pattern of other screens:

```kotlin
class BadgeManagementScreenSpec : ScreenSpec {
    @Composable
    override fun Content() {
        val vm: BadgeManagementViewModel = koinInject()
        val navigator: Navigator = koinInject()
        
        BadgeManagementScreen(
            viewModel = vm,
            onNavigateBack = { navigator.popBackStack() },
            onNavigateToBadgeStore = { 
                navigator.navigate(NavigationRoutes.badgeStore) 
            }
        )
    }
}
```

### 3. Register ViewModel in DI
Add to `PresentationModules.kt`:

```kotlin
viewModel { BadgeManagementViewModel(get(), get(), get()) }
```

### 4. Wire Up MoreScreen Navigation
In the file that instantiates MoreScreen, add:

```kotlin
onBadgeManagement = { 
    navigator.navigate(NavigationRoutes.badgeManagement) 
}
```

## Dependencies

The implementation depends on:
- `GetUserBadgesUseCase` - Fetches user's owned badges
- `SetPrimaryBadgeUseCase` - Sets the primary badge for reviews
- `SetFeaturedBadgesUseCase` - Sets featured badges for profile (max 3)
- `BadgeIcon` component - Displays badge icons with optional NFT animation
- `ProfileBadgeDisplay` component - Shows featured badges on profile
- `ReviewBadgeDisplay` component - Shows primary badge on reviews

## Features

### Selection Management
- **Primary Badge**: Single selection for reviews, with "None" option
- **Featured Badges**: Multi-selection (max 3) for profile display
- **Change Detection**: Save button only appears when changes are made
- **Validation**: Enforces max 3 featured badges limit

### User Experience
- **Live Preview**: Real-time preview of how badges will appear
- **Empty State**: Guides users to Badge Store if no badges owned
- **Loading States**: Shows loading indicators during data fetch
- **Error Handling**: Displays errors with retry option
- **Success Feedback**: Shows snackbar on successful save

### Responsive Design
- Adaptive grid layout (120.dp minimum column width)
- Works on mobile and desktop
- Proper spacing and padding throughout
- Material Design 3 styling

## Testing Checklist

- [ ] Load screen with no badges (empty state)
- [ ] Load screen with badges (content state)
- [ ] Select primary badge
- [ ] Clear primary badge selection
- [ ] Select featured badges (1, 2, 3)
- [ ] Try to select 4th featured badge (should be disabled)
- [ ] Deselect featured badges
- [ ] Verify preview updates in real-time
- [ ] Save changes successfully
- [ ] Handle save errors
- [ ] Navigate back
- [ ] Navigate to Badge Store from empty state

## Requirements Satisfied

This implementation satisfies the following requirements from the spec:
- **8.1**: Display all owned badges in management interface
- **8.2**: Allow users to set a primary badge for reviews
- **8.3**: Allow users to select up to 3 featured badges for profile
- **8.4**: Provide preview of how badges appear on profile and reviews
- **8.5**: Apply changes immediately when settings are saved
