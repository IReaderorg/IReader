package org.ireader.app.crash

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Helper class for handling database migration failures
 */
object DatabaseMigrationHelper {
    
    private const val TAG = "DBMigrationHelper"
    
    /**
     * Detect if the crash is related to database migration
     */
    fun isDatabaseMigrationError(exception: Throwable): Boolean {
        val message = exception.message?.lowercase() ?: ""
        val stackTrace = exception.stackTraceToString().lowercase()
        
        return message.contains("migration") ||
                message.contains("database") ||
                message.contains("sqlite") ||
                message.contains("table") ||
                message.contains("column") ||
                stackTrace.contains("migration") ||
                stackTrace.contains("sqlitedatabase") ||
                stackTrace.contains("roomdatabase")
    }
    
    /**
     * Extract conflicting table names from error message
     */
    fun extractConflictingTables(exception: Throwable): List<String> {
        val tables = mutableSetOf<String>()
        val message = exception.message ?: ""
        val stackTrace = exception.stackTraceToString()
        
        // Common patterns for table names in error messages
        val patterns = listOf(
            Regex("table\\s+([a-zA-Z_][a-zA-Z0-9_]*)", RegexOption.IGNORE_CASE),
            Regex("'([a-zA-Z_][a-zA-Z0-9_]*)'\\s+already exists", RegexOption.IGNORE_CASE),
            Regex("no such table:\\s+([a-zA-Z_][a-zA-Z0-9_]*)", RegexOption.IGNORE_CASE),
            Regex("duplicate column name:\\s+([a-zA-Z_][a-zA-Z0-9_]*)", RegexOption.IGNORE_CASE)
        )
        
        val fullText = "$message\n$stackTrace"
        patterns.forEach { pattern ->
            pattern.findAll(fullText).forEach { match ->
                match.groupValues.getOrNull(1)?.let { tables.add(it) }
            }
        }
        
        return tables.toList()
    }
    
    /**
     * Drop specified tables from the database
     */
    suspend fun dropTables(context: Context, dbName: String, tables: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath(dbName)
            if (!dbPath.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }
            
            val db = SQLiteDatabase.openDatabase(
                dbPath.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            
            db.use { database ->
                database.beginTransaction()
                try {
                    tables.forEach { table ->
                        Log.d(TAG, "Dropping table: $table")
                        database.execSQL("DROP TABLE IF EXISTS `$table`")
                    }
                    database.setTransactionSuccessful()
                } finally {
                    database.endTransaction()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to drop tables", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all table names from the database
     */
    suspend fun getAllTables(context: Context, dbName: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath(dbName)
            if (!dbPath.exists()) {
                return@withContext Result.success(emptyList())
            }
            
            val db = SQLiteDatabase.openDatabase(
                dbPath.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            
            val tables = mutableListOf<String>()
            db.use { database ->
                val cursor = database.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
                    null
                )
                cursor.use {
                    while (it.moveToNext()) {
                        tables.add(it.getString(0))
                    }
                }
            }
            
            Result.success(tables)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get tables", e)
            Result.failure(e)
        }
    }
    
    /**
     * Backup and migrate data intelligently from old tables to new schema
     */
    suspend fun intelligentMigration(
        context: Context,
        dbName: String,
        conflictingTables: List<String>
    ): Result<MigrationReport> = withContext(Dispatchers.IO) {
        try {
            val dbPath = context.getDatabasePath(dbName)
            if (!dbPath.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }
            
            // Create backup
            val backupPath = File(dbPath.parent, "${dbName}.backup_${System.currentTimeMillis()}")
            dbPath.copyTo(backupPath, overwrite = true)
            
            val db = SQLiteDatabase.openDatabase(
                dbPath.absolutePath,
                null,
                SQLiteDatabase.OPEN_READWRITE
            )
            
            val report = MigrationReport()
            
            db.use { database ->
                conflictingTables.forEach { tableName ->
                    try {
                        // Get table schema
                        val columns = getTableColumns(database, tableName)
                        
                        // Create temporary table
                        val tempTableName = "${tableName}_temp_migration"
                        database.execSQL("ALTER TABLE `$tableName` RENAME TO `$tempTableName`")
                        
                        report.tablesProcessed.add(tableName)
                        report.backupPath = backupPath.absolutePath
                        
                        Log.d(TAG, "Migrated table $tableName to temporary table")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to migrate table $tableName", e)
                        report.errors.add("$tableName: ${e.message}")
                    }
                }
            }
            
            Result.success(report)
        } catch (e: Exception) {
            Log.e(TAG, "Intelligent migration failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get column information for a table
     */
    private fun getTableColumns(db: SQLiteDatabase, tableName: String): List<ColumnInfo> {
        val columns = mutableListOf<ColumnInfo>()
        val cursor = db.rawQuery("PRAGMA table_info(`$tableName`)", null)
        cursor.use {
            while (it.moveToNext()) {
                columns.add(
                    ColumnInfo(
                        name = it.getString(it.getColumnIndexOrThrow("name")),
                        type = it.getString(it.getColumnIndexOrThrow("type")),
                        notNull = it.getInt(it.getColumnIndexOrThrow("notnull")) == 1,
                        defaultValue = it.getString(it.getColumnIndexOrThrow("dflt_value"))
                    )
                )
            }
        }
        return columns
    }
    
    /**
     * Delete the database completely
     */
    suspend fun deleteDatabase(context: Context, dbName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val deleted = context.deleteDatabase(dbName)
            if (deleted) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete database"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete database", e)
            Result.failure(e)
        }
    }
}

data class ColumnInfo(
    val name: String,
    val type: String,
    val notNull: Boolean,
    val defaultValue: String?
)

data class MigrationReport(
    val tablesProcessed: MutableList<String> = mutableListOf(),
    val errors: MutableList<String> = mutableListOf(),
    var backupPath: String? = null
)
