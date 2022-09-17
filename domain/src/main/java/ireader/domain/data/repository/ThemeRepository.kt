package ireader.domain.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.domain.models.theme.CustomTheme
import ireader.domain.models.theme.Theme

interface ThemeRepository {

    fun subscribe(): Flow<List<Theme>>

    suspend fun insert(theme: CustomTheme): Long
    suspend fun insert(theme: List<CustomTheme>)

    suspend fun delete(theme: CustomTheme)
    suspend fun deleteAll()
}

