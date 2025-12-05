# Translation Service - Developer Documentation

## Architecture Overview

The mass translation feature follows clean architecture with clear separation between domain, data, and presentation layers.

```
┌─────────────────────────────────────────────────────────────────┐
│                      Presentation Layer                          │
│  ┌─────────────────────┐  ┌──────────────────────────────────┐  │
│  │ TranslationController│  │ UnifiedTranslationDialog         │  │
│  │ (State Management)   │  │ (UI Components)                  │  │
│  └─────────────────────┘  └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Domain Layer                              │
│  ┌─────────────────────┐  ┌──────────────────────────────────┐  │
│  │ TranslationService  │  │ TranslationUseCases              │  │
│  │ (Interface)         │  │ - SaveTranslatedChapterUseCase   │  │
│  │                     │  │ - GetTranslatedChapterUseCase    │  │
│  └─────────────────────┘  │ - DeleteTranslatedChapterUseCase │  │
│                           └──────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Data Layer                               │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │ TranslatedChapterRepositoryImpl                              ││
│  │ - SQLDelight queries (translatedChapter.sq)                  ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. TranslationService Interface
**Location:** `domain/src/commonMain/kotlin/ireader/domain/services/common/TranslationService.kt`

Core interface defining translation operations:
- `queueChapters()` - Queue chapters for translation
- `pause()` / `resume()` - Control translation flow
- `cancelTranslation()` / `cancelAll()` - Cancel operations
- `retryTranslation()` - Retry failed translations
- `requiresRateLimiting()` / `isOfflineEngine()` - Engine classification

### 2. TranslationServiceImpl
**Location:** `domain/src/commonMain/kotlin/ireader/domain/services/translationService/TranslationServiceImpl.kt`

Implementation handling:
- Queue management with `TranslationTask` objects
- Rate limiting for web-based AI engines
- Progress tracking via `TranslationStateHolder`
- Chapter content download when needed
- Translation via `TranslationEnginesManager`
- Storage to `translatedChapter` table (not original chapter)

### 3. TranslationStateHolder
**Location:** `domain/src/commonMain/kotlin/ireader/domain/services/translationService/TranslationServiceState.kt`

Manages reactive state using `StateFlow`:
- `isRunning` / `isPaused` - Service state
- `currentBookId` - Active book being translated
- `translationProgress` - Per-chapter progress map
- `totalChapters` / `completedChapters` - Progress counters

### 4. TranslationPreferences
**Location:** `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/TranslationPreferences.kt`

User preferences:
- `bypassTranslationWarning` - Skip rate limit warnings
- `translationRateLimitDelayMs` - Delay between requests (default: 3000ms)
- `translationWarningThreshold` - Chapter count to trigger warning (default: 10)
- `autoDownloadBeforeTranslate` - Auto-download empty chapters

### 5. TranslationUseCases
**Location:** `domain/src/commonMain/kotlin/ireader/domain/usecases/translation/TranslationUseCases.kt`

- `SaveTranslatedChapterUseCase` - Saves to `translated_chapter` table
- `GetTranslatedChapterUseCase` - Retrieves translations
- `DeleteTranslatedChapterUseCase` - Removes translations
- `ApplyGlossaryToTextUseCase` - Applies glossary replacements

## Database Schema

**File:** `data/src/commonMain/sqldelight/data/translatedChapter.sq`

```sql
CREATE TABLE translated_chapter(
    _id INTEGER PRIMARY KEY AUTOINCREMENT,
    chapter_id INTEGER NOT NULL,
    book_id INTEGER NOT NULL,
    source_language TEXT NOT NULL,
    target_language TEXT NOT NULL,
    translator_engine_id INTEGER NOT NULL,
    translated_content TEXT AS List<Page> NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL,
    FOREIGN KEY(chapter_id) REFERENCES chapter(_id) ON DELETE CASCADE,
    FOREIGN KEY(book_id) REFERENCES book(_id) ON DELETE CASCADE,
    UNIQUE(chapter_id, target_language, translator_engine_id)
);
```

Key points:
- Translations stored separately from original chapters
- Unique constraint on (chapter_id, target_language, engine_id)
- Cascading deletes when chapter/book is removed

## Translation Flow

```
1. User selects chapters → TranslationController.showForMassTranslation()
2. User configures options → UnifiedTranslationDialog
3. User clicks Translate → TranslationController.translate()
4. Service queues chapters → TranslationService.queueChapters()
   ├── Check rate limit warning (if applicable)
   ├── Create TranslationTask for each chapter
   └── Start translation job
5. Process queue → TranslationServiceImpl.processQueue()
   ├── Apply rate limiting (for web-based engines)
   ├── Download content if needed
   ├── Translate via TranslationEnginesManager
   └── Save to translated_chapter table
6. Update progress → TranslationStateHolder
7. UI updates → TranslationDialogState observes StateFlow
```

## Rate Limiting

### Engine Classification
```kotlin
// Offline engines (no rate limiting)
offlineEngineIds = setOf(0L, 4L, 5L)  // ML Kit, LibreTranslate, Ollama

// Rate-limited engines (web-based AI)
rateLimitedEngineIds = setOf(2L, 3L, 6L, 7L, 8L)  // OpenAI, DeepSeek, ChatGPT, Gemini
```

### Rate Limit Logic
- Burst size: 5 requests before enforcing delay
- Default delay: 3000ms between requests
- Warning threshold: 10+ chapters triggers confirmation

## UI Components

### UnifiedTranslationDialog
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/translation/UnifiedTranslationDialog.kt`

Three screens:
1. **Options** - Engine selection, language selection
2. **Warning** - Rate limit confirmation
3. **Progress** - Translation progress with pause/cancel

### TranslationController
**Location:** `presentation/src/commonMain/kotlin/ireader/presentation/ui/component/translation/TranslationController.kt`

Manages dialog state and service interaction:
- `showForSingleChapter()` - Reader/TTS context
- `showForMassTranslation()` - Detail screen context
- `translate()` - Start translation
- `pause()` / `resume()` / `cancel()` - Control operations

## Adding New Translation Engines

1. Add engine ID to `TranslationEngines.ALL` in `UnifiedTranslationDialog.kt`
2. Classify in `TranslationServiceImpl`:
   - Add to `offlineEngineIds` if local
   - Add to `rateLimitedEngineIds` if web-based
3. Implement engine in `TranslationEnginesManager`

## Testing

```kotlin
// Unit test example
@Test
fun `queueChapters returns warning for large batch with rate-limited engine`() = runTest {
    val result = translationService.queueChapters(
        bookId = 1L,
        chapterIds = (1..15).map { it.toLong() },
        sourceLanguage = "en",
        targetLanguage = "zh",
        engineId = 2L, // OpenAI (rate-limited)
        bypassWarning = false
    )
    
    assertTrue(result is ServiceResult.Success)
    assertTrue((result as ServiceResult.Success).data is TranslationQueueResult.RateLimitWarning)
}
```

## Error Handling

- Download failures: Chapter marked as FAILED, can retry
- Translation failures: Chapter marked as FAILED with error message
- Service cancellation: All queued chapters marked as CANCELLED
- Rate limit exceeded: Automatic delay applied

## Performance Considerations

- Translations stored in separate table (no chapter table bloat)
- Indexed on chapter_id and book_id for fast lookups
- StateFlow for efficient UI updates
- Coroutine-based for non-blocking operations
