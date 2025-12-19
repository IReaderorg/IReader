package ireader.data.contentfilter

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ContentFilterRepository
import ireader.domain.models.entities.ContentFilter
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContentFilterRepositoryImpl(
    private val handler: DatabaseHandler
) : ContentFilterRepository {
    
    override fun getGlobalPatterns(): Flow<List<ContentFilter>> {
        return handler.subscribeToList { contentFilterQueries.getGlobalPatterns() }
            .map { list -> list.map { it.toContentFilter() } }
    }
    
    override suspend fun getEnabledGlobalPatterns(): List<ContentFilter> {
        return handler.awaitList { contentFilterQueries.getEnabledGlobalPatterns() }
            .map { it.toContentFilter() }
    }
    
    override fun getPatternsForBook(bookId: Long): Flow<List<ContentFilter>> {
        return handler.subscribeToList { contentFilterQueries.getPatternsForBook(bookId) }
            .map { list -> list.map { it.toContentFilter() } }
    }
    
    override suspend fun getEnabledPatternsForBook(bookId: Long): List<ContentFilter> {
        return handler.awaitList { contentFilterQueries.getEnabledPatternsForBook(bookId) }
            .map { it.toContentFilter() }
    }
    
    override fun getBookSpecificPatterns(bookId: Long): Flow<List<ContentFilter>> {
        return handler.subscribeToList { contentFilterQueries.getBookSpecificPatterns(bookId) }
            .map { list -> list.map { it.toContentFilter() } }
    }
    
    override suspend fun getPatternById(id: Long): ContentFilter? {
        return handler.awaitOneOrNull {
            contentFilterQueries.getPatternById(id)
        }?.toContentFilter()
    }
    
    override suspend fun getPresetPatterns(): List<ContentFilter> {
        return handler.awaitList {
            contentFilterQueries.getPresetPatterns()
        }.map { it.toContentFilter() }
    }
    
    override suspend fun insert(filter: ContentFilter): Long {
        val now = currentTimeToLong()
        return handler.await(inTransaction = true) {
            contentFilterQueries.insert(
                bookId = filter.bookId,
                name = filter.name,
                pattern = filter.pattern,
                description = filter.description,
                enabled = filter.enabled,
                isPreset = filter.isPreset,
                createdAt = now,
                updatedAt = now
            )
            contentFilterQueries.selectLastInsertedRowId().executeAsOne()
        }
    }
    
    override suspend fun update(filter: ContentFilter) {
        handler.await {
            contentFilterQueries.update(
                id = filter.id,
                name = filter.name,
                pattern = filter.pattern,
                description = filter.description,
                enabled = filter.enabled,
                updatedAt = currentTimeToLong()
            )
        }
    }
    
    override suspend fun toggleEnabled(id: Long) {
        handler.await {
            contentFilterQueries.toggleEnabled(
                id = id,
                updatedAt = currentTimeToLong()
            )
        }
    }
    
    override suspend fun delete(id: Long) {
        handler.await {
            contentFilterQueries.deletePattern(id)
        }
    }
    
    override suspend fun deleteBookPatterns(bookId: Long) {
        handler.await {
            contentFilterQueries.deleteBookPatterns(bookId)
        }
    }
    
    override suspend fun initializePresets() {
        val existingPresets = getPresetPatterns()
        if (existingPresets.isEmpty()) {
            val now = currentTimeToLong()
            ContentFilter.PRESETS.forEach { preset ->
                handler.await {
                    contentFilterQueries.insert(
                        bookId = null,
                        name = preset.name,
                        pattern = preset.pattern,
                        description = preset.description,
                        enabled = false, // Presets start disabled
                        isPreset = true,
                        createdAt = now,
                        updatedAt = now
                    )
                }
            }
        }
    }
    
    override suspend fun countPatterns(): Long {
        return handler.awaitOne { contentFilterQueries.countPatterns() }
    }
    
    override suspend fun countEnabledPatterns(): Long {
        return handler.awaitOne { contentFilterQueries.countEnabledPatterns() }
    }
    
    private fun data.Content_filter.toContentFilter(): ContentFilter {
        return ContentFilter(
            id = _id,
            bookId = book_id,
            name = name,
            pattern = pattern,
            description = description,
            enabled = enabled,
            isPreset = is_preset,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
}
