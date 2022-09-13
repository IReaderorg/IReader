package org.ireader.app.di

import android.app.Application
import android.content.Context
import com.squareup.sqldelight.db.SqlDriver
import data.Book
import ir.kazemcodes.infinityreader.Database
import okio.FileSystem
import ireader.core.api.db.Transactions
import org.koin.core.annotation.ComponentScan
import ireader.core.api.di.ISingleton
import ireader.data.local.*
import org.koin.core.annotation.Single

@org.koin.core.annotation.Module
@ComponentScan("org.ireader.app.di.DatabaseInject")
class DatabaseInject {


    @Single
    fun provideSqlDrive(
        app:Application
    ) : SqlDriver {
        return DatabaseDriverFactory(app).create()
    }
    @Single
    fun provideDatabase(driver:SqlDriver): Database {
        return createDatabase(driver)
    }
    @Single
    fun databaseHandler(driver:SqlDriver, db: Database) : DatabaseHandler {
        return             AndroidDatabaseHandler(
            driver = driver,
            db = db
        )
    }


        @Single
    fun provideFileSystem(): FileSystem {
        return FileSystem.SYSTEM
    }
    @Single
    fun androidTransaction(driver:SqlDriver, db: Database) : AndroidDatabaseHandler {
        return AndroidDatabaseHandler(
            driver = driver,
            db = db
        )
    }
    @Single
    fun provideTransaction(handler: AndroidDatabaseHandler) : Transactions {
        return AndroidTransaction(handler)
    }
}
