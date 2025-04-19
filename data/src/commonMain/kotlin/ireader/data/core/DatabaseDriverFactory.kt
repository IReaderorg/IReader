package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import data.Catalog
import data.Download
import data.Reader_theme
import data.Theme
import ir.kazemcodes.infinityreader.Database
import ireader.data.book.bookGenresConverter
import ireader.data.book.floatDoubleColumnAdapter
import ireader.data.book.intLongColumnAdapter
import ireader.data.book.longConverter
import ireader.data.chapter.chapterContentConvertor
import migrations.Book
import migrations.Chapter
import java.io.Reader

fun createDatabase(driver: SqlDriver): Database {
    return Database(
        driver = driver,
        bookAdapter = Book.Adapter(
            cover_last_modifiedAdapter = longConverter,
            date_addedAdapter = longConverter
        ),
        chapterAdapter = Chapter.Adapter(
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
            inverseSurfaceAdapter =intLongColumnAdapter ,
            outlineVariantAdapter =intLongColumnAdapter ,
            surfaceVariantAdapter =intLongColumnAdapter ,
            onBackgroundAdapter = intLongColumnAdapter,
            onPrimaryAdapter = intLongColumnAdapter,
            onSurfaceAdapter = intLongColumnAdapter,
            secondaryAdapter = intLongColumnAdapter,
            backgroundAdapter = intLongColumnAdapter,
            onTertiaryAdapter =intLongColumnAdapter ,
            onSecondaryAdapter =intLongColumnAdapter ,
            surfaceTintAdapter = intLongColumnAdapter, surfaceAdapter = intLongColumnAdapter,
            onErrorAdapter = intLongColumnAdapter,
            outlineAdapter = intLongColumnAdapter,
            primaryAdapter = intLongColumnAdapter, errorAdapter = intLongColumnAdapter, scrimAdapter = intLongColumnAdapter, onBarsAdapter = intLongColumnAdapter, barsAdapter = intLongColumnAdapter, tertiaryAdapter = intLongColumnAdapter, onErrorContainerAdapter = intLongColumnAdapter, primaryContainerAdapter = intLongColumnAdapter,
            inverseOnSurfaceAdapter = intLongColumnAdapter,
            onSurfaceVariantAdapter = intLongColumnAdapter, tertiaryContainerAdapter = intLongColumnAdapter, onTertiaryContainerAdapter = intLongColumnAdapter, onPrimaryContainerAdapter = intLongColumnAdapter, secondaryContainerAdapter = intLongColumnAdapter,
            onSecondaryContainerAdapter = intLongColumnAdapter,



        ),
        downloadAdapter = Download.Adapter(
            priorityAdapter = intLongColumnAdapter
        )
    )
}

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}