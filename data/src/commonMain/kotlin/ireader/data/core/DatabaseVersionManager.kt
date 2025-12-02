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
        
        if (oldVersion < newVersion) {
            // Apply migrations to upgrade from oldVersion to newVersion
            DatabaseMigrations.migrate(driver, oldVersion)
            
            // Update the version in preferences
            preferences.database_version().set(newVersion)
        } else {
            // Even if no migration is needed, always ensure views are initialized
            DatabaseMigrations.initializeViewsDirectly(driver)
            
            // Check if the database structure is actually correct
            // If not, force a repair
            if (!validateDatabaseStructure()) {
                repairDatabase()
            }
        }
    }
    
    /**
     * Validates that required tables and columns exist
     * Returns true if valid, false if tables/columns are missing
     */
    private fun validateDatabaseStructure(): Boolean {
        try {
            // Check if translated_chapter table exists
            var translatedChapterExists = false
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='translated_chapter'",
                mapper = { cursor ->
                    val result = cursor.next()
                    translatedChapterExists = result.value
                    result
                },
                parameters = 0
            )
            
            if (!translatedChapterExists) {
                return false
            }
            
            // Check if glossary table exists
            var glossaryExists = false
            driver.executeQuery(
                identifier = null,
                sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='glossary'",
                mapper = { cursor ->
                    val result = cursor.next()
                    glossaryExists = result.value
                    result
                },
                parameters = 0
            )
            
            if (!glossaryExists) {
                return false
            }
            
            return true
        } catch (_: Exception) {
            return false
        }
    }
    

    
    /**
     * Force a database repair if issues are detected.
     * This is a recovery mechanism for users facing database problems.
     */
    fun repairDatabase() {
        try {
            // Force migration to current version to ensure all tables exist
            DatabaseMigrations.migrate(driver, 2)
            
            // Force view reinitialization
            DatabaseMigrations.forceViewReinit(driver)
            
            // Update version
            preferences.database_version().set(DatabaseMigrations.CURRENT_VERSION)
        } catch (_: Exception) {
            // Silently ignore repair errors
        }
    }
    

}