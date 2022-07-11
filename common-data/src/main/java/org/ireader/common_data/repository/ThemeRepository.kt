package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.theme.BaseTheme
import org.ireader.common_models.theme.CustomTheme
import org.ireader.common_models.theme.ReaderTheme

interface ThemeRepository {

    fun subscribe(): Flow<List<BaseTheme>>

    suspend fun insert(theme: CustomTheme): Long
    suspend fun insert(theme: List<CustomTheme>)

    suspend fun delete(theme: CustomTheme)
    suspend fun deleteAll()
}

interface ReaderThemeRepository {

    fun subscribe(): Flow<List<ReaderTheme>>

    suspend fun insert(theme: ReaderTheme): Long
    suspend fun insert(theme: List<ReaderTheme>)

    suspend fun delete(theme: ReaderTheme)
    suspend fun deleteAll()
}
