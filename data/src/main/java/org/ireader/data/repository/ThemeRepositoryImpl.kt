package org.ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ireader.common_data.repository.ReaderThemeRepository
import org.ireader.common_data.repository.ThemeRepository
import org.ireader.common_models.theme.BaseTheme
import org.ireader.common_models.theme.CustomTheme
import org.ireader.common_models.theme.ReaderTheme
import org.ireader.core_ui.theme.themes
import org.ireader.data.local.dao.ReaderThemeDao
import org.ireader.data.local.dao.ThemeDao
import org.ireader.domain.use_cases.theme.toBaseTheme

class ThemeRepositoryImpl(
    private val themeDao: ThemeDao
) : ThemeRepository {
    override fun subscribe(): Flow<List<BaseTheme>> {
        return themeDao.subscribe().map { flow -> flow.map { it.toBaseTheme() } }
    }

    override suspend fun insert(theme: CustomTheme): Long {
        return themeDao.insertTheme(theme.copy(id = themes.lastIndex.toLong()))
    }

    override suspend fun insert(theme: List<CustomTheme>) {
        themeDao.insert(theme)
    }

    override suspend fun delete(theme: CustomTheme) {
        return themeDao.delete(theme)
    }

    override suspend fun deleteAll() {
        themeDao.deleteAll()
    }
}

class ReaderThemeRepositoryImpl(
    private val readerThemeDao: ReaderThemeDao
) : ReaderThemeRepository {
    override fun subscribe(): Flow<List<ReaderTheme>> {
        return readerThemeDao.subscribe()
    }

    override suspend fun insert(theme: ReaderTheme): Long {
        return readerThemeDao.insertTheme(theme)
    }

    override suspend fun insert(theme: List<ReaderTheme>) {
        readerThemeDao.insertThemes(theme)
    }

    override suspend fun delete(theme: ReaderTheme) {
        return readerThemeDao.delete(theme)
    }

    override suspend fun deleteAll() {
        readerThemeDao.deleteAll()
    }
}
