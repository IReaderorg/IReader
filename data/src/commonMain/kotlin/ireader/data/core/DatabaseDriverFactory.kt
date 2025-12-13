package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import data.Book
import data.Catalog
import data.Chapter
import data.ChapterHealth
import data.Download
import data.Global_glossary
import data.Glossary
import data.NftWallets
import data.Plugin
import data.Plugin_permission
import data.Plugin_purchase
import data.Plugin_repository
import data.Plugin_review
import data.Plugin_trial
import data.Reader_theme
import data.SourceComparison
import data.Theme
import data.Translated_chapter
import ir.kazemcodes.infinityreader.Database
import ireader.data.book.bookGenresConverter
import ireader.data.book.floatDoubleColumnAdapter
import ireader.data.book.intLongColumnAdapter
import ireader.data.book.longConverter
import ireader.data.chapter.chapterContentConvertor

fun createDatabase(driver: SqlDriver): Database {
    // Create the database instance with appropriate adapters
    val database = Database(
        driver = driver,
        bookAdapter = Book.Adapter(
            genreAdapter = bookGenresConverter,
            cover_last_modifiedAdapter = longConverter,
            date_addedAdapter = longConverter
        ),
        chapterAdapter = Chapter.Adapter(
            contentAdapter = chapterContentConvertor,
            date_fetchAdapter = longConverter,
            date_uploadAdapter = longConverter,
            typeAdapter = longConverter,
            chapter_numberAdapter = floatDoubleColumnAdapter
        ),
        reader_themeAdapter = Reader_theme.Adapter(
            background_colorAdapter = intLongColumnAdapter,
            on_textcolorAdapter = intLongColumnAdapter
        ),
        catalogAdapter = Catalog.Adapter(versionCodeAdapter = intLongColumnAdapter),
        themeAdapter = Theme.Adapter(
            errorContainerAdapter = intLongColumnAdapter,
            inversePrimaryAdapter = intLongColumnAdapter,
            inverseSurfaceAdapter = intLongColumnAdapter,
            outlineVariantAdapter = intLongColumnAdapter,
            surfaceVariantAdapter = intLongColumnAdapter,
            onBackgroundAdapter = intLongColumnAdapter,
            onPrimaryAdapter = intLongColumnAdapter,
            onSurfaceAdapter = intLongColumnAdapter,
            secondaryAdapter = intLongColumnAdapter,
            backgroundAdapter = intLongColumnAdapter,
            onTertiaryAdapter = intLongColumnAdapter,
            onSecondaryAdapter = intLongColumnAdapter,
            surfaceTintAdapter = intLongColumnAdapter,
            surfaceAdapter = intLongColumnAdapter,
            onErrorAdapter = intLongColumnAdapter,
            outlineAdapter = intLongColumnAdapter,
            primaryAdapter = intLongColumnAdapter,
            errorAdapter = intLongColumnAdapter,
            scrimAdapter = intLongColumnAdapter,
            onBarsAdapter = intLongColumnAdapter,
            barsAdapter = intLongColumnAdapter,
            tertiaryAdapter = intLongColumnAdapter,
            onErrorContainerAdapter = intLongColumnAdapter,
            primaryContainerAdapter = intLongColumnAdapter,
            inverseOnSurfaceAdapter = intLongColumnAdapter,
            onSurfaceVariantAdapter = intLongColumnAdapter,
            tertiaryContainerAdapter = intLongColumnAdapter,
            onTertiaryContainerAdapter = intLongColumnAdapter,
            onPrimaryContainerAdapter = intLongColumnAdapter,
            secondaryContainerAdapter = intLongColumnAdapter,
            onSecondaryContainerAdapter = intLongColumnAdapter
        ),
        downloadAdapter = Download.Adapter(
            priorityAdapter = intLongColumnAdapter
        ),
        glossaryAdapter = Glossary.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        translated_chapterAdapter = Translated_chapter.Adapter(
            translated_contentAdapter = chapterContentConvertor,
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter,
        ),
        chapterHealthAdapter = ChapterHealth.Adapter(
            longConverter
        ),
        sourceComparisonAdapter = SourceComparison.Adapter(
            longConverter,
            longConverter
        ),
        nftWalletsAdapter = NftWallets.Adapter(
            longConverter,
        ),
        pluginAdapter = Plugin.Adapter(
            install_dateAdapter = longConverter
        ),
        plugin_purchaseAdapter = Plugin_purchase.Adapter(
            timestampAdapter = longConverter
        ),
        plugin_reviewAdapter = Plugin_review.Adapter(
            timestampAdapter = longConverter
        ),
        plugin_trialAdapter = Plugin_trial.Adapter(
            start_dateAdapter = longConverter,
            expiration_dateAdapter = longConverter
        ),
        global_glossaryAdapter = Global_glossary.Adapter(
            created_atAdapter = longConverter,
            updated_atAdapter = longConverter
        ),
        plugin_permissionAdapter = Plugin_permission.Adapter(
            granted_atAdapter = longConverter
        ),
        plugin_repositoryAdapter = Plugin_repository.Adapter(
            last_updatedAdapter = longConverter,
            created_atAdapter = longConverter
        ),
    )

    return database
}

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}