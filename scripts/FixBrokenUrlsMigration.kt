package ireader.scripts

import ireader.core.source.SourceHelpers
import ireader.domain.data.repository.consolidated.BookRepository
import ireader.domain.data.repository.consolidated.ChapterRepository
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.updates.BookUpdate
import ireader.domain.models.updates.ChapterUpdate
import kotlinx.coroutines.flow.first

/**
 * Migration script to fix books and chapters that were saved with relative URLs
 * instead of absolute URLs due to the URL construction bug.
 * 
 * This should be run once after deploying the fix to repair existing data.
 * 
 * Usage:
 * ```kotlin
 * val migration = FixBrokenUrlsMigration(bookRepository, chapterRepository, catalogRepository)
 * migration.execute()
 * ```
 */
class FixBrokenUrlsMigration(
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val catalogRepository: CatalogSourceRepository
) {
    
    data class MigrationResult(
        val booksFixed: Int,
        val chaptersFixed: Int,
        val booksFailed: Int,
        val chaptersFailed: Int,
        val errors: List<String>
    )
    
    suspend fun execute(): MigrationResult {
        println("Starting URL migration...")
        
        var booksFixed = 0
        var chaptersFixed = 0
        var booksFailed = 0
        var chaptersFailed = 0
        val errors = mutableListOf<String>()
        
        try {
            // Get all catalogs/sources
            val catalogs = catalogRepository.getAllCatalogs().first()
            val catalogMap = catalogs.associateBy { it.sourceId }
            
            // Fix books
            println("Fixing books...")
            val books = bookRepository.getFavorites() // Or getAllBooks() if you have it
            
            for (book in books) {
                try {
                    // Skip if URL is already valid
                    if (book.key.startsWith("http://") || 
                        book.key.startsWith("https://") || 
                        book.key.startsWith("local_")) {
                        continue
                    }
                    
                    // Get the source for this book
                    val catalog = catalogMap[book.sourceId]
                    if (catalog == null) {
                        errors.add("Book ${book.id} (${book.title}): Source ${book.sourceId} not found")
                        booksFailed++
                        continue
                    }
                    
                    val source = catalog.source
                    if (source == null) {
                        errors.add("Book ${book.id} (${book.title}): Source instance not available")
                        booksFailed++
                        continue
                    }
                    
                    // Fix the URL
                    val fixedKey = SourceHelpers.buildAbsoluteUrl(source.baseUrl, book.key)
                    
                    // Update the book
                    val updated = bookRepository.update(
                        BookUpdate(
                            id = book.id,
                            key = fixedKey
                        )
                    )
                    
                    if (updated) {
                        println("Fixed book: ${book.title} - ${book.key} -> $fixedKey")
                        booksFixed++
                    } else {
                        errors.add("Book ${book.id} (${book.title}): Update failed")
                        booksFailed++
                    }
                    
                } catch (e: Exception) {
                    errors.add("Book ${book.id} (${book.title}): ${e.message}")
                    booksFailed++
                }
            }
            
            // Fix chapters
            println("Fixing chapters...")
            for (book in books) {
                try {
                    val chapters = chapterRepository.getChaptersByBookId(book.id)
                    
                    for (chapter in chapters) {
                        try {
                            // Skip if URL is already valid
                            if (chapter.key.startsWith("http://") || 
                                chapter.key.startsWith("https://") || 
                                chapter.key.startsWith("local_")) {
                                continue
                            }
                            
                            // Get the source for this book
                            val catalog = catalogMap[book.sourceId]
                            if (catalog == null) {
                                errors.add("Chapter ${chapter.id} (${chapter.name}): Source ${book.sourceId} not found")
                                chaptersFailed++
                                continue
                            }
                            
                            val source = catalog.source
                            if (source == null) {
                                errors.add("Chapter ${chapter.id} (${chapter.name}): Source instance not available")
                                chaptersFailed++
                                continue
                            }
                            
                            // Fix the URL
                            val fixedKey = SourceHelpers.buildAbsoluteUrl(source.baseUrl, chapter.key)
                            
                            // Update the chapter
                            val updated = chapterRepository.update(
                                ChapterUpdate(
                                    id = chapter.id,
                                    key = fixedKey
                                )
                            )
                            
                            if (updated) {
                                println("Fixed chapter: ${chapter.name} - ${chapter.key} -> $fixedKey")
                                chaptersFixed++
                            } else {
                                errors.add("Chapter ${chapter.id} (${chapter.name}): Update failed")
                                chaptersFailed++
                            }
                            
                        } catch (e: Exception) {
                            errors.add("Chapter ${chapter.id} (${chapter.name}): ${e.message}")
                            chaptersFailed++
                        }
                    }
                    
                } catch (e: Exception) {
                    errors.add("Book ${book.id} chapters: ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            errors.add("Migration failed: ${e.message}")
        }
        
        val result = MigrationResult(
            booksFixed = booksFixed,
            chaptersFixed = chaptersFixed,
            booksFailed = booksFailed,
            chaptersFailed = chaptersFailed,
            errors = errors
        )
        
        println("\n=== Migration Complete ===")
        println("Books fixed: $booksFixed")
        println("Chapters fixed: $chaptersFixed")
        println("Books failed: $booksFailed")
        println("Chapters failed: $chaptersFailed")
        println("Errors: ${errors.size}")
        
        if (errors.isNotEmpty()) {
            println("\nErrors:")
            errors.forEach { println("  - $it") }
        }
        
        return result
    }
    
    /**
     * Dry run - shows what would be fixed without actually updating
     */
    suspend fun dryRun(): MigrationResult {
        println("Starting URL migration (DRY RUN)...")
        
        var booksToFix = 0
        var chaptersToFix = 0
        val errors = mutableListOf<String>()
        
        try {
            val catalogs = catalogRepository.getAllCatalogs().first()
            val catalogMap = catalogs.associateBy { it.sourceId }
            
            val books = bookRepository.getFavorites()
            
            for (book in books) {
                if (!book.key.startsWith("http://") && 
                    !book.key.startsWith("https://") && 
                    !book.key.startsWith("local_")) {
                    
                    val catalog = catalogMap[book.sourceId]
                    if (catalog?.source != null) {
                        val fixedKey = SourceHelpers.buildAbsoluteUrl(catalog.source.baseUrl, book.key)
                        println("[DRY RUN] Would fix book: ${book.title}")
                        println("  Current: ${book.key}")
                        println("  Fixed:   $fixedKey")
                        booksToFix++
                    }
                }
                
                val chapters = chapterRepository.getChaptersByBookId(book.id)
                for (chapter in chapters) {
                    if (!chapter.key.startsWith("http://") && 
                        !chapter.key.startsWith("https://") && 
                        !chapter.key.startsWith("local_")) {
                        
                        val catalog = catalogMap[book.sourceId]
                        if (catalog?.source != null) {
                            val fixedKey = SourceHelpers.buildAbsoluteUrl(catalog.source.baseUrl, chapter.key)
                            println("[DRY RUN] Would fix chapter: ${chapter.name}")
                            println("  Current: ${chapter.key}")
                            println("  Fixed:   $fixedKey")
                            chaptersToFix++
                        }
                    }
                }
            }
            
        } catch (e: Exception) {
            errors.add("Dry run failed: ${e.message}")
        }
        
        println("\n=== Dry Run Complete ===")
        println("Books to fix: $booksToFix")
        println("Chapters to fix: $chaptersToFix")
        
        return MigrationResult(
            booksFixed = booksToFix,
            chaptersFixed = chaptersToFix,
            booksFailed = 0,
            chaptersFailed = 0,
            errors = errors
        )
    }
}
