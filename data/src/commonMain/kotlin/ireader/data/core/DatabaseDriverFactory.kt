package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import data.Book
import data.Catalog
import data.Chapter
import data.Download
import data.Glossary
import data.Reader_theme
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
            updated_atAdapter = longConverter
        )
    )
    
    return database
}

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}