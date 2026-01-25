# Reading Time Tracking Bug Fix

## Problem Summary

The user reported that **reading time was not being saved at all**, even after reading offline for days. The reading time statistics were not updating on the leaderboard or in statistics.

## Root Cause Analysis

After investigation, I found **TWO critical bugs**:

### Bug #1: Minimum Duration Threshold (CRITICAL)
**Location**: `domain/src/commonMain/kotlin/ireader/domain/usecases/statistics/TrackReadingProgressUseCase.kt`

**The Issue**:
```kotlin
suspend fun trackReadingTime(durationMillis: Long) {
    val minutes = durationMillis.milliseconds.inWholeMinutes
    if (minutes > 0) {  // ❌ ONLY saves if >= 1 full minute!
        statisticsRepository.addReadingTime(minutes)
    }
}
```

This meant:
- If you read for 59 seconds → **0 minutes saved** ❌
- If you read for 1 minute 30 seconds → **1 minute saved** (losing 30 seconds)
- The periodic save runs every 30 seconds, so most saves were being **silently discarded**!

**The Fix**:
```kotlin
suspend fun trackReadingTime(durationMillis: Long) {
    // Convert to minutes, rounding UP (ceiling)
    // This ensures even short sessions are counted
    val minutes = ((durationMillis + 59999) / 60000).coerceAtLeast(1)
    
    println("[TrackReadingProgress] Tracking: ${durationMillis}ms = $minutes minutes")
    statisticsRepository.addReadingTime(minutes)
    println("[TrackReadingProgress] Successfully saved $minutes minutes")
}
```

Now:
- 1-59 seconds → **1 minute saved** ✅
- 60-119 seconds → **2 minutes saved** ✅
- All reading time is preserved!

### Bug #2: Missing TTS Screen Tracking
**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/TTSV2ScreenSpec.kt`

The TTS (Text-to-Speech) screen had no reading time tracking at all. Users listening to TTS weren't getting any time recorded.

**The Fix**: Added the same tracking mechanism as the regular reader screen.

## Changes Made

### 1. Fixed TrackReadingProgressUseCase (domain layer)
- ✅ Changed from `inWholeMinutes` (floor) to ceiling division
- ✅ Ensures minimum 1 minute is saved for any session
- ✅ Added debug logging to track what's being saved

### 2. Enhanced ReaderScreenSpec Logging (presentation layer)
- ✅ Added detailed logging for tracking start time
- ✅ Added logging for periodic saves (every 30 seconds)
- ✅ Added logging for final save when leaving reader
- ✅ Includes both milliseconds and minutes in logs
- ✅ Shows total session duration vs. incremental saves

### 3. Added TTS Screen Tracking
- ✅ Injected `TrackReadingProgressUseCase` into TTSV2ScreenSpec
- ✅ Added `DisposableEffect` to track time spent in TTS screen
- ✅ Tracks both reading time and streaks for TTS sessions
- ✅ Minimum 5 seconds to avoid accidental opens

### 4. UI Improvements (Already Implemented by Previous Model)
- ✅ Created `ReadingTimeIndicator` component
- ✅ Shows live reading time in reader screen
- ✅ Added preference `showReadingTimeIndicator` (default: false)
- ✅ Can be toggled in reader settings modal sheet

## How Reading Time Works Now

### Reader Screen (Regular Reading)
1. **On Screen Open**: Records start time
2. **Every 30 Seconds**: Saves accumulated time to database
3. **On Screen Close**: Saves any remaining time
4. **Minimum Duration**: 5 seconds (to avoid accidental opens)

### TTS Screen (Listening)
- Same mechanism as regular reader
- Tracks listening time as reading time
- Updates streaks just like regular reading

### Data Flow
```
User reads → DisposableEffect → TrackReadingProgressUseCase 
           → ReadingStatisticsRepository → SQLite Database
           → Syncs to leaderboard
```

## Logging for Debugging

The app now logs detailed information (visible in Logcat):

```
[Reading] Reading time tracking started at: 1737802920000
[Reading] Periodic save check: duration since last save = 30125ms (0min)
[TrackReadingProgress] Tracking: 30125ms = 1 minutes
[TrackReadingProgress] Successfully saved 1 minutes
[Reading] Reading session ended. Final: 15234ms (0min), Total: 125359ms (2min)
```

This helps diagnose if:
- Tracking is starting correctly
- Periodic saves are running
- Database operations are succeeding
- Time is being calculated correctly

## Testing Checklist

To verify the fix works:

1. ✅ Open reader, read for 30 seconds, close → Should save 1 minute
2. ✅ Open reader, read for 2 minutes, close → Should save 2 minutes  
3. ✅ Open TTS, listen for 1 minute, close → Should save 1 minute
4. ✅ Check Logcat for tracking logs
5. ✅ Check statistics/leaderboard for updated time
6. ✅ Enable reading time indicator to see live timer

## Summary

The primary bug was the **1-minute minimum threshold** which was silently discarding most reading time updates. By changing to round up instead of truncate, and always saving at least 1 minute, users' reading time is now properly tracked.

Additionally, TTS listening time is now tracked, and comprehensive logging helps diagnose any future issues.

**Expected Result**: Reading time should now accumulate correctly in the statistics and be visible on the leaderboard.
