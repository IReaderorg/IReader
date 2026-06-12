# IReader Gamification System

Complete documentation of all gamification features, level systems, and engagement mechanics.

---

## Table of Contents

1. [Level System](#level-system)
2. [Reading Time Tracking](#reading-time-tracking)
3. [Stats Sync to Supabase](#stats-sync-to-supabase)
4. [Achievement System](#achievement-system)
5. [Spirit Stones](#spirit-stones)
6. [Daily Check-in](#daily-check-in)
7. [Badges](#badges)
8. [Reading Challenges](#reading-challenges)
9. [Milestones](#milestones)
10. [Leaderboard](#leaderboard)
11. [Profile Display](#profile-display)
12. [Data Flow Summary](#data-flow-summary)

---

## Level System

There are **two separate level systems** that coexist:

### 1. Local Level (Leaderboard)

- **Formula:** `level = floor(readingMinutes / 60) + 1`, XP = `minutes % 60`
- **Each level = 1 hour of reading**, linear progression
- Used **only on the Leaderboard screen**
- Source: `ReaderLevel.fromMinutes()` derives level from `total_reading_time_minutes` in the `leaderboard` table

**Level Titles:**

| Level Range | Title |
|---|---|
| 1-5 | Novice Reader |
| 6-15 | Curious Reader |
| 16-30 | Avid Reader |
| 31-50 | Bookworm |
| 51-100 | Master Reader |
| 101-200 | Literary Legend |
| 201+ | Reading Deity |

**Level Colors:**

| Level Range | Color | Hex |
|---|---|---|
| 1-5 | Gray | `#9E9E9E` |
| 6-15 | Green | `#4CAF50` |
| 16-30 | Blue | `#2196F3` |
| 31-50 | Purple | `#9C27B0` |
| 51-100 | Orange | `#FF9800` |
| 101-200 | Gold | `#FFD700` |
| 201+ | Pink/Diamond | `#E91E63` |

**Key files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/entities/ReaderLevel.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/level/CalculateLevelUseCase.kt`

### 2. Server Level (Gamification Economy)

- **Formula:** `level = (1 + sqrt(1 + 4*xp/50)) / 2` — quadratic curve, each level costs more XP
- **XP comes from achievements** (not reading time) — each achievement grants `rewardXp` when unlocked
- Used on **Profile screen** — shows `GamificationProfile.level`, `xp`, `levelTitle`
- Level and title are **recomputed server-side** after every `sync_reading_stats` or `evaluate_achievements` RPC call

**XP Formula (cumulative):**

```
XP required for level L = 50 * L * (L-1)
```

**Server SQL functions:**

```sql
-- Level calculation (quadratic formula inverse)
CREATE OR REPLACE FUNCTION public.calculate_level(p_xp BIGINT)
RETURNS INT LANGUAGE sql IMMUTABLE AS $$
    SELECT GREATEST(1, FLOOR((1 + SQRT(1 + 4 * (GREATEST(p_xp,0)::numeric / 50))) / 2))::INT;
$$;

-- Level title mapping
CREATE OR REPLACE FUNCTION public.level_title_for(p_level INT)
RETURNS TEXT LANGUAGE sql IMMUTABLE AS $$
    SELECT CASE
        WHEN p_level <= 2 THEN 'Novice Reader'
        WHEN p_level <= 5 THEN 'Apprentice Reader'
        WHEN p_level <= 11 THEN 'Adept Reader'
        WHEN p_level <= 19 THEN 'Avid Reader'
        WHEN p_level <= 29 THEN 'Master Reader'
        WHEN p_level <= 49 THEN 'Sage Reader'
        ELSE 'Grandmaster Reader'
    END;
$$;
```

**Key files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/gamification/GamificationModels.kt`
- `data/src/commonMain/kotlin/ireader/data/gamification/GamificationRepositoryImpl.kt`
- `supabase/schema.sql` (lines 336-455)

---

## Reading Time Tracking

**Trigger:** User opens the reader screen

### Flow

1. `DisposableEffect` in `ReaderScreenSpec.kt` records `startTime = currentTimeToLong()`
2. A coroutine saves every **30 seconds**:
   - `trackReadingTime(duration)` — converts ms to minutes (ceiling, min 1), adds to local DB
   - `updateReadingStreak()` — same day = keep, next day = increment, gap > 1 day = reset to 1
3. On 80% chapter progress → `onChapterProgressUpdate()` increments `chaptersRead` and adds words
4. On book completion → `trackBookCompletion()` increments `booksCompleted`
5. Floating `ReadingTimeIndicator` shows session time ("5m 23s") in real time

**Data stored locally** in `ReadingStatisticsType1`:
- `totalReadingTimeMinutes`, `totalChaptersRead`, `booksCompleted`
- `readingStreak`, `longestStreak`, `averageReadingSpeedWPM`, `favoriteGenres`
- `buddyLevel`, `buddyExperience` (Reading Buddy system)

**Key files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/ReaderScreenSpec.kt` (lines 192-256)
- `domain/src/commonMain/kotlin/ireader/domain/usecases/statistics/TrackReadingProgressUseCase.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ReadingTimeIndicator.kt`

---

## Stats Sync to Supabase

**Trigger:** Automatic every 5 minutes, or manual "Sync" button on leaderboard

### Flow

1. `StatisticsSyncService` reads local stats, fetches remote from `leaderboard` table
2. **Merge strategy:** `max(local, remote)` per field — prevents data loss across devices
3. Upserts merged values to `leaderboard` table
4. Derives `ReaderLevel` for display fields (level, title, xp)
5. Calls `checkAndAwardAchievements()` — checks thresholds and awards badges

**Supabase tables involved:** `leaderboard`, `user_badges`

**Key files:**
- `data/src/commonMain/kotlin/ireader/data/statistics/StatisticsSyncService.kt`

---

## Achievement System

**Trigger:** `ProfileViewModel.loadGamification()` when signed in

### Flow

1. Client calls `syncReadingStats()` RPC with local stats (minutes, chapters, books, streak, WPM, genres)
2. Server-side `sync_reading_stats` SQL function:
   - Monotonic merge into `leaderboard` table
   - Calls `evaluate_achievements()` which iterates **37 achievement definitions** across 12 categories
   - For each newly earned achievement: grants XP, spirit stones, titles, and badges
   - **Recomputes level and title** via `calculate_level(xp)` and `level_title_for(level)`
3. Returns `List<UnlockedAchievement>` to client
4. `AchievementUnlockDialog` shows on Profile screen with celebration

### Achievement Categories

| Category | Metrics | Example |
|---|---|---|
| READING_TIME | 60 min to 60,000 min | Read for 1 hour |
| CHAPTERS | 10 to 10,000 | Read 100 chapters |
| BOOKS | 1 to 100 | Finish 10 books |
| STREAK | 3 to 365 days | 7-day streak |
| CHECKIN | 7 to 365 days | Check in 30 days |
| REVIEW | 1 to 50 reviews | Write 10 reviews |
| HELPFUL | 10 to 500 votes | Get 100 helpful votes |
| VOTE | 10 to 1000 votes | Cast 100 power-stone votes |
| GENRE | 3 to 10 genres | Explore 5 genres |
| SPEED | 300 to 500 WPM | Read at 300+ WPM |
| SOCIAL | 1 to 50 followers | Get 10 followers |
| DISCORD | 1 (link account) | Link Discord |

**Key files:**
- `supabase/migrations/002_achievement_seed.sql` (37 achievement definitions)
- `data/src/commonMain/kotlin/ireader/data/gamification/GamificationRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/ireader/domain/models/gamification/GamificationModels.kt`

---

## Spirit Stones

Spirit stones are the **earned-only cosmetic currency**. Nothing costs real money.

### Earning Sources

| Source | Reward | Trigger |
|---|---|---|
| Daily check-in | 10/50/200 💎 | Tap "Claim" on Profile |
| Achievement unlock | Varies by tier | Sync stats when signed in |
| Daily reading challenge | 5-50 💎 | Complete daily goal |
| Weekly reading challenge | 20-200 💎 | Complete weekly goal |
| Monthly reading challenge | 50-500 💎 | Complete monthly goal |
| Milestones | 20-2000 💎 | Reach milestone thresholds |

### Spending

`spendStones(itemType, itemId, cost)` RPC — cosmetic titles and badges only.

**Key files:**
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/GamificationRepository.kt` (line 50)
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/spiritstone/SpiritStoneShopScreen.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/spiritstone/SpiritStoneShopViewModel.kt`

---

## Daily Check-in

**Trigger:** User taps "Claim" on `CheckinPanel` in Profile

### Flow

1. Calls `checkin_daily()` RPC
2. Server calculates streak (consecutive days) and reward amount
3. Returns `{already: bool, streak_day: int, reward: int}`
4. If not already checked in: updates streak display and spirit stone balance
5. Streak milestones: day 10, 50, 200 grant bonus stones

**Key files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/auth/ProfileViewModel.kt` (`checkIn()` method)
- `data/src/commonMain/kotlin/ireader/data/gamification/GamificationRepositoryImpl.kt` (`checkinDaily()`)

---

## Badges

### Two Awarding Paths

**Path A: Server-side via `evaluate_achievements()`**
- When an achievement completes, if it has a `reward_badge_id`, the badge is granted automatically

**Path B: Client-side via `StatisticsSyncService`**
- Checks hardcoded thresholds and inserts into `user_badges`:
  - Chapters: 10→novice_reader, 100→avid_reader, 500→bookworm, 1000→master_reader
  - Books: 1→first_finish, 10→book_collector, 50→library_master, 100→legendary_collector
  - Streak: 7→week_warrior, 30→month_master, 365→year_legend
  - Reviews: 1→first_critic, 10→thoughtful_critic, 50→master_critic, 100→legendary_critic

### Badge Types

| Type | Description |
|---|---|
| ACHIEVEMENT | Earned through reading milestones |
| PURCHASABLE | Bought with payment proof (admin-verified) |
| DEVELOPER | Grants access to Developer Portal |

### Rarity Levels

| Rarity | Color | Spirit Stone Cost |
|---|---|---|
| COMMON | Brown | 50 💎 |
| RARE | Blue | 150 💎 |
| EPIC | Purple | 300 💎 |
| LEGENDARY | Gold | 800 💎 |

### Badge Images

Badges use images from `https://raw.githubusercontent.com/IReaderorg/badge-repo/main/`. Each badge has an `image_url` field in the `badges` table.

### Profile Display

Horizontal scroll `BadgesShowcase` on profile with:
- Rarity-colored circular glow rings
- Primary badge (shown on reviews)
- Featured badges (max 3, shown on profile)

**Key files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/remote/Badge.kt`
- `data/src/commonMain/kotlin/ireader/data/badge/BadgeRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/ireader/domain/usecases/badge/CheckReadingAchievementsUseCase.kt`

---

## Reading Challenges

Challenges are **purely local** — stored in `PreferenceStore`, not synced to Supabase.

### Challenge Types

| Type | Goal Range | Time Window | Reward Formula |
|---|---|---|---|
| Daily | 15-90 minutes | Today (midnight to midnight) | `(minutes / 10).coerceIn(5, 50)` 💎 |
| Weekly | 2-7 hours | Monday to Sunday | `(minutes / 5).coerceIn(20, 200)` 💎 |
| Monthly | 10-30 hours | 30-day window | `(minutes / 3).coerceIn(50, 500)` 💎 |

### Flow

1. User sets a goal on Reading Hub via `ReadingChallengeCard`
2. Progress tracked via `getStatisticsFlow()` reactive observation
3. `updateChallengeProgress()` called on every stats change
4. When `currentMinutes >= goalMinutes`, challenge marked complete
5. Spirit stones awarded locally

**Key files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/gamification/ReadingChallenge.kt`
- `domain/src/commonMain/kotlin/ireader/domain/data/repository/ReadingChallengeRepository.kt`
- `data/src/commonMain/kotlin/ireader/data/challenge/ReadingChallengeRepositoryImpl.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readinghub/ReadingChallengeCard.kt`

---

## Milestones

**16 milestones** across 4 metrics. Checked on every stats update.

### Milestone Table

| Category | ID | Title | Threshold | Reward |
|---|---|---|---|---|
| Books | books_10 | Bookworm Initiate | 10 books | 50 💎 |
| Books | books_50 | Literary Explorer | 50 books | 200 💎 |
| Books | books_100 | Century Reader | 100 books | 500 💎 |
| Books | books_500 | Reading Master | 500 books | 1000 💎 |
| Chapters | chapters_100 | Chapter Hunter | 100 chapters | 50 💎 |
| Chapters | chapters_1000 | Chapter Devourer | 1,000 chapters | 200 💎 |
| Chapters | chapters_5000 | Chapter Legend | 5,000 chapters | 500 💎 |
| Chapters | chapters_10000 | Chapter Deity | 10,000 chapters | 1000 💎 |
| Streak | streak_7 | Week Warrior | 7 days | 20 💎 |
| Streak | streak_30 | Monthly Master | 30 days | 100 💎 |
| Streak | streak_100 | Century Streak | 100 days | 300 💎 |
| Streak | streak_365 | Year Legend | 365 days | 1000 💎 |
| Time | time_1000 | Dedicated Reader | 1,000 min | 50 💎 |
| Time | time_5000 | Time Lord | 5,000 min | 200 💎 |
| Time | time_10000 | Eternal Reader | 10,000 min | 500 💎 |
| Time | time_50000 | Reading Immortal | 50,000 min | 2000 💎 |

### Flow

1. `ReadingHubViewModel.checkMilestones()` called on every stats change
2. Iterates `Milestones.ALL`, skipping seen milestones
3. Compares relevant metric against threshold
4. First qualifying milestone triggers celebration overlay
5. Marked as "seen" in `PreferenceStore` to avoid re-showing
6. Multiple milestones queue and display sequentially

**Key files:**
- `domain/src/commonMain/kotlin/ireader/domain/models/gamification/ReadingChallenge.kt` (`Milestones` object)
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readinghub/ReadingHubViewModel.kt` (`checkMilestones()`)
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/readinghub/MilestoneCelebrationOverlay.kt`

---

## Leaderboard

**Trigger:** User opens the Leaderboard screen

### Flow

1. Queries `leaderboard` table ordered by `total_reading_time_minutes DESC`
2. Each entry derives level via `ReaderLevel.fromMinutes()`
3. User rank = count of users with more reading time + 1
4. Real-time updates via Supabase realtime channel

### Tier System (Percentile-Based)

| Tier | Percentile | Emblem |
|---|---|---|
| LEGEND | Top 1% | 👑 |
| DIAMOND | Top 5% | 💎 |
| PLATINUM | Top 15% | 💠 |
| GOLD | Top 35% | 🥇 |
| SILVER | Top 65% | 🥈 |
| BRONZE | Rest | 🥉 |

### UI Components

- **Podium:** Top 3 users with gold/silver/bronze columns
- **Rank rows:** All entries with level badge, XP, reading time
- **YOU bar:** Sticky bottom bar showing current user's rank, tier, percentile
- **UserProfileSheet:** Tap a row to see profile details

**Key files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/leaderboard/LeaderboardScreen.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/leaderboard/LeaderboardViewModel.kt`
- `data/src/commonMain/kotlin/ireader/data/leaderboard/LeaderboardRepositoryImpl.kt`
- `domain/src/commonMain/kotlin/ireader/domain/models/entities/ReaderLevel.kt`

---

## Profile Display

The Profile screen (`WebnovelProfile`) shows all gamification data:

### Layout (Top to Bottom)

1. **ProfileHeader:** Cover image, avatar with level ring, username, level title, bio, joined date
   - Pills: 💎 spirit stones, 🏆 rank, 🔥 streak, Discord status
2. **StatStrip:** Time, Books, Streak, Chapters
3. **XpPanel:** Level number, XP, gradient progress bar, "Next: Level N+1"
4. **CheckinPanel:** Daily check-in card with streak and "Claim" button (signed in only)
5. **ActiveTitlePanel:** Title name with rarity-colored glow border
6. **BadgesShowcase:** Horizontal scroll of badge images with rarity glow rings
7. **AchievementsShowcase:** Horizontal scroll of achievement medallions with progress bars
8. **TitlesPanel:** Clickable title chips to activate/deactivate
9. **FavoriteBooksPanel:** Horizontal scroll of book covers
10. **ActivityPanel:** Colored activity feed (achievements, reviews, votes)
11. **CommentsPanel:** Comment wall with post input (signed in only)
12. **DiscordPanel:** Link to Discord community

**Key files:**
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/auth/ProfileViewModel.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/auth/WebnovelProfile.kt`
- `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/auth/GamificationSections.kt`

---

## Data Flow Summary

```
Reading Activity (Reader Screen)
  │
  ▼
TrackReadingProgressUseCase (local DB: chapters, minutes, streak)
  │
  ├──→ ReaderTimeIndicator (UI: live session timer)
  │
  ▼
ReadingStatisticsRepository (local persistence)
  │
  ├──→ ReadingHubViewModel
  │       ├──→ Statistics UI (stat strip, progress)
  │       ├──→ Challenges (daily/weekly/monthly goals, stone rewards)
  │       ├──→ Milestones (16 milestones, celebration dialogs)
  │       └──→ Reading Buddy (mood, messages, XP)
  │
  ├──→ ProfileViewModel
  │       ├──→ ReaderLevel (local-first level/XP from minutes)
  │       ├──→ GamificationRepository.syncReadingStats (server RPC)
  │       ├──→ GamificationRepository.getProfile (level, stones, streak, titles)
  │       ├──→ GamificationRepository.getAchievements
  │       ├──→ GamificationRepository.checkinDaily (daily check-in → stones)
  │       └──→ WebnovelProfile (full profile display)
  │
  ├──→ StatisticsSyncService
  │       ├──→ Supabase leaderboard table (merge + upsert)
  │       └──→ checkAndAwardAchievements (badges table)
  │
  ├──→ LeaderboardRepository
  │       ├──→ Supabase leaderboard table (read + realtime)
  │       └──→ LeaderboardScreen (podium, rank rows, tier display)
  │
  └──→ BadgeRepository
          ├──→ Supabase user_badges / badges tables
          ├──→ get_user_badges RPC
          ├──→ check_and_award_achievement_badge RPC
          └──→ BadgesShowcase on profile
```

### Two Parallel Systems

1. **Legacy/Supabase-direct path:** `StatisticsSyncService` + `LeaderboardRepositoryImpl` + `BadgeRepositoryImpl` — uses Supabase client directly, upserts to `leaderboard` and `user_badges` tables
2. **Newer `BackendService` path:** `GamificationRepositoryImpl` — uses a backend abstraction with RPCs like `sync_reading_stats`, `evaluate_achievements`, `checkin_daily`, `spend_stones`

Both run in parallel when the user is signed in. The local statistics database is the source of truth before sign-in; once signed in, the server becomes canonical.
