package ireader.data.core

import app.cash.sqldelight.db.SqlDriver
import data.DatabaseMigrations
import ireader.domain.preferences.prefs.AppPreferences

/**
 * Manages database version upgrades and applies migrations when necessary.
 */
class DatabaseVersionManager(
    private val driver: SqlDriver,
    private val preferences: AppPreferences
) {
    /**
     * Check if the database needs to be upgraded and apply migrations if necessary.
     * This should be called when the database is first opened.
     */
    fun upgrade() {
        val oldVersion = preferences.database_version().get()
        val newVersion = DatabaseMigrations.CURRENT_VERSION
        
        println("Database Version: Current=$oldVersion, Target=$newVersion")
        
        if (oldVersion < newVersion) {
            println("Upgrading database from version $oldVersion to $newVersion")
            // Apply migrations to upgrade from oldVersion to newVersion
            DatabaseMigrations.migrate(driver, oldVersion)
            
            // Update the version in preferences
            preferences.database_version().set(newVersion)
            println("Database upgrade completed successfully")
        } else {
            // Even if no migration is needed, always ensure views are initialized
            println("No database upgrade needed, but ensuring views are initialized")
            DatabaseMigrations.initializeViewsDirectly(driver)
        }
        
        // Validate database structure
        validateDatabaseStructure()
    }
    
    /**
     * Validates that essential database components exist
     */
    private fun validateDatabaseStructure() {
        try {
            // Check if views exist
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='view'",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            )
            
            // Check that book table has expected data
            driver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM book",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            )
            
            // Check favorite books count
            driver.executeQuery(
                identifier = null,
                sql = "SELECT COUNT(*) FROM book WHERE favorite = 1",
                mapper = { cursor ->
                    cursor.next()
                },
                parameters = 0
            )
        } catch (e: Exception) {
            println("Error validating database structure: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Force a database repair if issues are detected.
     * This is a recovery mechanism for users facing database problems.
     */
    fun repairDatabase() {
        println("Attempting database repair...")
        try {
            // Force view reinitialization
            DatabaseMigrations.forceViewReinit(driver)
            
            // Validate the structure again
            validateDatabaseStructure()
            
            println("Database repair completed")
        } catch (e: Exception) {
            println("Error during database repair: ${e.message}")
            e.printStackTrace()
        }
    }
}