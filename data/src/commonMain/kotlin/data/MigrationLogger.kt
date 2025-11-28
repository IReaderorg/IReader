package data

import ireader.core.log.Log

/**
 * Logger for database migrations.
 * Provides structured logging with proper log levels instead of println.
 */
object MigrationLogger {
    
    private const val TAG = "DatabaseMigration"
    
    /**
     * Log migration start
     */
    fun logMigrationStart(fromVersion: Int, toVersion: Int) {
        Log.info { "[$TAG] Starting migration from version $fromVersion to $toVersion..." }
    }
    
    /**
     * Log migration success
     */
    fun logMigrationSuccess(toVersion: Int) {
        Log.info { "[$TAG] Successfully migrated to version $toVersion" }
    }
    
    /**
     * Log migration error
     */
    fun logMigrationError(toVersion: Int, error: Exception) {
        Log.error { "[$TAG] Error migrating to version $toVersion: ${error.message}" }
    }
    
    /**
     * Log table creation
     */
    fun logTableCreated(tableName: String) {
        Log.debug { "[$TAG] Created table: $tableName" }
    }
    
    /**
     * Log table dropped
     */
    fun logTableDropped(tableName: String) {
        Log.debug { "[$TAG] Dropped table: $tableName" }
    }
    
    /**
     * Log index creation
     */
    fun logIndexCreated(indexName: String) {
        Log.debug { "[$TAG] Created index: $indexName" }
    }
    
    /**
     * Log column added
     */
    fun logColumnAdded(tableName: String, columnName: String) {
        Log.debug { "[$TAG] Added column '$columnName' to table '$tableName'" }
    }
    
    /**
     * Log view created
     */
    fun logViewCreated(viewName: String) {
        Log.debug { "[$TAG] Created view: $viewName" }
    }
    
    /**
     * Log view error
     */
    fun logViewError(viewName: String, error: Exception) {
        Log.warn { "[$TAG] Error creating view '$viewName': ${error.message}" }
    }
    
    /**
     * Log general info message
     */
    fun logInfo(message: String) {
        Log.info { "[$TAG] $message" }
    }
    
    /**
     * Log debug message
     */
    fun logDebug(message: String) {
        Log.debug { "[$TAG] $message" }
    }
    
    /**
     * Log warning message
     */
    fun logWarning(message: String) {
        Log.warn { "[$TAG] $message" }
    }
    
    /**
     * Log error message
     */
    fun logError(message: String, error: Exception? = null) {
        if (error != null) {
            Log.error { "[$TAG] $message" }
        } else {
            Log.error { "[$TAG] $message" }
        }
    }
}
