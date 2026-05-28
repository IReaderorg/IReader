# LNReader Import Feature - Developer Guide

## Overview

This document describes the LNReader backup import feature, which allows users to import their library from LNReader (a third-party novel reader app) into IReader.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Backup Screen (UI)                        │
│  User clicks "Import from LNReader" → picks .zip file       │
└──────────────────────────┬──────────────────────────────────┘
                           │ URI
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              BackupScreenViewModel                           │
│  importLNReaderBackupFromUri(uri)                            │
│    → collects progress from ImportLNReaderBackup.invoke()    │
│    → maps to LNReaderImportProgress for dialog               │
└──────────────────────────┬──────────────────────────────────┘
                           │ Flow<ImportProgress>
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              ImportLNReaderBackup (Common)                   │
│                                                              │
│  invoke(uri, options)                                        │
│    → importFromUri(uri, options)                             │
│        ├─ Android: uses streamingImporter (low memory)       │
│        └─ Other:  loads entire file into memory (fallback)   │
│                                                              │
│  Platform-specific streaming:                                │
│    → LNReaderStreamingImporter.import(uri, options)          │
│        → Parses ZIP entries one at a time                    │
│        → Imports novels + chapters immediately               │
│        → Imports categories after novels                     │
│                                                              │
│  Shared import logic:                                        │
│    → importBackupData(backup, options)                       │
│        → importNovel() for each novel                        │
│        → importCategory() for each category                  │
│        → associate novels with categories                    │
└─────────────────────────────────────────────────────────────┘
```

## File Structure

```
domain/src/
├── commonMain/kotlin/ireader/domain/usecases/backup/lnreader/
│   ├── ImportLNReaderBackup.kt          # Main use case (common logic)
│   ├── LNReaderBackupParser.kt          # JSON parsing for backup files
│   ├── LNReaderSourceMapper.kt          # Maps plugin IDs to source IDs
│   ├── LNReaderImportException.kt       # Exception types
│   ├── LNReaderImportGuide.md           # This file
│   └── models/
│       ├── LNReaderBackup.kt            # Backup data model
│       ├── LNReaderNovel.kt             # Novel data model
│       ├── LNReaderChapter.kt           # Chapter data model
│       └── LNReaderCategory.kt          # Category data model
│
├── androidMain/kotlin/ireader/domain/usecases/backup/lnreader/
│   ├── ImportLNReaderBackupStreaming.kt # Android streaming implementation
│   └── LNReaderBackupParserPlatform.kt  # Android platform parser
│
└── desktopMain/java/ireader/domain/usecases/backup/lnreader/
    └── ImportLNReaderBackup.desktop.kt  # Desktop stub (uses fallback)
```

## Backup File Format

LNReader backups are ZIP files with the following structure:

```
lnreader_backup_YYYY-MM-DD_HH_MM.zip
├── Version.json              # Backup version (e.g., {"version":"2.0.3"})
├── Category.json             # Categories with novel IDs
├── Setting.json              # App settings (not imported)
├── NovelAndChapters/
│   ├── 1.json                # Novel 1 with all chapters
│   ├── 2.json                # Novel 2 with all chapters
│   └── ...
└── download.zip              # Downloaded content (skipped, ~100MB)
```

### Novel JSON Format

```json
{
  "id": 1,
  "path": "fiction/27781",
  "pluginId": "royalroad",
  "name": "Horizon",
  "cover": "/Novels/royalroad/1/cover.png",
  "summary": "A man from Earth...",
  "author": "Avery Light",
  "status": "Completed",
  "genres": "Reincarnation, Action, Fantasy",
  "inLibrary": 1,
  "isLocal": 0,
  "totalPages": 0,
  "chapters": [
    {
      "id": 1,
      "novelId": 1,
      "path": "fiction/27781/chapter/414853",
      "name": "Chapter 1 - In Deep",
      "releaseTime": "2019-10-20T13:14:13Z",
      "bookmark": 0,
      "unread": 0,
      "isDownloaded": 1,
      "chapterNumber": 0,
      "position": 0,
      "progress": 100
    }
  ]
}
```

## Import Process

### Step 1: File Selection
- User selects a `.zip` file via Android's file picker
- URI is passed to `BackupScreenViewModel.importLNReaderBackupFromUri()`

### Step 2: Streaming Import (Android)
The `LNReaderStreamingImporter` processes the ZIP file entry-by-entry:

1. **Open InputStream** from URI (on IO dispatcher)
2. **Wrap in BufferedInputStream** (64KB buffer) + ZipInputStream
3. **For each ZIP entry**:
   - `Version.json` → Skip (version info only)
   - `Category.json` → Parse and collect categories
   - `Setting.json` → Skip (settings not imported)
   - `NovelAndChapters/*.json` → Parse novel + import immediately
   - Other entries → Skip efficiently (important for large `download.zip`)
4. **Import categories** and associate with novels
5. **Emit Complete** with import result

### Step 3: Novel Import
For each novel in the backup:

1. **Map plugin ID** → IReader source ID (via `LNReaderSourceMapper`)
2. **Check if book exists** in database
3. **Handle conflict** based on strategy:
   - `SKIP`: Skip existing novels
   - `MERGE`: Merge reading progress only
   - `OVERWRITE`: Delete and re-import
4. **Insert/update book** in database
5. **Insert chapters** in database

### Step 4: Category Import
1. Create categories that don't exist
2. Associate novels with their categories
3. Create "LNReader" category for novels in default categories

## Platform-Specific Implementations

### Android (Streaming)
- **File**: `ImportLNReaderBackupStreaming.kt`
- **Approach**: Streaming ZIP parsing (entry-by-entry)
- **Memory**: Low (processes one entry at a time)
- **Registration**: Via DI using `LNReaderStreamingImporter` interface

### Desktop/Other (Fallback)
- **File**: `ImportLNReaderBackup.desktop.kt` (stub)
- **Approach**: Load entire file into memory
- **Memory**: High (loads full ZIP as ByteArray)
- **Limitation**: May OOM on very large backups

## DI Registration

```kotlin
// Common (DomainModules.kt)
factory {
    ImportLNReaderBackup(...).also { instance ->
        // Inject platform-specific streaming importer if available
        instance.streamingImporter = getOrNull<ImportLNReaderBackup.LNReaderStreamingImporter>()
    }
}

// Android (DomainModule.kt)
factory<ImportLNReaderBackup.LNReaderStreamingImporter> {
    ImportLNReaderBackup(...).createAndroidStreamingImporter()
}
```

## Progress Reporting

The import emits progress updates via `Flow<ImportProgress>`:

```kotlin
sealed class ImportProgress {
    object Starting                                    // Import started
    data class Parsing(val message: String)            // Parsing backup
    data class ImportingNovels(current, total, name)   // Importing novels
    data class ImportingCategories(current, total)     // Importing categories
    data class Complete(val result: ImportResult)      // Import complete
    data class Error(val error: Throwable)             // Import failed
}
```

## Import Result

```kotlin
data class ImportResult(
    val novelsImported: Int,
    val novelsSkipped: Int,
    val novelsFailed: Int,
    val chaptersImported: Int,
    val categoriesImported: Int,
    val errors: List<ImportError>
)
```

## Chapter Content

**Important**: LNReader backups do NOT contain chapter content (the actual text of each chapter). They only contain chapter metadata (name, path, read status, etc.). Chapter content is fetched from the source when the user opens the chapter in IReader.

This is by design - the `content` field in imported chapters will be empty (`emptyList()`).

## Error Handling

The import handles various error scenarios:

- **Invalid backup**: File is not a valid LNReader backup
- **Corrupted backup**: ZIP parsing fails
- **Empty backup**: No novels or categories found
- **Database errors**: Failed to insert/update books or chapters
- **Unknown errors**: Caught and reported via `ImportProgress.Error`

## Adding a New Platform

To add streaming import support for a new platform (e.g., iOS):

1. Create `ImportLNReaderBackup.ios.kt` in the iOS source set
2. Implement `createIosStreamingImporter()` function
3. Register in iOS DI module:
   ```kotlin
   factory<ImportLNReaderBackup.LNReaderStreamingImporter> {
       ImportLNReaderBackup(...).createIosStreamingImporter()
   }
   ```

## Testing

Test the import with:
1. A small backup file (few novels)
2. A large backup file (98+ novels, ~100MB)
3. A backup with categories
4. A backup with existing novels (test conflict strategies)

## Common Issues

1. **Stuck on "Parsing"**: Usually means the streaming importer isn't registered. Check DI configuration.
2. **Empty chapters**: Expected - content is fetched on read.
3. **OOM on desktop**: Expected for large backups - use Android for large imports.
4. **Missing source mapping**: Novels with unmapped plugin IDs get source ID -1 (local).
