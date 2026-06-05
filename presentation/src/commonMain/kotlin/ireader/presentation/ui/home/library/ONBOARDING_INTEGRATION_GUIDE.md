# Onboarding Integration Guide

This guide explains how to integrate the onboarding feature into your app's navigation system.

## Overview

The onboarding feature has been implemented with the following components:
- `OnboardingStep.kt` - Data model for onboarding steps
- `EmptyLibraryOnboarding.kt` - Main onboarding UI component
- `OnboardingStepIndicator.kt` - Visual progress indicator
- `OnboardingActionCard.kt` - Action card components
- `OnboardingPreferences.kt` - DataStore-based preferences

## Integration Steps

### Step 1: Update LibraryViewModel

Add the following methods to `LibraryViewModel.kt`:

```kotlin
// Add these imports at the top
import ireader.presentation.ui.home.library.OnboardingState
import ireader.presentation.ui.home.library.OnboardingStep

// Add onboarding state flow (after line ~140, with other state declarations)
private val _onboardingState = MutableStateFlow(OnboardingState())
val onboardingState: StateFlow<OnboardingState> = _onboardingState

// Add these methods at the end of the class (before the closing brace)

/**
 * Check if onboarding should be shown
 */
fun shouldShowOnboarding(): Boolean {
    val currentState = _onboardingState.value
    return !currentState.isComplete && !currentState.isSkipped
}

/**
 * Mark the current onboarding step as complete and advance to next step
 */
fun completeOnboardingStep(step: OnboardingStep) {
    _onboardingState.update { currentState ->
        val newCompletedSteps = currentState.completedSteps + step
        val nextStep = when (step) {
            is OnboardingStep.AddRepository -> OnboardingStep.InstallSource
            is OnboardingStep.InstallSource -> OnboardingStep.ExploreBooks
            is OnboardingStep.ExploreBooks -> OnboardingStep.Complete
            else -> currentState.currentStep
        }
        currentState.copy(
            currentStep = nextStep,
            completedSteps = newCompletedSteps,
            hasRepositories = step is OnboardingStep.AddRepository || currentState.hasRepositories,
            hasSources = step is OnboardingStep.InstallSource || currentState.hasSources,
            hasBooks = step is OnboardingStep.ExploreBooks || currentState.hasBooks
        )
    }
}

/**
 * Skip the entire onboarding flow
 */
fun skipOnboarding() {
    _onboardingState.update { currentState ->
        currentState.copy(
            isSkipped = true,
            isOnboardingVisible = false
        )
    }
}

/**
 * Show the onboarding screen
 */
fun showOnboarding() {
    _onboardingState.update { currentState ->
        currentState.copy(
            isOnboardingVisible = true,
            currentStep = OnboardingStep.AddRepository
        )
    }
}

/**
 * Reset onboarding state (for testing)
 */
fun resetOnboarding() {
    _onboardingState.value = OnboardingState()
}
```

### Step 2: Update LibraryScreenState

The `LibraryScreenState` already has the onboarding fields added:
- `showOnboarding: Boolean`
- `onboardingState: OnboardingState`

Update the state combine flow in LibraryViewModel to include these fields:

```kotlin
// In the state combine flow (around line 179-212), add these lines:
showOnboarding = shouldShowOnboarding() && filteredBooks.isEmpty(),
onboardingState = _onboardingState.value
```

### Step 3: Connect Navigation Callbacks

In the parent composable that hosts `LibraryScreen`, add the navigation callbacks:

```kotlin
LibraryScreen(
    // ... existing parameters ...
    onNavigateToSources = {
        // Navigate to the sources/extensions screen
        navController.navigate("sources")
    },
    onNavigateToExplore = {
        // Navigate to the explore screen
        navController.navigate("explore")
    },
    onShowAddRepository = {
        // Show the add repository dialog
        // This can be done by setting a state variable that triggers the dialog
        showAddRepositoryDialog = true
    },
    onSkipOnboarding = {
        vm.skip