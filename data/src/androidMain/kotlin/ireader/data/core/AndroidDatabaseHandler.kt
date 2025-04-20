package ireader.data.core

import androidx.paging.PagingSource
import app.cash.sqldelight.ExecutableQuery
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import data.DatabaseMigrations
import ir.kazemcodes.infinityreader.Database
import ireader.domain.preferences.prefs.AppPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AndroidDatabaseHandler(
    val db: Database,
    private val driver: SqlDriver,
    val queryDispatcher: CoroutineDispatcher = Dispatchers.IO,
    val transactionDispatcher: CoroutineDispatcher = queryDispatcher,
    val preferencesHelper: AppPreferences
) : DatabaseHandler {

    val suspendingTransactionId = ThreadLocal<Int>()
    override fun initialize() {
        println("Database initialization started")
        try {
            // Get current version from preferences
            val oldVersion = preferencesHelper.database_version().get()
            val currentVersion = DatabaseMigrations.CURRENT_VERSION
            
            println("Database version: Current=$oldVersion, Target=$currentVersion")

            // Apply migrations if needed
            if (oldVersion < currentVersion) {
                println("Database upgrade needed from $oldVersion to $currentVersion")
                try {
                    DatabaseMigrations.migrate(driver, oldVersion)
                    println("Database migration successfully completed")
                    
                    // Update the stored version
                    preferencesHelper.database_version().set(currentVersion)
                    println("Database version updated to $currentVersion")
                } catch (e: Exception) {
                    println("ERROR during database migration: ${e.message}")
                    e.printStackTrace()
                    
                    // Try to recover by forcing view creation
                    try {
                        println("Attempting recovery by reinitializing views...")
                        DatabaseMigrations.forceViewReinit(driver)
                    } catch (e2: Exception) {
                        println("Recovery attempt also failed: ${e2.message}")
                        e2.printStackTrace()
                    }
                }
            } else {
                println("No database upgrade needed, ensuring views exist...")
                // Even if no migration is needed, ensure views are initialized
                DatabaseMigrations.initializeViewsDirectly(driver)
            }
            
            println("Database initialization completed successfully")
        } catch (e: Exception) {
            println("CRITICAL ERROR during database initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun repairDatabase() {
        println("Repairing database from AndroidDatabaseHandler")
        try {
            // Force create views
            DatabaseMigrations.forceViewReinit(driver)
            
            // Verify library data is accessible
            driver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM book WHERE favorite = 1",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            )
        } catch (e: Exception) {
            println("Error during database repair: ${e.message}")
            e.printStackTrace()
        }
    }

    override suspend fun <T> await(inTransaction: Boolean, block: suspend Database.() -> T): T {
        return dispatch(inTransaction, block)
    }

    override suspend fun <T : Any> awaitList(
        inTransaction: Boolean,
        block: suspend Database.() -> Query<T>,
    ): List<T> {
        return dispatch(inTransaction) { block(db).executeAsList() }
    }

    override suspend fun <T : Any> awaitListAsync(
        inTransaction: Boolean,
        block: suspend Database.() -> ExecutableQuery<T>
    ): List<T> {
        return dispatch(inTransaction) { block(db).executeAsList() }
    }

    override suspend fun <T : Any> awaitOne(
        inTransaction: Boolean,
        block: suspend Database.() -> Query<T>,
    ): T {
        return dispatch(inTransaction) { block(db).executeAsOne() }
    }

    override suspend fun <T : Any> awaitOneAsync(
        inTransaction: Boolean,
        block: suspend Database.() -> ExecutableQuery<T>
    ): T {
        return dispatch(inTransaction) { block(db).executeAsOne() }
    }

    override suspend fun <T : Any> awaitOneOrNull(
        inTransaction: Boolean,
        block: suspend Database.() -> Query<T>,
    ): T? {
        return dispatch(inTransaction) { block(db).executeAsOneOrNull() }
    }

    override suspend fun <T : Any> awaitOneOrNullAsync(
        inTransaction: Boolean,
        block: suspend Database.() -> ExecutableQuery<T>
    ): T? {
        return dispatch(inTransaction) { block(db).executeAsOneOrNull() }
    }

    override fun <T : Any> subscribeToList(block: Database.() -> Query<T>): Flow<List<T>> {
        return block(db).asFlow().mapToList(queryDispatcher)
    }

    override fun <T : Any> subscribeToOne(block: Database.() -> Query<T>): Flow<T> {
        return block(db).asFlow().mapToOne(queryDispatcher)
    }

    override fun <T : Any> subscribeToOneOrNull(block: Database.() -> Query<T>): Flow<T?> {
        return block(db).asFlow().mapToOneOrNull(queryDispatcher)
    }

    fun <T : Any> subscribeToPagingSource(
        countQuery: Database.() -> Query<Long>,
        queryProvider: Database.(Long, Long) -> Query<T>,
    ): PagingSource<Long, T> {
        return QueryPagingSource(
            handler = this,
            countQuery = countQuery,
            queryProvider = { limit, offset ->
                queryProvider.invoke(db, limit, offset)
            },
        )
    }

    private suspend fun <T> dispatch(inTransaction: Boolean, block: suspend Database.() -> T): T {
        // Create a transaction if needed and run the calling block inside it.
        if (inTransaction) {
            return withTransaction { block(db) }
        }

        // If we're currently in the transaction thread, there's no need to dispatch our query.
        if (driver.currentTransaction() != null) {
            return block(db)
        }

        // Get the current database context and run the calling block.
        val context = getCurrentDatabaseContext()
        return withContext(context) { block(db) }
    }
}
