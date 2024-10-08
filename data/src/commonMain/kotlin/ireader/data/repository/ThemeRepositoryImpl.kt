package ireader.data.repository


import app.cash.sqldelight.Query
import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.models.theme.*
import kotlinx.coroutines.flow.Flow

class ThemeRepositoryImpl(
    private val handler: DatabaseHandler,
) : ThemeRepository {
    override fun subscribe(): Flow<List<Theme>> {
        return handler.subscribeToList { themesQueries.subscribe(appThemeMapper) }
    }

    override suspend fun insert(theme: CustomTheme): Long {
        return handler.awaitOneAsync(inTransaction = true) {
            theme.let { theme ->
                themesQueries.upsert(
                    primary = theme.materialColor.primary,
                    primaryContainer = theme.materialColor.primaryContainer,
                    onPrimary = theme.materialColor.onPrimary,
                    secondary = theme.materialColor.secondary,
                    onSecondary = theme.materialColor.onSecondary,
                    background = theme.materialColor.background,
                    surface = theme.materialColor.surface,
                    onBackground = theme.materialColor.onBackground,
                    onSurface = theme.materialColor.onSurface,
                    error = theme.materialColor.error,
                    onError = theme.materialColor.onError,
                    surfaceTint = theme.materialColor.surfaceTint,
                    secondaryContainer = theme.materialColor.secondaryContainer,
                    errorContainer = theme.materialColor.errorContainer,
                    inverseOnSurface = theme.materialColor.inverseOnSurface,
                    inversePrimary = theme.materialColor.inversePrimary,
                    inverseSurface = theme.materialColor.inverseSurface,
                    onErrorContainer = theme.materialColor.onErrorContainer,
                    onPrimaryContainer = theme.materialColor.onPrimaryContainer,
                    onSecondaryContainer = theme.materialColor.onSecondaryContainer,
                    outline = theme.materialColor.outline,
                    tertiary = theme.materialColor.tertiary,
                    tertiaryContainer = theme.materialColor.tertiaryContainer,
                    scrim = theme.materialColor.scrim,
                    outlineVariant = theme.materialColor.outlineVariant,
                    isDark = theme.dark,
                    onBars = theme.extraColors.onBars,
                    bars = theme.extraColors.bars,
                    isBarLight = theme.dark,
                    setOnTertiary = theme.materialColor.onTertiary,
                    id = null,
                    surfaceVariant = theme.materialColor.surfaceVariant,
                    onTertiaryContainer = theme.materialColor.onTertiaryContainer,
                    onSurfaceVariant = theme.materialColor.onSurfaceVariant

                )
            }
           themesQueries.selectLastInsertedRowId()
        }
    }

    override suspend fun insert(theme: List<CustomTheme>) {
        handler.await {
            theme.forEach { theme ->
                themesQueries.upsert(
                    primary = theme.materialColor.primary,
                    primaryContainer = theme.materialColor.primaryContainer,
                    onPrimary = theme.materialColor.onPrimary,
                    secondary = theme.materialColor.secondary,
                    onSecondary = theme.materialColor.onSecondary,
                    background = theme.materialColor.background,
                    surface = theme.materialColor.surface,
                    onBackground = theme.materialColor.onBackground,
                    onSurface = theme.materialColor.onSurface,
                    error = theme.materialColor.error,
                    onError = theme.materialColor.onError,
                    surfaceTint = theme.materialColor.surfaceTint,
                    secondaryContainer = theme.materialColor.secondaryContainer,
                    errorContainer = theme.materialColor.errorContainer,
                    inverseOnSurface = theme.materialColor.inverseOnSurface,
                    inversePrimary = theme.materialColor.inversePrimary,
                    inverseSurface = theme.materialColor.inverseSurface,
                    onErrorContainer = theme.materialColor.onErrorContainer,
                    onPrimaryContainer = theme.materialColor.onPrimaryContainer,
                    onSecondaryContainer = theme.materialColor.onSecondaryContainer,
                    outline = theme.materialColor.outline,
                    tertiary = theme.materialColor.tertiary,
                    tertiaryContainer = theme.materialColor.tertiaryContainer,
                    scrim = theme.materialColor.scrim,
                    outlineVariant = theme.materialColor.outlineVariant,
                    isDark = theme.dark,
                    onBars = theme.extraColors.onBars,
                    bars = theme.extraColors.bars,
                    isBarLight = theme.dark,
                    setOnTertiary = theme.materialColor.onTertiary,
                    id = null,
                    surfaceVariant = theme.materialColor.surfaceVariant,
                    onTertiaryContainer = theme.materialColor.onTertiaryContainer,
                    onSurfaceVariant = theme.materialColor.onSurfaceVariant
                )
            }
        }
    }


    override suspend fun delete(theme: CustomTheme) {
        handler.await {
            themesQueries.delete(theme.id)
        }
    }

    override suspend fun deleteAll() {
        handler.await {
            themesQueries.deleteAll()
        }
    }

}

class ReaderThemeRepositoryImpl(
    private val handler: DatabaseHandler,
) : ReaderThemeRepository {
    override fun subscribe(): Flow<List<ReaderTheme>> {
        return handler.subscribeToList {
            readerThemesQueries.subscribe(readerMapper)
        }
    }

    override suspend fun insert(theme: ReaderTheme): Long {
        return handler.awaitOneAsync(inTransaction = true) {
            theme.let { theme ->
                readerThemesQueries.upsert(theme.backgroundColor, theme.onTextColor, theme.id)
            }
       readerThemesQueries.selectLastInsertedRowId()
        }
    }

    override suspend fun insert(theme: List<ReaderTheme>) {
        handler.await(true) {
            theme.forEach { theme ->
                readerThemesQueries.upsert(theme.backgroundColor, theme.onTextColor, theme.id)
            }

        }
    }

    override suspend fun delete(theme: ReaderTheme) {
        handler.await {
            readerThemesQueries.delete(theme.id)
        }
    }

    override suspend fun deleteAll() {
        handler.await {
            readerThemesQueries.deleteAll()
        }
    }

}
