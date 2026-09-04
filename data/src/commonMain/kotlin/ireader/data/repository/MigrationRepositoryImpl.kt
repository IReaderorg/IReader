package ireader.data.repository

import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.MigrationRepository
import ireader.domain.models.migration.*
import ireader.domain.preferences.prefs.UiPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
        _migrationJobs.update { current -> current + job }
    }
    
    override fun getAllMigrationJobs(): Flow<List<MigrationJob>> {
        return _migrationJobs.asStateFlow()
    }
    
    override suspend fun updateMigrationJobStatus(jobId: String, status: MigrationJobStatus) {
        _migrationJobs.update { current ->
            current.map { if (it.id == jobId) it.copy(status = status) else it }
        }
    }
    
    override suspend fun updateMigrationJobProgress(
        jobId: String,
        progress: Float,
        completedBooks: Int,
        failedBooks: Int
    ) {
        _migrationJobs.update { current ->
            current.map { job ->
                if (job.id == jobId) {
                    job
                } else job
            }
        }
    }
    
    override suspend fun deleteMigrationJob(jobId: String) {
        _migrationJobs.update { current -> current.filter { it.id != jobId } }
    }
}
