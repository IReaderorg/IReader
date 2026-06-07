# User Title Feature

## Overview
User Titles are cosmetic items that display under the user's name. Each title has a passive effect that boosts XP gain. Effects are limited-duration to encourage active engagement.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                UserTitleScreen                       │
│  (Composable UI - observes UserTitleViewModel)       │
├─────────────────────────────────────────────────────┤
│               UserTitleViewModel                     │
│  (State: UserTitleScreenState)                      │
│  - titles: List<UserTitle>                          │
│  - activeTitleId: String?                           │
├─────────────────────────────────────────────────────┤
│              ActivateTitleUseCase                    │
│  (Domain logic - activates title with effect)        │
│  - Check if title is earned                         │
│  - Apply effect with duration                       │
│  - Set as active title                             │
└─────────────────────────────────────────────────────┘
```

## Files

| File | Purpose |
|------|---------|
| `UserTitleScreen.kt` | UI showing title collection with rarity and activation |
| `UserTitleViewModel.kt` | State management for titles |
| `UserTitleScreenSpec.kt` | Navigation wiring |
| `README.md` | This documentation |

## Title Rarities

| Rarity | Color | Description |
|--------|-------|-------------|
| COMMON | Gray | Basic titles, easy to earn |
| RARE | Blue | Moderate difficulty |
| EPIC | Purple | Hard to earn |
| LEGENDARY | Orange | Very rare |

## Title Effects

| Effect | Multiplier | Duration | Description |
|--------|-----------|----------|-------------|
| ReadingTimeBonus | 2x | 24h | Double XP from reading |
| ChapterBonus | 2x | 12h | Double XP from chapters |
| BookCompletionBonus | 3x | 24h | Triple XP from books |
| StreakBonus | 2x | 48h | Double XP from streaks |
| TimeRestrictedBonus | 3x | 168h | Triple XP during night hours (22:00-06:00) |

## Data Flow

```
User earns title → Add to UserTitle.isEarned = true
    ↓
User taps "Activate" → ActivateTitleUseCase
    ↓
Set activeTitleId → Apply effect with duration
    ↓
UserTitleViewModel updates state → UI re-renders
    ↓
Effect expires → Auto-deactivate → User can activate another
```

## Reusable Components Used
- `FeatureScreenScaffold` - Common top bar with back navigation
