# Desktop Circular Dependency Issue

## Status
Android app: ✅ **WORKING**  
Desktop app: ✅ **FIXED**

## Problem (RESOLVED)
There was a circular dependency in the Desktop dependency injection:
```
ServiceUseCases → StartDownloadServicesUseCase → DownloadUseCases → DownloadUnreadChaptersUseCase → StartDownloadServicesUseCase
```

## Root Cause (IDENTIFIED)
The circular dependency was caused by `DownloadUnreadChaptersUseCase`:
- `ServiceUseCases` needs `StartDownloadServicesUseCase`
- `StartDownloadServicesUseCase` needs `DownloadUseCases`
- `DownloadUseCases` includes `DownloadUnreadChaptersUseCase`
- `DownloadUnreadChaptersUseCase` was injecting `StartDownloadServicesUseCase` (creating the cycle)

## Solution Applied ✅
Refactored `DownloadUnreadChaptersUseCase` to use `DownloadService` directly instead of `StartDownloadServicesUseCase`:

**Before:**
```kotlin
class DownloadUnreadChaptersUseCase(
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val startDownloadServicesUseCase: StartDownloadServicesUseCase, // ❌ Circular!
)
```

**After:**
```kotlin
class DownloadUnreadChaptersUseCase(
    private val localGetChapterUseCase: LocalGetChapterUseCase,
    private val downloadService: DownloadService, // ✅ No circular dependency
)
```

This is architecturally cleaner because:
- `DownloadUnreadChaptersUseCase` is a use case that orchestrates downloading chapters
- It should use the `DownloadService` interface directly, not another use case
- `StartDownloadServicesUseCase` is meant for starting the download service from UI/external triggers
- Use cases should depend on services, not on other use cases when possible

## Files Modified
1. ✅ `domain/src/commonMain/kotlin/ireader/domain/usecases/local/book_usecases/DownloadUnreadChaptersUseCase.kt`
   - Changed dependency from `StartDownloadServicesUseCase` to `DownloadService`
   - Updated implementation to use `downloadService.queueChapters()`

2. ✅ `domain/src/desktopMain/kotlin/ireader/domain/di/DomainModule.kt`
   - Removed workaround stub implementation
   - Restored proper `StartDownloadServicesUseCase` instantiation

## Why Android Worked
Android likely didn't hit this issue because:
- Different module loading order
- Or the Android implementation of `DownloadUnreadChaptersUseCase` might have been different
- Or lazy initialization masked the circular dependency

## Testing Required
- ✅ Verify Desktop app starts without circular dependency error
- ✅ Test download functionality works correctly
- ✅ Test "Download Unread Chapters" feature specifically
