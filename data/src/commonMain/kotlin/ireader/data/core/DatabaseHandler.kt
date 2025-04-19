package ireader.data.core

import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import data.DatabaseMigrations
import ir.kazemcodes.infinityreader.Database
import ireader.domain.preferences.prefs.AppPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Handler for database operations.
 * This class ensures proper initialization of views and handles database operations.
 */

    interface DatabaseHandler {
    /**
     * Initialize the database by applying migrations and creating views if needed.
     */
    fun initialize()

    suspend fun <T> await(inTransaction: Boolean = false, block: suspend Database.() -> T): T

    suspend fun <T : Any> awaitList(
        inTransaction: Boolean = false,
        block: suspend Database.() -> Query<T>,
    ): List<T>
    suspend fun <T : Any> awaitListAsync(
        inTransaction: Boolean = false,
        block: suspend Database.() -> ExecutableQuery<T>,
    ): List<T>

    /**
     * Execute a database query that returns a single result.
     */
    suspend fun <T : Any> awaitOne(
        inTransaction: Boolean = false,
        block: suspend Database.() -> Query<T>,
    ): T
    suspend fun <T : Any> awaitOneAsync(
        inTransaction: Boolean = false,
        block: suspend Database.() -> ExecutableQuery<T>,
    ): T

    suspend fun <T : Any> awaitOneOrNull(
        inTransaction: Boolean = false,
        block: suspend Database.() -> Query<T>,
    ): T?
    suspend fun <T : Any> awaitOneOrNullAsync(
        inTransaction: Boolean = false,
        block: suspend Database.() -> ExecutableQuery<T>,
    ): T?

    fun <T : Any> subscribeToList(block: Database.() -> Query<T>): Flow<List<T>>

    fun <T : Any> subscribeToOne(block: Database.() -> Query<T>): Flow<T>

    fun <T : Any> subscribeToOneOrNull(block: Database.() -> Query<T>): Flow<T?>


}
