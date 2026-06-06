# Fix: Leaderboard Reading Time Sync Bug

## Problem

There are two sync paths for leaderboard statistics, and they behave differently:

### Path 1: `StatisticsSyncService.syncStatistics()` (CORRECT)
- Fetches remote stats from leaderboard
- Takes `maxOf(local, remote)` for each field
- Upserts merged values to remote
- Updates local if remote had higher values

### Path 2: `LeaderboardUseCases.syncCurrentUserStats()` (BUGGY)
- Called by the UI "Sync" button in `LeaderboardViewModel.syncUserStats()`
- Takes **local** stats and pushes directly to remote via `syncUserStats()`
- Does NOT fetch remote first
- Does NOT compare values
- Does NOT update local if remote is higher

### Impact
If a user reads on Device A (syncs 500 min), then reads on Device B (200 min local), pressing Sync on Device B **overwrites** the leaderboard with 200 min, losing 300 min. Device B's local time also never gets updated to 500 min.

## Current Data Flow (Buggy)

```
User taps Sync button
    -> LeaderboardViewModel.syncUserStats()
        -> LeaderboardUseCases.syncCurrentUserStats()
            -> Gets local stats (e.g., 200 min)
            -> Pushes local stats directly to remote (OVERWRITES 500 min with 200 min)
            -> Returns success
        -> Reloads leaderboard (now shows 200 min - DATA LOSS)
```

## Desired Data Flow

```
User taps Sync button
    -> LeaderboardViewModel.syncUserStats()
        -> LeaderboardUseCases.syncCurrentUserStats()
            -> Gets local stats (e.g., 200 min)
            -> Fetches remote stats (e.g., 500 min)
            -> Merges: takes max(200, 500) = 500 min
            -> Upserts 500 min to remote (no data loss)
            -> Updates local to 500 min (syncs from remote)
            -> Returns success
        -> Reloads leaderboard (shows correct 500 min)
```

## Implementation Plan

### Step 1: Add `getUserLeaderboardEntry()` to `LeaderboardRepository` interface
- Add a method to fetch the current user's leaderboard entry by userId
- Returns `Result<UserLeaderboardStats?>` - null if user not on leaderboard yet

### Step 2: Implement `getUserLeaderboardEntry()` in `LeaderboardRepositoryImpl`
- Query the leaderboard table filtering by `user_id`
- Map the DTO to `UserLeaderboardStats` domain object
- Return null if no entry found

### Step 3: Fix `LeaderboardUseCases.syncCurrentUserStats()`
- Fetch remote stats using the new repository method
- If remote exists, merge using `maxOf(local, remote)` for all fields
- Upsert merged stats to remote
- If remote had higher values, update local statistics to match

### Step 4: Verify `LeaderboardViewModel` behavior
- After sync, `loadUserRank()` and `loadLeaderboard()` are already called
- These will now show the correct merged values
- No changes needed in ViewModel

## Files to Modify

| File | Change |
|------|--------|
| `domain/.../repository/LeaderboardRepository.kt` | Add `getUserLeaderboardEntry(userId): Result<UserLeaderboardStats?>` |
| `data/.../leaderboard/LeaderboardRepositoryImpl.kt` | Implement `getUserLeaderboardEntry()` |
| `domain/.../usecases/leaderboard/LeaderboardUseCases.kt` | Rewrite `syncCurrentUserStats()` with merge logic |

## Merge Logic (per field)

| Field | Merge Rule |
|-------|-----------|
| `totalReadingTimeMinutes` | `maxOf(local, remote)` |
| `totalChaptersRead` | `maxOf(local, remote)` |
| `booksCompleted` | `maxOf(local, remote)` |
| `readingStreak` | `maxOf(local, remote)` |
| `hasBadge` | Preserve remote (badge status) |
| `badgeType` | Preserve remote |

## Edge Cases

1. **No remote entry exists**: Use local values (first sync)
2. **No local reading time**: Use remote values (restore from cloud)
3. **Both exist**: Take maximum of each field
4. **User not authenticated**: Return failure (already handled)
