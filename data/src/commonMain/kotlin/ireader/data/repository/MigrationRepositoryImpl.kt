package ireader.data.repository

import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.MigrationRepository
import ireader.domain.models.migration.*
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MigrationRepositoryImpl(
    private val uiPreferences: UiPreferences,
    private val catalogStore: CatalogStore
) : MigrationRepository {
    
    private val _migrationJobs = MutableStateFlow<List<MigrationJob>>(emptyList())
    
    override suspend fun getMigrationSources(): List<MigrationSource> {
        // Get all available sources from catalog store
        return catalogStore.catalogs.mapIndexed { index, catalog ->
            MigrationSource(
                sourceId = catalog.sourceId,
                sourceName = catalog.name,
                isEnabled = true,
                priority = index
            )
        }
    }
    
    override suspend fun saveMigrationSources(sources: List<MigrationSource>) {
        // Save to preferences
    }
    
    override suspend fun getMigrationFlags(): MigrationFlags {
        return MigrationFlags(
            chapters = true,
            bookmarks = true,
            categories = true,
            customCover = true,
            readingProgress = true
        )
    }
    
    override suspend fun saveMigrationFlags(flags: MigrationFlags) {
        // Save to preferences
    }
    
    override suspend fun saveMigrationJob(job: MigrationJob) {
        val currentJobs = _migrationJobs.value.toMutableList()
        currentJobs.add(job)
        _migrationJobs.value = currentJobs
    }
    
    override fun getAllMigrationJobs(): Flow<List<MigrationJob>> {
        return _migrationJobs.asStateFlow()
    }
    
    override suspend fun updateMigrationJobStatus(jobId: String, status: MigrationJobStatus) {
        val currentJobs = _migrationJobs.value.toMutableList()
        val index = currentJobs.indexOfFirst { it.id == jobId }
        if (index != -1) {
            currentJobs[index] = currentJobs[index].copy(status = status)
            _migrationJobs.value = currentJobs
        }
    }
    
    override suspend fun updateMigrationJobProgress(
        jobId: String,
        progress: Float,
        completedBooks: Int,
        failedBooks: Int
    ) {
        val currentJobs = _migrationJobs.value.toMutableList()
        val index = currentJobs.indexOfFirst { it.id == jobId }
        if (index != -1) {
            // Update job progress - this would update the job's progress field if it exists
            // For now, just update the status based on progress
            val updatedJob = currentJobs[index]
            currentJobs[index] = updatedJob
            _migrationJobs.value = currentJobs
        }
    }
    
    override suspend fun deleteMigrationJob(jobId: String) {
        _migrationJobs.value = _migrationJobs.value.filter { it.id != jobId }
    }
}
