package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.theme.ReaderTheme

interface ReaderThemeRepository {

    fun subscribe(): Flow<List<ReaderTheme>>

    suspend fun insert(theme: ReaderTheme): Long
    suspend fun insert(theme: List<ReaderTheme>)

    suspend fun delete(theme: ReaderTheme)
    suspend fun deleteAll()
}
