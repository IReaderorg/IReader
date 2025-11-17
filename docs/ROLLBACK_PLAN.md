# Rollback Plan for Mihon-Inspired Improvements

## Overview

This document outlines the comprehensive rollback plan for the Mihon-inspired improvements to IReader. It provides step-by-step procedures for reverting changes in case of critical issues, ensuring minimal disruption to users and maintaining data integrity.

## Table of Contents

1. [Rollback Triggers](#rollback-triggers)
2. [Rollback Procedures](#rollback-procedures)
3. [Data Recovery](#data-recovery)
4. [Verification Steps](#verification-steps)
5. [Communication Plan](#communication-plan)

## Rollback Triggers

### Critical Issues

Initiate rollback immediately if:

1. **Data Corruption**
   - User data is being corrupted or lost
   - Database integrity checks fail
   - Books or chapters disappear from library

2. **Crash Rate Increase**
   - Crash rate increases by more than 5%
   - Critical crashes affecting core functionality
   - ANR (Application Not Responding) rate spike

3. **Performance Degradation**
   - App startup time increases by more than 20%
   - Screen loading time increases by more than 30%
   - Memory usage increases by more than 50%
   - Battery drain increases significantly

4. **Functional Regressions**
   - Core features stop working (reading, library, downloads)
   - User cannot access their content
   - Sync failures causing data loss

### Warning Signs

Monitor and consider rollback if:

1. **User Complaints**
   - Significant increase in negative reviews
   - Multiple reports of the same issue
   - User satisfaction scores drop

2. **Error Rates**
   - Error rate increases by more than 10%
   - Specific error patterns emerge
   - Backend service failures

3. **Adoption Issues**
   - Users actively downgrading to previous version
   - Feature adoption lower than expected
   - Increased support tickets

## Rollback Procedures

### Phase 1: Immediate Response (0-15 minutes)

#### Step 1: Disable Feature Flags

```kotlin
// In FeatureFlags.kt or via remote config
FeatureFlags.disableAllFeatures()

// Specific flags to disable:
FeatureFlags.useNewRepositories = false
FeatureFlags.useStateScreenModel = false
FeatureFlags.useNewUIComponents = false
FeatureFlags.useResponsiveDesign = false
FeatureFlags.useFastScrollLists = false
```

#### Step 2: Stop Rollout

- Halt any ongoing staged rollout
- Prevent new users from receiving the update
- Notify team members of the rollback

#### Step 3: Assess Impact

```kotlin
// Check affected users
val affectedUsers = analyticsService.getAffectedUserCount()
val errorRate = analyticsService.getCurrentErrorRate()
val crashRate = analyticsService.getCurrentCrashRate()

Log.error("Rollback initiated: $affectedUsers users affected, " +
         "error rate: $errorRate%, crash rate: $crashRate%")
```

### Phase 2: Code Rollback (15-30 minutes)

#### Step 1: Revert Repository Changes

```kotlin
// In Koin modules
single<BookRepository> { 
    LegacyBookRepositoryImpl(get()) // Use legacy implementation
}

single<ChapterRepository> { 
    LegacyChapterRepositoryImpl(get())
}

single<CategoryRepository> { 
    LegacyCategoryRepositoryImpl(get())
}
```

#### Step 2: Revert State Management

```kotlin
// In screen implementations
@Composable
fun BookDetailScreen(bookId: Long) {
    // Use ViewModel instead of StateScreenModel
    val viewModel = viewModel<BookDetailViewModel>()
    
    // Legacy implementation
}
```

#### Step 3: Revert UI Components

```kotlin
// Replace new components with legacy ones
@Composable
fun ErrorDisplay(error: String, retry: () -> Unit) {
    // Use legacy ErrorScreen
    ErrorScreen(
        errorMessage = error,
        retry = retry
    )
}
```

### Phase 3: Database Rollback (30-60 minutes)

#### Step 1: Stop Database Migrations

```kotlin
// Disable automatic migrations
FeatureFlags.autoMigration = false

// Mark migration as incomplete
FeatureFlags.migrationCompleted = false
```

#### Step 2: Restore from Backup

```kotlin
suspend fun restoreDatabaseFromBackup() {
    try {
        handler.await {
            // Drop current tables
            execSQL("DROP TABLE IF EXISTS books")
            execSQL("DROP TABLE IF EXISTS chapters")
            execSQL("DROP TABLE IF EXISTS categories")
            
            // Restore from backup
            execSQL("ALTER TABLE books_backup RENAME TO books")
            execSQL("ALTER TABLE chapters_backup RENAME TO chapters")
            execSQL("ALTER TABLE categories_backup RENAME TO categories")
            
            // Verify restoration
            val bookCount = rawQuery("SELECT COUNT(*) FROM books", null).use {
                it.moveToFirst()
                it.getLong(0)
            }
            
            Log.info("Database restored: $bookCount books")
        }
    } catch (e: Exception) {
        Log.error("Database restoration failed", e)
        throw RollbackException("Failed to restore database", e)
    }
}
```

#### Step 3: Verify Data Integrity

```kotlin
suspend fun verifyDatabaseIntegrity(): Boolean {
    return try {
        handler.await {
            // Check book count
            val bookCount = rawQuery("SELECT COUNT(*) FROM books", null).use {
                it.moveToFirst()
                it.getLong(0)
            }
            
            // Check chapter count
            val chapterCount = rawQuery("SELECT COUNT(*) FROM chapters", null).use {
                it.moveToFirst()
                it.getLong(0)
            }
            
            // Check for orphaned chapters
            val orphanedChapters = rawQuery(
                "SELECT COUNT(*) FROM chapters WHERE bookId NOT IN (SELECT id FROM books)",
                null
            ).use {
                it.moveToFirst()
                it.getLong(0)
            }
            
            Log.info("Integrity check: $bookCount books, $chapterCount chapters, " +
                    "$orphanedChapters orphaned chapters")
            
            orphanedChapters == 0L
        }
    } catch (e: Exception) {
        Log.error("Integrity check failed", e)
        false
    }
}
```

### Phase 4: Dependency Injection Rollback (60-90 minutes)

#### Step 1: Update Koin Modules

```kotlin
// In repositoryInjectModule.kt
val repositoryModule = module {
    // Use legacy repositories
    single<BookRepository> { LegacyBookRepositoryImpl(get()) }
    single<ChapterRepository> { LegacyChapterRepositoryImpl(get()) }
    single<CategoryRepository> { LegacyCategoryRepositoryImpl(get()) }
    single<DownloadRepository> { LegacyDownloadRepositoryImpl(get()) }
    single<HistoryRepository> { LegacyHistoryRepositoryImpl(get()) }
    
    // Remove new use cases
    // single<GetBook> { GetBook(get()) }
    // single<GetChapters> { GetChapters(get()) }
}
```

#### Step 2: Restart Koin

```kotlin
// In Application class
fun rollbackDependencyInjection() {
    stopKoin()
    startKoin {
        androidContext(this@Application)
        modules(legacyModules)
    }
}
```

### Phase 5: Verification and Monitoring (90-120 minutes)

#### Step 1: Smoke Tests

```kotlin
suspend fun runSmokeTests(): TestResults {
    val results = TestResults()
    
    // Test book retrieval
    results.bookRetrieval = testBookRetrieval()
    
    // Test chapter loading
    results.chapterLoading = testChapterLoading()
    
    // Test library operations
    results.libraryOperations = testLibraryOperations()
    
    // Test reading functionality
    results.readingFunctionality = testReadingFunctionality()
    
    return results
}

suspend fun testBookRetrieval(): Boolean {
    return try {
        val repository = get<BookRepository>()
        val books = repository.findAllBooks()
        books.isNotEmpty()
    } catch (e: Exception) {
        Log.error("Book retrieval test failed", e)
        false
    }
}
```

#### Step 2: Monitor Key Metrics

```kotlin
class RollbackMonitor {
    suspend fun monitorPostRollback() {
        val metrics = collectMetrics()
        
        Log.info("""
            Post-rollback metrics:
            - Crash rate: ${metrics.crashRate}%
            - Error rate: ${metrics.errorRate}%
            - Startup time: ${metrics.startupTime}ms
            - Memory usage: ${metrics.memoryUsage}MB
            - Active users: ${metrics.activeUsers}
        """.trimIndent())
        
        if (metrics.crashRate > 2.0) {
            Log.error("Crash rate still high after rollback!")
        }
    }
}
```

## Data Recovery

### Backup Strategy

#### Automatic Backups

```kotlin
class AutomaticBackupService {
    suspend fun createBackup() {
        val timestamp = System.currentTimeMillis()
        val backupPath = "backups/database_$timestamp.db"
        
        try {
            // Copy database file
            databaseFile.copyTo(File(backupPath))
            
            // Store backup metadata
            backupMetadata.save(BackupInfo(
                timestamp = timestamp,
                path = backupPath,
                version = appVersion,
                bookCount = getBookCount(),
                chapterCount = getChapterCount()
            ))
            
            Log.info("Backup created: $backupPath")
        } catch (e: Exception) {
            Log.error("Backup creation failed", e)
        }
    }
}
```

#### Manual Backup

```kotlin
@Composable
fun BackupScreen() {
    Button(onClick = {
        scope.launch {
            backupService.createManualBackup()
        }
    }) {
        Text("Create Backup")
    }
}
```

### Recovery Procedures

#### Restore from Backup

```kotlin
suspend fun restoreFromBackup(backupPath: String): Boolean {
    return try {
        // Stop database access
        database.close()
        
        // Restore backup file
        File(backupPath).copyTo(databaseFile, overwrite = true)
        
        // Reopen database
        database = openDatabase()
        
        // Verify restoration
        verifyDatabaseIntegrity()
    } catch (e: Exception) {
        Log.error("Restore failed", e)
        false
    }
}
```

#### Export User Data

```kotlin
suspend fun exportUserData(): File {
    val exportFile = File(exportDir, "user_data_${System.currentTimeMillis()}.json")
    
    val userData = UserData(
        books = repository.findAllBooks(),
        chapters = repository.findAllChapters(),
        categories = repository.findAllCategories(),
        history = repository.findAllHistory(),
        settings = preferencesManager.getAllSettings()
    )
    
    exportFile.writeText(json.encodeToString(userData))
    return exportFile
}
```

## Verification Steps

### Post-Rollback Checklist

- [ ] Feature flags disabled
- [ ] Legacy code paths active
- [ ] Database restored from backup
- [ ] Data integrity verified
- [ ] Dependency injection updated
- [ ] Smoke tests passed
- [ ] Crash rate normalized
- [ ] Error rate normalized
- [ ] Performance metrics acceptable
- [ ] User feedback monitored
- [ ] Team notified
- [ ] Documentation updated

### Testing Procedures

```kotlin
class RollbackVerification {
    suspend fun verifyRollback(): VerificationResult {
        val checks = listOf(
            verifyFeatureFlags(),
            verifyDatabaseIntegrity(),
            verifyRepositories(),
            verifyUIComponents(),
            verifyPerformance()
        )
        
        return VerificationResult(
            allPassed = checks.all { it },
            details = checks
        )
    }
    
    private suspend fun verifyFeatureFlags(): Boolean {
        return !FeatureFlags.useNewRepositories &&
               !FeatureFlags.useStateScreenModel &&
               !FeatureFlags.useNewUIComponents
    }
    
    private suspend fun verifyRepositories(): Boolean {
        val repository = get<BookRepository>()
        return repository is LegacyBookRepositoryImpl
    }
}
```

## Communication Plan

### Internal Communication

#### Team Notification

```kotlin
suspend fun notifyTeam(rollbackReason: String) {
    slackService.sendMessage(
        channel = "#engineering",
        message = """
            🚨 ROLLBACK INITIATED 🚨
            
            Reason: $rollbackReason
            Time: ${Date()}
            Affected users: ${getAffectedUserCount()}
            
            Status: In progress
            ETA: 2 hours
            
            Action items:
            - Monitor crash reports
            - Review error logs
            - Prepare hotfix
        """.trimIndent()
    )
}
```

#### Status Updates

```kotlin
suspend fun sendStatusUpdate(status: RollbackStatus) {
    slackService.sendMessage(
        channel = "#engineering",
        message = """
            Rollback Status Update
            
            Phase: ${status.phase}
            Progress: ${status.progress}%
            Issues: ${status.issues.size}
            
            Next steps: ${status.nextSteps}
        """.trimIndent()
    )
}
```

### User Communication

#### In-App Notification

```kotlin
@Composable
fun RollbackNotification() {
    if (rollbackInProgress) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Maintenance in Progress") },
            text = {
                Text(
                    "We're performing maintenance to improve your experience. " +
                    "Some features may be temporarily unavailable."
                )
            },
            confirmButton = {
                Button(onClick = { /* Dismiss */ }) {
                    Text("OK")
                }
            }
        )
    }
}
```

#### Social Media

```
We're aware of issues affecting some users and are working on a fix. 
Your data is safe and we'll have everything back to normal soon. 
Thank you for your patience!
```

## Post-Rollback Analysis

### Root Cause Analysis

```kotlin
data class RollbackAnalysis(
    val trigger: String,
    val affectedUsers: Int,
    val duration: Long,
    val dataLoss: Boolean,
    val rootCause: String,
    val preventionMeasures: List<String>,
    val lessonsLearned: List<String>
)

suspend fun conductPostMortem(): RollbackAnalysis {
    return RollbackAnalysis(
        trigger = identifyTrigger(),
        affectedUsers = countAffectedUsers(),
        duration = calculateRollbackDuration(),
        dataLoss = checkForDataLoss(),
        rootCause = analyzeRootCause(),
        preventionMeasures = identifyPreventionMeasures(),
        lessonsLearned = gatherLessonsLearned()
    )
}
```

### Improvement Actions

1. **Enhanced Testing**
   - Add more integration tests
   - Improve test coverage
   - Add performance benchmarks

2. **Better Monitoring**
   - Add more metrics
   - Improve alerting
   - Faster detection

3. **Gradual Rollout**
   - Smaller user segments
   - Longer monitoring periods
   - Better rollback triggers

4. **Documentation**
   - Update rollback procedures
   - Document edge cases
   - Improve runbooks

## Conclusion

This rollback plan ensures that we can quickly and safely revert changes if issues arise, minimizing impact on users and maintaining data integrity. Regular drills and updates to this plan are essential for maintaining readiness.

For questions or to report issues during a rollback, contact the on-call engineer or post in #engineering-alerts.
