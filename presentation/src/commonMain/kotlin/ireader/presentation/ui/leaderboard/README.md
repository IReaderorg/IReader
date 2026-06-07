# Leaderboard Feature

## Overview
The Leaderboard screen shows a ranked list of readers based on total reading time. It features a game-like level system with XP progress, user rank card, and top-3 podium display.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│               LeaderboardScreen                      │
│  (Composable UI - observes LeaderboardViewModel)     │
├─────────────────────────────────────────────────────┤
│             LeaderboardViewModel                     │
│  (State: LeaderboardScreenState)                    │
│  - leaderboard: List<LeaderboardEntry>              │
│  - userRank, lastSyncTime                          │
│  - isSyncing, isLoading                            │
│  - isRealtimeEnabled                               │
├─────────────────────────────────────────────────────┤
│          LeaderboardRepositoryImpl                   │
│  (Data layer - fetches from Supabase)               │
│  - getLeaderboard() → List<LeaderboardEntry>        │
│  - syncUserStats()                                 │
├─────────────────────────────────────────────────────┤
│           StatisticsSyncService                     │
│  (Syncs reading stats to Supabase)                  │
│  - Calculates ReaderLevel from minutes              │
│  - Updates LeaderboardEntry with level/xp           │
└─────────────────────────────────────────────────────┘
```

## Files

| File | Purpose |
|------|---------|
| `LeaderboardScreen.kt` | Main UI with user rank, podium, and full list |
| `LeaderboardViewModel.kt` | State management with sync and realtime |
| `LeaderboardState.kt` | Immutable state data class |
| `LeaderboardScreenSpec.kt` | Navigation wiring |
| `README.md` | This documentation |

## Level System

Reading time is converted to levels and displayed instead of raw hours.

**Formula:** `Level = floor(totalMinutes / 60) + 1`

| Level Range | Title | Display |
|-------------|-------|---------|
| 1-5 | Novice Reader | "Lvl 1 • Novice Reader" |
| 6-15 | Curious Reader | "Lvl 6 • Curious Reader" |
| 16-30 | Avid Reader | "Lvl 16 • Avid Reader" |
| 31-50 | Bookworm | "Lvl 31 • Bookworm" |
| 51-100 | Master Reader | "Lvl 51 • Master Reader" |
| 101-200 | Literary Legend | "Lvl 101 • Literary Legend" |
| 200+ | Reading Deity | "Lvl 201+ • Reading Deity" |

## Data Flow

```
User reads → TrackReadingProgressUseCase
    ↓
StatisticsSyncService.syncUserStats()
    ↓
Calculate ReaderLevel.fromMinutes(totalMinutes)
    ↓
Update Supabase leaderboard entry
    ↓
LeaderboardViewModel loads from Supabase
    ↓
formatReadingTime(minutes) → "Lvl X • Title"
    ↓
UI displays: UserRankCard, Top3Podium, FullList
```

## Key Components

- **EnhancedUserRankCard** - Shows user's current rank and level
- **EnhancedTopThreePodium** - Visual podium for top 3 readers
- **EnhancedLeaderboardEntryCard** - Individual entry with level badge
- **CompactRealtimeToggle** - Toggle for live updates

## Reusable Components Used
- `AsyncImage` - Avatar images
- `FeatureScreenScaffold` - (Available for future use)
