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
            
            // Check if the database structure is actually correct
            // If not, force a repair
            if (!validateDatabaseColumns()) {
                println("Database structure validation failed. Running repair...")
                repairDatabase()
            }
        }
        
        // Validate database structure
        validateDatabaseStructure()
    }
    
    /**
     * Validates that the book table has all required columns
     * Returns true if valid, false if columns are missing
     */
    private fun validateDatabaseColumns(): Boolean {
        try {
            val existingColumns = mutableSetOf<String>()
            
            driver.executeQuery(
                identifier = null,
                sql = "PRAGMA table_info(book)",
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        if (columnName != null) {
                            existingColumns.add(columnName)
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            val requiredColumns = setOf(
                "_id", "source", "url", "title", "status", "favorite",
                "initialized", "viewer", "chapter_flags", "cover_last_modified", 
                "date_added", "last_update", "next_update"
            )
            
            val missingColumns = requiredColumns - existingColumns
            if (missingColumns.isNotEmpty()) {
                println("Missing columns in book table: $missingColumns")
                return false
            }
            
            return true
        } catch (e: Exception) {
            println("Error validating database columns: ${e.message}")
            return false
        }
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
            // First, try to add any missing columns
            addMissingColumns()
            
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
    
    /**
     * Adds any missing columns to the book table
     */
    private fun addMissingColumns() {
        try {
            val existingColumns = mutableSetOf<String>()
            
            driver.executeQuery(
                identifier = null,
                sql = "PRAGMA table_info(book)",
                mapper = { cursor ->
                    var result = cursor.next()
                    while (result.value) {
                        val columnName = cursor.getString(1)
                        if (columnName != null) {
                            existingColumns.add(columnName)
                        }
                        result = cursor.next()
                    }
                    result
                },
                parameters = 0
            )
            
            println("Current columns in book table: $existingColumns")
            
            // Define all columns that should exist
            val requiredColumns = mapOf(
                "last_update" to "INTEGER",
                "next_update" to "INTEGER",
                "initialized" to "INTEGER NOT NULL DEFAULT 0",
                "viewer" to "INTEGER NOT NULL DEFAULT 0",
                "chapter_flags" to "INTEGER NOT NULL DEFAULT 0",
                "cover_last_modified" to "INTEGER NOT NULL DEFAULT 0",
                "date_added" to "INTEGER NOT NULL DEFAULT 0"
            )
            
            // Add missing columns
            requiredColumns.forEach { (columnName, columnType) ->
                if (!existingColumns.contains(columnName)) {
                    try {
                        val sql = "ALTER TABLE book ADD COLUMN $columnName $columnType"
                        driver.execute(null, sql, 0)
                        println("Added missing column: $columnName")
                    } catch (e: Exception) {
                        println("Could not add column $columnName: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Error adding missing columns: ${e.message}")
            e.printStackTrace()
        }
    }
}