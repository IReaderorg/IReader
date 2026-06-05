# Empty Library Onboarding UX Implementation Plan

## Overview
Add a guided step-by-step onboarding experience when users open an empty library screen. The flow will guide users through:
1. Library Screen (empty state) → Navigate to Sources
2. Sources Screen (no sources) → Navigate to Add Repository
3. After adding repository → Install a source
4. After installing source → Go to Explore screen to add books

## Current State Analysis

### Library Screen
- Location: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/LibraryScreen.kt`
- Empty state: Shows `EmptyScreen(text = localize(Res.string.empty_library))` at line 165
- State check: `!state.isLoading && state.isEmpty && state.filters.isEmpty()`

### Sources/Extension Screen
- Location: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/`
- `AddRepositoryDialog` - Dialog for adding custom repositories
- `ExtensionScreen` - Main extensions screen
- `RepositoryManagementScreen` - Screen for managing repositories

### Explore Screen
- Location: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreen.kt`
- Used for browsing books from installed sources

## Implementation Steps

### Task 1: Create Onboarding Step Data Model
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/`

- [ ] Create `OnboardingStep.kt` with:
  - `OnboardingStep` sealed class with steps:
    - `Welcome` - Initial welcome message
    - `AddRepository` - Step to add a repository
    - `InstallSource` - Step to install a source
    - `ExploreBooks` - Step to explore and add books
    - `Complete` - Onboarding complete
  - `OnboardingState` data class to track current step

### Task 2: Create EmptyLibraryOnboardingComponent
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/components/`

- [ ] Create `EmptyLibraryOnboarding.kt` with:
  - Composable function showing step-by-step guide
  - Visual indicators for each step (numbered circles with icons)
  - Action buttons for each step:
    - "Go to Sources" button
    - "Add Repository" button (opens dialog)
    - "Install Source" button
    - "Explore Books" button
  - Progress indicator showing current step
  - Skip option for experienced users
  - Animated transitions between steps

### Task 3: Add Navigation Callbacks to LibraryScreen
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/LibraryScreen.kt`

- [ ] Add new parameters to `LibraryScreen`:
  - `onNavigateToSources: () -> Unit` - Navigate to sources screen
  - `onNavigateToExplore: () -> Unit` - Navigate to explore screen
  - `onShowAddRepository: () -> Unit` - Show add repository dialog
- [ ] Update empty state to show `EmptyLibraryOnboarding` instead of basic `EmptyScreen`
- [ ] Pass navigation callbacks to the onboarding component

### Task 4: Create OnboardingViewModel
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/`

- [ ] Create `OnboardingViewModel.kt`:
  - Track current onboarding step
  - Check if user has sources installed
  - Check if user has repositories added
  - Persist onboarding completion state
  - Methods:
    - `getCurrentStep()` - Returns current step based on app state
    - `markStepComplete(step: OnboardingStep)` - Mark step as done
    - `skipOnboarding()` - Skip entire onboarding
    - `resetOnboarding()` - Reset for testing

### Task 5: Update LibraryScreenState
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryScreenState.kt`

- [ ] Add onboarding-related state:
  - `showOnboarding: Boolean = false` - Whether to show onboarding
  - `onboardingStep: OnboardingStep = OnboardingStep.Welcome` - Current step
  - `hasSources: Boolean = false` - Whether user has installed sources
  - `hasRepositories: Boolean = false` - Whether user has added repositories

### Task 6: Update LibraryViewModel
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt`

- [ ] Add onboarding logic:
  - Check if onboarding should be shown (first time user)
  - Track whether user has sources
  - Track whether user has repositories
  - Update state when sources/repositories change
  - Methods:
    - `shouldShowOnboarding(): Boolean`
    - `updateOnboardingState()`
    - `completeOnboardingStep(step: OnboardingStep)`

### Task 7: Add Localized Strings
**Location**: `i18n/src/commonMain/resources/`

- [ ] Add new string resources:
  - `onboarding_welcome_title` - "Welcome to Your Library"
  - `onboarding_welcome_message` - "Let's get you started with reading!"
  - `onboarding_step_add_repository` - "Step 1: Add a Repository"
  - `onboarding_step_add_repository_desc` - "Add a repository to access book sources"
  - `onboarding_step_install_source` - "Step 2: Install a Source"
  - `onboarding_step_install_source_desc` - "Install a source from the repository"
  - `onboarding_step_explore_books` - "Step 3: Explore Books"
  - `onboarding_step_explore_books_desc` - "Browse and add books to your library"
  - `onboarding_go_to_sources` - "Go to Sources"
  - `onboarding_add_repository` - "Add Repository"
  - `onboarding_install_source` - "Install Source"
  - `onboarding_explore_books` - "Explore Books"
  - `onboarding_skip` - "Skip Setup"
  - `onboarding_continue` - "Continue"

### Task 8: Create OnboardingPreferences
**Location**: `core/src/commonMain/kotlin/ireader/core/prefs/`

- [ ] Create `OnboardingPreferences.kt`:
  - Store onboarding completion state
  - Store current step
  - Store whether user has seen onboarding
  - Methods:
    - `isOnboardingCompleted(): Boolean`
    - `setOnboardingCompleted(completed: Boolean)`
    - `getCurrentStep(): OnboardingStep`
    - `setCurrentStep(step: OnboardingStep)`

### Task 9: Integrate with Extension Screen
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/`

- [ ] Update `ExtensionScreen` to show onboarding hints when accessed from library onboarding
- [ ] Add callback for when repository is added
- [ ] Add callback for when source is installed
- [ ] Show success feedback and next step guidance

### Task 10: Integrate with Explore Screen
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/`

- [ ] Update `ExploreScreen` to show onboarding hints when accessed from library onboarding
- [ ] Highlight "Add to Library" functionality
- [ ] Show completion message when first book is added

### Task 11: Add Visual Design Elements
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/components/`

- [ ] Create `OnboardingStepIndicator.kt`:
  - Numbered circles for each step
  - Checkmark for completed steps
  - Highlight for current step
  - Connecting lines between steps
- [ ] Create `OnboardingActionCard.kt`:
  - Card component for each step
  - Icon, title, description
  - Action button
  - Completion state styling

### Task 12: Add Animations
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/components/`

- [ ] Add smooth transitions between steps
- [ ] Add celebration animation when onboarding completes
- [ ] Add progress bar animation
- [ ] Add button press animations

## Technical Considerations

### State Management
- Use `OnboardingViewModel` to manage onboarding state
- Persist state using `OnboardingPreferences`
- React to changes in sources/repositories

### Navigation
- Pass navigation callbacks from parent composable
- Use existing navigation infrastructure
- Ensure back button works correctly

### Performance
- Lazy load onboarding components
- Cache onboarding state
- Minimize recomposition

### Accessibility
- Add content descriptions for all interactive elements
- Ensure proper focus order
- Support screen readers

## Testing

### Unit Tests
- Test `OnboardingViewModel` state transitions
- Test `OnboardingPreferences` persistence
- Test step completion logic

### Integration Tests
- Test navigation flow from library → sources → explore
- Test onboarding state persistence across app restarts
- Test skip onboarding functionality

### UI Tests
- Test onboarding component rendering
- Test step indicator updates
- Test action button functionality

## Files to Create/Modify

### New Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/OnboardingStep.kt`
2. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/components/EmptyLibraryOnboarding.kt`
3. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/components/OnboardingStepIndicator.kt`
4. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/components/OnboardingActionCard.kt`
5. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/OnboardingViewModel.kt`
6. `core/src/commonMain/kotlin/ireader/core/prefs/OnboardingPreferences.kt`

### Modified Files
1. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/LibraryScreen.kt`
2. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryScreenState.kt`
3. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/library/viewmodel/LibraryViewModel.kt`
4. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/sources/extension/ExtensionScreen.kt`
5. `presentation/src/commonMain/kotlin/ireader/presentation/ui/home/explore/ExploreScreen.kt`
6. `i18n/src/commonMain/resources/` (add new strings)

## Success Criteria
- [ ] New users see onboarding when library is empty
- [ ] Users can navigate through steps smoothly
- [ ] Each step clearly explains what to do
- [ ] Users can skip onboarding if desired
- [ ] Onboarding state persists across app restarts
- [ ] Onboarding completes when first book is added
- [ ] UI is visually appealing and consistent with app design
- [ ] All strings are localized
- [ ] Accessibility requirements are met
