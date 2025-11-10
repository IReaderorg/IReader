# Auto-Chapter Repair System

## Overview

The Auto-Chapter Repair System automatically detects broken or corrupted chapters and attempts to find working replacements from alternative sources.

## Components

### 1. ChapterHealthChecker
**Location:** `domain/src/commonMain/kotlin/ireader/domain/services/ChapterHealthChecker.kt`

Detects broken chapters based on:
- Word count < 50
- Empty or blank content
- Low alphabetic character ratio (< 50%) indicating scrambled text

### 2. ChapterHealth Data Model
**Location:** `domain/src/commonMain/kotlin/ireader/domain/models/entities/ChapterHealth.kt`

Tracks chapter health status including:
- Whether chapter is broken
- Reason for breakage
- Repair attempt timestamp
- Repair success status
- Replacement source ID

### 3. ChapterHealthRepository
**Location:** 
- Interface: `domain/src/commonMain/kotlin/ireader/domain/data/repository/ChapterHealthRepository.kt`
- Implementation: `data/src/commonMain/kotlin/ireader/data/chapterhealth/ChapterHealthRepositoryImpl.kt`
- Database Schema: `data/src/commonMain/sqldelight/data/chapterHealth.sq`

Provides CRUD operations for chapter health records with 24-hour caching.

### 4. AutoRepairChapterUseCase
**Location:** `domain/src/commonMain/kotlin/ireader/domain/usecases/chapter/AutoRepairChapterUseCase.kt`

Searches all installed sources for working chapter replacements:
1. Checks if repair was recently attempted (24-hour cooldown)
2. Searches all catalogs for the novel
3. Finds matching chapter by number or name
4. Validates replacement content
5. Updates database with repaired chapter
6. Records repair success/failure

### 5. UI Components

#### ChapterRepairBanner
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/components/ChapterRepairBanner.kt`

Displays when a broken chapter is detected with:
- Warning message
- "Repair" button
- "Dismiss" button
- Loading indicator during repair

#### ChapterRepairSuccessBanner
Shows success message when repair completes, auto-dismisses after 5 seconds.

### 6. ViewModel Integration
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/reader/viewmodel/ReaderScreenViewModelChapterHealth.kt`

Extension functions for ReaderScreenViewModel:
- `checkChapterHealth()` - Checks chapter health after loading
- `repairChapter()` - Triggers repair process
- `dismissRepairBanner()` - Hides repair banner
- `dismissRepairSuccessBanner()` - Hides success banner

## Usage Flow

1. User opens a chapter
2. `ReaderScreenViewModel.getLocalChapter()` loads the chapter
3. `checkChapterHealth()` is automatically called
4. If broken, repair banner appears
5. User clicks "Repair" button
6. `AutoRepairChapterUseCase` searches alternative sources
7. If found, chapter is replaced and success banner shows
8. If not found, error message is displayed

## Caching Strategy

- **Health Check Cache:** 24 hours
- **Repair Attempt Cache:** 24 hours (prevents repeated failed attempts)
- **Old entries cleanup:** Automatically removes entries older than 24 hours

## Error Handling

- Silently fails health checks to avoid disrupting reading
- Shows user-friendly error messages for repair failures
- Handles network errors gracefully
- Continues to next source if one fails

## Database Migration

The system requires a database migration to add the `chapterHealth` table. The migration is defined in `data/src/commonMain/sqldelight/data/chapterHealth.sq`.

## Future Enhancements

- Automatic repair without user interaction (configurable)
- Repair history tracking
- Source reliability scoring
- Batch repair for multiple broken chapters
- User preferences for repair behavior
