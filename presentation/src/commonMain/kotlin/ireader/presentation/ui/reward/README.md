# Reward Feature

## Overview
The Reward feature gamifies the reading experience by tracking user progress through levels, XP, and achievements. Users earn rewards by reading, completing chapters/books, and maintaining streaks.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                  RewardScreen                        │
│  (Composable UI - observes RewardViewModel)          │
├─────────────────────────────────────────────────────┤
│                  RewardViewModel                     │
│  (State: RewardScreenState)                         │
│  - currentLevel, currentXp, xpToNextLevel           │
│  - achievements: List<UserAchievement>              │
│  - rewards: List<Reward>                            │
├─────────────────────────────────────────────────────┤
│              RewardEngineUseCase                     │
│  (Domain logic - calculates XP and achievements)     │
│  - calculateReadingXp(minutes)                      │
│  - calculateChapterXp()                             │
│  - calculateBookCompletionXp()                      │
│  - checkAchievements(stats) -> List<UserAchievement>│
│  - createLevelUpReward(level) -> Reward             │
└─────────────────────────────────────────────────────┘
```

## Files

| File | Purpose |
|------|---------|
| `RewardScreen.kt` | UI composable showing level card, achievements, and rewards |
| `RewardViewModel.kt` | State management for the reward screen |
| `RewardScreenSpec.kt` | Navigation wiring (connects screen to nav graph) |
| `README.md` | This documentation |

## Data Flow

```
User reads → TrackReadingProgressUseCase → XpEvent
    ↓
RewardEngineUseCase.calculateReadingXp(minutes)
    ↓
Update totalMinutes → Check level up
    ↓
If level up → createLevelUpReward(newLevel)
    ↓
RewardViewModel updates state → RewardScreen re-renders
```

## Level System

| Level Range | Title |
|-------------|-------|
| 1-5 | Novice Reader |
| 6-15 | Curious Reader |
| 16-30 | Avid Reader |
| 31-50 | Bookworm |
| 51-100 | Master Reader |
| 101-200 | Literary Legend |
| 200+ | Reading Deity |

**Formula:** `Level = floor(totalMinutes / 60) + 1`

## XP Rules

| Action | XP |
|--------|-----|
| 1 minute reading | 1 XP |
| 1 chapter read | 5 XP |
| 1 book completed | 50 XP |
| 7-day streak | 100 XP |
| 30-day streak | 500 XP |

## Reusable Components Used
- `FeatureScreenScaffold` - Common top bar with back navigation and loading/empty states
