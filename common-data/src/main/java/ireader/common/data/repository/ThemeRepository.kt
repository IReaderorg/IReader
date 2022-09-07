package ireader.common.data.repository

import kotlinx.coroutines.flow.Flow
import ireader.common.models.theme.BaseTheme
import ireader.common.models.theme.CustomTheme

interface ThemeRepository {

    fun subscribe(): Flow<List<BaseTheme>>

    suspend fun insert(theme: CustomTheme): Long
    suspend fun insert(theme: List<CustomTheme>)

    suspend fun delete(theme: CustomTheme)
    suspend fun deleteAll()
}

