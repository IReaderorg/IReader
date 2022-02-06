package ir.kazemcodes.infinity.core.data.repository

import ir.kazemcodes.infinity.core.data.local.dao.SourceTowerDao
import ir.kazemcodes.infinity.core.domain.repository.LocalSourceRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.feature_sources.sources.models.SourceTower
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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