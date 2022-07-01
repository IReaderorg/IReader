package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.theme.BaseTheme
import org.ireader.common_models.theme.CustomTheme

interface ThemeRepository {

    fun subscribe() : Flow<List<BaseTheme>>

    suspend fun insert(theme: CustomTheme): Long

}