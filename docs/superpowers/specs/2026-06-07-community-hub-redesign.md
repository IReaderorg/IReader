# Community Hub Redesign - Full Spec

## Problem
New gamification screens (Rewards, SpiritStone, UserTitle) exist but are not linked from the Community Hub. The Leaderboard and Popular Books screens need deeper integration with the new features to create a cohesive, engaging community experience.

## Goals
1. Link all new screens from Community Hub
2. Redesign Leaderboard to show user profiles with level, title, and spirit stones
3. Redesign Popular Books with social features (who's reading, source install flow)
4. Create a unified community experience that drives engagement

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    CommunityHubScreen                         │
│  (Central hub - links to all community features)             │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Leaderboard   │  │Popular Books │  │   Rewards    │       │
│  │              │  │              │  │              │       │
│  │ - User rank  │  │ - Cover img  │  │ - Level card │       │
│  │ - Level badge│  │ - Description│  │ - XP progress│       │
│  │ - Title      │  │ - Source     │  │ - Achievemts│       │
│  │ - Spirit     │  │ - Readers    │  │ - Rewards    │       │
│  │   stones     │  │ - In library │  │              │       │
│  │ - Podium     │  │ - Install    │  │              │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Spirit Stones│  │ User Titles  │  │  Reviews     │       │
│  │              │  │              │  │              │       │
│  │ - Balance    │  │ - Collection │  │ - All reviews│       │
│  │ - Shop       │  │ - Rarity     │  │              │       │
│  │ - Purchase   │  │ - Effects    │  │              │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Section 1: Community Hub Integration

### Changes to CommunityHubScreenSpec.kt
Add navigation callbacks for:
- `onRewards` → NavigationRoutes.rewards
- `onSpiritStones` → NavigationRoutes.spiritStones
- `onUserTitles` → NavigationRoutes.userTitles

### Changes to CommunityHubScreen.kt
Add new menu items in the community hub:
- "Rewards" (icon: EmojiEvents, gold) - Shows level and XP
- "Spirit Stones" (icon: Diamond, cyan) - Shows balance
- "Titles" (icon: WorkspacePremium, gold) - Shows active title

## Section 2: Leaderboard Redesign

### Current State
- Shows reading time, rank, avatar
- Has level system (Lvl X • Title)

### New Features to Add
1. **User Profile Card** (tappable):
   - Level badge with XP progress
   - Active title display
   - Spirit stone balance
   - Achievement count

2. **Enhanced Entry Cards**:
   - Level badge instead of raw hours
   - Active title below username
   - Spirit stone indicator
   - Tap to view full profile

3. **Profile Detail Dialog** (on tap):
   - Large level badge
   - XP progress bar
   - Active title with effect description
   - Spirit stone balance
   - Recent achievements (last 3)
   - "View Full Profile" → navigates to Rewards screen

### Data Flow
```
LeaderboardEntry
    ↓
Enriched with: level, levelTitle, xp, xpToNextLevel
    ↓
formatReadingTime(minutes) → "Lvl X • Title"
    ↓
Tap entry → ProfileDialog
    ↓
Show: level, title, spirit stones, achievements
    ↓
"View Full Profile" → RewardsScreen
```

## Section 3: Popular Books Redesign

### Current State
- Shows book cover, title, description, source badge, reader count
- Tap → check library → open or search

### New Features to Add
1. **Social Reading Indicators**:
   - "X friends reading" (if synced books from friends)
   - Source install prompt with SourceInstallDialog
   - "Add to library" button for books not in library

2. **Enhanced Book Cards**:
   - Larger cover image
   - Description (2 lines)
   - Source badge (color-coded)
   - Reader count with icon
   - "In library" checkmark
   - Quick actions: Read / Search / Install Source

3. **Source Resolution Flow**:
```
User taps book
    ↓
ResolvePopularBookSourceUseCase(book)
    ↓
If source installed → Open book detail
If not installed → Show SourceInstallDialog
    ↓
Dialog: "You need [sourceName] to read this book"
    ↓
[Install] → Navigate to extension installer
[Cancel] → Dismiss
```

## Section 4: Cross-Feature Integration

### Leaderboard ↔ Rewards
- Tapping a leaderboard entry shows their level/title
- "View Full Profile" navigates to Rewards screen
- Rewards screen shows leaderboard rank

### Popular Books ↔ Spirit Stones
- Books can be "boosted" with spirit stones (future)
- Source installation could cost spirit stones (future)

### User Titles ↔ Leaderboard
- Active title displayed on leaderboard entries
- Title effects boost XP gain

### Rewards ↔ All Features
- Reading books → XP → Level up
- Level up → Spirit stones earned
- Achievements → Titles earned
- Titles → XP boost → Faster level up

## Section 5: Navigation Routes to Add

```kotlin
// Already added:
const val rewards = "rewards"
const val spiritStones = "spiritStones"
const val userTitles = "userTitles"

// New routes for profile dialog:
const val userProfile = "userProfile/{userId}"
```

## Implementation Order
1. Add new routes to CommunityHubScreenSpec and CommunityHubScreen
2. Redesign LeaderboardScreen with profile cards
3. Redesign Popular Books with source install flow
4. Add cross-feature navigation
5. Final compile and test
