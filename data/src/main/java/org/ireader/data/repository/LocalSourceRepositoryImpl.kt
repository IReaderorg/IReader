package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.local.dao.SourceTowerDao
import org.ireader.domain.repository.LocalSourceRepository
import org.ireader.domain.source.SourceTower
import org.ireader.domain.utils.Resource

class LocalSourceRepositoryImpl(private val sourceTowerDao: SourceTowerDao) :
    LocalSourceRepository {
    override fun getSources(): Flow<Resource<List<SourceTower>>> = flow {
        try {
            val result = sourceTowerDao.getSources().map { it.toSource() }
            emit(Resource.Success(result))

        } catch (e: Exception) {
            emit(Resource.Error(uiText = UiText.ExceptionString(e)))
        }

    }

    override suspend fun getLocalSources(): List<SourceTower> {
        return sourceTowerDao.getSources().map { it.toSource() }
    }

    override suspend fun addAllSources(sources: List<SourceTower>) {
        sourceTowerDao.addAllSources(sources.map { it.toSourceEntity() })
    }

    override suspend fun deleteAllSources() {
        sourceTowerDao.deleteAllSources()
    }

    override suspend fun deleteSource(sourceEntity: SourceTower) {
        sourceTowerDao.deleteSource(sourceEntity.toSourceEntity())
    }

    override suspend fun addSource(sources: SourceTower) {
        sourceTowerDao.addSource(sources.toSourceEntity())
    }
}