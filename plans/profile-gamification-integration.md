# Profile Screen Gamification Integration Plan

## Vision
Remove separate Reward/SpiritStone/Title screens. Integrate everything into a unified Profile Screen with deep Supabase integration. The profile becomes the user's identity card showing their reading journey.

## Current State
- ProfileScreen exists with user info, badges, reading stats
- ProfileViewModel has RemoteBackendUseCases, BadgeRepository, ReadingStatisticsRepository
- Separate RewardViewModel, SpiritStoneViewModel, UserTitleViewModel exist but should be removed
- LeaderboardEntry has level, levelTitle, xp, xpToNextLevel fields

## New Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ProfileScreen                         │
│  (Unified gamification hub)                              │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────┐    │
│  │  User Header                                     │    │
│  │  - Avatar, Username, Level Badge                 │    │
│  │  - Active Title Display                          │    │
│  │  - Spirit Stone Balance                          │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Stats Section                                   │    │
│  │  - Reading Time, Chapters, Books, Streak         │    │
│  │  - XP Progress Bar → Next Level                  │    │
│  │  - Leaderboard Rank                              │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Achievements Section                            │    │
│  │  - Recent achievements with icons                │    │
│  │  - Progress toward next achievement              │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Titles Section                                  │    │
│  │  - Collection grid with rarity colors            │    │
│  │  - Active title with effect description          │    │
│  │  - Tap to activate/deactivate                    │    │
│  └─────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────┐    │
│  │  Shop Section (Spirit Stones)                    │    │
│  │  - Balance display                               │    │
│  │  - Shop items grid                               │    │
│  │  - Purchase history                              │    │
│  └─────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────┘
```

## Changes

### 1. Remove Separate Screens
- Delete RewardScreen.kt, RewardViewModel.kt, RewardScreenSpec.kt
- Delete SpiritStoneScreen.kt, SpiritStoneViewModel.kt, SpiritStoneScreenSpec.kt
- Delete UserTitleScreen.kt, UserTitleViewModel.kt, UserTitleScreenSpec.kt
- Remove DI registrations from ScreenModelModule.kt
- Remove navigation routes from NavigationRoutes.kt
- Remove composable entries from CommonNavHost.kt
- Remove from CommunityHubScreenSpec.kt and CommunityHubScreen.kt

### 2. Enhance ProfileViewModel
Add to ProfileState:
- currentLevel: Int
- currentXp: Long
- xpToNextLevel: Long
- levelTitle: String
- spiritStoneBalance: Long
- activeTitle: UserTitle?
- ownedTitles: List<UserTitle>
- recentAchievements: List<UserAchievement>
- leaderboardRank: Int?

Add to ProfileViewModel:
- Load level/XP from Supabase user profile
- Load spirit stone balance from Supabase
- Load active title and owned titles
- Load recent achievements
- Load leaderboard rank
- Activate/deactivate titles
- Purchase shop items

### 3. Enhance ProfileScreen
Add new sections:
- Level/XP progress bar below user header
- Spirit stone balance display
- Active title with effect description
- Achievements grid
- Titles collection grid
- Shop items (compact)

### 4. Supabase Integration
New Supabase tables/functions:
- user_profiles: level, xp, spirit_stones, active_title_id
- user_titles: user_id, title_id, is_active, purchased_at
- user_achievements: user_id, achievement_id, earned_at
- spirit_stone_transactions: user_id, amount, type, description

### 5. Community Hub Update
Replace separate gamification links with single "Profile" link that opens the enhanced ProfileScreen.

## Implementation Order
1. Remove separate screens and DI registrations
2. Update CommunityHubScreen to link to Profile instead
3. Enhance ProfileViewModel with gamification state
4. Enhance ProfileScreen with new sections
5. Add Supabase data loading in ProfileViewModel
6. Final compile and test
