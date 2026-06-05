# Onboarding UX - Refactor & Implementation Plan

## Current State Analysis

### What We Have Now:
1. **OnboardingStep.kt** - Data model with 5 steps (Welcome, AddRepository, InstallSource, ExploreBooks, Complete)
2. **EmptyLibraryOnboarding.kt** - Full-screen onboarding UI component
3. **OnboardingStepIndicator.kt** - Visual progress indicator
4. **OnboardingActionCard.kt** - Action card for each step
5. **OnboardingPreferences.kt** - DataStore preferences (not yet integrated)
6. **LibraryViewModel** - Has onboarding state and methods
7. **LibraryScreenTopBar** - Has lightbulb hint button (shows when library empty)
8. **LibraryScreenSpec** - Shows onboarding dialog when triggered

### Current Issues:
1. Onboarding shows as full-screen overlay - user wants it inside the tab content
2. Lightbulb button shows immediately - should wait for database result
3. Clicking lightbulb does nothing visible - dialog not properly displayed
4. Code is scattered across multiple files - needs simplification
5. Navigation from onboarding callbacks is complex and error-prone

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        LibraryScreenSpec                                 │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │                      IScaffold                                     │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │                    TopBar                                    │  │  │
│  │  │  [Sort] [Search] [💡 Hint] [More...]                        │  │  │
│  │  │                          │                                   │  │  │
│  │  │                          ▼                                   │  │  │
│  │  │              showOnboardingDialog()                          │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │                                                                   │  │
│  │  ┌─────────────────────────────────────────────────────────────┐  │  │
│  │  │                   TabContent                                 │  │  │
│  │  │                                                               │  │  │
│  │  │  ┌───────────────────────────────────────────────────────┐  │  │  │
│  │  │  │              LibraryContent                            │  │  │  │
│  │  │  │  - Shows books grid when books exist                   │  │  │  │
│  │  │  │  - Shows EmptyScreen when no books                     │  │  │  │
│  │  │  └───────────────────────────────────────────────────────┘  │  │  │
│  │  │                                                               │  │  │
│  │  │  ┌───────────────────────────────────────────────────────┐  │  │  │
│  │  │  │     EmptyLibraryOnboarding (INSIDE tab content)        │  │  │  │
│  │  │  │  - Shows when library is empty AND onboarding needed   │  │  │  │
│  │  │  │  - Step-by-step guide with action buttons              │  │  │  │
│  │  │  │  - Dismisses on skip or completion                     │  │  │  │
│  │  │  └───────────────────────────────────────────────────────┘  │  │  │
│  │  │                                                               │  │  │
│  │  └─────────────────────────────────────────────────────────────┘  │  │
│  │                                                                   │  │
│  └───────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────┘
```

## User Flow Diagram

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   App Launch    │────▶│  Library Screen │────▶│  Load Books DB  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                                        │
                                    ┌───────────────────┼───────────────────┐
                                    │                   │                   │
                                    ▼                   ▼                   ▼
                            ┌───────────┐       ┌───────────┐       ┌───────────┐
                            │ Has Books │       │  Loading  │       │  No Books │
                            │ Show Grid │       │ Show Loader│       │ Show Empty│
                            └───────────┘       └───────────┘       └───────────┘
                                                                        │
                                                                        ▼
                                                                ┌───────────────┐
                                                                │ Check Onboard │
                                                                │   State       │
                                                                └───────────────┘
                                                                        │
                                                    ┌───────────────────┼───────────────────┐
                                                    │                   │                   │
                                                    ▼                   ▼                   ▼
                                            ┌───────────┐       ┌───────────┐       ┌───────────┐
                                            │ Completed │       │  Skipped  │       │   New     │
                                            │ Show Empty│       │ Show Empty│       │ Show Hint │
                                            │   Screen  │       │   Screen  │       │  Button   │
                                            └───────────┘       └───────────┘       └───────────┘
                                                                                        │
                                                                                        ▼
                                                                                ┌───────────────┐
                                                                                │ Click Hint    │
                                                                                │   Button      │
                                                                                └───────────────┘
                                                                                        │
                                                                                        ▼
                                                                                ┌───────────────┐
                                                                                │   Show        │
                                                                                │  Onboarding   │
                                                                                │   Guide       │
                                                                                └───────────────┘
                                                                                        │
                                                                    ┌───────────────────┼───────────────────┐
                                                                    │                   │                   │
                                                                    ▼                   ▼                   ▼
                                                            ┌───────────┐       ┌───────────┐       ┌───────────┐
                                                            │Add Repo   │       │Install    │       │ Explore   │
                                                            │           │       │Source     │       │  Books    │
                                                            └───────────┘       └───────────┘       └───────────┘
```

## Simplified Implementation Plan

### Phase 1: Clean Up & Simplify
**Goal:** Remove unused code and simplify the architecture

1. **Remove unused files/methods:**
   - Remove `OnboardingPreferences.kt` (not integrated, using ViewModel state instead)
   - Remove `NavigationRequest` sealed class (not needed with simplified approach)
   - Remove `showOnboardingDialog` state (use simpler boolean flag)
   - Remove complex navigation callbacks from onboarding

2. **Simplify EmptyLibraryOnboarding:**
   - Remove navigation callbacks (onNavigateToSources, onNavigateToExplore, onShowAddRepository)
   - Add simple `onDismiss` callback
   - Show informational content only (no direct navigation)
   - User dismisses and navigates manually

3. **Simplify LibraryViewModel:**
   - Keep only: `showOnboarding`, `hideOnboarding`, `isOnboardingCompleted`
   - Remove: NavigationRequest, showOnboardingDialog, complex state management

### Phase 2: Integrate Onboarding into Tab Content
**Goal:** Show onboarding inside the tab content area, not as overlay

1. **Modify LibraryScreenSpec:**
   - Remove dialog-based onboarding
   - Add onboarding content inside the tab content area
   - Show onboarding when: `!isLoading && isEmpty && !isOnboardingCompleted`

2. **Modify LibraryScreenTopBar:**
   - Keep lightbulb hint button
   - Show button only when: `isEmpty && !isLoading && !isOnboardingCompleted`
   - Button toggles onboarding visibility

3. **Update empty state logic:**
   ```
   if (isLoading) {
       Show Loading
   } else if (isEmpty && !isOnboardingCompleted) {
       Show Onboarding
   } else if (isEmpty) {
       Show Empty Screen
   } else {
       Show Books Grid
   }
   ```

### Phase 3: Database-Aware Showing
**Goal:** Only show onboarding after database query completes

1. **Wait for database result:**
   - Don't show onboarding until `isLoading == false`
   - Use `state.isLoading` to determine when data is ready

2. **Persist onboarding state:**
   - Use simple boolean in preferences: `onboardingCompleted`
   - Save when user skips or completes onboarding
   - Check on app start

### Phase 4: Polish & Test
**Goal:** Ensure smooth UX

1. **Animations:**
   - Fade in onboarding when appearing
   - Smooth transitions between states

2. **Accessibility:**
   - Content descriptions for all buttons
   - Proper focus order

3. **Testing:**
   - Test with empty library
   - Test with books present
   - Test skip functionality
   - Test persistence across app restarts

## File Changes Summary

### Files to Modify:
1. **LibraryScreenSpec.kt** - Main integration point
2. **LibraryScreenTopBar.kt** - Hint button visibility
3. **LibraryViewModel.kt** - Simplified state management
4. **EmptyLibraryOnboarding.kt** - Simplified component
5. **LibraryScreenState.kt** - Add onboardingCompleted flag

### Files to Delete:
1. **OnboardingPreferences.kt** - Not needed (use simple preference)
2. **OnboardingStep.kt** - Simplify to simple enum
3. **OnboardingStepIndicator.kt** - Simplify or remove
4. **OnboardingActionCard.kt** - Simplify or remove

### New Simplified Structure:
```
LibraryScreenSpec
├── TopBar (with hint button)
└── TabContent
    ├── Loading State
    ├── Books Grid (when books exist)
    ├── Onboarding (when empty & not completed)
    └── Empty Screen (when empty & completed/skipped)
```

## Success Criteria
- [ ] Onboarding shows inside tab content (not overlay)
- [ ] Hint button only appears after database loads
- [ ] Onboarding only shows when library is empty
- [ ] User can skip/dismiss onboarding
- [ ] State persists across app restarts
- [ ] Code is simplified and maintainable
- [ ] Build passes without errors
