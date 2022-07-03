package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ireader.common_data.repository.ThemeRepository
import org.ireader.common_models.theme.BaseTheme
import org.ireader.common_models.theme.CustomTheme
import org.ireader.data.local.dao.ThemeDao
import org.ireader.domain.use_cases.theme.toBaseTheme

class ThemeRepositoryImpl(
    private val themeDao: ThemeDao
) : ThemeRepository  {
    override fun subscribe() : Flow<List<BaseTheme>> {
        return themeDao.subscribe().map {flow -> flow.map { it.toBaseTheme() } }
    }

    override suspend fun insert(theme: CustomTheme): Long {
        return themeDao.insertTheme(theme)
    }

    override suspend fun delete(theme: CustomTheme) {
        return themeDao.delete(theme)
    }
}