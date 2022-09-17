package ireader.domain.data.repository

import ireader.common.models.entities.ExtensionSource
import kotlinx.coroutines.flow.Flow

interface CatalogSourceRepository {

    fun subscribe() : Flow<List<ExtensionSource>>
    suspend fun insert(extensionSource: ExtensionSource)
}