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
        }
    }
}