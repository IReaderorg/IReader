package ireader.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ireader.common.data.repository.ReaderThemeRepository
import ireader.common.data.repository.ThemeRepository
import ireader.common.models.theme.BaseTheme
import ireader.common.models.theme.CustomTheme
import ireader.common.models.theme.ReaderTheme
import ireader.core.ui.theme.themes
import ireader.data.local.dao.ReaderThemeDao
import ireader.data.local.dao.ThemeDao
import ireader.domain.use_cases.theme.toBaseTheme

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
