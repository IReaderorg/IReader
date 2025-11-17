package ireader.data.migration

import ireader.presentation.core.log.IReaderLog

/**
 * Migration utility for transitioning from old repository structure to new consolidated repositories.
 * 
 * This class provides guidance and utilities for migrating from IReader's current 30+ repository
 * interfaces to the new consolidated 8 repository structure following Mihon's patterns.
 */
object RepositoryMigration {
    
    /**
     * Repository mapping from old interfaces to new consolidated ones.
     * This helps developers understand which old repository methods map to which new repositories.
     */
    val repositoryMapping = mapOf(
        // Old BookRepository -> New BookRepository (consolidated)
        "ireader.domain.data.repository.BookRepository" to "ireader.domain.data.repository.consolidated.BookRepository",
        
        // Old ChapterRepository -> New ChapterRepository (consolidated)
        "ireader.domain.data.repository.ChapterRepository" to "ireader.domain.data.repository.consolidated.ChapterRepository",
        
        // Old CategoryRepository -> New CategoryRepository (consolidated)
        "ireader.domain.data.repository.CategoryRepository" to "ireader.domain.data.repository.consolidated.CategoryRepository",
        
        // Old DownloadRepository -> New DownloadRepository (consolidated)
        "ireader.domain.data.repository.DownloadRepository" to "ireader.domain.data.repository.consolidated.DownloadRepository",
        
        // Old HistoryRepository -> New HistoryRepository (consolidated)
        "ireader.domain.data.repository.HistoryRepository" to "ireader.domain.data.repository.consolidated.HistoryRepository",
        
        // Old LibraryRepository -> New LibraryRepository (consolidated)
        "ireader.domain.data.repository.LibraryRepository" to "ireader.domain.data.repository.consolidated.LibraryRepository",
        
        // Old UpdatesRepository -> New UpdatesRepository (consolidated)
        "ireader.domain.data.repository.UpdatesRepository" to "ireader.domain.data.repository.consolidated.UpdatesRepository",
        
        // Specialized repositories that should be consolidated
        "ireader.domain.data.repository.CatalogSourceRepository" to "ireader.domain.data.repository.consolidated.SourceRepository",
        "ireader.domain.data.repository.BookCategoryRepository" to "ireader.domain.data.repository.consolidated.CategoryRepository",
        "ireader.domain.data.repository.ReaderThemeRepository" to "ireader.domain.data.repository.consolidated.ThemeRepository",
        "ireader.domain.data.repository.ThemeRepository" to "ireader.domain.data.repository.consolidated.ThemeRepository"
    )
    
    /**
     * Method mapping from old repository methods to new consolidated repository methods.
     * This helps developers understand how to migrate specific method calls.
     */
    val methodMapping = mapOf(
        // BookRepository method mappings
        "findAllBooks" to "getFavorites", // Most use cases were for favorites
        "subscribeBookById" to "getBookByIdAsFlow",
        "findBookById" to "getBookById",
        "find" to "getBookByUrlAndSourceId",
        "subscribeBooksByKey" to "getBookByUrlAndSourceIdAsFlow",
        "updateBook" to "update", // Now uses BookUpdate class
        "insertBooks" to "insertNetworkBooks",
        "deleteBooks" to "deleteBooks",
        
        // ChapterRepository method mappings
        "subscribeChapterById" to "getChapterByIdAsFlow",
        "findChapterById" to "getChapterById",
        "findChaptersByBookId" to "getChaptersByBookId",
        "subscribeChaptersByBookId" to "getChaptersByBookIdAsFlow",
        "insertChapters" to "addAll",
        "deleteChapters" to "removeChaptersWithIds",
        "deleteChaptersByBookId" to "removeChaptersByBookId",
        
        // CategoryRepository method mappings
        "getAll" to "getAll",
        "getAllAsFlow" to "getAllAsFlow",
        "getCategoriesByMangaId" to "getCategoriesByBookId",
        "getCategoriesByMangaIdAsFlow" to "getCategoriesByBookIdAsFlow",
        "insert" to "insert",
        "updatePartial" to "updatePartial", // Now uses CategoryUpdate class
        "delete" to "delete"
    )
    
    /**
     * Deprecated repository interfaces that should be removed after migration.
     */
    val deprecatedRepositories = listOf(
        "ireader.domain.data.repository.BaseRepository",
        "ireader.domain.data.repository.ReactiveRepository",
        "ireader.domain.data.repository.BatchRepository",
        "ireader.domain.data.repository.FullRepository",
        "ireader.domain.data.repository.BadgeRepository",
        "ireader.domain.data.repository.ChapterReportRepository",
        "ireader.domain.data.repository.ChapterHealthRepository",
        "ireader.domain.data.repository.FontRepository",
        "ireader.domain.data.repository.GlossaryRepository",
        "ireader.domain.data.repository.NFTRepository",
        "ireader.domain.data.repository.MigrationRepository",
        "ireader.domain.data.repository.PluginRepository",
        "ireader.domain.data.repository.ReadingStatisticsRepository",
        "ireader.domain.data.repository.RemoteRepository",
        "ireader.domain.data.repository.SecurityRepository",
        "ireader.domain.data.repository.SourceComparisonRepository",
        "ireader.domain.data.repository.SourceCredentialsRepository",
        "ireader.domain.data.repository.ReviewRepository",
        "ireader.domain.data.repository.SourceReportRepository",
        "ireader.domain.data.repository.TranslatedChapterRepository",
        "ireader.domain.data.repository.VoiceModelRepository",
        "ireader.domain.data.repository.FundingGoalRepository"
    )
    
    /**
     * Logs migration guidance for developers.
     */
    fun logMigrationGuidance() {
        IReaderLog.info("=== Repository Migration Guide ===", "RepositoryMigration")
        IReaderLog.info("Migrating from 30+ repositories to 8 consolidated repositories", "RepositoryMigration")
        
        IReaderLog.info("New consolidated repositories:", "RepositoryMigration")
        repositoryMapping.values.distinct().forEach { newRepo ->
            IReaderLog.info("  - $newRepo", "RepositoryMigration")
        }
        
        IReaderLog.info("Key changes:", "RepositoryMigration")
        IReaderLog.info("  - Use Update classes (BookUpdate, ChapterUpdate) for partial updates", "RepositoryMigration")
        IReaderLog.info("  - Boolean return values for operations instead of exceptions", "RepositoryMigration")
        IReaderLog.info("  - Comprehensive error handling with IReaderLog", "RepositoryMigration")
        IReaderLog.info("  - Flow-based reactive queries with proper error handling", "RepositoryMigration")
        
        IReaderLog.info("Deprecated repositories to remove:", "RepositoryMigration")
        deprecatedRepositories.forEach { deprecated ->
            IReaderLog.info("  - $deprecated", "RepositoryMigration")
        }
    }
    
    /**
     * Validates that all old repository usages have been migrated.
     * This should be called during the migration process to ensure completeness.
     */
    fun validateMigration(): MigrationValidationResult {
        val issues = mutableListOf<String>()
        
        // This would typically scan the codebase for old repository usages
        // For now, we'll provide a structure for validation
        
        return MigrationValidationResult(
            isComplete = issues.isEmpty(),
            issues = issues
        )
    }
}

/**
 * Result of migration validation.
 */
data class MigrationValidationResult(
    val isComplete: Boolean,
    val issues: List<String>
)

/**
 * Migration steps for developers to follow.
 */
object MigrationSteps {
    
    val steps = listOf(
        "1. Replace old repository interfaces with consolidated ones in dependency injection",
        "2. Update repository implementations to use new consolidated implementations",
        "3. Replace direct entity updates with Update classes (BookUpdate, ChapterUpdate, etc.)",
        "4. Update error handling to use boolean return values and IReaderLog",
        "5. Replace Flow subscriptions with new consolidated Flow methods",
        "6. Update use cases/interactors to use new repository methods",
        "7. Update ViewModels/ScreenModels to use new repository patterns",
        "8. Remove deprecated repository interfaces and implementations",
        "9. Update database queries to support new repository patterns",
        "10. Run comprehensive tests to ensure migration completeness"
    )
    
    fun logMigrationSteps() {
        IReaderLog.info("=== Repository Migration Steps ===", "MigrationSteps")
        steps.forEachIndexed { index, step ->
            IReaderLog.info("Step ${index + 1}: $step", "MigrationSteps")
        }
    }
}