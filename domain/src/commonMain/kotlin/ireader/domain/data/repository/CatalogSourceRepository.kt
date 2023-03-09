package ireader.domain.data.repository

import ireader.domain.models.entities.ExtensionSource
import kotlinx.coroutines.flow.Flow

interface CatalogSourceRepository {

    fun subscribe() : Flow<List<ExtensionSource>>
    suspend fun find(id: Long) : ExtensionSource?
    suspend fun insert(extensionSource: ExtensionSource)
    suspend fun delete(extensionSource: ExtensionSource)
}