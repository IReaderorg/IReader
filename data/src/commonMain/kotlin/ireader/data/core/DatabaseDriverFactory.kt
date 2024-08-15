package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import data.Book
import data.Chapter
import ir.kazemcodes.infinityreader.Database
import ireader.data.book.bookGenresConverter
import ireader.data.chapter.chapterContentConvertor

fun createDatabase(driver: SqlDriver): Database {
    return Database(
        driver = driver,
        bookAdapter = Book.Adapter(
            genreAdapter = bookGenresConverter
        ),
        chapterAdapter = Chapter.Adapter(
            chapterContentConvertor
        ),
    )
}

expect class DatabaseDriverFactory {
    fun create(): SqlDriver
}