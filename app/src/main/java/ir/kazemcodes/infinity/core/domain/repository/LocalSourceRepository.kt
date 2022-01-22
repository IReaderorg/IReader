package ir.kazemcodes.infinity.core.domain.repository

import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower
import kotlinx.coroutines.flow.Flow

interface LocalSourceRepository {
    fun getSources (): Flow<Resource<List<SourceTower>>>
    suspend fun getLocalSources (): List<SourceTower>
    suspend fun addAllSources(sources: List<SourceTower>)
    suspend fun deleteAllSources()
    suspend fun deleteSource(sourceEntity: SourceTower)
    suspend fun addSource(sources: SourceTower)

}