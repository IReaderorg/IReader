package ireader.domain.usecases.backup.lnreader

import android.content.Context
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.lnreader.LNReaderCategory
import ireader.domain.models.lnreader.LNReaderNovel
import ireader.domain.models.lnreader.LNReaderVersion
import ireader.domain.usecases.file.AndroidFileSaver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.InputStream

/**
 * Android-specific streaming implementation for LNReader backup import
 * 
 * This implementation processes the backup file entry-by-entry without loading
 * the entire file into memory, preventing OutOfMemoryError on large backups.
 */
internal suspend fun ImportLNReaderBackup.invokeStreamingPlatform(
    uri: Uri,
    options: ImportLNReaderBackup.ImportOptions
): Flow<ImportLNReaderBackup.ImportProgress> = flow {
    emit(ImportLNReaderBackup.ImportProgress.Parsing("Opening backup file..."))
    
    // Get Android context from FileSaver
    val context = (fileSaver as? AndroidFileSaver)?.context
        ?: throw LNReaderImportException.UnknownException("Android context not available")
    
    // Open input stream from URI
    val inputStream = try {
        context.contentResolver.openInputStream(android.net.Uri.parse(uri.toString()))
            ?: throw LNReaderImportException.ReadFailedException("Cannot open file")
    } catch (e: Exception) {
        throw LNReaderImportException.ReadFailedException(
            e.message ?: "Unable to open file", e
        )
    }
    
    try {
        // Process backup using streaming callback
        processBackupStreaming(inputStream, options).collect { progress ->
            emit(progress)
        }
    } finally {
        inputStream.close()
    }
}

/**
 * Process backup using streaming to avoid loading entire file into memory
 */
private suspend fun ImportLNReaderBackup.processBackupStreaming(
    inputStream: InputStream,
    options: ImportLNReaderBackup.ImportOptions
): Flow<ImportLNReaderBackup.ImportProgress> = flow {
    emit(ImportLNReaderBackup.ImportProgress.Parsing("Parsing backup structure..."))
    
    var version: LNReaderVersion? = null
    val categories = mutableListOf<LNReaderCategory>()
    val novelIdMap = mutableMapOf<Int, Long>()
    val defaultCategoryNames = setOf("local", "default", "uncategorized", "all", "library")
    val novelsInDefaultCategories = mutableSetOf<Int>()
    
    var novelsImported = 0
    var novelsSkipped = 0
    var novelsFailed = 0
    var chaptersImported = 0
    var categoriesImported = 0
    val errors = mutableListOf<ImportLNReaderBackup.ImportError>()
    
    // Use streaming callback to process entries one at a time
    val callback = object : LNReaderBackupStreamCallback {
        override suspend fun onVersion(ver: LNReaderVersion) {
            version = ver
            ireader.core.log.Log.debug { "LNReader backup version: ${ver.version}" }
        }
        
        override suspend fun onCategories(cats: List<LNReaderCategory>) {
            categories.addAll(cats)
            ireader.core.log.Log.debug { "Found ${cats.size} categories" }
        }
        
        override suspend fun onSettings(settings: Map<String, String>) {
            // Settings import not implemented yet
            ireader.core.log.Log.debug { "Found ${settings.size} settings (not imported)" }
        }
        
        override suspend fun onNovel(novel: LNReaderNovel) {
            // Import novel immediately (don't accumulate in memory)
            try {
                val result = importNovel(novel, options)
                when (result) {
                    is ImportLNReaderBackup.NovelImportResult.Imported -> {
                        novelsImported++
                        chaptersImported += result.chaptersImported
                        novelIdMap[novel.id] = result.bookId
                    }
                    is ImportLNReaderBackup.NovelImportResult.Skipped -> {
                        novelsSkipped++
                        // Still track the mapping for category associations
                        val sourceId = sourceMapper.mapPluginId(novel.pluginId)
                            ?: sourceMapper.getUnmappedSourceId()
                        val existing = bookRepository.find(novel.path, sourceId)
                        if (existing != null) {
                            novelIdMap[novel.id] = existing.id
                        }
                    }
                    is ImportLNReaderBackup.NovelImportResult.Failed -> {
                        novelsFailed++
                        errors.add(ImportLNReaderBackup.ImportError("Novel", novel.name, result.error))
                    }
                }
            } catch (e: Exception) {
                novelsFailed++
                errors.add(ImportLNReaderBackup.ImportError("Novel", novel.name, e.message ?: "Unknown error"))
                ireader.core.log.Log.warn(e, "Failed to import novel: ${novel.name}")
            }
        }
        
        override suspend fun onProgress(current: Int, total: Int) {
            emit(ImportLNReaderBackup.ImportProgress.ImportingNovels(
                current, total, "Processing novel $current of $total"
            ))
        }
    }
    
    // Parse backup using streaming
    parseBackupStreamingPlatform(inputStream, callback)
    
    // Import categories after novels are processed
    if (options.importCategories && categories.isNotEmpty()) {
        val allCategories = categoryRepository.getAll()
        
        // Filter out default categories
        val categoriesToImport = categories.filter { category ->
            val isDefault = defaultCategoryNames.any {
                category.name.equals(it, ignoreCase = true)
            }
            if (isDefault) {
                novelsInDefaultCategories.addAll(category.novelIds)
            }
            !isDefault
        }
        
        // Import non-default categories
        categoriesToImport.forEachIndexed { index, category ->
            emit(ImportLNReaderBackup.ImportProgress.ImportingCategories(index + 1, categoriesToImport.size))
            try {
                importCategory(category)
                categoriesImported++
            } catch (e: Exception) {
                errors.add(ImportLNReaderBackup.ImportError("Category", category.name, e.message ?: "Unknown error"))
            }
        }
        
        // Create or get "LNReader" category for novels from default categories
        var lnReaderCategoryId: Long? = null
        if (novelsInDefaultCategories.isNotEmpty()) {
            val existingLnReaderCategory = allCategories.find {
                it.name.equals("LNReader", ignoreCase = true)
            }
            lnReaderCategoryId = if (existingLnReaderCategory != null) {
                existingLnReaderCategory.id
            } else {
                val newCategory = Category(
                    id = 0,
                    name = "LNReader",
                    order = (allCategories.maxOfOrNull { it.order } ?: 0) + 1,
                    flags = 0
                )
                categoryRepository.insert(newCategory)
                categoriesImported++
                categoryRepository.getAll().find {
                    it.name.equals("LNReader", ignoreCase = true)
                }?.id
            }
        }
        
        // Associate novels with categories
        if (lnReaderCategoryId != null) {
            for (lnNovelId in novelsInDefaultCategories) {
                val bookId = novelIdMap[lnNovelId] ?: continue
                try {
                    bookCategoryRepository.insert(BookCategory(bookId, lnReaderCategoryId))
                } catch (e: Exception) {
                    // Ignore duplicate associations
                }
            }
        }
        
        // Associate novels with their non-default categories
        for (category in categories) {
            if (defaultCategoryNames.any { category.name.equals(it, ignoreCase = true) }) {
                continue
            }
            
            try {
                val categoryId = allCategories.find {
                    it.name.equals(category.name, ignoreCase = true)
                }?.id ?: continue
                for (lnNovelId in category.novelIds) {
                    val bookId = novelIdMap[lnNovelId] ?: continue
                    try {
                        bookCategoryRepository.insert(BookCategory(bookId, categoryId))
                    } catch (e: Exception) {
                        // Ignore duplicate associations
                    }
                }
            } catch (e: Exception) {
                ireader.core.log.Log.warn(e, "Failed to import category associations for: ${category.name}")
            }
        }
    }
    
    val result = ImportLNReaderBackup.ImportResult(
        novelsImported = novelsImported,
        novelsSkipped = novelsSkipped,
        novelsFailed = novelsFailed,
        chaptersImported = chaptersImported,
        categoriesImported = categoriesImported,
        errors = errors
    )
    
    emit(ImportLNReaderBackup.ImportProgress.Complete(result))
}

/**
 * Extension property to access Android context from FileSaver
 */
private val ImportLNReaderBackup.context: Context?
    get() = (fileSaver as? AndroidFileSaver)?.context
