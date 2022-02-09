package org.ireader.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.source.SourceTower
import org.ireader.domain.utils.Resource

interface LocalSourceRepository {
    fun getSources(): Flow<Resource<List<SourceTower>>>
    suspend fun getLocalSources(): List<SourceTower>
    suspend fun addAllSources(sources: List<SourceTower>)
    suspend fun deleteAllSources()
    suspend fun deleteSource(sourceEntity: SourceTower)
    suspend fun addSource(sources: SourceTower)

}